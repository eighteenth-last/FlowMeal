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
import { ref, reactive, h } from 'vue'
import { useMessage, NTag } from 'naive-ui'

const message = useMessage()
const loading = ref(false)
const showCreate = ref(false)

// 模拟数据
const procList = ref([
  { id: 1, supplier: '绿源食材', material: '大米', quantity: 500, unit: 'kg', status: 'PENDING', createdAt: '2024-01-10' },
  { id: 2, supplier: '鲜味供应商', material: '食用油', quantity: 200, unit: 'L',  status: 'DELIVERED', createdAt: '2024-01-09' }
])

const consumeSummary = ref([
  { name: '大米', used: 420, total: 500, unit: 'kg', percent: 84 },
  { name: '食用油', used: 120, total: 200, unit: 'L',  percent: 60 },
  { name: '蔬菜', used: 80,  total: 150, unit: 'kg', percent: 53 }
])

const supplierOptions = [
  { label: '绿源食材', value: '绿源食材' },
  { label: '鲜味供应商', value: '鲜味供应商' },
  { label: '金鸿粮油', value: '金鸿粮油' }
]

const unitOptions = [
  { label: 'kg', value: 'kg' },
  { label: 'L', value: 'L' },
  { label: '箱', value: '箱' },
  { label: '袋', value: '袋' }
]

const form = reactive({ supplier: null, materialName: '', quantity: 1, unit: 'kg', remark: '' })

const statusMap = { PENDING: { type: 'warning', text: '待发货' }, DELIVERED: { type: 'success', text: '已到货' } }

const procColumns = [
  { title: '供应商', key: 'supplier' },
  { title: '原料', key: 'material' },
  { title: '数量', key: 'quantity', render: row => `${row.quantity}${row.unit}` },
  {
    title: '状态', key: 'status',
    render: row => {
      const s = statusMap[row.status] || { type: 'default', text: row.status }
      return h(NTag, { type: s.type, size: 'small' }, { default: () => s.text })
    }
  },
  { title: '日期', key: 'createdAt' }
]

const submitCreate = () => {
  if (!form.supplier || !form.materialName) {
    message.warning('请填写完整信息')
    return
  }
  procList.value.unshift({
    id: Date.now(),
    supplier: form.supplier,
    material: form.materialName,
    quantity: form.quantity,
    unit: form.unit,
    status: 'PENDING',
    createdAt: new Date().toISOString().split('T')[0]
  })
  message.success('采购单创建成功')
  showCreate.value = false
}
</script>
