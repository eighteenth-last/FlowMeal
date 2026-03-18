import request, { get, del } from '@/utils/request'

/** 获取购物车 */
export const getCart = () => get('/cart')

/** 添加商品到购物车（后端用 @RequestParam，需要 query string） */
export const addToCart = (merchantId, productId, quantity = 1) =>
  request({ url: `/cart/add?merchantId=${merchantId}&productId=${productId}&quantity=${quantity}`, method: 'POST' })

/** 修改数量（后端用 @RequestParam） */
export const updateCartQty = (productId, quantity) =>
  request({ url: `/cart/qty?productId=${productId}&quantity=${quantity}`, method: 'PUT' })

/** 删除购物车商品 */
export const removeCartItem = (productId) => del(`/cart/${productId}`)

/** 清空购物车 */
export const clearCart = () => del('/cart/clear')
