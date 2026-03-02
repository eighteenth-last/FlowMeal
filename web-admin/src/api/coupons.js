import request from './request'

/** 优惠券列表 */
export const getCoupons = (params) => request.get('/admin/coupons', { params })

/** 创建优惠券 */
export const createCoupon = (data) => request.post('/admin/coupons', data)

/** 停用优惠券 */
export const disableCoupon = (id) => request.put(`/admin/coupons/${id}/disable`)

/** 删除优惠券 */
export const deleteCoupon = (id) => request.delete(`/admin/coupons/${id}`)
