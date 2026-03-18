import { get, post, put } from '@/utils/request'

/** 下单 */
export const placeOrder = (data) => post('/order/place', data)

/** 我的订单列表 */
export const getMyOrders = (params) => get('/order/my', params)

/** 订单详情 */
export const getOrderDetail = (orderId) => get(`/order/${orderId}`)

/** 取消订单（reason 作为 query param） */
export const cancelOrder = (orderId, reason) =>
  put(`/order/${orderId}/cancel${reason ? '?reason=' + encodeURIComponent(reason) : ''}`)
