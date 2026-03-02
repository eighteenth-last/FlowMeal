package com.flowmeal.module.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 登录响应 - 包含 Token 和用户基本信息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResp {

    private String token;
    private Long userId;
    private String username;
    private String nickname;
    private String avatar;
    /** 角色: USER / MERCHANT / RIDER / ADMIN */
    private String role;
}
