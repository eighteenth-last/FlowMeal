package com.flowmeal.module.product.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.flowmeal.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 餐品分类实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("product_category")
public class ProductCategory extends BaseEntity {

    private Long merchantId;
    private String name;
    private Integer sort;
}
