package com.flowmeal.module.order.enums;

import com.flowmeal.common.exception.BusinessException;
import com.flowmeal.common.result.ResultCode;

/**
 * 订单状态枚举 - 状态机
 * 禁止跨状态跳转，非法跳转直接抛出异常
 */
public enum OrderStatus {

    WAIT_PAY("待支付"),
    WAIT_MERCHANT_CONFIRM("待商家确认"),
    MERCHANT_CONFIRMED("商家已确认"),
    RIDER_ACCEPTED("骑手已接单"),
    DELIVERING("配送中"),
    COMPLETED("已完成"),
    CANCELLED("已取消");

    private final String desc;

    OrderStatus(String desc) {
        this.desc = desc;
    }

    public String getDesc() {
        return desc;
    }

    /**
     * 状态流转校验 - 禁止非法跨状态跳转
     */
    public static void checkTransition(String current, String target) {
        OrderStatus from = valueOf(current);
        OrderStatus to = valueOf(target);
        boolean legal = switch (from) {
            case WAIT_PAY            -> to == WAIT_MERCHANT_CONFIRM || to == CANCELLED;
            case WAIT_MERCHANT_CONFIRM -> to == MERCHANT_CONFIRMED || to == CANCELLED;
            case MERCHANT_CONFIRMED  -> to == RIDER_ACCEPTED || to == CANCELLED;
            case RIDER_ACCEPTED      -> to == DELIVERING;
            case DELIVERING          -> to == COMPLETED;
            default -> false;
        };
        if (!legal) {
            throw new BusinessException(ResultCode.ORDER_STATUS_ILLEGAL);
        }
    }
}
