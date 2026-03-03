<template>
  <div class="fade-in">
    <div class="page-header">
      <h1 class="page-title">平台数据大盘</h1>
      <p class="page-subtitle">实时监控平台运营核心指标</p>
    </div>

    <!-- 统计卡片 -->
    <div class="stat-grid">
      <div class="stat-card" v-for="card in statCards" :key="card.key">
        <div class="stat-icon" :style="{ backgroundColor: card.bgColor, color: card.color }">
          <n-icon size="28"><component :is="card.icon" /></n-icon>
        </div>
        <div class="stat-info">
          <p class="stat-label">{{ card.label }}</p>
          <h3 class="stat-value">
            {{ card.value ?? '-' }}
            <span class="stat-unit" v-if="card.value !== null">{{ card.unit }}</span>
          </h3>
          <p class="stat-trend" v-if="card.trend">
            <span class="trend-up">↑ {{ card.trend }}</span> 较昨日
          </p>
        </div>
      </div>
    </div>

    <!-- 图表区 -->
    <div class="chart-grid">
      <div class="chart-card">
        <div class="chart-header">
          <h3 class="chart-title">近7天订单趋势</h3>
        </div>
        <div class="chart-body">
          <v-chart :option="orderChartOption" style="height:280px" autoresize />
        </div>
      </div>
      <div class="chart-card">
        <div class="chart-header">
          <h3 class="chart-title">近7天营业额(元)</h3>
        </div>
        <div class="chart-body">
          <v-chart :option="revenueChartOption" style="height:280px" autoresize />
        </div>
      </div>
    </div>

    <!-- 待审核商家 -->
    <div class="table-card">
      <div class="table-header">
        <h3 class="table-title">
          <n-icon color="#FFD100" style="margin-right:8px;vertical-align:-2px"><StorefrontOutline /></n-icon>
          商家入驻待审核
        </h3>
        <router-link to="/merchants" class="more-link">查看全部</router-link>
      </div>
      <div class="table-body">
        <n-data-table
          :columns="auditColumns"
          :data="pendingMerchants"
          :loading="tableLoading"
          :pagination="false"
          size="medium"
          :bordered="false"
          striped
        />
      </div>
    </div>
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
  { key: 'users',     label: '注册用户总数',  value: stats.value.userCount,    unit: '人', color: '#3b82f6', bgColor: '#eff6ff', icon: PeopleOutline,     trend: stats.value.userTrend },
  { key: 'merchants', label: '入驻商家数',  value: stats.value.merchantCount, unit: '家', color: '#f97316', bgColor: '#fff7ed', icon: StorefrontOutline, trend: null },
  { key: 'orders',    label: '今日订单量',  value: stats.value.orderCount,   unit: '单', color: '#10b981', bgColor: '#f0fdf4', icon: ReaderOutline,     trend: stats.value.orderTrend },
  { key: 'revenue',   label: '今日营业总额',  value: stats.value.revenue,      unit: '元', color: '#ca8a04', bgColor: '#fefce8', icon: CashOutline,       trend: stats.value.revenueTrend }
])

const days = computed(() => orderTrend.value.map(i => i.date))
const lineBase = (data, color, areaColor) => ({
  tooltip: { trigger: 'axis' },
  grid: { left: '3%', right: '4%', bottom: '3%', top: '5%', containLabel: true },
  xAxis: { type: 'category', boundaryGap: false, data: days.value, axisLine: { lineStyle: { color: '#e5e7eb' } }, axisLabel: { color: '#6b7280' } },
  yAxis: { type: 'value', splitLine: { lineStyle: { color: '#f3f4f6' } }, axisLabel: { color: '#6b7280' } },
  series: [{ data, type: 'line', smooth: true, areaStyle: { color: areaColor, opacity: 0.8 }, itemStyle: { color } }]
})

const orderChartOption = computed(() => lineBase(orderTrend.value.map(i => i.count), '#3b82f6', '#eff6ff'))
const revenueChartOption = computed(() => lineBase(revenueTrend.value.map(i => i.revenue), '#ca8a04', '#fefce8'))

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

<style scoped>
.page-header {
  margin-bottom: 24px;
}
.page-title {
  font-size: 24px;
  font-weight: 700;
  color: #1f2937;
  margin: 0 0 4px 0;
}
.page-subtitle {
  font-size: 14px;
  color: #6b7280;
  margin: 0;
}

/* 统计卡片 */
.stat-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 24px;
  margin-bottom: 32px;
}
.stat-card {
  background: white;
  border-radius: 16px;
  padding: 24px;
  border: 1px solid #f3f4f6;
  box-shadow: 0 1px 2px 0 rgba(0, 0, 0, 0.05);
  display: flex;
  align-items: center;
  gap: 16px;
  transition: box-shadow 0.2s;
}
.stat-card:hover {
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1), 0 2px 4px -1px rgba(0, 0, 0, 0.06);
}
.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}
.stat-label {
  font-size: 14px;
  color: #6b7280;
  font-weight: 500;
  margin: 0 0 4px 0;
}
.stat-value {
  font-size: 24px;
  font-weight: 700;
  color: #111827;
  margin: 0;
}
.stat-unit {
  font-size: 14px;
  font-weight: normal;
  color: #6b7280;
  margin-left: 4px;
}
.stat-trend {
  margin: 4px 0 0;
  font-size: 12px;
  color: #9ca3af;
}
.trend-up {
  color: #10b981;
  font-weight: 600;
}

/* 图表区 */
.chart-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 24px;
  margin-bottom: 24px;
}
.chart-card {
  background: white;
  border-radius: 16px;
  border: 1px solid #f3f4f6;
  box-shadow: 0 1px 2px 0 rgba(0, 0, 0, 0.05);
  overflow: hidden;
}
.chart-header {
  padding: 16px 20px;
  border-bottom: 1px solid #f3f4f6;
}
.chart-title {
  font-size: 16px;
  font-weight: 600;
  color: #1f2937;
  margin: 0;
}
.chart-body {
  padding: 16px;
}

/* 待审核商家表格 */
.table-card {
  background: white;
  border-radius: 16px;
  border: 1px solid #f3f4f6;
  box-shadow: 0 1px 2px 0 rgba(0, 0, 0, 0.05);
}
.table-header {
  padding: 20px;
  border-bottom: 1px solid #f3f4f6;
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.table-title {
  font-size: 18px;
  font-weight: 700;
  color: #1f2937;
  margin: 0;
  display: flex;
  align-items: center;
}
.more-link {
  font-size: 14px;
  color: #3b82f6;
  text-decoration: none;
}
.more-link:hover {
  text-decoration: underline;
}
.table-body {
  padding: 0 20px 20px;
}

.fade-in {
  animation: fadeIn 0.3s ease-in-out;
}
@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}
</style>
