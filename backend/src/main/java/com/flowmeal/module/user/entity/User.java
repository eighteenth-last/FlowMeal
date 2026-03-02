package com.flowmeal.module.user.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.flowmeal.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("user")
public class User extends BaseEntity {

    private String username;
    private String phone;
    private String password;
    private String avatar;
    private String nickname;
    /** 状态: 1=正常 0=冻结 */
    private Integer status;
}
