package com.flowmeal.module.user.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.flowmeal.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 收货地址实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("address")
public class Address extends BaseEntity {

    private Long userId;
    private String receiver;
    private String phone;
    private String province;
    private String city;
    private String district;
    private String detail;
    /** 是否默认地址: 1=是 0=否 */
    private Integer isDefault;
}
