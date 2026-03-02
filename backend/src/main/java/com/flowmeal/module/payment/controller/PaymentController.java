package com.flowmeal.module.payment.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.flowmeal.common.config.AlipayProperties;
import com.flowmeal.common.exception.BusinessException;
import com.flowmeal.common.result.Result;
import com.flowmeal.common.result.ResultCode;
import com.flowmeal.module.order.entity.Orders;
import com.flowmeal.module.order.enums.OrderStatus;
import com.flowmeal.module.order.mapper.OrdersMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * 支付接口 - 支付宝沙箱
 */
@Slf4j
@Tag(name = "支付接口")
@RestController
@RequestMapping("/payment/alipay")
@RequiredArgsConstructor
public class PaymentController {

    private final AlipayProperties alipayProperties;
    private final OrdersMapper ordersMapper;

    /**
     * 发起支付（PC端，返回支付页面 HTML）
     */
    @Operation(summary = "支付宝 - 发起支付")
    @GetMapping("/pay/{orderNo}")
    public void pay(@PathVariable String orderNo, HttpServletResponse response) throws Exception {
        Orders order = getOrder(orderNo);
        if (!OrderStatus.WAIT_PAY.name().equals(order.getStatus())) {
            throw new BusinessException(ResultCode.PAYMENT_ALREADY_PAID);
        }

        AlipayClient client = buildClient();
        AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();
        request.setNotifyUrl(alipayProperties.getNotifyUrl());
        request.setReturnUrl(alipayProperties.getReturnUrl());
        request.setBizContent("{" +
                "  \"out_trade_no\": \"" + order.getOrderNo() + "\"," +
                "  \"total_amount\": \"" + order.getActualAmount() + "\"," +
                "  \"subject\": \"FlowMeal订单-" + order.getOrderNo() + "\"," +
                "  \"product_code\": \"FAST_INSTANT_TRADE_PAY\"" +
                "}");

        String form = client.pageExecute(request).getBody();
        response.setContentType("text/html;charset=utf-8");
        response.getWriter().write(form);
    }

    /**
     * 支付宝异步回调（服务端通知）
     */
    @Operation(summary = "支付宝 - 异步通知回调")
    @PostMapping("/notify")
    public String notify(@RequestParam String out_trade_no,
                          @RequestParam String trade_no,
                          @RequestParam String trade_status) {
        log.info("支付宝回调: orderNo={}, tradeNo={}, status={}", out_trade_no, trade_no, trade_status);

        if ("TRADE_SUCCESS".equals(trade_status) || "TRADE_FINISHED".equals(trade_status)) {
            updateOrderPaid(out_trade_no, trade_no);
        }
        return "success";
    }

    /**
     * 支付宝同步跳转（用户端回跳）
     */
    @Operation(summary = "支付宝 - 同步跳转")
    @GetMapping("/return")
    public Result<String> payReturn(@RequestParam String out_trade_no) {
        Orders order = getOrder(out_trade_no);
        return Result.success("支付结果：" + order.getStatus());
    }

    /**
     * 主动查询订单支付状态
     */
    @Operation(summary = "查询订单支付状态")
    @GetMapping("/query/{orderNo}")
    public Result<String> queryPayStatus(@PathVariable String orderNo) throws AlipayApiException {
        AlipayClient client = buildClient();
        AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
        request.setBizContent("{\"out_trade_no\":\"" + orderNo + "\"}");

        AlipayTradeQueryResponse response = client.execute(request);
        if (response.isSuccess()) {
            String status = response.getTradeStatus();
            if ("TRADE_SUCCESS".equals(status) || "TRADE_FINISHED".equals(status)) {
                updateOrderPaid(orderNo, response.getTradeNo());
            }
            return Result.success(status);
        }
        return Result.fail(ResultCode.PAYMENT_FAILED);
    }

    // ========== 私有工具 ==========

    private AlipayClient buildClient() {
        return new DefaultAlipayClient(
                alipayProperties.getGatewayUrl(),
                alipayProperties.getAppId(),
                alipayProperties.getPrivateKey(),
                "json",
                "UTF-8",
                alipayProperties.getAlipayPublicKey(),
                "RSA2"
        );
    }

    private Orders getOrder(String orderNo) {
        Orders order = ordersMapper.selectOne(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Orders>()
                .eq(Orders::getOrderNo, orderNo));
        if (order == null) {
            throw new BusinessException(ResultCode.ORDER_NOT_FOUND);
        }
        return order;
    }

    private void updateOrderPaid(String orderNo, String tradeNo) {
        ordersMapper.update(null, new LambdaUpdateWrapper<Orders>()
                .eq(Orders::getOrderNo, orderNo)
                .eq(Orders::getStatus, OrderStatus.WAIT_PAY.name()) // 防止重复处理
                .set(Orders::getStatus, OrderStatus.WAIT_MERCHANT_CONFIRM.name())
                .set(Orders::getTradeNo, tradeNo)
                .set(Orders::getPaymentType, "ALIPAY")
                .set(Orders::getPayTime, LocalDateTime.now()));
        log.info("订单支付成功，状态更新: orderNo={}", orderNo);
    }
}
