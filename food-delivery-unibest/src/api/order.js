import { get, put } from '@/utils/request'

/** 抢单大厅 */
export const getHallOrders = (params) => get('/order/rider/hall', params)

/** 订单详情 */
export const getOrderDetail = (orderId) => get(`/order/${orderId}`)

/** 骑手接单 */
export const acceptOrder = (orderId) => put(`/order/${orderId}/rider/accept`)

/** 开始配送 */
export const startDeliver = (orderId) => put(`/order/${orderId}/rider/deliver`)

/** 完成配送 */
export const completeDeliver = (orderId) => put(`/order/${orderId}/rider/complete`)
