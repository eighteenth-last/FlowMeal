import request from './request'

/** 原料消耗上报列表 */
export const getMaterials = (params) =>
  request.get('/merchant/materials', { params })

/** 上报原料消耗 */
export const reportMaterials = (data) =>
  request.post('/merchant/materials/report', data)

/** 申请采购 */
export const applyProcurement = (data) =>
  request.post('/merchant/materials/procurement', data)
