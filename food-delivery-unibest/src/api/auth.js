import { post } from '@/utils/request'

/** 骑手登录 */
export const riderLogin = (data) => post('/auth/rider/login', data)

/** 骑手注册 */
export const riderRegister = (data) => post('/auth/rider/register', data)

/** 退出登录 */
export const logout = () => post('/auth/logout')
