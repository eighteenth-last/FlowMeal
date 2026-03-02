package com.flowmeal.module.auth.service;

import com.flowmeal.module.auth.dto.*;

/**
 * 认证服务接口
 */
public interface AuthService {

    /** 用户注册 */
    void userRegister(UserRegisterReq req);

    /** 用户登录（用户名或手机号） */
    LoginResp userLogin(LoginReq req);

    /** 商家注册 */
    void merchantRegister(MerchantRegisterReq req);

    /** 商家登录 */
    LoginResp merchantLogin(LoginReq req);

    /** 外卖员注册 */
    void riderRegister(RiderRegisterReq req);

    /** 外卖员登录 */
    LoginResp riderLogin(LoginReq req);

    /** 管理员登录 */
    LoginResp adminLogin(LoginReq req);

    /** 退出登录 */
    void logout();
}
