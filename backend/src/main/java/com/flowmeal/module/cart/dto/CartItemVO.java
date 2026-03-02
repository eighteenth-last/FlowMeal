package com.flowmeal.module.cart.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 购物车条目视图对象
 */
@Data
public class CartItemVO {

    private Long productId;
    private String productName;
    private String productImage;
    private BigDecimal price;
    private BigDecimal discountPrice;
    private Integer quantity;
    private BigDecimal subtotal;
    private Integer stock;
    private Integer productStatus;
}
