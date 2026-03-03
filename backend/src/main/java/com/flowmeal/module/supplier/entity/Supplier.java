package com.flowmeal.module.supplier.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.flowmeal.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("supplier")
public class Supplier extends BaseEntity {

    /** 供应商名称 */
    private String name;

    /** 联系人 */
    private String contact;

    /** 联系电话 */
    private String phone;

    /** 供应商地址 */
    private String address;

    /** 供应类别（粮油/蔬菜/肉类） */
    private String category;

    /** 状态: 1=合作中 0=停用 */
    private Integer status;

    /** 备注 */
    private String remark;
}
