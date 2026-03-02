package com.flowmeal.module.order.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.flowmeal.common.result.Result;
import com.flowmeal.module.order.dto.PlaceOrderReq;
import com.flowmeal.module.order.entity.Orders;
import com.flowmeal.module.order.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 订单接口（用户端 + 商家端 + 骑手端）
 */
@Tag(name = "订单接口")
@RestController
@RequestMapping("/order")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

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
    public Result<Page<Orders>> myOrders(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.success(orderService.listUserOrders(StpUtil.getLoginIdAsLong(), status, page, size));
    }

    @Operation(summary = "订单详情")
    @GetMapping("/{orderId}")
    public Result<Orders> detail(@PathVariable Long orderId) {
        return Result.success(orderService.getOrderDetail(orderId));
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
