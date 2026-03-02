package com.flowmeal.module.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 商家注册请求
 */
@Data
public class MerchantRegisterReq {

    @NotBlank(message = "商家账号不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    private String password;

    @NotBlank(message = "店铺名称不能为空")
    private String shopName;

    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    private String address;
    private String businessHours;
}
