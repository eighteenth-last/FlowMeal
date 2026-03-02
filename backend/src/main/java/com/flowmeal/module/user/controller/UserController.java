package com.flowmeal.module.user.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.flowmeal.common.result.Result;
import com.flowmeal.module.user.entity.Address;
import com.flowmeal.module.user.entity.User;
import com.flowmeal.module.user.mapper.AddressMapper;
import com.flowmeal.module.user.mapper.UserMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户端接口（个人信息 + 地址管理）
 */
@Tag(name = "用户接口")
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserMapper userMapper;
    private final AddressMapper addressMapper;

    @Operation(summary = "获取个人信息")
    @GetMapping("/info")
    public Result<User> info() {
        User user = userMapper.selectById(StpUtil.getLoginIdAsLong());
        if (user != null) user.setPassword(null); // 不返回密码
        return Result.success(user);
    }

    @Operation(summary = "修改昵称/头像")
    @PutMapping("/info")
    public Result<Void> updateInfo(@RequestBody User req) {
        Long userId = StpUtil.getLoginIdAsLong();
        userMapper.update(null, new LambdaUpdateWrapper<User>()
                .eq(User::getId, userId)
                .set(req.getNickname() != null, User::getNickname, req.getNickname())
                .set(req.getAvatar() != null, User::getAvatar, req.getAvatar()));
        return Result.success();
    }

    // ========== 地址管理 ==========

    @Operation(summary = "地址列表")
    @GetMapping("/addresses")
    public Result<List<Address>> listAddresses() {
        Long userId = StpUtil.getLoginIdAsLong();
        return Result.success(addressMapper.selectList(new LambdaQueryWrapper<Address>()
                .eq(Address::getUserId, userId)
                .orderByDesc(Address::getIsDefault)));
    }

    @Operation(summary = "添加地址")
    @PostMapping("/addresses")
    public Result<Void> addAddress(@RequestBody Address address) {
        Long userId = StpUtil.getLoginIdAsLong();
        address.setUserId(userId);
        // 如果设为默认，先取消其他默认
        if (Integer.valueOf(1).equals(address.getIsDefault())) {
            clearDefaultAddress(userId);
        }
        addressMapper.insert(address);
        return Result.success();
    }

    @Operation(summary = "修改地址")
    @PutMapping("/addresses/{id}")
    public Result<Void> updateAddress(@PathVariable Long id, @RequestBody Address address) {
        Long userId = StpUtil.getLoginIdAsLong();
        address.setId(id);
        address.setUserId(userId);
        if (Integer.valueOf(1).equals(address.getIsDefault())) {
            clearDefaultAddress(userId);
        }
        addressMapper.updateById(address);
        return Result.success();
    }

    @Operation(summary = "删除地址")
    @DeleteMapping("/addresses/{id}")
    public Result<Void> deleteAddress(@PathVariable Long id) {
        addressMapper.delete(new LambdaQueryWrapper<Address>()
                .eq(Address::getId, id)
                .eq(Address::getUserId, StpUtil.getLoginIdAsLong()));
        return Result.success();
    }

    @Operation(summary = "设为默认地址")
    @PutMapping("/addresses/{id}/default")
    public Result<Void> setDefault(@PathVariable Long id) {
        Long userId = StpUtil.getLoginIdAsLong();
        clearDefaultAddress(userId);
        addressMapper.update(null, new LambdaUpdateWrapper<Address>()
                .eq(Address::getId, id)
                .eq(Address::getUserId, userId)
                .set(Address::getIsDefault, 1));
        return Result.success();
    }

    private void clearDefaultAddress(Long userId) {
        addressMapper.update(null, new LambdaUpdateWrapper<Address>()
                .eq(Address::getUserId, userId)
                .set(Address::getIsDefault, 0));
    }
}
