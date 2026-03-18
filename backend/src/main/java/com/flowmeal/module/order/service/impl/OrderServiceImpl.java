package com.flowmeal.module.order.service.impl;

import cn.hutool.core.util.IdUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.flowmeal.common.exception.BusinessException;
import com.flowmeal.common.result.ResultCode;
import com.flowmeal.module.merchant.entity.Merchant;
import com.flowmeal.module.merchant.mapper.MerchantMapper;
import com.flowmeal.module.order.dto.OrderDetailVO;
import com.flowmeal.module.order.dto.PlaceOrderReq;
import com.flowmeal.module.order.entity.OrderItem;
import com.flowmeal.module.order.entity.Orders;
import com.flowmeal.module.order.enums.OrderStatus;
import com.flowmeal.module.order.mapper.OrderItemMapper;
import com.flowmeal.module.order.mapper.OrdersMapper;
import com.flowmeal.module.order.service.OrderService;
import com.flowmeal.module.product.entity.Product;
import com.flowmeal.module.product.mapper.ProductMapper;
import com.flowmeal.module.rider.entity.DeliveryRecord;
import com.flowmeal.module.rider.mapper.DeliveryRecordMapper;
import com.flowmeal.module.rider.mapper.RiderMapper;
import com.flowmeal.module.user.entity.Address;
import com.flowmeal.module.user.mapper.AddressMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 订单服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrdersMapper ordersMapper;
    private final OrderItemMapper orderItemMapper;
    private final ProductMapper productMapper;
    private final AddressMapper addressMapper;
    private final MerchantMapper merchantMapper;
    private final RiderMapper riderMapper;
    private final DeliveryRecordMapper deliveryRecordMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    // Redis 分布式锁 key 前缀
    private static final String LOCK_PREFIX = "lock:stock:";
    // 新订单 WebSocket 通知 channel 前缀
    private static final String NEW_ORDER_CHANNEL = "notify:merchant:";

    @Override
    @Transactional
    public String placeOrder(Long userId, PlaceOrderReq req) {
        // 1. 校验商家
        Merchant merchant = merchantMapper.selectById(req.getMerchantId());
        if (merchant == null || merchant.getAuditStatus() != 1) {
            throw new BusinessException(ResultCode.MERCHANT_NOT_FOUND);
        }
        if (merchant.getStatus() == 0) {
            throw new BusinessException(ResultCode.MERCHANT_SHOP_CLOSED);
        }

        // 2. 校验地址
        Address address = addressMapper.selectOne(new LambdaQueryWrapper<Address>()
                .eq(Address::getId, req.getAddressId())
                .eq(Address::getUserId, userId));
        if (address == null) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "收货地址不存在");
        }

        // 3. 检查库存并扣减（分布式锁）
        List<OrderItem> items = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (PlaceOrderReq.OrderItemReq itemReq : req.getItems()) {
            String lockKey = LOCK_PREFIX + itemReq.getProductId();
            Boolean locked = redisTemplate.opsForValue()
                    .setIfAbsent(lockKey, "1", 5, TimeUnit.SECONDS);

            try {
                if (Boolean.FALSE.equals(locked)) {
                    throw new BusinessException("商品库存操作繁忙，请稍后重试");
                }

                Product product = productMapper.selectById(itemReq.getProductId());
                if (product == null || product.getStatus() == 0) {
                    throw new BusinessException(ResultCode.PRODUCT_OFF_SHELF);
                }
                if (product.getStock() < itemReq.getQuantity()) {
                    throw new BusinessException(ResultCode.PRODUCT_STOCK_INSUFFICIENT);
                }

                // 扣减库存
                productMapper.update(null, new LambdaUpdateWrapper<Product>()
                        .eq(Product::getId, product.getId())
                        .setSql("stock = stock - " + itemReq.getQuantity())
                        .setSql("sales = sales + " + itemReq.getQuantity()));

                BigDecimal price = product.getDiscountPrice() != null
                        ? product.getDiscountPrice() : product.getPrice();
                BigDecimal subtotal = price.multiply(BigDecimal.valueOf(itemReq.getQuantity()));
                totalAmount = totalAmount.add(subtotal);

                OrderItem item = new OrderItem();
                item.setProductId(product.getId());
                item.setProductName(product.getName());
                item.setProductImage(product.getImage());
                item.setUnitPrice(price);
                item.setQuantity(itemReq.getQuantity());
                item.setSubtotal(subtotal);
                items.add(item);

            } finally {
                redisTemplate.delete(lockKey);
            }
        }

        // 4. 构建订单
        String orderNo = "FM" + System.currentTimeMillis() + IdUtil.randomUUID().substring(0, 4).toUpperCase();
        BigDecimal actualAmount = totalAmount.add(merchant.getDeliveryFee() != null
                ? merchant.getDeliveryFee() : BigDecimal.ZERO);

        Orders order = new Orders();
        order.setOrderNo(orderNo);
        order.setUserId(userId);
        order.setMerchantId(req.getMerchantId());
        order.setAddressId(req.getAddressId());
        order.setAddressSnapshot(JSONUtil.toJsonStr(address));
        order.setTotalAmount(totalAmount);
        order.setDeliveryFee(merchant.getDeliveryFee() != null ? merchant.getDeliveryFee() : BigDecimal.ZERO);
        order.setActualAmount(actualAmount);
        order.setRemark(req.getRemark());
        order.setStatus(OrderStatus.WAIT_PAY.name());
        ordersMapper.insert(order);

        // 5. 插入订单详情
        items.forEach(item -> {
            item.setOrderId(order.getId());
            item.setCreatedAt(LocalDateTime.now());
            orderItemMapper.insert(item);
        });

        log.info("订单创建成功: orderNo={}, userId={}, amount={}", orderNo, userId, actualAmount);
        return orderNo;
    }

    @Override
    @Transactional
    public void cancelOrder(Long orderId, Long userId, String reason) {
        Orders order = getAndCheckOwner(orderId, userId);
        OrderStatus.checkTransition(order.getStatus(), OrderStatus.CANCELLED.name());

        ordersMapper.update(null, new LambdaUpdateWrapper<Orders>()
                .eq(Orders::getId, orderId)
                .set(Orders::getStatus, OrderStatus.CANCELLED.name())
                .set(Orders::getCancelReason, reason)
                .set(Orders::getUpdatedAt, LocalDateTime.now()));

        log.info("订单取消: orderId={}, reason={}", orderId, reason);
    }

    @Override
    @Transactional
    public void merchantAccept(Long orderId, Long merchantId) {
        Orders order = ordersMapper.selectById(orderId);
        checkOrderExists(order);
        if (!order.getMerchantId().equals(merchantId)) {
            throw new BusinessException(ResultCode.ORDER_NOT_OWNER);
        }
        OrderStatus.checkTransition(order.getStatus(), OrderStatus.MERCHANT_CONFIRMED.name());

        ordersMapper.update(null, new LambdaUpdateWrapper<Orders>()
                .eq(Orders::getId, orderId)
                .set(Orders::getStatus, OrderStatus.MERCHANT_CONFIRMED.name())
                .set(Orders::getAcceptTime, LocalDateTime.now()));

        log.info("商家接单: orderId={}, merchantId={}", orderId, merchantId);
    }

    @Override
    @Transactional
    public void merchantReject(Long orderId, Long merchantId, String reason) {
        Orders order = ordersMapper.selectById(orderId);
        checkOrderExists(order);
        if (!order.getMerchantId().equals(merchantId)) {
            throw new BusinessException(ResultCode.ORDER_NOT_OWNER);
        }
        // 拒单视为取消
        OrderStatus.checkTransition(order.getStatus(), OrderStatus.CANCELLED.name());

        ordersMapper.update(null, new LambdaUpdateWrapper<Orders>()
                .eq(Orders::getId, orderId)
                .set(Orders::getStatus, OrderStatus.CANCELLED.name())
                .set(Orders::getRejectReason, reason)
                .set(Orders::getCancelReason, "商家拒单: " + reason));

        log.info("商家拒单: orderId={}, reason={}", orderId, reason);
    }

    @Override
    @Transactional
    public void riderAccept(Long orderId, Long riderId) {
        Orders order = ordersMapper.selectById(orderId);
        checkOrderExists(order);
        OrderStatus.checkTransition(order.getStatus(), OrderStatus.RIDER_ACCEPTED.name());

        ordersMapper.update(null, new LambdaUpdateWrapper<Orders>()
                .eq(Orders::getId, orderId)
                .set(Orders::getStatus, OrderStatus.RIDER_ACCEPTED.name())
                .set(Orders::getRiderId, riderId)
                .set(Orders::getPickUpTime, LocalDateTime.now()));

        // 创建配送记录
        DeliveryRecord record = new DeliveryRecord();
        record.setOrderId(orderId);
        record.setRiderId(riderId);
        record.setAcceptTime(LocalDateTime.now());
        record.setCreatedAt(LocalDateTime.now());
        deliveryRecordMapper.insert(record);

        log.info("骑手接单: orderId={}, riderId={}", orderId, riderId);
    }

    @Override
    @Transactional
    public void startDelivery(Long orderId, Long riderId) {
        Orders order = ordersMapper.selectById(orderId);
        checkOrderExists(order);
        if (!riderId.equals(order.getRiderId())) {
            throw new BusinessException(ResultCode.ORDER_NOT_OWNER);
        }
        OrderStatus.checkTransition(order.getStatus(), OrderStatus.DELIVERING.name());

        ordersMapper.update(null, new LambdaUpdateWrapper<Orders>()
                .eq(Orders::getId, orderId)
                .set(Orders::getStatus, OrderStatus.DELIVERING.name())
                .set(Orders::getDeliverTime, LocalDateTime.now()));

        deliveryRecordMapper.update(null, new LambdaUpdateWrapper<DeliveryRecord>()
                .eq(DeliveryRecord::getOrderId, orderId)
                .eq(DeliveryRecord::getRiderId, riderId)
                .set(DeliveryRecord::getPickupTime, LocalDateTime.now()));
    }

    @Override
    @Transactional
    public void completeDelivery(Long orderId, Long riderId) {
        Orders order = ordersMapper.selectById(orderId);
        checkOrderExists(order);
        if (!riderId.equals(order.getRiderId())) {
            throw new BusinessException(ResultCode.ORDER_NOT_OWNER);
        }
        OrderStatus.checkTransition(order.getStatus(), OrderStatus.COMPLETED.name());

        LocalDateTime now = LocalDateTime.now();
        ordersMapper.update(null, new LambdaUpdateWrapper<Orders>()
                .eq(Orders::getId, orderId)
                .set(Orders::getStatus, OrderStatus.COMPLETED.name())
                .set(Orders::getCompleteTime, now));

        deliveryRecordMapper.update(null, new LambdaUpdateWrapper<DeliveryRecord>()
                .eq(DeliveryRecord::getOrderId, orderId)
                .eq(DeliveryRecord::getRiderId, riderId)
                .set(DeliveryRecord::getCompleteTime, now));

        // 累加骑手接单数
        riderMapper.update(null, new LambdaUpdateWrapper<com.flowmeal.module.rider.entity.Rider>()
                .eq(com.flowmeal.module.rider.entity.Rider::getId, riderId)
                .setSql("total_orders = total_orders + 1"));

        log.info("配送完成: orderId={}, riderId={}", orderId, riderId);
    }

    @Override
    public Page<Orders> listUserOrders(Long userId, String status, int page, int size) {
        LambdaQueryWrapper<Orders> wrapper = new LambdaQueryWrapper<Orders>()
                .eq(Orders::getUserId, userId)
                .orderByDesc(Orders::getCreatedAt);
        if (status != null && !status.isBlank()) {
            wrapper.eq(Orders::getStatus, status);
        }
        return ordersMapper.selectPage(new Page<>(page, size), wrapper);
    }

    @Override
    public Page<Orders> listMerchantOrders(Long merchantId, String status, int page, int size) {
        LambdaQueryWrapper<Orders> wrapper = new LambdaQueryWrapper<Orders>()
                .eq(Orders::getMerchantId, merchantId)
                .ne(Orders::getUserId, 0L)   // 排除 POS 线下订单（userId=0）
                .orderByDesc(Orders::getCreatedAt);
        if (status != null && !status.isBlank()) {
            wrapper.eq(Orders::getStatus, status);
        }
        return ordersMapper.selectPage(new Page<>(page, size), wrapper);
    }

    @Override
    public Page<Orders> listHallOrders(int page, int size) {
        return ordersMapper.selectPage(
                new Page<>(page, size),
                new LambdaQueryWrapper<Orders>()
                        .eq(Orders::getStatus, OrderStatus.MERCHANT_CONFIRMED.name())
                        .orderByAsc(Orders::getCreatedAt)
        );
    }

    @Override
    public Orders getOrderDetail(Long orderId) {
        Orders order = ordersMapper.selectById(orderId);
        checkOrderExists(order);
        return order;
    }

    @Override
    public OrderDetailVO getOrderDetailVO(Long orderId) {
        Orders order = ordersMapper.selectById(orderId);
        checkOrderExists(order);

        OrderDetailVO vo = new OrderDetailVO();
        // copy all fields from Orders
        vo.setId(order.getId());
        vo.setOrderNo(order.getOrderNo());
        vo.setUserId(order.getUserId());
        vo.setMerchantId(order.getMerchantId());
        vo.setRiderId(order.getRiderId());
        vo.setAddressId(order.getAddressId());
        vo.setAddressSnapshot(order.getAddressSnapshot());
        vo.setTotalAmount(order.getTotalAmount());
        vo.setDeliveryFee(order.getDeliveryFee());
        vo.setActualAmount(order.getActualAmount());
        vo.setRemark(order.getRemark());
        vo.setStatus(order.getStatus());
        vo.setCancelReason(order.getCancelReason());
        vo.setRejectReason(order.getRejectReason());
        vo.setPayTime(order.getPayTime());
        vo.setAcceptTime(order.getAcceptTime());
        vo.setPickUpTime(order.getPickUpTime());
        vo.setDeliverTime(order.getDeliverTime());
        vo.setCompleteTime(order.getCompleteTime());
        vo.setPaymentType(order.getPaymentType());
        vo.setTradeNo(order.getTradeNo());
        vo.setCreatedAt(order.getCreatedAt());
        vo.setUpdatedAt(order.getUpdatedAt());

        // enrich merchant info
        Merchant merchant = merchantMapper.selectById(order.getMerchantId());
        if (merchant != null) {
            vo.setShopName(merchant.getShopName());
            vo.setShopAvatar(merchant.getAvatar());
        }

        // enrich rider info
        if (order.getRiderId() != null) {
            com.flowmeal.module.rider.entity.Rider rider = riderMapper.selectById(order.getRiderId());
            if (rider != null) {
                vo.setRiderName(rider.getRealName());
                vo.setRiderPhone(rider.getPhone());
            }
        }

        // enrich order items
        List<OrderItem> items = orderItemMapper.selectList(
                new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, orderId));
        vo.setItems(items);

        return vo;
    }

    // ========== 私有方法 ==========

    private Orders getAndCheckOwner(Long orderId, Long userId) {
        Orders order = ordersMapper.selectById(orderId);
        checkOrderExists(order);
        if (!order.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.ORDER_NOT_OWNER);
        }
        return order;
    }

    private void checkOrderExists(Orders order) {
        if (order == null) {
            throw new BusinessException(ResultCode.ORDER_NOT_FOUND);
        }
    }
}
