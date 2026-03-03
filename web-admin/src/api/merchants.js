import request from './request'

/** 商家列表（分页，支持按名称/状态筛选） */
export const getMerchants = (params) => request.get('/admin/merchants', { params })

/** 审核商家 auditStatus: 1-通过 2-拒绝 */
export const auditMerchant = (id, data) =>
  request.put(`/admin/merchants/${id}/audit`, null, {
    params: { auditStatus: data.status === 'APPROVED' ? 1 : 2, remark: data.remark }
  })

/** 禁用/启用商家 */
export const toggleMerchantStatus = (id, status) =>
  request.put(`/admin/merchants/${id}/status`, null, { params: { status } })
