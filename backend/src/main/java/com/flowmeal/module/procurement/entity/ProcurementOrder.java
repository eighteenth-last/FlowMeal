package com.flowmeal.module.procurement.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.flowmeal.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 采购单实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("procurement_order")
public class ProcurementOrder extends BaseEntity {

    /** 供应商名称 */
    private String supplier;

    /** 原料名称 */
    private String materialName;

    /** 采购数量 */
    private Integer quantity;

    /** 单位 */
    private String unit;

    /** 备注 */
    private String remark;

    /** 状态: PENDING=待发货 SHIPPED=已发货 DELIVERED=已到货 */
    private String status;
}
