package com.flowmeal.module.order.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.flowmeal.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("orders")
public class Orders extends BaseEntity {

    private String orderNo;
    private Long userId;
    private Long merchantId;
    private Long riderId;
    private Long addressId;
    /** 地址快照 JSON 字符串 */
    private String addressSnapshot;
    private BigDecimal totalAmount;
    private BigDecimal deliveryFee;
    private BigDecimal actualAmount;
    private String remark;
    /**
     * 订单状态枚举:
     * WAIT_PAY / WAIT_MERCHANT_CONFIRM / MERCHANT_CONFIRMED /
     * RIDER_ACCEPTED / DELIVERING / COMPLETED / CANCELLED
     */
    private String status;
    private String cancelReason;
    private String rejectReason;
    private LocalDateTime payTime;
    private LocalDateTime acceptTime;
    private LocalDateTime pickUpTime;
    private LocalDateTime deliverTime;
    private LocalDateTime completeTime;
    /** 支付方式: ALIPAY / WECHAT */
    private String paymentType;
    private String tradeNo;
}
