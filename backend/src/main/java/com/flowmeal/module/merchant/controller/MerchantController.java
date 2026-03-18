package com.flowmeal.module.merchant.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.flowmeal.common.result.Result;
import com.flowmeal.module.merchant.entity.Merchant;
import com.flowmeal.module.merchant.mapper.MerchantMapper;
import com.flowmeal.module.material.entity.MaterialReport;
import com.flowmeal.module.material.mapper.MaterialReportMapper;
import com.flowmeal.module.order.entity.OrderItem;
import com.flowmeal.module.order.entity.Orders;
import com.flowmeal.module.order.mapper.OrderItemMapper;
import com.flowmeal.module.order.mapper.OrdersMapper;
import com.flowmeal.module.product.entity.Product;
import com.flowmeal.module.product.mapper.ProductMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Tag(name = "商家接口")
@RestController
@RequiredArgsConstructor
public class MerchantController {

    private final MerchantMapper merchantMapper;
    private final OrdersMapper ordersMapper;
    private final OrderItemMapper orderItemMapper;
    private final ProductMapper productMapper;
    private final MaterialReportMapper materialReportMapper;

    // ===== 用户端（公开） =====

    @Operation(summary = "用户端 - 商家列表（分页）")
    @GetMapping("/shop/list")
    public Result<Page<Merchant>> shopList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword) {
        LambdaQueryWrapper<Merchant> wrapper = new LambdaQueryWrapper<Merchant>()
                .eq(Merchant::getAuditStatus, 1).orderByDesc(Merchant::getStatus);
        if (keyword != null && !keyword.isBlank() && !keyword.equals("undefined") && !keyword.equals("null")) {
            wrapper.like(Merchant::getShopName, keyword);
        }
        Page<Merchant> result = merchantMapper.selectPage(new Page<>(page, size), wrapper);
        result.getRecords().forEach(m -> m.setPassword(null));
        return Result.success(result);
    }

    @Operation(summary = "用户端 - 商家详情")
    @GetMapping("/shop/{id}")
    public Result<Merchant> shopDetail(@PathVariable Long id) {
        Merchant merchant = merchantMapper.selectById(id);
        if (merchant != null) merchant.setPassword(null);
        return Result.success(merchant);
    }

    // ===== 商家端（需 MERCHANT 角色） =====

    @Operation(summary = "商家端 - 获取我的店铺信息")
    @GetMapping("/merchant/shop")
    public Result<Merchant> myShop() {
        Merchant merchant = merchantMapper.selectById(getMerchantId());
        if (merchant != null) merchant.setPassword(null);
        return Result.success(merchant);
    }

    @Operation(summary = "商家端 - 修改店铺信息")
    @PutMapping("/merchant/shop")
    public Result<Void> updateShop(@RequestBody Merchant req) {
        req.setId(getMerchantId());
        req.setPassword(null);
        req.setAuditStatus(null);
        merchantMapper.updateById(req);
        return Result.success();
    }

    @Operation(summary = "商家端 - 切换营业状态")
    @PutMapping("/merchant/shop/status")
    public Result<Void> toggleStatus(@RequestParam Integer status) {
        merchantMapper.update(null, new LambdaUpdateWrapper<Merchant>()
                .eq(Merchant::getId, getMerchantId()).set(Merchant::getStatus, status));
        return Result.success();
    }

    // ===== 商家统计 =====

    @Operation(summary = "今日营业概览")
    @GetMapping("/merchant/stats/today")
    public Result<Map<String, Object>> todaySummary() {
        Long merchantId = getMerchantId();
        LocalDateTime start = LocalDate.now().atStartOfDay();
        LocalDateTime end = start.plusDays(1);
        List<Orders> todayOrders = ordersMapper.selectList(new LambdaQueryWrapper<Orders>()
                .eq(Orders::getMerchantId, merchantId)
                .ge(Orders::getCreatedAt, start).lt(Orders::getCreatedAt, end));
        // 已付款订单：排除 WAIT_PAY 和 CANCELLED
        Set<String> UNPAID = Set.of("WAIT_PAY", "CANCELLED");
        List<Orders> paidOrders = todayOrders.stream()
                .filter(o -> !UNPAID.contains(o.getStatus())).collect(Collectors.toList());
        BigDecimal revenue = paidOrders.stream().map(Orders::getActualAmount).filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal avg = paidOrders.isEmpty() ? BigDecimal.ZERO
                : revenue.divide(BigDecimal.valueOf(paidOrders.size()), 2, RoundingMode.HALF_UP);
        // 新客户：今日有真实用户（userId != 0）且在此之前从未在本商家下过单
        Set<Long> todayRealUids = paidOrders.stream().map(Orders::getUserId)
                .filter(uid -> uid != null && uid != 0L).collect(Collectors.toSet());
        long newCustomers = todayRealUids.stream().filter(uid ->
            ordersMapper.selectCount(new LambdaQueryWrapper<Orders>()
                .eq(Orders::getMerchantId, merchantId).eq(Orders::getUserId, uid)
                .lt(Orders::getCreatedAt, start)) == 0
        ).count();
        Map<String, Object> data = new LinkedHashMap<>();
        // orderCount 仅计已付款订单，与 revenue/avgAmount 口径一致
        data.put("orderCount", paidOrders.size());
        data.put("revenue", revenue);
        data.put("avgAmount", avg);
        data.put("newCustomers", newCustomers);
        return Result.success(data);
    }

    @Operation(summary = "近7天营业额趋势")
    @GetMapping("/merchant/stats/revenue-trend")
    public Result<List<Map<String, Object>>> revenueTrend() {
        return Result.success(buildMerchantTrend(7, true));
    }

    @Operation(summary = "近7天订单量趋势")
    @GetMapping("/merchant/stats/order-trend")
    public Result<List<Map<String, Object>>> orderTrend() {
        return Result.success(buildMerchantTrend(7, false));
    }

    @Operation(summary = "商品销售排行 TOP10")
    @GetMapping("/merchant/stats/product-rank")
    public Result<List<Map<String, Object>>> productRank() {
        Long merchantId = getMerchantId();
        // 从 order_item 实时汇总销售量，只统计已付款订单（排除 WAIT_PAY 和 CANCELLED）
        List<Map<String, Object>> result = orderItemMapper.selectList(
                new LambdaQueryWrapper<OrderItem>()
                        .inSql(OrderItem::getOrderId,
                                "SELECT id FROM orders WHERE merchant_id = " + merchantId
                                + " AND status NOT IN ('WAIT_PAY','CANCELLED')")
        ).stream()
                .collect(Collectors.groupingBy(
                        OrderItem::getProductName,
                        Collectors.summingInt(OrderItem::getQuantity)
                ))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(10)
                .map(e -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("name", e.getKey());
                    m.put("sales", e.getValue());
                    return m;
                })
                .collect(Collectors.toList());
        return Result.success(result);
    }

    // ===== 原料管理 =====

    @Operation(summary = "原料消耗记录")
    @GetMapping("/merchant/materials")
    public Result<List<MaterialReport>> listMaterials(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "30") int size) {
        Long merchantId = getMerchantId();
        List<MaterialReport> list = materialReportMapper.selectList(
                new LambdaQueryWrapper<MaterialReport>()
                        .eq(MaterialReport::getMerchantId, merchantId)
                        .orderByDesc(MaterialReport::getReportTime));
        return Result.success(list);
    }

    @Operation(summary = "上报原料消耗")
    @PostMapping("/merchant/materials/report")
    public Result<Void> reportMaterials(@RequestBody Map<String, Object> body) {
        Long merchantId = getMerchantId();
        LocalDateTime now = LocalDateTime.now();
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> items = (List<Map<String, Object>>) body.get("items");
        if (items == null || items.isEmpty()) return Result.success();
        for (Map<String, Object> item : items) {
            MaterialReport r = new MaterialReport();
            r.setMerchantId(merchantId);
            r.setMaterialName(String.valueOf(item.get("name")));
            Object consumed = item.get("consumed");
            r.setConsumed(consumed instanceof Number ? ((Number) consumed).intValue() : 0);
            r.setUnit(item.getOrDefault("unit", "kg").toString());
            r.setReportTime(now);
            materialReportMapper.insert(r);
        }
        return Result.success();
    }

    @Operation(summary = "申请采购补货")
    @PostMapping("/merchant/materials/procurement")
    public Result<Void> applyProcurement(@RequestBody Map<String, Object> body) {
        return Result.success();
    }

    // ===== 私有工具 =====

    // ===== POS 收银台 =====

    @Operation(summary = "POS - 获取当前商家全部上架商品")
    @GetMapping("/merchant/pos/products")
    public Result<List<Product>> posProducts() {
        Long merchantId = getMerchantId();
        List<Product> list = productMapper.selectList(
                new LambdaQueryWrapper<Product>()
                        .eq(Product::getMerchantId, merchantId)
                        .eq(Product::getStatus, 1)
                        .orderByAsc(Product::getCategoryId)
                        .orderByAsc(Product::getSort));
        return Result.success(list);
    }

    @Operation(summary = "POS - 线下收银结算")
    @PostMapping("/merchant/pos/checkout")
    @SuppressWarnings("unchecked")
    public Result<Map<String, Object>> posCheckout(@RequestBody Map<String, Object> body) {
        Long merchantId = getMerchantId();
        List<Map<String, Object>> items = (List<Map<String, Object>>) body.get("items");
        String paymentType = (String) body.getOrDefault("paymentType", "CASH");
        String remark = (String) body.getOrDefault("remark", "");
        if (items == null || items.isEmpty()) return Result.fail(400, "购物车为空");

        BigDecimal total = BigDecimal.ZERO;
        for (Map<String, Object> item : items) {
            BigDecimal price = new BigDecimal(item.get("price").toString());
            int qty = Integer.parseInt(item.get("qty").toString());
            total = total.add(price.multiply(BigDecimal.valueOf(qty)));
        }

        String orderNo = "POS" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                         + String.format("%03d", new Random().nextInt(1000));

        Orders order = new Orders();
        order.setOrderNo(orderNo);
        order.setUserId(0L);
        order.setMerchantId(merchantId);
        order.setAddressId(0L);
        order.setAddressSnapshot("{\"type\":\"线下收款\"}");
        order.setTotalAmount(total);
        order.setDeliveryFee(BigDecimal.ZERO);
        order.setActualAmount(total);
        order.setRemark(remark.isEmpty() ? "POS线下订单" : remark);
        order.setStatus("COMPLETED");
        order.setPaymentType(paymentType);
        order.setPayTime(LocalDateTime.now());
        order.setCompleteTime(LocalDateTime.now());
        // 支付宝/微信扫码：订单状态为待支付，等回调更新
        if ("ALIPAY".equals(paymentType) || "WECHAT".equals(paymentType)) {
            order.setStatus("WAIT_PAY");
            order.setPayTime(null);
            order.setCompleteTime(null);
        }
        ordersMapper.insert(order);

        for (Map<String, Object> item : items) {
            OrderItem oi = new OrderItem();
            oi.setOrderId(order.getId());
            if (item.get("productId") != null)
                oi.setProductId(Long.parseLong(item.get("productId").toString()));
            oi.setProductName((String) item.get("name"));
            oi.setProductImage((String) item.getOrDefault("image", null));
            oi.setUnitPrice(new BigDecimal(item.get("price").toString()));
            int qty = Integer.parseInt(item.get("qty").toString());
            oi.setQuantity(qty);
            oi.setSubtotal(oi.getUnitPrice().multiply(BigDecimal.valueOf(qty)));
            orderItemMapper.insert(oi);
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("orderNo", orderNo);
        result.put("totalAmount", total);
        result.put("needScan", "ALIPAY".equals(paymentType) || "WECHAT".equals(paymentType));
        result.put("paymentType", paymentType);
        return Result.success(result);
    }

    private Long getMerchantId() {
        return Long.parseLong(StpUtil.getLoginIdAsString().replace("merchant:", ""));
    }

    private List<Map<String, Object>> buildMerchantTrend(int days, boolean revenueMode) {
        Long merchantId = getMerchantId();
        LocalDate today = LocalDate.now();
        LocalDateTime rangeStart = today.minusDays(days - 1).atStartOfDay();
        List<Orders> orders = ordersMapper.selectList(new LambdaQueryWrapper<Orders>()
                .eq(Orders::getMerchantId, merchantId).ge(Orders::getCreatedAt, rangeStart));
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MM-dd");
        List<Map<String, Object>> result = new ArrayList<>();
        for (int i = days - 1; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            List<Orders> dayOrders = orders.stream()
                    .filter(o -> o.getCreatedAt() != null && o.getCreatedAt().toLocalDate().equals(date))
                    .collect(Collectors.toList());
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("date", date.format(fmt));
            if (revenueMode) {
                // 已付款：排除 WAIT_PAY 和 CANCELLED
                Set<String> UNPAID = Set.of("WAIT_PAY", "CANCELLED");
                BigDecimal rev = dayOrders.stream()
                        .filter(o -> !UNPAID.contains(o.getStatus()))
                        .map(Orders::getActualAmount).filter(Objects::nonNull)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                item.put("revenue", rev);
            } else {
                item.put("count", (long) dayOrders.size());
            }
            result.add(item);
        }
        return result;
    }
}
