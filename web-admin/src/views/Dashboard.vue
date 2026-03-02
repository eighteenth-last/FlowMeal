<template>
  <div>
    <!-- 统计卡片 -->
    <n-grid :cols="4" :x-gap="16" :y-gap="16">
      <n-gi v-for="card in statCards" :key="card.key">
        <n-card :bordered="false" style="border-radius:12px">
          <n-statistic :label="card.label" :value="card.value ?? '-'">
            <template #prefix>
              <n-icon :color="card.color" size="22"><component :is="card.icon" /></n-icon>
            </template>
            <template #suffix>
              <span style="font-size:12px;color:#999">{{ card.unit }}</span>
            </template>
          </n-statistic>
          <div style="margin-top:8px;font-size:12px;color:#52c41a" v-if="card.trend">
            ↑ {{ card.trend }} 较昨日
          </div>
        </n-card>
      </n-gi>
    </n-grid>

    <!-- 图表区 -->
    <n-grid :cols="2" :x-gap="16" style="margin-top:16px">
      <n-gi>
        <n-card title="近7天订单量" :bordered="false" style="border-radius:12px">
          <v-chart :option="orderChartOption" style="height:260px" autoresize />
        </n-card>
      </n-gi>
      <n-gi>
        <n-card title="近7天营业额(元)" :bordered="false" style="border-radius:12px">
          <v-chart :option="revenueChartOption" style="height:260px" autoresize />
        </n-card>
      </n-gi>
    </n-grid>

    <!-- 待审核商家 -->
    <n-card title="待审核商家" :bordered="false" style="margin-top:16px;border-radius:12px">
      <n-data-table
        :columns="auditColumns"
        :data="pendingMerchants"
        :loading="tableLoading"
        :pagination="false"
        size="small"
      />
    </n-card>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, h } from 'vue'
import { useMessage, NButton, NSpace, NTag } from 'naive-ui'
import { PeopleOutline, StorefrontOutline, ReaderOutline, CashOutline } from '@vicons/ionicons5'
import VChart from 'vue-echarts'
import { use } from 'echarts/core'
import { LineChart, BarChart } from 'echarts/charts'
import { GridComponent, TooltipComponent, LegendComponent } from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'
import { getDashboard, getOrderTrend, getRevenueTrend } from '@/api/dashboard'
import { getMerchants, auditMerchant } from '@/api/merchants'
import { useRouter } from 'vue-router'

use([LineChart, BarChart, GridComponent, TooltipComponent, LegendComponent, CanvasRenderer])

const message = useMessage()
const router = useRouter()

const stats = ref({})
const orderTrend = ref([])
const revenueTrend = ref([])
const pendingMerchants = ref([])
const tableLoading = ref(false)

const statCards = computed(() => [
  { key: 'users',     label: '注册用户数',  value: stats.value.userCount,    unit: '人', color: '#1677ff', icon: PeopleOutline,     trend: stats.value.userTrend },
  { key: 'merchants', label: '在营商家数',  value: stats.value.merchantCount, unit: '家', color: '#52c41a', icon: StorefrontOutline, trend: null },
  { key: 'orders',    label: '今日订单数',  value: stats.value.orderCount,   unit: '单', color: '#fa8c16', icon: ReaderOutline,     trend: stats.value.orderTrend },
  { key: 'revenue',   label: '今日营业额',  value: stats.value.revenue,      unit: '元', color: '#FFD100', icon: CashOutline,       trend: stats.value.revenueTrend }
])

const days = computed(() => orderTrend.value.map(i => i.date))
const lineBase = (data, color) => ({
  tooltip: { trigger: 'axis' },
  xAxis: { type: 'category', data: days.value, axisLine: { lineStyle: { color: '#eee' } } },
  yAxis: { type: 'value', splitLine: { lineStyle: { color: '#f0f0f0' } } },
  series: [{ data, type: 'line', smooth: true, areaStyle: { opacity: 0.15 }, itemStyle: { color } }]
})

const orderChartOption = computed(() => lineBase(orderTrend.value.map(i => i.count), '#1677ff'))
const revenueChartOption = computed(() => lineBase(revenueTrend.value.map(i => i.revenue), '#FFD100'))

const auditColumns = [
  { title: '商家名称', key: 'shopName' },
  { title: '联系人', key: 'contactName' },
  { title: '手机', key: 'phone' },
  { title: '提交时间', key: 'createdAt' },
  {
    title: '操作',
    key: 'actions',
    render: (row) =>
      h(NSpace, {}, {
        default: () => [
          h(NButton, { size: 'small', type: 'primary', onClick: () => handleAudit(row.id, 'APPROVED') }, { default: () => '通过' }),
          h(NButton, { size: 'small', type: 'error',   onClick: () => handleAudit(row.id, 'REJECTED') }, { default: () => '驳回' })
        ]
      })
  }
]

const handleAudit = async (id, status) => {
  try {
    await auditMerchant(id, { status })
    message.success(status === 'APPROVED' ? '已通过审核' : '已驳回')
    loadPending()
  } catch (e) {
    message.error(e.message)
  }
}

const loadPending = async () => {
  tableLoading.value = true
  try {
    const res = await getMerchants({ status: 'PENDING', page: 1, size: 10 })
    pendingMerchants.value = res?.records || []
  } catch {} finally {
    tableLoading.value = false
  }
}

onMounted(async () => {
  try {
    const [s, ot, rt] = await Promise.all([getDashboard(), getOrderTrend(), getRevenueTrend()])
    stats.value = s || {}
    orderTrend.value = ot || []
    revenueTrend.value = rt || []
  } catch {}
  loadPending()
})
</script>
