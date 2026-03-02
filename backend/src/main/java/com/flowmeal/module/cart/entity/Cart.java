package com.flowmeal.module.cart.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.flowmeal.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 购物车实体（持久化，主缓存在 Redis）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("cart")
public class Cart extends BaseEntity {

    private Long userId;
    private Long merchantId;
    private Long productId;
    private Integer quantity;
}
