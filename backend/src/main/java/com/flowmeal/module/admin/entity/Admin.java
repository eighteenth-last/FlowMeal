package com.flowmeal.module.admin.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.flowmeal.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 管理员实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("admin")
public class Admin extends BaseEntity {

    private String username;
    private String password;
    private String realName;
    private String avatar;
    /** 角色: ADMIN / SUPER_ADMIN */
    private String role;
    /** 状态: 1=正常 0=禁用 */
    private Integer status;
}
