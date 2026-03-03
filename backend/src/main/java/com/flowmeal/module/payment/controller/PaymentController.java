package com.flowmeal.module.payment.controller;

import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradePrecreateRequest;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.request.AlipayTradeWapPayRequest;
import com.alipay.api.response.AlipayTradePrecreateResponse;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.alipay.api.response.AlipayTradeWapPayResponse;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.flowmeal.common.config.AlipayProperties;
import com.flowmeal.common.config.WechatPayProperties;
import com.flowmeal.common.result.Result;
import com.flowmeal.module.order.entity.Orders;
import com.flowmeal.module.order.mapper.OrdersMapper;
import com.wechat.pay.java.core.RSAAutoCertificateConfig;
import com.wechat.pay.java.core.notification.NotificationParser;
import com.wechat.pay.java.core.notification.RequestParam;
import com.wechat.pay.java.service.payments.model.Transaction;
import com.wechat.pay.java.service.payments.nativepay.NativePayService;
import com.wechat.pay.java.service.payments.nativepay.model.Amount;
import com.wechat.pay.java.service.payments.nativepay.model.PrepayRequest;
import com.wechat.pay.java.service.payments.nativepay.model.PrepayResponse;
import com.wechat.pay.java.service.payments.nativepay.model.QueryOrderByOutTradeNoRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 支付接口
 * - POS 商家端：支付宝当面付扫码 / 微信 Native 扫码
 * - 用户端小程序/H5：支付宝手机网站支付跳转
 */
@Slf4j
@Tag(name = "支付接口")
@RestController
@RequiredArgsConstructor
public class PaymentController {

    private final AlipayProperties alipayProps;
    private final WechatPayProperties wechatProps;
    private final OrdersMapper ordersMapper;

    //  POS 支付宝当面付扫码 

    @Operation(summary = "POS - 获取支付宝当面付二维码")
    @GetMapping("/payment/pos/alipay/qrcode/{orderNo}")
    public Result<Map<String, String>> posAlipayQrcode(@PathVariable String orderNo) throws Exception {
        Orders order = getOrder(orderNo);
        AlipayClient client = buildAlipayClient();

        AlipayTradePrecreateRequest req = new AlipayTradePrecreateRequest();
        req.setNotifyUrl(alipayProps.getNotifyUrl());
        req.setBizContent("{" +
                "\"out_trade_no\":\"" + order.getOrderNo() + "\"," +
                "\"total_amount\":\"" + order.getActualAmount() + "\"," +
                "\"subject\":\"FlowMeal订单-" + order.getOrderNo() + "\"," +
                "\"timeout_express\":\"15m\"}");

        AlipayTradePrecreateResponse resp = client.execute(req);
        if (!resp.isSuccess()) {
            log.error("支付宝当面付失败: {}", resp.getSubMsg());
            return Result.fail(500, "支付宝下单失败：" + resp.getSubMsg());
        }
        Map<String, String> data = new HashMap<>();
        data.put("qrCode", resp.getQrCode());
        data.put("orderNo", orderNo);
        return Result.success(data);
    }

    //  POS 微信 Native 扫码 

    @Operation(summary = "POS - 获取微信 Native 二维码")
    @GetMapping("/payment/pos/wechat/qrcode/{orderNo}")
    public Result<Map<String, String>> posWechatQrcode(@PathVariable String orderNo) {
        Orders order = getOrder(orderNo);
        try {
            RSAAutoCertificateConfig config = buildWxConfig();
            NativePayService service = new NativePayService.Builder().config(config).build();

            PrepayRequest request = new PrepayRequest();
            request.setAppid(wechatProps.getAppId());
            request.setMchid(wechatProps.getMchId());
            request.setDescription("FlowMeal订单-" + order.getOrderNo());
            request.setOutTradeNo(order.getOrderNo());
            request.setNotifyUrl(wechatProps.getNotifyUrl());

            Amount amount = new Amount();
            amount.setTotal(order.getActualAmount().multiply(BigDecimal.valueOf(100)).intValue());
            request.setAmount(amount);

            PrepayResponse response = service.prepay(request);
            Map<String, String> data = new HashMap<>();
            data.put("codeUrl", response.getCodeUrl());
            data.put("orderNo", orderNo);
            return Result.success(data);
        } catch (Exception e) {
            log.error("微信支付下单失败: {}", e.getMessage(), e);
            return Result.fail(500, "微信支付下单失败：" + e.getMessage());
        }
    }

