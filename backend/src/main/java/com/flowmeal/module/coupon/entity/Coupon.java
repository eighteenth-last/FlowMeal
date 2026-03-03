package com.flowmeal.module.coupon.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.flowmeal.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 优惠券实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("coupon")
public class Coupon extends BaseEntity {

    /** 券名称 */
    private String name;

    /** 券类型: FULL_REDUCTION=满减 / DISCOUNT=折扣 / NEW_USER=新人券 */
    private String type;

    /** 面值/折扣值 */
    private BigDecimal value;

    /** 使用门槛金额 */
    private BigDecimal minAmount;

    /** 总发行量 */
    private Integer total;

    /** 已领取数量 */
    private Integer claimedCount;

    /** 有效期开始 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    /** 有效期结束 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

    /** 状态: ACTIVE=生效 INACTIVE=停用 */
    private String status;
}
