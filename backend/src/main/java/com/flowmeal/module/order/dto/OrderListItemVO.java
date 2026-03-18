package com.flowmeal.module.order.dto;

import com.flowmeal.module.order.entity.Orders;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 订单列表项 VO（含商家名称、首个商品信息）
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class OrderListItemVO extends Orders {

    /** 商家店铺名 */
    private String shopName;
    /** 商家头像 */
    private String shopAvatar;
    /** 首个商品名称 */
    private String firstItemName;
    /** 首个商品图片 */
    private String firstItemImage;
    /** 商品总数量 */
    private Integer totalQuantity;
}
