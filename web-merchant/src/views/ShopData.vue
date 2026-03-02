<template>
  <div>
    <!-- 今日概览 -->
    <n-grid :cols="4" :x-gap="16" style="margin-bottom:16px">
      <n-gi v-for="card in todayCards" :key="card.key">
        <n-card :bordered="false" style="border-radius:12px">
          <n-statistic :label="card.label" :value="card.value ?? '—'">
            <template #suffix>
              <span style="font-size:12px;color:#999">{{ card.unit }}</span>
            </template>
          </n-statistic>
        </n-card>
      </n-gi>
    </n-grid>

    <!-- 图表区 -->
    <n-grid :cols="2" :x-gap="16" :y-gap="16">
      <n-gi>
        <n-card title="近7天营业额(元)" :bordered="false" style="border-radius:12px">
          <v-chart :option="revenueOption" style="height:260px" autoresize />
        </n-card>
      </n-gi>
      <n-gi>
        <n-card title="近7天订单量" :bordered="false" style="border-radius:12px">
          <v-chart :option="orderOption" style="height:260px" autoresize />
        </n-card>
      </n-gi>
      <n-gi :span="2">
        <n-card title="商品销售排行 TOP10" :bordered="false" style="border-radius:12px">
          <v-chart :option="rankOption" style="height:240px" autoresize />
        </n-card>
      </n-gi>
    </n-grid>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import VChart from 'vue-echarts'
import { use } from 'echarts/core'
import { LineChart, BarChart } from 'echarts/charts'
import { GridComponent, TooltipComponent, LegendComponent } from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'
import { getTodaySummary, getRevenueTrend, getOrderTrend, getProductRank } from '@/api/shopdata'

use([LineChart, BarChart, GridComponent, TooltipComponent, LegendComponent, CanvasRenderer])

const today = ref({})
const revenueTrend = ref([])
const orderTrend = ref([])
const productRank = ref([])

const todayCards = computed(() => [
  { key: 'revenue',     label: '今日营业额',   value: today.value.revenue,     unit: '元' },
  { key: 'orders',      label: '今日订单数',   value: today.value.orderCount,  unit: '单' },
  { key: 'avgAmount',   label: '客单价',       value: today.value.avgAmount,   unit: '元' },
  { key: 'newCustomers',label: '新客户数',     value: today.value.newCustomers, unit: '人' }
])

const days = computed(() => revenueTrend.value.map(i => i.date))

const lineOption = (data, color) => ({
  tooltip: { trigger: 'axis' },
  xAxis: { type: 'category', data: days.value, axisLine: { lineStyle: { color: '#eee' } } },
  yAxis: { type: 'value', splitLine: { lineStyle: { color: '#f0f0f0' } } },
  series: [{ data, type: 'line', smooth: true, areaStyle: { opacity: 0.15 }, itemStyle: { color } }]
})

const revenueOption = computed(() => lineOption(revenueTrend.value.map(i => i.revenue), '#FFD100'))
const orderOption = computed(() => lineOption(orderTrend.value.map(i => i.count), '#1677ff'))

const rankOption = computed(() => ({
  tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
  xAxis: {
    type: 'category',
    data: productRank.value.map(i => i.name),
    axisLabel: { interval: 0, rotate: 30, fontSize: 11 }
  },
  yAxis: { type: 'value', name: '销售量' },
  series: [{
    data: productRank.value.map(i => i.salesCount),
    type: 'bar',
    barMaxWidth: 40,
    itemStyle: { color: '#FFD100', borderRadius: [4, 4, 0, 0] }
  }]
}))

onMounted(async () => {
  try {
    const [t, rt, ot, pr] = await Promise.all([
      getTodaySummary(), getRevenueTrend(), getOrderTrend(), getProductRank()
    ])
    today.value = t || {}
    revenueTrend.value = rt || []
    orderTrend.value = ot || []
    productRank.value = (pr || []).slice(0, 10)
  } catch {
    // 使用模拟数据展示图表形态
    const mockDays = ['1/4', '1/5', '1/6', '1/7', '1/8', '1/9', '1/10']
    revenueTrend.value = mockDays.map((d, i) => ({ date: d, revenue: 800 + i * 120 + Math.random() * 200 | 0 }))
    orderTrend.value   = mockDays.map((d, i) => ({ date: d, count: 20 + i * 3 + Math.random() * 10 | 0 }))
    productRank.value  = ['黄金炒饭', '鱼香肉丝', '红烧肉', '番茄炒蛋', '辣椒炒肉'].map((n, i) => ({ name: n, salesCount: 80 - i * 12 }))
    today.value = { revenue: '1,248.00', orderCount: 36, avgAmount: '34.67', newCustomers: 8 }
  }
})
</script>
