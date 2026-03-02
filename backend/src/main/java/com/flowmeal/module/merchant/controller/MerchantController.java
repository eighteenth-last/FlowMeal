package com.flowmeal.module.merchant.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.flowmeal.common.result.Result;
import com.flowmeal.module.merchant.entity.Merchant;
import com.flowmeal.module.merchant.mapper.MerchantMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 商家接口（用户端浏览 + 商家端管理）
 */
@Tag(name = "商家接口")
@RestController
@RequiredArgsConstructor
public class MerchantController {

    private final MerchantMapper merchantMapper;

    // ========== 用户端（公开） ==========

    @Operation(summary = "用户端 - 商家列表（分页）")
    @GetMapping("/shop/list")
    public Result<Page<Merchant>> shopList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword) {
        LambdaQueryWrapper<Merchant> wrapper = new LambdaQueryWrapper<Merchant>()
                .eq(Merchant::getAuditStatus, 1)  // 仅展示审核通过的商家
                .orderByDesc(Merchant::getStatus); // 营业中的靠前
        if (keyword != null && !keyword.isBlank()) {
            wrapper.like(Merchant::getShopName, keyword);
        }
        Page<Merchant> result = merchantMapper.selectPage(new Page<>(page, size), wrapper);
        // 不返回密码字段
        result.getRecords().forEach(m -> m.setPassword(null));
        return Result.success(result);
    }

    @Operation(summary = "用户端 - 商家详情")
    @GetMapping("/shop/{id}")
    public Result<Merchant> shopDetail(@PathVariable Long id) {
        Merchant merchant = merchantMapper.selectById(id);
        if (merchant != null) merchant.setPassword(null);
        return Result.success(merchant);
    }

    // ========== 商家端（需 MERCHANT 角色） ==========

    @Operation(summary = "商家端 - 获取我的店铺信息")
    @GetMapping("/merchant/shop")
    public Result<Merchant> myShop() {
        Merchant merchant = merchantMapper.selectById(getMerchantId());
        if (merchant != null) merchant.setPassword(null);
        return Result.success(merchant);
    }

    @Operation(summary = "商家端 - 修改店铺信息")
    @PutMapping("/merchant/shop")
    public Result<Void> updateShop(@RequestBody Merchant req) {
        Long merchantId = getMerchantId();
        req.setId(merchantId);
        req.setPassword(null);      // 不允许通过此接口改密码
        req.setAuditStatus(null);   // 不允许自己改审核状态
        merchantMapper.updateById(req);
        return Result.success();
    }

    @Operation(summary = "商家端 - 切换营业状态")
    @PutMapping("/merchant/shop/status")
    public Result<Void> toggleStatus(@RequestParam Integer status) {
        merchantMapper.update(null, new LambdaUpdateWrapper<Merchant>()
                .eq(Merchant::getId, getMerchantId())
                .set(Merchant::getStatus, status));
        return Result.success();
    }

    private Long getMerchantId() {
        return Long.parseLong(StpUtil.getLoginIdAsString().replace("merchant:", ""));
    }
}
