<template>
  <div>
    <n-space style="margin-bottom:16px">
      <n-button type="primary" @click="openCreate">+ 创建优惠券</n-button>
    </n-space>

    <n-grid :cols="3" :x-gap="16" :y-gap="16">
      <n-gi v-for="coupon in coupons" :key="coupon.id">
        <n-card :bordered="false" style="border-radius:12px;position:relative">
          <n-tag
            :type="coupon.status === 'ACTIVE' ? 'success' : 'default'"
            size="small"
            style="position:absolute;top:12px;right:12px"
          >
            {{ coupon.status === 'ACTIVE' ? '进行中' : '已结束' }}
          </n-tag>

          <div style="font-size:28px;font-weight:800;color:#FFD100">
            ¥{{ coupon.faceValue }}
          </div>
          <div style="font-size:13px;color:#666;margin:4px 0">
            满 {{ coupon.minAmount }} 元可用
          </div>
          <div style="font-weight:600;font-size:15px;margin:8px 0">{{ coupon.name }}</div>
          <div style="font-size:12px;color:#999">
            有效期：{{ coupon.startDate }} ~ {{ coupon.endDate }}
          </div>

          <n-divider style="margin:12px 0" />

          <div style="font-size:12px;color:#999;margin-bottom:6px">
            已领取 {{ coupon.claimedCount }} / {{ coupon.totalCount }}
          </div>
          <n-progress
            type="line"
            :percentage="Math.round(coupon.claimedCount / coupon.totalCount * 100)"
            :color="'#FFD100'"
            :height="8"
          />

          <n-space style="margin-top:12px" v-if="coupon.status === 'ACTIVE'">
            <n-popconfirm @positive-click="handleDisable(coupon.id)">
              <template #trigger>
                <n-button size="small" type="warning">停用</n-button>
              </template>
              确认停用该优惠券？
            </n-popconfirm>
          </n-space>
        </n-card>
      </n-gi>
    </n-grid>

    <!-- 创建弹窗 -->
    <n-modal v-model:show="showCreate" title="创建优惠券" preset="card" style="width:480px" :mask-closable="false">
      <n-form :model="form" label-placement="left" label-width="90" :rules="rules" ref="formRef">
        <n-form-item label="券名称" path="name">
          <n-input v-model:value="form.name" placeholder="如：新用户专享券" />
        </n-form-item>
        <n-form-item label="面值(元)" path="faceValue">
          <n-input-number v-model:value="form.faceValue" :min="1" :max="999" style="width:100%" />
        </n-form-item>
        <n-form-item label="使用门槛" path="minAmount">
          <n-input-number v-model:value="form.minAmount" :min="0" style="width:100%" placeholder="0 表示无门槛" />
        </n-form-item>
        <n-form-item label="发放数量" path="totalCount">
          <n-input-number v-model:value="form.totalCount" :min="1" style="width:100%" />
        </n-form-item>
        <n-form-item label="有效期" path="dateRange">
          <n-date-picker v-model:value="form.dateRange" type="daterange" style="width:100%" />
        </n-form-item>
      </n-form>
      <template #footer>
        <n-space justify="end">
          <n-button @click="showCreate = false">取消</n-button>
          <n-button type="primary" :loading="submitLoading" @click="submitCreate">创 建</n-button>
        </n-space>
      </template>
    </n-modal>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useMessage } from 'naive-ui'
import { getCoupons, createCoupon, disableCoupon } from '@/api/coupons'

const message = useMessage()
const coupons = ref([])
const showCreate = ref(false)
const submitLoading = ref(false)
const formRef = ref(null)

const form = reactive({
  name: '', faceValue: 5, minAmount: 20, totalCount: 100, dateRange: null
})

const rules = {
  name: [{ required: true, message: '请填写券名称', trigger: 'blur' }],
  faceValue: [{ required: true, type: 'number', message: '请填写面值', trigger: 'blur' }],
  totalCount: [{ required: true, type: 'number', message: '请填写数量', trigger: 'blur' }]
}

const loadData = async () => {
  try {
    const res = await getCoupons()
    coupons.value = res?.records || res || []
  } catch {
    // 备用模拟数据
    coupons.value = [
      { id: 1, name: '新用户专享券', faceValue: 10, minAmount: 30, totalCount: 500, claimedCount: 312, startDate: '2024-01-01', endDate: '2024-03-31', status: 'ACTIVE' },
      { id: 2, name: '满减优惠券',   faceValue: 5,  minAmount: 20, totalCount: 1000, claimedCount: 1000, startDate: '2023-12-01', endDate: '2023-12-31', status: 'ENDED' }
    ]
  }
}

const openCreate = () => {
  Object.assign(form, { name: '', faceValue: 5, minAmount: 20, totalCount: 100, dateRange: null })
  showCreate.value = true
}

const submitCreate = () => {
  formRef.value?.validate(async (errors) => {
    if (errors) return
    submitLoading.value = true
    try {
      const [start, end] = form.dateRange || []
      await createCoupon({
        name: form.name,
        faceValue: form.faceValue,
        minAmount: form.minAmount,
        totalCount: form.totalCount,
        startDate: start ? new Date(start).toISOString().split('T')[0] : null,
        endDate: end ? new Date(end).toISOString().split('T')[0] : null
      })
      message.success('创建成功')
      showCreate.value = false
      loadData()
    } catch (e) {
      message.error(e.message || '创建失败')
    } finally {
      submitLoading.value = false
    }
  })
}

const handleDisable = async (id) => {
  try {
    await disableCoupon(id)
    message.success('已停用')
    loadData()
  } catch (e) {
    message.error(e.message)
  }
}

onMounted(loadData)
</script>
