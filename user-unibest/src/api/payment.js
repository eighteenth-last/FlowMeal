import { get } from '@/utils/request'

/** 发起支付 */
export const payOrder = (orderNo) => get(`/payment/alipay/pay/${orderNo}`)

/** 查询支付状态 */
export const queryPayStatus = (orderNo) => get(`/payment/alipay/query/${orderNo}`)
