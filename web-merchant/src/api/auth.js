import request from './request'

export const merchantLogin = (data) => request.post('/auth/merchant/login', data)

export const logout = () => request.post('/auth/logout')