    //  用户端 支付宝手机网站支付 

    @Operation(summary = "用户端 - 支付宝WAP支付跳转URL")
    @GetMapping("/payment/h5/alipay/{orderNo}")
    public Result<String> h5AlipayPay(@PathVariable String orderNo) throws Exception {
        Orders order = getOrder(orderNo);
        AlipayClient client = buildAlipayClient();

        AlipayTradeWapPayRequest req = new AlipayTradeWapPayRequest();
        req.setNotifyUrl(alipayProps.getNotifyUrl());
        req.setReturnUrl(alipayProps.getReturnUrl());
        req.setBizContent("{" +
                "\"out_trade_no\":\"" + order.getOrderNo() + "\"," +
                "\"total_amount\":\"" + order.getActualAmount() + "\"," +
                "\"subject\":\"FlowMeal订单-" + order.getOrderNo() + "\"," +
                "\"product_code\":\"QUICK_WAP_WAY\"," +
                "\"timeout_express\":\"30m\"}");

        AlipayTradeWapPayResponse resp = client.pageExecute(req, "GET");
        return Result.success(resp.getBody());
    }

    //  查询支付状态（前端轮询） 

    @Operation(summary = "查询订单支付状态")
    @GetMapping("/payment/query/{orderNo}")
    public Result<Map<String, Object>> queryPayStatus(@PathVariable String orderNo) {
        Orders order = getOrder(orderNo);
        Map<String, Object> data = new HashMap<>();
        data.put("orderNo", orderNo);
        data.put("status", order.getStatus());
        data.put("paid", !"WAIT_PAY".equals(order.getStatus()));

        // 若仍未支付且为支付宝，主动查询一次（微信支付靠回调，无需轮询查）
        if ("WAIT_PAY".equals(order.getStatus()) && "ALIPAY".equals(order.getPaymentType())) {
            try {
                AlipayClient client = buildAlipayClient();
                AlipayTradeQueryRequest req = new AlipayTradeQueryRequest();
                req.setBizContent("{\"out_trade_no\":\"" + orderNo + "\"}");
                AlipayTradeQueryResponse resp = client.execute(req);
                if (resp.isSuccess()) {
                    String ts = resp.getTradeStatus();
                    if ("TRADE_SUCCESS".equals(ts) || "TRADE_FINISHED".equals(ts)) {
                        markOrderPaid(orderNo, resp.getTradeNo(), "ALIPAY");
                        data.put("status", "WAIT_MERCHANT_CONFIRM");
                        data.put("paid", true);
                    }
                } else if ("ACQ.TRADE_NOT_EXIST".equals(resp.getSubCode())) {
                    // 顾客尚未付款，属正常状态，不记录错误
                    log.debug("支付宝交易尚未创建（顾客未扫码）: {}", orderNo);
                } else {
                    log.warn("主动查询支付宝返回非成功: {} {}", resp.getCode(), resp.getSubMsg());
                }
            } catch (Exception e) {
                log.warn("主动查询支付宝异常: {}", e.getMessage());
            }
        }
        // 微信支付主动查单（回调不可靠时的兼容保障）
        if ("WAIT_PAY".equals(order.getStatus()) && "WECHAT".equals(order.getPaymentType())) {
            try {
                RSAAutoCertificateConfig config = buildWxConfig();
                NativePayService wxService = new NativePayService.Builder().config(config).build();
                QueryOrderByOutTradeNoRequest wxQuery = new QueryOrderByOutTradeNoRequest();
                wxQuery.setMchid(wechatProps.getMchId());
                wxQuery.setOutTradeNo(orderNo);
                Transaction tx = wxService.queryOrderByOutTradeNo(wxQuery);
                if (Transaction.TradeStateEnum.SUCCESS.equals(tx.getTradeState())) {
                    markOrderPaid(orderNo, tx.getTransactionId(), "WECHAT");
                    data.put("status", "WAIT_MERCHANT_CONFIRM");
                    data.put("paid", true);
                    log.info("微信支付主动查单成功: {}", orderNo);
                }
            } catch (Exception e) {
                log.warn("微信支付主动查单异常: {}", e.getMessage());
            }
        }
        return Result.success(data);
    }

