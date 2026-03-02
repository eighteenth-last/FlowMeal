package com.flowmeal.module.order.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.flowmeal.module.order.dto.PlaceOrderReq;
import com.flowmeal.module.order.entity.Orders;

/**
 * 订单服务接口
 */
public interface OrderService {

    /** 用户下单 */
    String placeOrder(Long userId, PlaceOrderReq req);

    /** 取消订单 */
    void cancelOrder(Long orderId, Long userId, String reason);

    /** 商家接单 */
    void merchantAccept(Long orderId, Long merchantId);

    /** 商家拒单 */
    void merchantReject(Long orderId, Long merchantId, String reason);

    /** 骑手接单 */
    void riderAccept(Long orderId, Long riderId);

    /** 开始配送 */
    void startDelivery(Long orderId, Long riderId);

    /** 完成配送 */
    void completeDelivery(Long orderId, Long riderId);

    /** 用户端 - 查询我的订单（分页） */
    Page<Orders> listUserOrders(Long userId, String status, int page, int size);

    /** 商家端 - 查询店铺订单（分页） */
    Page<Orders> listMerchantOrders(Long merchantId, String status, int page, int size);

    /** 骑手端 - 查询待接订单大厅（MERCHANT_CONFIRMED） */
    Page<Orders> listHallOrders(int page, int size);

    /** 查询订单详情 */
    Orders getOrderDetail(Long orderId);
}
