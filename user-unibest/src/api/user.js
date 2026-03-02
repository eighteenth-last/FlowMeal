import { get, put, post, del } from '@/utils/request'

/** 获取个人信息 */
export const getUserInfo = () => get('/user/info')

/** 修改个人信息 */
export const updateUserInfo = (data) => put('/user/info', data)

/** 地址列表 */
export const getAddresses = () => get('/user/addresses')

/** 添加地址 */
export const addAddress = (data) => post('/user/addresses', data)

/** 修改地址 */
export const updateAddress = (id, data) => put(`/user/addresses/${id}`, data)

/** 删除地址 */
export const deleteAddress = (id) => del(`/user/addresses/${id}`)

/** 设为默认地址 */
export const setDefaultAddress = (id) => put(`/user/addresses/${id}/default`)
