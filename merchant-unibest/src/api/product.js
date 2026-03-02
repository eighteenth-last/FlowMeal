import request from '@/utils/request'

// 获取商家餐品列表
export const getProducts = (params) => request({ url: '/merchant/products', data: params })

// 添加餐品
export const addProduct = (data) => request({ url: '/merchant/products', method: 'POST', data })

// 修改餐品
export const updateProduct = (id, data) => request({ url: `/merchant/products/${id}`, method: 'PUT', data })

// 删除餐品
export const deleteProduct = (id) => request({ url: `/merchant/products/${id}`, method: 'DELETE' })

// 获取分类列表
export const getCategories = () => request({ url: '/merchant/categories' })

// 添加分类
export const addCategory = (data) => request({ url: '/merchant/categories', method: 'POST', data })

// 修改分类
export const updateCategory = (id, data) => request({ url: `/merchant/categories/${id}`, method: 'PUT', data })

// 删除分类
export const deleteCategory = (id) => request({ url: `/merchant/categories/${id}`, method: 'DELETE' })