    //  支付宝异步回调 

    @Operation(summary = "支付宝回调通知")
    @PostMapping("/payment/callback/alipay")
    public String alipayCallback(HttpServletRequest request) {
        try {
            Map<String, String> params = new HashMap<>();
            request.getParameterMap().forEach((k, v) -> params.put(k, v[0]));

            boolean ok = AlipaySignature.rsaCheckV1(
                    params, alipayProps.getAlipayPublicKey(),
                    alipayProps.getCharset(), alipayProps.getSignType());
            if (!ok) { log.warn("支付宝回调签名验证失败"); return "fail"; }

            String ts = params.get("trade_status");
            if ("TRADE_SUCCESS".equals(ts) || "TRADE_FINISHED".equals(ts)) {
                markOrderPaid(params.get("out_trade_no"), params.get("trade_no"), "ALIPAY");
                log.info("支付宝回调成功: orderNo={}", params.get("out_trade_no"));
            }
            return "success";
        } catch (Exception e) {
            log.error("支付宝回调异常: {}", e.getMessage(), e);
            return "fail";
        }
    }

    //  微信支付回调 

    @Operation(summary = "微信支付回调通知")
    @PostMapping("/payment/callback/wechat")
    public Map<String, String> wechatCallback(@RequestBody String body, HttpServletRequest request) {
        Map<String, String> result = new HashMap<>();
        try {
            RequestParam requestParam = new RequestParam.Builder()
                    .serialNumber(request.getHeader("Wechatpay-Serial"))
                    .nonce(request.getHeader("Wechatpay-Nonce"))
                    .timestamp(request.getHeader("Wechatpay-Timestamp"))
                    .signature(request.getHeader("Wechatpay-Signature"))
                    .body(body)
                    .build();

            NotificationParser parser = new NotificationParser(buildWxConfig());
            Transaction tx = parser.parse(requestParam, Transaction.class);

            if (Transaction.TradeStateEnum.SUCCESS.equals(tx.getTradeState())) {
                markOrderPaid(tx.getOutTradeNo(), tx.getTransactionId(), "WECHAT");
                log.info("微信支付回调成功: orderNo={}", tx.getOutTradeNo());
            }
            result.put("code", "SUCCESS");
            result.put("message", "成功");
        } catch (Exception e) {
            log.error("微信回调异常: {}", e.getMessage(), e);
            result.put("code", "FAIL");
            result.put("message", e.getMessage());
        }
        return result;
    }

    //  私有工具 

    private AlipayClient buildAlipayClient() {
        return new DefaultAlipayClient(
                alipayProps.getGatewayUrl(), alipayProps.getAppId(),
                alipayProps.getPrivateKey(), "json", alipayProps.getCharset(),
                alipayProps.getAlipayPublicKey(), alipayProps.getSignType());
    }

    private RSAAutoCertificateConfig buildWxConfig() {
        return new RSAAutoCertificateConfig.Builder()
                .merchantId(wechatProps.getMchId())
                .privateKey(wechatProps.getPrivateKey())
                .merchantSerialNumber(wechatProps.getCertSerialNo())
                .apiV3Key(wechatProps.getApiV3Key())
                .build();
    }

    private Orders getOrder(String orderNo) {
        Orders order = ordersMapper.selectOne(
                new LambdaQueryWrapper<Orders>().eq(Orders::getOrderNo, orderNo));
        if (order == null) throw new RuntimeException("订单不存在: " + orderNo);
        return order;
    }

    private void markOrderPaid(String orderNo, String tradeNo, String paymentType) {
        ordersMapper.update(null, new LambdaUpdateWrapper<Orders>()
                .eq(Orders::getOrderNo, orderNo)
                .eq(Orders::getStatus, "WAIT_PAY")
                .set(Orders::getStatus, "WAIT_MERCHANT_CONFIRM")
                .set(Orders::getTradeNo, tradeNo)
                .set(Orders::getPaymentType, paymentType)
                .set(Orders::getPayTime, LocalDateTime.now()));
    }
}
