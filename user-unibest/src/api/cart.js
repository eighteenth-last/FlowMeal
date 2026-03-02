import { get, post, put, del } from '@/utils/request'

/** 获取购物车 */
export const getCart = () => get('/cart')

/** 添加商品到购物车 */
export const addToCart = (merchantId, productId, quantity = 1) =>
  post('/cart/add', { merchantId, productId, quantity })

/** 修改数量 */
export const updateCartQty = (productId, quantity) =>
  put('/cart/qty', { productId, quantity })

/** 删除购物车商品 */
export const removeCartItem = (productId) => del(`/cart/${productId}`)

/** 清空购物车 */
export const clearCart = () => del('/cart/clear')
