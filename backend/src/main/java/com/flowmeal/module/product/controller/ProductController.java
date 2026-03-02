package com.flowmeal.module.product.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.flowmeal.common.result.Result;
import com.flowmeal.module.product.entity.Product;
import com.flowmeal.module.product.entity.ProductCategory;
import com.flowmeal.module.product.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 餐品接口
 */
@Tag(name = "餐品接口")
@RestController
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    // ========== 用户端（公开/登录可访问） ==========

    @Operation(summary = "用户端 - 查看店铺餐品列表")
    @GetMapping("/shop/{merchantId}/products")
    public Result<List<Product>> listForUser(@PathVariable Long merchantId) {
        return Result.success(productService.listWithCategory(merchantId));
    }

    @Operation(summary = "用户端 - 查看店铺分类")
    @GetMapping("/shop/{merchantId}/categories")
    public Result<List<ProductCategory>> listCategories(@PathVariable Long merchantId) {
        return Result.success(productService.listCategories(merchantId));
    }

    // ========== 商家端（需 MERCHANT 角色） ==========

    @Operation(summary = "商家端 - 餐品列表（分页）")
    @GetMapping("/merchant/products")
    public Result<Page<Product>> merchantList(@RequestParam(defaultValue = "1") int page,
                                               @RequestParam(defaultValue = "10") int size) {
        Long merchantId = getMerchantId();
        return Result.success(productService.listByMerchant(merchantId, page, size));
    }

    @Operation(summary = "商家端 - 添加餐品")
    @PostMapping("/merchant/products")
    public Result<Void> add(@RequestBody Product product) {
        product.setMerchantId(getMerchantId());
        productService.addProduct(product);
        return Result.success();
    }

    @Operation(summary = "商家端 - 修改餐品")
    @PutMapping("/merchant/products/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody Product product) {
        product.setId(id);
        productService.updateProduct(product, getMerchantId());
        return Result.success();
    }

    @Operation(summary = "商家端 - 删除餐品")
    @DeleteMapping("/merchant/products/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        productService.deleteProduct(id, getMerchantId());
        return Result.success();
    }

    @Operation(summary = "商家端 - 上/下架餐品")
    @PutMapping("/merchant/products/{id}/status")
    public Result<Void> toggleStatus(@PathVariable Long id, @RequestParam Integer status) {
        productService.toggleStatus(id, getMerchantId(), status);
        return Result.success();
    }

    @Operation(summary = "商家端 - 添加分类")
    @PostMapping("/merchant/categories")
    public Result<Void> addCategory(@RequestBody ProductCategory category) {
        category.setMerchantId(getMerchantId());
        productService.addCategory(category);
        return Result.success();
    }

    @Operation(summary = "商家端 - 删除分类")
    @DeleteMapping("/merchant/categories/{id}")
    public Result<Void> deleteCategory(@PathVariable Long id) {
        productService.deleteCategory(id, getMerchantId());
        return Result.success();
    }

    // ========== 私有工具 ==========

    private Long getMerchantId() {
        // 商家登录key格式: merchant:{id}
        String loginId = StpUtil.getLoginIdAsString();
        return Long.parseLong(loginId.replace("merchant:", ""));
    }
}
