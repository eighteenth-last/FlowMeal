import request from './request'

/** 今日营业概览 */
export const getTodaySummary = () =>
  request.get('/merchant/stats/today')

/** 近7天营业额折线图数据 */
export const getRevenueTrend = () =>
  request.get('/merchant/stats/revenue-trend')

/** 近7天订单量折线图数据 */
export const getOrderTrend = () =>
  request.get('/merchant/stats/order-trend')

/** 商品销售排行 */
export const getProductRank = () =>
  request.get('/merchant/stats/product-rank')
