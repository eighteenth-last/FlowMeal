package com.flowmeal.module.material.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.flowmeal.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("material_report")
public class MaterialReport extends BaseEntity {

    /** 商家ID */
    private Long merchantId;

    /** 原料名称 */
    private String materialName;

    /** 消耗量 */
    private Integer consumed;

    /** 单位 */
    private String unit;

    /** 上报时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime reportTime;
}
