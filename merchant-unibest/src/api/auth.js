import request from '@/utils/request'

// 商家登录
export const merchantLogin = (data) => request({ url: '/auth/merchant/login', method: 'POST', data })

// 商家注册
export const merchantRegister = (data) => request({ url: '/auth/merchant/register', method: 'POST', data })

// 退出登录
export const logout = () => request({ url: '/auth/logout', method: 'POST' })
