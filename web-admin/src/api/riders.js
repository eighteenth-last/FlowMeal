import request from './request'

/** 骑手列表（分页） */
export const getRiders = (params) => request.get('/admin/riders', { params })

/** 审核骑手 */
export const auditRider = (id, status) =>
  request.put(`/admin/riders/${id}/audit`, { status })

/** 切换骑手状态 */
export const toggleRiderStatus = (id, status) =>
  request.put(`/admin/riders/${id}/status`, { status })
