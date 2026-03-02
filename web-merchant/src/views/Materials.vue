<template>
  <div>
    <n-grid :cols="2" :x-gap="16" :y-gap="16">
      <!-- 左：原料消耗上报 -->
      <n-gi>
        <n-card title="原料消耗上报" :bordered="false" style="border-radius:12px">
          <n-form label-placement="left" label-width="80">
            <n-space vertical>
              <div v-for="(item, idx) in reportItems" :key="idx" style="display:flex;gap:8px;align-items:center">
                <n-input v-model:value="item.name" placeholder="原料名称" style="flex:2" />
                <n-input-number v-model:value="item.consumed" :min="0" placeholder="消耗量" style="flex:1" />
                <n-select v-model:value="item.unit" :options="unitOptions" style="width:80px" />
                <n-button circle type="error" size="small" @click="reportItems.splice(idx,1)" v-if="reportItems.length > 1">-</n-button>
              </div>
            </n-space>
          </n-form>
          <n-space style="margin-top:12px">
            <n-button dashed @click="addItem">+ 添加原料</n-button>
            <n-button type="primary" :loading="reportLoading" @click="submitReport">提交上报</n-button>
          </n-space>
        </n-card>
      </n-gi>

      <!-- 右：历史上报记录 -->
      <n-gi>
        <n-card title="上报记录" :bordered="false" style="border-radius:12px">
          <n-data-table
            :columns="histColumns"
            :data="histData"
            :loading="histLoading"
            :max-height="400"
            size="small"
          />
        </n-card>
      </n-gi>
    </n-grid>

    <!-- 采购申请 -->
    <n-card title="申请采购补货" :bordered="false" style="border-radius:12px;margin-top:16px">
      <n-grid :cols="4" :x-gap="12" style="margin-bottom:12px">
        <n-gi>
          <n-input v-model:value="procForm.materialName" placeholder="原料名称" />
        </n-gi>
        <n-gi>
          <n-input-number v-model:value="procForm.quantity" :min="1" style="width:100%" placeholder="数量" />
        </n-gi>
        <n-gi>
          <n-select v-model:value="procForm.unit" :options="unitOptions" />
        </n-gi>
        <n-gi>
          <n-button type="primary" @click="submitProcurement">提交申请</n-button>
        </n-gi>
      </n-grid>
    </n-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useMessage } from 'naive-ui'
import { getMaterials, reportMaterials, applyProcurement } from '@/api/materials'

const message = useMessage()
const reportLoading = ref(false)
const histLoading = ref(false)
const histData = ref([])

const reportItems = ref([{ name: '', consumed: 0, unit: 'kg' }])
const procForm = reactive({ materialName: '', quantity: 1, unit: 'kg' })

const unitOptions = [
  { label: 'kg', value: 'kg' },
  { label: 'L',  value: 'L' },
  { label: '箱', value: '箱' },
  { label: '个', value: '个' }
]

const histColumns = [
  { title: '原料', key: 'name' },
  { title: '消耗量', key: 'consumed', render: row => `${row.consumed}${row.unit}` },
  { title: '上报时间', key: 'createdAt' }
]

const addItem = () => reportItems.value.push({ name: '', consumed: 0, unit: 'kg' })

const submitReport = async () => {
  const valid = reportItems.value.every(i => i.name.trim())
  if (!valid) { message.warning('请填写所有原料名称'); return }
  reportLoading.value = true
  try {
    await reportMaterials({ items: reportItems.value })
    message.success('上报成功')
    reportItems.value = [{ name: '', consumed: 0, unit: 'kg' }]
    loadHistory()
  } catch (e) {
    message.error(e.message)
  } finally {
    reportLoading.value = false
  }
}

const submitProcurement = async () => {
  if (!procForm.materialName) { message.warning('请填写原料名称'); return }
  try {
    await applyProcurement({ ...procForm })
    message.success('采购申请已提交')
    procForm.materialName = ''
    procForm.quantity = 1
  } catch (e) {
    message.error(e.message)
  }
}

const loadHistory = async () => {
  histLoading.value = true
  try {
    const res = await getMaterials({ page: 1, size: 30 })
    histData.value = res?.records || []
  } catch {
    histData.value = []
  } finally {
    histLoading.value = false
  }
}

onMounted(loadHistory)
</script>
