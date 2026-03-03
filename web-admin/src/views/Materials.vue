<template>
  <div>
    <!-- 顶部统计卡片 -->
    <div class="stat-row">
      <div class="stat-card">
        <div class="stat-val">{{ totalRecords }}</div>
        <div class="stat-label">上报记录总数</div>
      </div>
      <div class="stat-card">
        <div class="stat-val">{{ summaryData.length }}</div>
        <div class="stat-label">原料品类数</div>
      </div>
      <div class="stat-card">
        <div class="stat-val">{{ totalMerchants }}</div>
        <div class="stat-label">上报商家数</div>
      </div>
      <div class="stat-card">
        <div class="stat-val">{{ todayCount }}</div>
        <div class="stat-label">今日新增条数</div>
      </div>
    </div>

    <!-- 标签页 -->
    <n-tabs v-model:value="activeTab" type="line" animated>
      <!-- 汇总分析 -->
      <n-tab-pane name="summary" tab="汇总分析">
        <n-card :bordered="false" style="border-radius:0 0 12px 12px">
          <template #header>
            <div style="display:flex;justify-content:space-between;align-items:center">
              <span>按原料名称聚合（累计消耗量）</span>
              <n-input
                v-model:value="summaryFilter"
                placeholder="搜索原料"
                clearable
                size="small"
                style="width:200px"
              />
            </div>
          </template>
          <n-data-table
            :columns="summaryColumns"
            :data="filteredSummary"
            :loading="loadingSummary"
            :pagination="{ pageSize: 15 }"
            size="small"
            striped
          />
        </n-card>
      </n-tab-pane>

      <!-- 明细记录 -->
      <n-tab-pane name="detail" tab="明细记录">
        <n-card :bordered="false" style="border-radius:0 0 12px 12px">
          <template #header>
            <n-space>
              <n-input
                v-model:value="filterKw"
                placeholder="原料/商家ID搜索"
                clearable
                style="width:220px"
              />
              <n-button type="primary" size="small" @click="loadDetail">查询</n-button>
              <n-button size="small" @click="filterKw = ''; loadDetail()">重置</n-button>
            </n-space>
          </template>
          <n-data-table
            :columns="detailColumns"
            :data="filteredDetail"
            :loading="loadingDetail"
            :pagination="{ pageSize: 15 }"
            size="small"
            striped
          />
        </n-card>
      </n-tab-pane>
    </n-tabs>

    <!-- 创建采购单弹窗 -->
    <n-modal v-model:show="showProcModal" title="创建采购单" preset="card" style="width:500px">
      <n-form :model="procForm" label-placement="left" label-width="80">
        <n-form-item label="原料名称">
          <n-input v-model:value="procForm.materialName" placeholder="原料名称" />
        </n-form-item>
        <n-form-item label="采购数量">
          <n-input-number v-model:value="procForm.quantity" :min="1" style="width:100%" />
        </n-form-item>
        <n-form-item label="单位">
          <n-select v-model:value="procForm.unit" :options="unitOptions" />
        </n-form-item>
        <n-form-item label="供应商">
          <n-select
            v-model:value="procForm.supplier"
            :options="supplierOptions"
            placeholder="选择供应商（可选）"
            clearable
          />
        </n-form-item>
        <n-form-item label="备注">
          <n-input v-model:value="procForm.remark" type="textarea" :rows="2" placeholder="可填写采购原因或说明" />
        </n-form-item>
      </n-form>
      <template #footer>
        <n-space justify="end">
          <n-button @click="showProcModal = false">取消</n-button>
          <n-button type="primary" :loading="procSubmitting" @click="submitProcurement">提 交</n-button>
        </n-space>
      </template>
    </n-modal>
  </div>
</template>

<script setup>
import { ref, computed, reactive, h, onMounted } from 'vue'
import { useMessage, NButton, NTag } from 'naive-ui'
import request from '@/api/request'

const message = useMessage()
const activeTab = ref('summary')

// ─── 顶部指标 ───────────────────────────────────────────
const totalRecords = ref(0)
const totalMerchants = ref(0)
const todayCount = ref(0)

// ─── 汇总分析 ───────────────────────────────────────────
const loadingSummary = ref(false)
const summaryData = ref([])
const summaryFilter = ref('')

const filteredSummary = computed(() => {
  if (!summaryFilter.value) return summaryData.value
  const kw = summaryFilter.value.toLowerCase()
  return summaryData.value.filter(r => (r.materialName || '').toLowerCase().includes(kw))
})

