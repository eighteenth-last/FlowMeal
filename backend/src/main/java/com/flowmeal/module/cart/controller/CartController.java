package com.flowmeal.module.cart.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.flowmeal.common.result.Result;
import com.flowmeal.module.cart.dto.CartItemVO;
import com.flowmeal.module.cart.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 购物车接口（用户端）
 */
@Tag(name = "购物车接口")
@RestController
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @Operation(summary = "添加商品到购物车")
    @PostMapping("/add")
    public Result<Void> add(@RequestParam Long merchantId,
                            @RequestParam Long productId,
                            @RequestParam(defaultValue = "1") Integer quantity) {
        Long userId = StpUtil.getLoginIdAsLong();
        cartService.addItem(userId, merchantId, productId, quantity);
        return Result.success();
    }

    @Operation(summary = "修改购物车商品数量")
    @PutMapping("/qty")
    public Result<Void> updateQty(@RequestParam Long productId,
                                   @RequestParam Integer quantity) {
        cartService.updateQty(StpUtil.getLoginIdAsLong(), productId, quantity);
        return Result.success();
    }

    @Operation(summary = "删除购物车商品")
    @DeleteMapping("/{productId}")
    public Result<Void> remove(@PathVariable Long productId) {
        cartService.removeItem(StpUtil.getLoginIdAsLong(), productId);
        return Result.success();
    }

    @Operation(summary = "清空购物车")
    @DeleteMapping("/clear")
    public Result<Void> clear() {
        cartService.clearCart(StpUtil.getLoginIdAsLong());
        return Result.success();
    }

    @Operation(summary = "获取购物车列表")
    @GetMapping
    public Result<List<CartItemVO>> list() {
        return Result.success(cartService.getCart(StpUtil.getLoginIdAsLong()));
    }
}
