<template>
  <div>
    <n-space style="margin-bottom:16px">
      <n-button type="primary" @click="openCreate">+ 创建优惠券</n-button>
    </n-space>

    <n-grid :cols="4" :x-gap="20" :y-gap="20">
      <n-gi v-for="coupon in coupons" :key="coupon.id">
        <div class="coupon-card" :class="{'is-ended': coupon.status !== 'ACTIVE'}">
          <!-- 装饰性圆切角口 -->
          <div class="notch left-notch"></div>
          <div class="notch right-notch"></div>

          <!-- 顶部金额与标签 -->
          <div class="coupon-top">
            <div class="coupon-amount">
              <span class="currency">¥</span>
              <span class="value">{{ coupon.value }}</span>
            </div>
            <n-tag :type="coupon.status === 'ACTIVE' ? 'error' : 'default'" size="small" round>
              {{ coupon.status === 'ACTIVE' ? '进行中' : '已结束' }}
            </n-tag>
          </div>

          <!-- 适用门槛与名称 -->
          <div class="coupon-info">
            <div class="coupon-name">{{ coupon.name }}</div>
            <div class="coupon-condition">满 {{ coupon.minAmount }} 元可用</div>
            <div class="coupon-date">
              有效期：{{ coupon.startTime ? coupon.startTime.substring(0, 10) : '-' }} ~ {{ coupon.endTime ? coupon.endTime.substring(0, 10) : '-' }}
            </div>
          </div>

          <div class="divider"></div>

          <!-- 底部进度条与操作 -->
          <div class="coupon-bottom">
            <div class="progress-wrap">
              <div class="progress-text">已领取 {{ coupon.claimedCount }} / {{ coupon.total }}</div>
              <n-progress
                type="line"
                :percentage="coupon.total ? Math.round(coupon.claimedCount / coupon.total * 100) : 0"
                :color="coupon.status === 'ACTIVE' ? '#ff4d4f' : '#ccc'"
                :indicator-placement="'inside'"
                :height="10"
              />
            </div>
            <div v-if="coupon.status === 'ACTIVE'" class="action-btn">
              <n-popconfirm @positive-click="handleDisable(coupon.id)">
                <template #trigger>
                  <n-button size="small" type="warning" ghost round>停用</n-button>
                </template>
                确认停用该优惠券？
              </n-popconfirm>
            </div>
          </div>
        </div>
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
      const fmt = (ts) => {
        if (!ts) return null
        const d = new Date(ts)
        const pad = (n) => String(n).padStart(2, '0')
        return `${d.getFullYear()}-${pad(d.getMonth()+1)}-${pad(d.getDate())} 00:00:00`
      }
      await createCoupon({
        name: form.name,
        value: form.faceValue,
        minAmount: form.minAmount,
        total: form.totalCount,
        startTime: fmt(start),
        endTime: fmt(end)
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

<style scoped>
.coupon-card {
  position: relative;
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 4px 16px rgba(0,0,0,0.04);
  transition: all 0.3s ease;
  overflow: hidden;
  border: 1px solid #f0f0f0;
}

.coupon-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0,0,0,0.08);
}

.coupon-card.is-ended {
  opacity: 0.7;
  background: #fafafa;
}

/* 两侧半圆缺口（常见优惠券样式） */
.notch {
  position: absolute;
  top: 140px;
  width: 20px;
  height: 20px;
  background-color: #f5f6fa; /* 需要和页面背景色一致 */
  border-radius: 50%;
  z-index: 10;
  border: 1px solid #f0f0f0;
}

.left-notch {
  left: -11px;
  border-right-color: transparent;
  border-bottom-color: transparent;
  transform: rotate(-45deg);
}

.right-notch {
  right: -11px;
  border-left-color: transparent;
  border-bottom-color: transparent;
  transform: rotate(45deg);
}

.coupon-top {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 12px;
}

.coupon-amount {
  color: #ff4d4f; /* 红色更像优惠券 */
  line-height: 1;
}
.is-ended .coupon-amount { color: #999; }

.currency {
  font-size: 18px;
  font-weight: 600;
  margin-right: 2px;
}

.value {
  font-size: 36px;
  font-weight: 800;
  letter-spacing: -1px;
}

.coupon-info {
  margin-bottom: 16px;
}

.coupon-name {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin-bottom: 6px;
}

.coupon-condition {
  font-size: 13px;
  color: #666;
  background: #fff5f5;
  display: inline-block;
  padding: 2px 8px;
  border-radius: 4px;
  margin-bottom: 8px;
}

.is-ended .coupon-condition { background: #f0f0f0; color: #999; }

.coupon-date {
  font-size: 12px;
  color: #999;
}

.divider {
  border-top: 1px dashed #e8e8e8;
  margin: 0 -20px 16px;
}

.coupon-bottom {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
}

.progress-wrap {
  flex: 1;
  margin-right: 16px;
}

.progress-text {
  font-size: 12px;
  color: #999;
  margin-bottom: 6px;
}

.action-btn {
  flex-shrink: 0;
}
</style>

