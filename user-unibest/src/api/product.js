import { get } from '@/utils/request'

/** 店铺分类列表 */
export const getCategories = (merchantId) => get(`/shop/${merchantId}/categories`)

/** 店铺餐品列表 */
export const getProducts = (merchantId) => get(`/shop/${merchantId}/products`)
