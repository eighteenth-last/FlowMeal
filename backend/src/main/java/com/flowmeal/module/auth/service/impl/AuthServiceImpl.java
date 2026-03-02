package com.flowmeal.module.auth.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.flowmeal.common.exception.BusinessException;
import com.flowmeal.common.result.ResultCode;
import com.flowmeal.module.admin.entity.Admin;
import com.flowmeal.module.admin.mapper.AdminMapper;
import com.flowmeal.module.auth.dto.*;
import com.flowmeal.module.auth.service.AuthService;
import com.flowmeal.module.merchant.entity.Merchant;
import com.flowmeal.module.merchant.mapper.MerchantMapper;
import com.flowmeal.module.rider.entity.Rider;
import com.flowmeal.module.rider.mapper.RiderMapper;
import com.flowmeal.module.user.entity.User;
import com.flowmeal.module.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 认证服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserMapper userMapper;
    private final MerchantMapper merchantMapper;
    private final RiderMapper riderMapper;
    private final AdminMapper adminMapper;
    private final PasswordEncoder passwordEncoder;

    // ========== 用户 ==========

    @Override
    @Transactional
    public void userRegister(UserRegisterReq req) {
        // 校验手机号唯一
        Long phoneCount = userMapper.selectCount(new LambdaQueryWrapper<User>()
                .eq(User::getPhone, req.getPhone()));
        if (phoneCount > 0) {
            throw new BusinessException(ResultCode.USER_PHONE_EXISTS);
        }
        // 校验用户名唯一
        Long usernameCount = userMapper.selectCount(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, req.getUsername()));
        if (usernameCount > 0) {
            throw new BusinessException(ResultCode.USER_USERNAME_EXISTS);
        }

        User user = new User();
        user.setUsername(req.getUsername());
        user.setPhone(req.getPhone());
        user.setPassword(passwordEncoder.encode(req.getPassword()));
        user.setNickname(req.getNickname() != null ? req.getNickname() : req.getUsername());
        user.setStatus(1);
        userMapper.insert(user);
        log.info("用户注册成功: username={}", req.getUsername());
    }

    @Override
    public LoginResp userLogin(LoginReq req) {
        // 支持用户名或手机号登录
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, req.getUsername())
                .or().eq(User::getPhone, req.getUsername()));
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        if (!passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            throw new BusinessException(ResultCode.USER_PASSWORD_ERROR);
        }
        if (user.getStatus() == 0) {
            throw new BusinessException(ResultCode.USER_ACCOUNT_FROZEN);
        }

        // Sa-Token 登录，附加角色
        StpUtil.login(user.getId());
        StpUtil.getSession().set("role", "USER");

        return LoginResp.builder()
                .token(StpUtil.getTokenValue())
                .userId(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .avatar(user.getAvatar())
                .role("USER")
                .build();
    }

    // ========== 商家 ==========

    @Override
    @Transactional
    public void merchantRegister(MerchantRegisterReq req) {
        Long count = merchantMapper.selectCount(new LambdaQueryWrapper<Merchant>()
                .eq(Merchant::getUsername, req.getUsername()));
        if (count > 0) {
            throw new BusinessException(ResultCode.MERCHANT_USERNAME_EXISTS);
        }

        Merchant merchant = new Merchant();
        merchant.setUsername(req.getUsername());
        merchant.setPassword(passwordEncoder.encode(req.getPassword()));
        merchant.setShopName(req.getShopName());
        merchant.setPhone(req.getPhone());
        merchant.setAddress(req.getAddress());
        merchant.setBusinessHours(req.getBusinessHours());
        merchant.setStatus(0);      // 默认打烊
        merchant.setAuditStatus(0); // 待审核
        merchantMapper.insert(merchant);
        log.info("商家注册成功，等待审核: username={}", req.getUsername());
    }

    @Override
    public LoginResp merchantLogin(LoginReq req) {
        Merchant merchant = merchantMapper.selectOne(new LambdaQueryWrapper<Merchant>()
                .eq(Merchant::getUsername, req.getUsername()));
        if (merchant == null) {
            throw new BusinessException(ResultCode.MERCHANT_NOT_FOUND);
        }
        if (!passwordEncoder.matches(req.getPassword(), merchant.getPassword())) {
            throw new BusinessException(ResultCode.USER_PASSWORD_ERROR);
        }
        if (merchant.getAuditStatus() != 1) {
            throw new BusinessException(ResultCode.MERCHANT_NOT_AUDIT);
        }

        // Sa-Token 登录（使用负数区分用户与商家，避免 ID 冲突）
        StpUtil.login("merchant:" + merchant.getId());
        StpUtil.getSession().set("role", "MERCHANT");

        return LoginResp.builder()
                .token(StpUtil.getTokenValue())
                .userId(merchant.getId())
                .username(merchant.getUsername())
                .nickname(merchant.getShopName())
                .avatar(merchant.getAvatar())
                .role("MERCHANT")
                .build();
    }

    // ========== 外卖员 ==========

    @Override
    @Transactional
    public void riderRegister(RiderRegisterReq req) {
        Long count = riderMapper.selectCount(new LambdaQueryWrapper<Rider>()
                .eq(Rider::getUsername, req.getUsername()));
        if (count > 0) {
            throw new BusinessException(ResultCode.USER_USERNAME_EXISTS);
        }

        Rider rider = new Rider();
        rider.setUsername(req.getUsername());
        rider.setPassword(passwordEncoder.encode(req.getPassword()));
        rider.setRealName(req.getRealName());
        rider.setPhone(req.getPhone());
        rider.setIdCard(req.getIdCard());
        rider.setOnlineStatus(0);
        rider.setStatus(1);
        rider.setAuditStatus(0);
        rider.setTotalOrders(0);
        riderMapper.insert(rider);
    }

    @Override
    public LoginResp riderLogin(LoginReq req) {
        Rider rider = riderMapper.selectOne(new LambdaQueryWrapper<Rider>()
                .eq(Rider::getUsername, req.getUsername()));
        if (rider == null) {
            throw new BusinessException(ResultCode.RIDER_NOT_FOUND);
        }
        if (!passwordEncoder.matches(req.getPassword(), rider.getPassword())) {
            throw new BusinessException(ResultCode.USER_PASSWORD_ERROR);
        }
        if (rider.getAuditStatus() != 1) {
            throw new BusinessException(ResultCode.RIDER_NOT_AUDIT);
        }
        if (rider.getStatus() == 0) {
            throw new BusinessException(ResultCode.USER_ACCOUNT_FROZEN);
        }

        StpUtil.login("rider:" + rider.getId());
        StpUtil.getSession().set("role", "RIDER");

        return LoginResp.builder()
                .token(StpUtil.getTokenValue())
                .userId(rider.getId())
                .username(rider.getUsername())
                .nickname(rider.getRealName())
                .avatar(rider.getAvatar())
                .role("RIDER")
                .build();
    }

    // ========== 管理员 ==========

    @Override
    public LoginResp adminLogin(LoginReq req) {
        Admin admin = adminMapper.selectOne(new LambdaQueryWrapper<Admin>()
                .eq(Admin::getUsername, req.getUsername()));
        if (admin == null || admin.getStatus() == 0) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        if (!passwordEncoder.matches(req.getPassword(), admin.getPassword())) {
            throw new BusinessException(ResultCode.USER_PASSWORD_ERROR);
        }

        StpUtil.login("admin:" + admin.getId());
        StpUtil.getSession().set("role", admin.getRole());

        return LoginResp.builder()
                .token(StpUtil.getTokenValue())
                .userId(admin.getId())
                .username(admin.getUsername())
                .nickname(admin.getRealName())
                .avatar(admin.getAvatar())
                .role(admin.getRole())
                .build();
    }

    // ========== 通用 ==========

    @Override
    public void logout() {
        StpUtil.logout();
    }
}
