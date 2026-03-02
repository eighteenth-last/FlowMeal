package com.flowmeal.module.rider.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.flowmeal.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 外卖员实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("rider")
public class Rider extends BaseEntity {

    private String username;
    private String password;
    private String realName;
    private String phone;
    private String avatar;
    private String idCard;
    /** 在线状态: 1=在线 0=离线 */
    private Integer onlineStatus;
    /** 账号状态: 1=正常 0=封禁 */
    private Integer status;
    /** 审核状态: 0=待审核 1=通过 2=拒绝 */
    private Integer auditStatus;
    private Integer totalOrders;
}
