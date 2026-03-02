import request from './request'

/** 管理后台数据大盘汇总 */
export const getDashboard = () => request.get('/admin/dashboard')

/** 近 7 天订单趋势 */
export const getOrderTrend = () => request.get('/admin/dashboard/order-trend')

/** 近 7 天营业额趋势 */
export const getRevenueTrend = () => request.get('/admin/dashboard/revenue-trend')
