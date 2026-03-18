package com.flowmeal.module.order.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.flowmeal.common.result.Result;
import com.flowmeal.module.order.dto.OrderDetailVO;
import com.flowmeal.module.order.dto.OrderListItemVO;
import com.flowmeal.module.order.dto.PlaceOrderReq;
import com.flowmeal.module.order.entity.OrderItem;
import com.flowmeal.module.order.entity.Orders;
import com.flowmeal.module.order.service.OrderService;
import com.flowmeal.module.merchant.entity.Merchant;
import com.flowmeal.module.merchant.mapper.MerchantMapper;
import com.flowmeal.module.order.mapper.OrderItemMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 订单接口（用户端 + 商家端 + 骑手端）
 */
@Tag(name = "订单接口")
@RestController
@RequestMapping("/order")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final MerchantMapper merchantMapper;
    private final OrderItemMapper orderItemMapper;

    // ========== 用户端 ==========

    @Operation(summary = "用户端 - 下单")
    @PostMapping("/place")
    public Result<String> placeOrder(@Valid @RequestBody PlaceOrderReq req) {
        Long userId = StpUtil.getLoginIdAsLong();
        String orderNo = orderService.placeOrder(userId, req);
        return Result.success("下单成功", orderNo);
    }

    @Operation(summary = "用户端 - 取消订单")
    @PutMapping("/{orderId}/cancel")
    public Result<Void> cancelOrder(@PathVariable Long orderId,
                                     @RequestParam(required = false) String reason) {
        orderService.cancelOrder(orderId, StpUtil.getLoginIdAsLong(), reason);
        return Result.success();
    }

    @Operation(summary = "用户端 - 我的订单列表")
    @GetMapping("/my")
    public Result<Page<OrderListItemVO>> myOrders(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<Orders> orderPage = orderService.listUserOrders(StpUtil.getLoginIdAsLong(), status, page, size);
        List<Orders> records = orderPage.getRecords();

        // batch fetch merchant info
        Map<Long, Merchant> merchantMap = new HashMap<>();
        records.stream().map(Orders::getMerchantId).distinct().forEach(mid -> {
            Merchant m = merchantMapper.selectById(mid);
            if (m != null) merchantMap.put(mid, m);
        });

        // batch fetch first item per order
        List<Long> orderIds = records.stream().map(Orders::getId).collect(Collectors.toList());
        Map<Long, List<OrderItem>> itemsMap = new HashMap<>();
        if (!orderIds.isEmpty()) {
            orderItemMapper.selectList(new LambdaQueryWrapper<OrderItem>()
                    .in(OrderItem::getOrderId, orderIds))
                    .forEach(item -> itemsMap.computeIfAbsent(item.getOrderId(), k -> new java.util.ArrayList<>()).add(item));
        }

        List<OrderListItemVO> voList = records.stream().map(order -> {
            OrderListItemVO vo = new OrderListItemVO();
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
            vo.setPaymentType(order.getPaymentType());
            vo.setCreatedAt(order.getCreatedAt());
            vo.setUpdatedAt(order.getUpdatedAt());

            Merchant m = merchantMap.get(order.getMerchantId());
            if (m != null) {
                vo.setShopName(m.getShopName());
                vo.setShopAvatar(m.getAvatar());
            }

            List<OrderItem> items = itemsMap.getOrDefault(order.getId(), List.of());
            if (!items.isEmpty()) {
                vo.setFirstItemName(items.get(0).getProductName());
                vo.setFirstItemImage(items.get(0).getProductImage());
                vo.setTotalQuantity(items.stream().mapToInt(OrderItem::getQuantity).sum());
            }
            return vo;
        }).collect(Collectors.toList());

        Page<OrderListItemVO> voPage = new Page<>(orderPage.getCurrent(), orderPage.getSize(), orderPage.getTotal());
        voPage.setRecords(voList);
        return Result.success(voPage);
    }

    @Operation(summary = "订单详情")
    @GetMapping("/{orderId}")
    public Result<OrderDetailVO> detail(@PathVariable Long orderId) {
        return Result.success(orderService.getOrderDetailVO(orderId));
    }

    // ========== 商家端 ==========

    @Operation(summary = "商家端 - 接单")
    @PutMapping("/{orderId}/merchant/accept")
    public Result<Void> merchantAccept(@PathVariable Long orderId) {
        orderService.merchantAccept(orderId, getMerchantId());
        return Result.success();
    }

    @Operation(summary = "商家端 - 拒单")
    @PutMapping("/{orderId}/merchant/reject")
    public Result<Void> merchantReject(@PathVariable Long orderId,
                                        @RequestParam String reason) {
        orderService.merchantReject(orderId, getMerchantId(), reason);
        return Result.success();
    }

    @Operation(summary = "商家端 - 订单列表")
    @GetMapping("/merchant/list")
    public Result<Page<Orders>> merchantOrders(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.success(orderService.listMerchantOrders(getMerchantId(), status, page, size));
    }

    // ========== 骑手端 ==========

    @Operation(summary = "骑手端 - 抢单大厅")
    @GetMapping("/rider/hall")
    public Result<Page<Orders>> hall(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.success(orderService.listHallOrders(page, size));
    }

    @Operation(summary = "骑手端 - 接单")
    @PutMapping("/{orderId}/rider/accept")
    public Result<Void> riderAccept(@PathVariable Long orderId) {
        orderService.riderAccept(orderId, getRiderId());
        return Result.success();
    }

    @Operation(summary = "骑手端 - 开始配送")
    @PutMapping("/{orderId}/rider/deliver")
    public Result<Void> startDeliver(@PathVariable Long orderId) {
        orderService.startDelivery(orderId, getRiderId());
        return Result.success();
    }

    @Operation(summary = "骑手端 - 完成配送")
    @PutMapping("/{orderId}/rider/complete")
    public Result<Void> completeDeliver(@PathVariable Long orderId) {
        orderService.completeDelivery(orderId, getRiderId());
        return Result.success();
    }

    // ========== 私有工具 ==========

    private Long getMerchantId() {
        return Long.parseLong(StpUtil.getLoginIdAsString().replace("merchant:", ""));
    }

    private Long getRiderId() {
        return Long.parseLong(StpUtil.getLoginIdAsString().replace("rider:", ""));
    }
}
