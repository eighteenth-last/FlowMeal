package com.flowmeal.module.admin.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.flowmeal.common.result.Result;
import com.flowmeal.module.merchant.entity.Merchant;
import com.flowmeal.module.merchant.mapper.MerchantMapper;
import com.flowmeal.module.rider.entity.Rider;
import com.flowmeal.module.rider.mapper.RiderMapper;
import com.flowmeal.module.user.entity.User;
import com.flowmeal.module.user.mapper.UserMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 管理员接口（需 ADMIN 角色）
 */
@Tag(name = "管理员接口")
@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserMapper userMapper;
    private final MerchantMapper merchantMapper;
    private final RiderMapper riderMapper;

    // ========== 用户管理 ==========

    @Operation(summary = "用户列表")
    @GetMapping("/users")
    public Result<Page<User>> listUsers(@RequestParam(defaultValue = "1") int page,
                                         @RequestParam(defaultValue = "10") int size,
                                         @RequestParam(required = false) String keyword) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<User>()
                .orderByDesc(User::getCreatedAt);
        if (keyword != null && !keyword.isBlank()) {
            wrapper.like(User::getUsername, keyword).or().like(User::getPhone, keyword);
        }
        return Result.success(userMapper.selectPage(new Page<>(page, size), wrapper));
    }

    @Operation(summary = "冻结/解冻用户")
    @PutMapping("/users/{id}/status")
    public Result<Void> toggleUserStatus(@PathVariable Long id, @RequestParam Integer status) {
        userMapper.update(null, new LambdaUpdateWrapper<User>()
                .eq(User::getId, id)
                .set(User::getStatus, status));
        return Result.success();
    }

    // ========== 商家管理 ==========

    @Operation(summary = "商家列表")
    @GetMapping("/merchants")
    public Result<Page<Merchant>> listMerchants(@RequestParam(defaultValue = "1") int page,
                                                  @RequestParam(defaultValue = "10") int size,
                                                  @RequestParam(required = false) Integer auditStatus) {
        LambdaQueryWrapper<Merchant> wrapper = new LambdaQueryWrapper<Merchant>()
                .orderByDesc(Merchant::getCreatedAt);
        if (auditStatus != null) {
            wrapper.eq(Merchant::getAuditStatus, auditStatus);
        }
        return Result.success(merchantMapper.selectPage(new Page<>(page, size), wrapper));
    }

    @Operation(summary = "审核商家")
    @PutMapping("/merchants/{id}/audit")
    public Result<Void> auditMerchant(@PathVariable Long id,
                                       @RequestParam Integer auditStatus,
                                       @RequestParam(required = false) String remark) {
        merchantMapper.update(null, new LambdaUpdateWrapper<Merchant>()
                .eq(Merchant::getId, id)
                .set(Merchant::getAuditStatus, auditStatus)
                .set(Merchant::getAuditRemark, remark));
        return Result.success();
    }

    // ========== 外卖员管理 ==========

    @Operation(summary = "外卖员列表")
    @GetMapping("/riders")
    public Result<Page<Rider>> listRiders(@RequestParam(defaultValue = "1") int page,
                                           @RequestParam(defaultValue = "10") int size,
                                           @RequestParam(required = false) Integer auditStatus) {
        LambdaQueryWrapper<Rider> wrapper = new LambdaQueryWrapper<Rider>()
                .orderByDesc(Rider::getCreatedAt);
        if (auditStatus != null) {
            wrapper.eq(Rider::getAuditStatus, auditStatus);
        }
        return Result.success(riderMapper.selectPage(new Page<>(page, size), wrapper));
    }

    @Operation(summary = "审核外卖员")
    @PutMapping("/riders/{id}/audit")
    public Result<Void> auditRider(@PathVariable Long id, @RequestParam Integer auditStatus) {
        riderMapper.update(null, new LambdaUpdateWrapper<Rider>()
                .eq(Rider::getId, id)
                .set(Rider::getAuditStatus, auditStatus));
        return Result.success();
    }

    @Operation(summary = "封禁/解封外卖员")
    @PutMapping("/riders/{id}/status")
    public Result<Void> toggleRiderStatus(@PathVariable Long id, @RequestParam Integer status) {
        riderMapper.update(null, new LambdaUpdateWrapper<Rider>()
                .eq(Rider::getId, id)
                .set(Rider::getStatus, status));
        return Result.success();
    }
}
