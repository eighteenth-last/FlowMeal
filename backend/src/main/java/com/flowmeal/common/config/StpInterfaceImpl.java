package com.flowmeal.common.config;

import cn.dev33.satoken.stp.StpInterface;
import cn.dev33.satoken.stp.StpUtil;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Sa-Token 权限/角色获取实现。
 * Sa-Token 的 checkRole() / checkPermission() 都依赖此接口。
 */
@Component
public class StpInterfaceImpl implements StpInterface {

    /**
     * 获取当前账号的权限列表（暂不细分权限码，返回空即可）
     */
    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        return new ArrayList<>();
    }

    /**
     * 获取当前账号的角色列表。
     * 登录时已将 role 写入 Session，从 Session 中读取。
     */
    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        List<String> roles = new ArrayList<>();
        try {
            String role = (String) StpUtil.getSessionByLoginId(loginId).get("role");
            if (role != null) {
                roles.add(role);
            }
        } catch (Exception ignored) {
        }
        return roles;
    }
}
