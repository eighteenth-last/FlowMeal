import request from './request'

export const adminLogin = (data) => request.post('/auth/admin/login', data)
