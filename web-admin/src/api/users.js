import request from './request'

/** 用户列表（分页，支持按手机号/昵称搜索） */
export const getUsers = (params) => request.get('/admin/users', { params })

/** 切换用户状态 0-正常 1-封禁 */
export const toggleUserStatus = (id, status) =>
  request.put(`/admin/users/${id}/status`, null, { params: { status } })
