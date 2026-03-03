<template>
  <div>
    <n-grid :cols="2" :x-gap="16" :y-gap="16">
      <!-- 左侧：供应商原料采购申请列表 -->
      <n-gi>
        <n-card title="采购申请列表" :bordered="false" style="border-radius:12px">
          <template #header-extra>
            <n-button type="primary" size="small" @click="showCreate = true">+ 新建采购单</n-button>
          </template>
          <n-data-table
            :columns="procColumns"
            :data="procList"
            :loading="loading"
            size="small"
          />
        </n-card>
      </n-gi>

      <!-- 右侧：汇总统计 -->
      <n-gi>
        <n-card title="原料消耗汇总（近30天）" :bordered="false" style="border-radius:12px">
          <n-list>
            <n-list-item v-for="item in consumeSummary" :key="item.name">
              <n-thing :title="item.name">
                <template #description>
                  <n-progress
                    type="line"
                    :percentage="item.percent"
                    :color="item.percent > 80 ? '#ff4d4f' : '#FFD100'"
                    :height="10"
                  />
                  <span style="font-size:12px;color:#999">消耗 {{ item.used }}{{ item.unit }} / 采购 {{ item.total }}{{ item.unit }}</span>
                </template>
              </n-thing>
            </n-list-item>
          </n-list>
        </n-card>
      </n-gi>
    </n-grid>

    <!-- 新建采购单弹窗 -->
    <n-modal v-model:show="showCreate" title="新建采购单" preset="card" style="width:520px">
      <n-form :model="form" label-placement="left" label-width="80">
        <n-form-item label="供应商">
          <n-select v-model:value="form.supplier" :options="supplierOptions" placeholder="选择供应商" />
        </n-form-item>
        <n-form-item label="原料名称">
          <n-input v-model:value="form.materialName" placeholder="如：大米、食用油" />
        </n-form-item>
        <n-form-item label="采购数量">
          <n-input-number v-model:value="form.quantity" :min="1" style="width:100%" />
        </n-form-item>
        <n-form-item label="单位">
          <n-select v-model:value="form.unit" :options="unitOptions" />
        </n-form-item>
        <n-form-item label="备注">
          <n-input v-model:value="form.remark" type="textarea" :rows="2" />
        </n-form-item>
      </n-form>
      <template #footer>
        <n-space justify="end">
          <n-button @click="showCreate = false">取消</n-button>
          <n-button type="primary" @click="submitCreate">提 交</n-button>
        </n-space>
      </template>
    </n-modal>
  </div>
</template>

<script setup>
import { ref, reactive, h, onMounted } from 'vue'
import { useMessage, NTag, NButton, NSpace } from 'naive-ui'
import request from '@/api/request'

const message = useMessage()
const loading = ref(false)
const showCreate = ref(false)
const submitLoading = ref(false)

const procList = ref([])

const consumeSummary = ref([])

const loadSummary = async () => {
  try {
    const res = await request.get('/admin/materials/summary') || []
    const list = Array.isArray(res) ? res : (res?.records || [])
    const maxConsumed = list.length ? Math.max(...list.map(i => i.totalConsumed)) : 1
    consumeSummary.value = list.slice(0, 10).map(item => ({
      name: item.materialName,
      used: item.totalConsumed,
      total: item.totalConsumed,  // 以累计消耗作为参考基准
      unit: item.unit,
      percent: Math.round((item.totalConsumed / maxConsumed) * 100)
    }))
  } catch { /* ignore */ }
}

const supplierOptions = ref([])

const loadSuppliers = async () => {
  try {
    const list = await request.get('/admin/suppliers') || []
    supplierOptions.value = list
      .filter(s => s.status === 1)
      .map(s => ({ label: s.name, value: s.name }))
  } catch { /* ignore */ }
}

const unitOptions = [
  { label: 'kg', value: 'kg' },
  { label: 'L', value: 'L' },
  { label: '箱', value: '箱' },
  { label: '袋', value: '袋' }
]

const form = reactive({ supplier: null, materialName: '', quantity: 1, unit: 'kg', remark: '' })

const statusMap = {
  PENDING:   { type: 'warning', text: '待发货' },
  SHIPPED:   { type: 'info',    text: '已发货' },
  DELIVERED: { type: 'success', text: '已到货' }
}

const procColumns = [
  { title: '供应商', key: 'supplier' },
  { title: '原料', key: 'materialName' },
  { title: '数量', key: 'quantity', render: row => `${row.quantity}${row.unit}` },
  {
    title: '状态', key: 'status',
    render: row => {
      const s = statusMap[row.status] || { type: 'default', text: row.status }
      return h(NTag, { type: s.type, size: 'small' }, { default: () => s.text })
    }
  },
  {
    title: '操作', key: 'actions',
    render: row => row.status === 'PENDING'
      ? h(NButton, { size: 'small', type: 'primary', onClick: () => markDelivered(row.id) }, { default: () => '确认到货' })
      : null
  },
  { title: '日期', key: 'createdAt' }
]

const loadData = async () => {
  loading.value = true
  try {
    const res = await request.get('/admin/procurement')
    procList.value = res || []
  } catch (e) {
    message.error(e.message || '加载失败')
  } finally {
    loading.value = false
  }
}

const submitCreate = async () => {
  if (!form.supplier || !form.materialName) {
    message.warning('请填写完整信息')
    return
  }
  submitLoading.value = true
  try {
    await request.post('/admin/procurement', {
      supplier: form.supplier,
      materialName: form.materialName,
      quantity: form.quantity,
      unit: form.unit,
      remark: form.remark
    })
    message.success('采购单创建成功')
    showCreate.value = false
    Object.assign(form, { supplier: null, materialName: '', quantity: 1, unit: 'kg', remark: '' })
    loadData()
  } catch (e) {
    message.error(e.message || '创建失败')
  } finally {
    submitLoading.value = false
  }
}

const markDelivered = async (id) => {
  try {
    await request.put(`/admin/procurement/${id}/status`, null, { params: { status: 'DELIVERED' } })
    message.success('已标记为到货')
    loadData()
  } catch (e) {
    message.error(e.message)
  }
}

onMounted(() => {
  loadData()
  loadSuppliers()
  loadSummary()
})
</script>
