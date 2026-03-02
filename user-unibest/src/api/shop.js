import { get } from '@/utils/request'

/** 商家列表 */
export const getShopList = (params) => get('/shop/list', params)

/** 商家详情 */
export const getShopDetail = (id) => get(`/shop/${id}`)
