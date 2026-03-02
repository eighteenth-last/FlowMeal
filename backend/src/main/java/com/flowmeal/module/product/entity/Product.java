package com.flowmeal.module.product.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.flowmeal.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 餐品实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("product")
public class Product extends BaseEntity {

    private Long merchantId;
    private Long categoryId;
    private String name;
    private String description;
    private String image;
    private BigDecimal price;
    private BigDecimal discountPrice;
    private Integer stock;
    private Integer sales;
    /** 状态: 1=上架 0=下架 */
    private Integer status;
    private Integer sort;
}
