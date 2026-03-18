package com.flowmeal.module.order.dto;

import com.flowmeal.module.order.entity.OrderItem;
import com.flowmeal.module.order.entity.Orders;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 订单详情 VO（含商家、骑手、商品明细）
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class OrderDetailVO extends Orders {

    /** 商家店铺名 */
    private String shopName;
    /** 商家头像 */
    private String shopAvatar;
    /** 骑手姓名 */
    private String riderName;
    /** 骑手电话 */
    private String riderPhone;
    /** 订单商品明细 */
    private List<OrderItem> items;
}
