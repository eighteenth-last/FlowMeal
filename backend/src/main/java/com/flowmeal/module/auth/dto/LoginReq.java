package com.flowmeal.module.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 通用登录请求（用户名/手机号 + 密码）
 */
@Data
public class LoginReq {

    @NotBlank(message = "账号不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    private String password;
}
