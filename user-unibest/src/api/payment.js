import { get } from '@/utils/request'

/** H5 支付宝 WAP 支付 — 返回重定向 URL 字符串 */
export const getAlipayH5Url = (orderNo) => get(`/payment/h5/alipay/${orderNo}`)

/** 查询订单支付状态 */
export const queryPayStatus = (orderNo) => get(`/payment/query/${orderNo}`)
