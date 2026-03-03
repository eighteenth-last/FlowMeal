<template>
  <div>
    <n-grid :cols="2" :x-gap="16" :y-gap="16">
      <!-- 左：原料消耗上报 + 申请补货 合并 -->
      <n-gi>
        <n-card :bordered="false" style="border-radius:12px">
          <template #header>
            <span>原料消耗上报</span>
            <n-tag size="small" type="info" style="margin-left:8px;font-weight:400">填写补货量可同步申请采购</n-tag>
          </template>

          <!-- 表头 -->
          <div style="display:grid;grid-template-columns:2fr 1.2fr 72px 1.2fr 32px;gap:8px;margin-bottom:6px;font-size:12px;color:#999;padding:0 2px">
            <span>原料名称 *</span>
            <span>消耗量 *</span>
            <span>单位</span>
            <span>申请补货量 <n-tooltip><template #trigger><span style="cursor:help">ⓘ</span></template>填写后自动提交采购申请</n-tooltip></span>
            <span></span>
          </div>

          <div v-for="(item, idx) in reportItems" :key="idx"
            style="display:grid;grid-template-columns:2fr 1.2fr 72px 1.2fr 32px;gap:8px;margin-bottom:8px;align-items:center">
            <n-input v-model:value="item.name" placeholder="如：鸡肉" size="small" />
            <n-input-number v-model:value="item.consumed" :min="0" size="small" :show-button="false" style="width:100%" />
            <n-select v-model:value="item.unit" :options="unitOptions" size="small" />
            <n-input-number
              v-model:value="item.procureQty"
              :min="0" size="small" :show-button="false" placeholder="不填则不申请"
              style="width:100%"
            />
            <n-button
              v-if="reportItems.length > 1"
              circle type="error" size="tiny"
              @click="reportItems.splice(idx,1)"
            >-</n-button>
            <span v-else></span>
          </div>

          <n-space style="margin-top:12px">
            <n-button dashed size="small" @click="addItem">+ 添加原料</n-button>
            <n-button type="primary" :loading="reportLoading" @click="submitAll">提交上报</n-button>
          </n-space>

          <!-- 提交结果提示 -->
          <n-alert v-if="lastResult" :type="lastResult.type" style="margin-top:12px" closable @close="lastResult=null">
            {{ lastResult.msg }}
          </n-alert>
        </n-card>
      </n-gi>

      <!-- 右：历史上报记录 -->
      <n-gi>
        <n-card title="上报记录" :bordered="false" style="border-radius:12px">
          <n-data-table
            :columns="histColumns"
            :data="histData"
            :loading="histLoading"
            :max-height="460"
            size="small"
          />
        </n-card>
      </n-gi>
    </n-grid>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useMessage } from 'naive-ui'
import { getMaterials, reportMaterials, applyProcurement } from '@/api/materials'

const message = useMessage()
const reportLoading = ref(false)
const histLoading = ref(false)
const histData = ref([])
const lastResult = ref(null)

const reportItems = ref([{ name: '', consumed: 0, unit: 'kg', procureQty: null }])

const unitOptions = [
  { label: 'kg', value: 'kg' },
  { label: 'L',  value: 'L'  },
  { label: '箱', value: '箱' },
  { label: '个', value: '个' }
]

const histColumns = [
  { title: '原料', key: 'materialName', width: 100 },
  { title: '消耗量', key: 'consumed', render: row => `${row.consumed} ${row.unit}` },
  {
    title: '上报时间', key: 'reportTime',
    render: row => row.reportTime ? String(row.reportTime).replace('T', ' ').substring(0, 16) : '-'
  }
]

const addItem = () => reportItems.value.push({ name: '', consumed: 0, unit: 'kg', procureQty: null })

/** 一次提交：上报消耗 + 有补货量则同步申请采购 */
const submitAll = async () => {
  const valid = reportItems.value.every(i => i.name.trim())
  if (!valid) { message.warning('请填写所有原料名称'); return }
  reportLoading.value = true
  lastResult.value = null
  try {
    // 1. 上报消耗
    await reportMaterials({ items: reportItems.value })

    // 2. 有填写补货量的行，逐条提交采购申请
    const procureRows = reportItems.value.filter(i => i.procureQty && i.procureQty > 0)
    let procureCount = 0
    for (const row of procureRows) {
      await applyProcurement({ materialName: row.name, quantity: row.procureQty, unit: row.unit })
      procureCount++
    }

    const tip = procureCount > 0
      ? `上报成功，已同步提交 ${procureCount} 条采购申请`
      : '消耗上报成功'
    lastResult.value = { type: 'success', msg: tip }
    message.success(tip)
    reportItems.value = [{ name: '', consumed: 0, unit: 'kg', procureQty: null }]
    loadHistory()
  } catch (e) {
    lastResult.value = { type: 'error', msg: e.message }
    message.error(e.message)
  } finally {
    reportLoading.value = false
  }
}

const loadHistory = async () => {
  histLoading.value = true
  try {
    const res = await getMaterials({ page: 1, size: 50 })
    histData.value = Array.isArray(res) ? res : (res?.records || [])
  } catch {
    histData.value = []
  } finally {
    histLoading.value = false
  }
}

onMounted(loadHistory)
</script>
