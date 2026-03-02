package com.flowmeal.common.result;

import lombok.Getter;

/**
 * 业务状态码枚举
 */
@Getter
public enum ResultCode {

    // 通用
    SUCCESS(200, "操作成功"),
    FAIL(500, "操作失败"),
    PARAM_ERROR(400, "参数错误"),
    UNAUTHORIZED(401, "请先登录"),
    FORBIDDEN(403, "无权限访问"),
    NOT_FOUND(404, "资源不存在"),

    // 用户相关 1000~1099
    USER_NOT_FOUND(1001, "用户不存在"),
    USER_PASSWORD_ERROR(1002, "密码错误"),
    USER_ACCOUNT_FROZEN(1003, "账号已被冻结"),
    USER_PHONE_EXISTS(1004, "手机号已被注册"),
    USER_USERNAME_EXISTS(1005, "用户名已存在"),

    // 商家相关 1100~1199
    MERCHANT_NOT_FOUND(1101, "商家不存在"),
    MERCHANT_NOT_AUDIT(1102, "商家尚未通过审核"),
    MERCHANT_SHOP_CLOSED(1103, "店铺已打烊"),
    MERCHANT_USERNAME_EXISTS(1104, "商家账号已存在"),

    // 外卖员相关 1200~1299
    RIDER_NOT_FOUND(1201, "外卖员不存在"),
    RIDER_NOT_AUDIT(1202, "外卖员账号尚未审核通过"),
    RIDER_OFFLINE(1203, "外卖员当前离线"),

    // 餐品相关 1300~1399
    PRODUCT_NOT_FOUND(1301, "餐品不存在"),
    PRODUCT_OFF_SHELF(1302, "餐品已下架"),
    PRODUCT_STOCK_INSUFFICIENT(1303, "餐品库存不足"),

    // 订单相关 1400~1499
    ORDER_NOT_FOUND(1401, "订单不存在"),
    ORDER_STATUS_ILLEGAL(1402, "订单状态不合法，禁止跨状态跳转"),
    ORDER_CANNOT_CANCEL(1403, "当前订单状态不允许取消"),
    ORDER_NOT_OWNER(1404, "无权操作此订单"),

    // 支付相关 1500~1599
    PAYMENT_FAILED(1501, "支付失败"),
    PAYMENT_ORDER_NOT_FOUND(1502, "支付订单不存在"),
    PAYMENT_ALREADY_PAID(1503, "订单已支付");

    private final Integer code;
    private final String message;

    ResultCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}
