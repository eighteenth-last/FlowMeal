import request from './request'

/** 商家列表（分页，支持按名称/状态筛选） */
export const getMerchants = (params) => request.get('/admin/merchants', { params })

/** 审核商家 status: APPROVED / REJECTED */
export const auditMerchant = (id, data) => request.put(`/admin/merchants/${id}/audit`, data)

/** 禁用/启用商家 */
export const toggleMerchantStatus = (id, status) =>
  request.put(`/admin/merchants/${id}/status`, { status })
