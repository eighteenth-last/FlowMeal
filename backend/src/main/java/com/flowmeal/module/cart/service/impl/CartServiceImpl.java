package com.flowmeal.module.cart.service.impl;

import com.flowmeal.common.exception.BusinessException;
import com.flowmeal.common.result.ResultCode;
import com.flowmeal.module.cart.dto.CartItemVO;
import com.flowmeal.module.cart.service.CartService;
import com.flowmeal.module.product.entity.Product;
import com.flowmeal.module.product.mapper.ProductMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 购物车服务 - Redis Hash 实现
 * Key:   cart:{userId}
 * Field: {productId}
 * Value: quantity (Integer)
 */
@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ProductMapper productMapper;

    private static final String CART_KEY_PREFIX = "cart:";

    private String cartKey(Long userId) {
        return CART_KEY_PREFIX + userId;
    }

    @Override
    public void addItem(Long userId, Long merchantId, Long productId, Integer quantity) {
        Product product = productMapper.selectById(productId);
        if (product == null || product.getStatus() == 0) {
            throw new BusinessException(ResultCode.PRODUCT_OFF_SHELF);
        }
        if (!product.getMerchantId().equals(merchantId)) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "商品不属于该商家");
        }

        String key = cartKey(userId);
        Object existing = redisTemplate.opsForHash().get(key, productId.toString());
        int currentQty = existing == null ? 0 : ((Number) existing).intValue();
        redisTemplate.opsForHash().put(key, productId.toString(), currentQty + quantity);
    }

    @Override
    public void updateQty(Long userId, Long productId, Integer quantity) {
        if (quantity <= 0) {
            removeItem(userId, productId);
            return;
        }
        redisTemplate.opsForHash().put(cartKey(userId), productId.toString(), quantity);
    }

    @Override
    public void removeItem(Long userId, Long productId) {
        redisTemplate.opsForHash().delete(cartKey(userId), productId.toString());
    }

    @Override
    public void clearCart(Long userId) {
        redisTemplate.delete(cartKey(userId));
    }

    @Override
    public List<CartItemVO> getCart(Long userId) {
        Map<Object, Object> entries = redisTemplate.opsForHash().entries(cartKey(userId));
        List<CartItemVO> result = new ArrayList<>();

        for (Map.Entry<Object, Object> entry : entries.entrySet()) {
            Long productId = Long.parseLong(entry.getKey().toString());
            int qty = ((Number) entry.getValue()).intValue();

            Product product = productMapper.selectById(productId);
            if (product == null) continue;

            CartItemVO vo = new CartItemVO();
            vo.setProductId(productId);
            vo.setProductName(product.getName());
            vo.setProductImage(product.getImage());
            vo.setPrice(product.getPrice());
            vo.setDiscountPrice(product.getDiscountPrice());
            vo.setQuantity(qty);
            vo.setStock(product.getStock());
            vo.setProductStatus(product.getStatus());

            BigDecimal unitPrice = Objects.requireNonNullElse(product.getDiscountPrice(), product.getPrice());
            vo.setSubtotal(unitPrice.multiply(BigDecimal.valueOf(qty)));
            result.add(vo);
        }

        return result;
    }
}
