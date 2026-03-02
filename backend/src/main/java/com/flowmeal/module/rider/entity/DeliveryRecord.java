package com.flowmeal.module.rider.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 配送记录实体
 */
@Data
@TableName("delivery_record")
public class DeliveryRecord {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long orderId;
    private Long riderId;
    private LocalDateTime acceptTime;
    private LocalDateTime pickupTime;
    private LocalDateTime completeTime;
    private Integer durationMin;
    private BigDecimal distanceKm;
    private String remark;
    private LocalDateTime createdAt;
}
