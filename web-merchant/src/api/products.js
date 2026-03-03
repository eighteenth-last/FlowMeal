import request from './request'

/** 商品分类列表 */
export const getCategories = (merchantId) =>
  request.get(`/shop/${merchantId}/categories`)

/** 商品列表（当前商家） */
export const getProducts = (params) =>
  request.get('/merchant/products', { params })

/** 新增商品 */
export const createProduct = (data) => request.post('/merchant/products', data)

/** 修改商品 */
export const updateProduct = (id, data) =>
  request.put(`/merchant/products/${id}`, data)

/** 上架/下架 */
export const toggleProductStatus = (id, status) =>
  request.put(`/merchant/products/${id}/status`, null, { params: { status } })

/** 删除商品 */
export const deleteProduct = (id) => request.delete(`/merchant/products/${id}`)
