package com.flowmeal.module.auth.controller;

import com.flowmeal.common.result.Result;
import com.flowmeal.module.auth.dto.*;
import com.flowmeal.module.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 认证接口（公开，无需登录）
 */
@Slf4j
@Tag(name = "认证接口", description = "注册/登录/退出")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    // ========== 用户端 ==========

    @Operation(summary = "用户注册")
    @PostMapping("/user/register")
    public Result<Void> userRegister(@Valid @RequestBody UserRegisterReq req) {
        authService.userRegister(req);
        return Result.success();
    }

    @Operation(summary = "用户登录")
    @PostMapping("/user/login")
    public Result<LoginResp> userLogin(@Valid @RequestBody LoginReq req) {
        return Result.success(authService.userLogin(req));
    }

    // ========== 商家端 ==========

    @Operation(summary = "商家注册")
    @PostMapping("/merchant/register")
    public Result<String> merchantRegister(@Valid @RequestBody MerchantRegisterReq req) {
        authService.merchantRegister(req);
        return Result.success("注册成功，请等待管理员审核");
    }

    @Operation(summary = "商家登录")
    @PostMapping("/merchant/login")
    public Result<LoginResp> merchantLogin(@Valid @RequestBody LoginReq req) {
        return Result.success(authService.merchantLogin(req));
    }

    // ========== 外卖员端 ==========

    @Operation(summary = "外卖员注册")
    @PostMapping("/rider/register")
    public Result<String> riderRegister(@Valid @RequestBody RiderRegisterReq req) {
        authService.riderRegister(req);
        return Result.success("注册成功，请等待管理员审核");
    }

    @Operation(summary = "外卖员登录")
    @PostMapping("/rider/login")
    public Result<LoginResp> riderLogin(@Valid @RequestBody LoginReq req) {
        return Result.success(authService.riderLogin(req));
    }

    // ========== 管理员端 ==========

    @Operation(summary = "管理员登录")
    @PostMapping("/admin/login")
    public Result<LoginResp> adminLogin(@Valid @RequestBody LoginReq req) {
        return Result.success(authService.adminLogin(req));
    }

    // ========== 通用 ==========

    @Operation(summary = "退出登录")
    @PostMapping("/logout")
    public Result<String> logout() {
        authService.logout();
        return Result.success("退出成功");
    }
}
