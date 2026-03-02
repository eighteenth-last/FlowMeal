package com.flowmeal.module.merchant.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.flowmeal.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 商家实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("merchant")
public class Merchant extends BaseEntity {

    private String username;
    private String password;
    private String shopName;
    private String phone;
    private String avatar;
    private String description;
    private String notice;
    private String address;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String businessHours;
    private BigDecimal minPrice;
    private BigDecimal deliveryFee;
    /** 营业状态: 1=营业中 0=打烊 */
    private Integer status;
    /** 审核状态: 0=待审核 1=通过 2=拒绝 */
    private Integer auditStatus;
    private String auditRemark;
}
