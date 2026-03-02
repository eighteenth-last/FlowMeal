package com.flowmeal.module.cart.service;

import com.flowmeal.module.cart.dto.CartItemVO;

import java.util.List;

/**
 * 购物车服务接口（基于 Redis）
 */
public interface CartService {

    /** 添加商品 */
    void addItem(Long userId, Long merchantId, Long productId, Integer quantity);

    /** 修改数量 */
    void updateQty(Long userId, Long productId, Integer quantity);

    /** 删除商品 */
    void removeItem(Long userId, Long productId);

    /** 清空用户购物车 */
    void clearCart(Long userId);

    /** 获取购物车列表 */
    List<CartItemVO> getCart(Long userId);
}
