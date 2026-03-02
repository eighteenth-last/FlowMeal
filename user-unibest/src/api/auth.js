import { post } from '@/utils/request'

/** 用户注册 */
export const userRegister = (data) => post('/auth/user/register', data)

/** 用户登录 */
export const userLogin = (data) => post('/auth/user/login', data)

/** 退出登录 */
export const logout = () => post('/auth/logout')
