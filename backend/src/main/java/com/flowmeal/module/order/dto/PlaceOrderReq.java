package com.flowmeal.module.order.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 提交订单请求
 */
@Data
public class PlaceOrderReq {

    @NotNull(message = "收货地址不能为空")
    private Long addressId;

    @NotNull(message = "商家ID不能为空")
    private Long merchantId;

    @NotEmpty(message = "订单商品不能为空")
    private List<OrderItemReq> items;

    private String remark;

    @Data
    public static class OrderItemReq {
        @NotNull(message = "商品ID不能为空")
        private Long productId;

        @NotNull(message = "商品数量不能为空")
        @Min(value = 1, message = "商品数量至少为1")
        private Integer quantity;
    }
}
