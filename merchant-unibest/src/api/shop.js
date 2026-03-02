import request from '@/utils/request'

// 获取自己的店铺信息
export const getMyShop = () => request({ url: '/merchant/shop' })

// 更新店铺信息
export const updateShop = (data) => request({ url: '/merchant/shop', method: 'PUT', data })

// 切换营业状态 (1-营业 0-打烊)
export const toggleShopStatus = (status) => request({ url: '/merchant/shop/status', method: 'PUT', data: { status } })