const summaryColumns = [
  { title: '原料名称', key: 'materialName' },
  {
    title: '累计消耗量', key: 'totalConsumed',
    render: row => h('span', { style: 'font-weight:600;color:#111827' }, `${row.totalConsumed} ${row.unit}`)
  },
  {
    title: '上报商家', key: 'merchantNames',
    render: row => {
      const names = row.merchantNames || []
      if (!names.length) return '-'
      return h('div', { style: 'display:flex;flex-wrap:wrap;gap:4px' },
        names.map(name => h(NTag, { size: 'small', type: 'info', round: true }, { default: () => name }))
      )
    }
  },
  { title: '上报商家数', key: 'merchantCount', width: 100 },
  { title: '上报条数', key: 'recordCount', width: 90 },
  {
    title: '操作', key: 'action', width: 130,
    render: row => h(NButton, {
      size: 'small',
      type: 'primary',
      onClick: () => openProcModal(row)
    }, { default: () => '集中采购' })
  }
]

// ─── 明细记录 ───────────────────────────────────────────
const loadingDetail = ref(false)
const detailData = ref([])
const filterKw = ref('')

const filteredDetail = computed(() => {
  if (!filterKw.value) return detailData.value
  const kw = filterKw.value.toLowerCase()
  return detailData.value.filter(r =>
    String(r.merchantId).includes(kw) ||
    (r.materialName || '').toLowerCase().includes(kw)
  )
})

const detailColumns = [
  { title: '商家ID', key: 'merchantId', width: 90 },
  { title: '店铺名称', key: 'shopName' },
  { title: '原料名称', key: 'materialName' },
  { title: '消耗量', key: 'consumed', render: row => `${row.consumed} ${row.unit}`, width: 100 },
  {
    title: '上报时间', key: 'reportTime', width: 160,
    render: row => row.reportTime ? String(row.reportTime).replace('T', ' ').substring(0, 16) : '-'
  },
  { title: '创建时间', key: 'createdAt', width: 160 }
]

// ─── 创建采购单 ─────────────────────────────────────────
const showProcModal = ref(false)
const procSubmitting = ref(false)
const supplierOptions = ref([])
const unitOptions = [
  { label: 'kg', value: 'kg' },
  { label: 'L', value: 'L' },
  { label: '箱', value: '箱' },
  { label: '袋', value: '袋' }
]
const procForm = reactive({ materialName: '', quantity: 1, unit: 'kg', supplier: null, remark: '' })

const openProcModal = (row) => {
  procForm.materialName = row.materialName || ''
  procForm.quantity = Number(row.totalConsumed) || 1
  procForm.unit = row.unit || 'kg'
  procForm.supplier = null
  procForm.remark = `根据近期消耗上报，累计消耗 ${row.totalConsumed}${row.unit}，涉及 ${row.merchantCount} 家商家`
  showProcModal.value = true
}

const submitProcurement = async () => {
  if (!procForm.materialName || !procForm.quantity) {
    message.warning('请填写原料名称和数量')
    return
  }
  procSubmitting.value = true
  try {
    await request.post('/admin/procurement', {
      supplier: procForm.supplier || '待确认',
      materialName: procForm.materialName,
      quantity: procForm.quantity,
      unit: procForm.unit,
      remark: procForm.remark
    })
    message.success('采购单创建成功，可在「采购管理」中查看')
    showProcModal.value = false
  } catch (e) {
    message.error(e.message || '创建失败')
  } finally {
    procSubmitting.value = false
  }
}

// ─── 数据加载 ───────────────────────────────────────────
const loadSummary = async () => {
  loadingSummary.value = true
  try {
    const res = await request.get('/admin/materials/summary')
    summaryData.value = Array.isArray(res) ? res : (res?.records || [])
  } catch (e) {
    message.error(e.message || '加载汇总失败')
  } finally {
    loadingSummary.value = false
  }
}

const loadDetail = async () => {
  loadingDetail.value = true
  try {
    const res = await request.get('/admin/materials/detail')
    const list = Array.isArray(res) ? res : (res?.records || [])
    detailData.value = list
    totalRecords.value = list.length
    totalMerchants.value = new Set(list.map(r => r.merchantId)).size
    const today = new Date().toISOString().slice(0, 10)
    todayCount.value = list.filter(r => r.createdAt && String(r.createdAt).startsWith(today)).length
  } catch (e) {
    message.error(e.message || '加载明细失败')
  } finally {
    loadingDetail.value = false
  }
}

const loadSuppliers = async () => {
  try {
    const list = await request.get('/admin/suppliers') || []
    supplierOptions.value = list
      .filter(s => s.status === 1)
      .map(s => ({ label: s.name, value: s.name }))
  } catch { /* ignore */ }
}

onMounted(() => {
  loadSummary()
  loadDetail()
  loadSuppliers()
})
</script>

<style scoped>
.stat-row {
  display: flex;
  gap: 16px;
  margin-bottom: 16px;
}
.stat-card {
  flex: 1;
  background: #fff;
  border-radius: 10px;
  padding: 16px 20px;
  text-align: center;
  box-shadow: 0 1px 6px rgba(0,0,0,0.06);
}
.stat-val {
  font-size: 28px;
  font-weight: 700;
  color: #111827;
}
.stat-label {
  font-size: 12px;
  color: #999;
  margin-top: 4px;
}
</style>
