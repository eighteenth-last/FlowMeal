<template>
  <div class="orders-page">
    <!-- 状态 Tab -->
    <n-card :bordered="false" style="border-radius:12px;margin-bottom:16px">
      <n-tabs v-model:value="activeTab" type="line" @update:value="handleTabChange">
        <n-tab name="WAIT_MERCHANT_CONFIRM">待接单</n-tab>
        <n-tab name="MERCHANT_CONFIRMED">已接单</n-tab>
        <n-tab name="DELIVERING">配送中</n-tab>
        <n-tab name="COMPLETED">已完成</n-tab>
        <n-tab name="CANCELLED">已取消</n-tab>
      </n-tabs>
    </n-card>

    <n-grid :cols="2" :x-gap="16">
      <!-- 左：订单列表 -->
      <n-gi>
        <n-card :bordered="false" style="border-radius:12px;height:calc(100vh - 220px);overflow:auto">
          <div v-if="loading" style="text-align:center;padding:40px">
            <n-spin />
          </div>
          <n-empty v-else-if="orders.length === 0" description="暂无订单" style="margin-top:60px" />
          <div v-else>
            <div
              v-for="order in orders"
              :key="order.id"
              class="order-item"
              :class="{ active: selectedOrder?.id === order.id }"
              @click="selectOrder(order)"
            >
              <n-space justify="space-between" align="center">
                <span class="order-no">{{ order.orderNo }}</span>
                <n-tag :type="statusTagType[order.status]" size="small">
                  {{ statusLabel[order.status] || order.status }}
                </n-tag>
              </n-space>
              <div style="font-size:12px;color:#999;margin-top:4px">
                {{ order.createdAt }} · ¥{{ order.totalAmount }}
              </div>
              <div style="font-size:13px;margin-top:4px;color:#333">
                {{ order.addressSnapshot ? JSON.parse(order.addressSnapshot).address : '' }}
              </div>
            </div>
          </div>
        </n-card>
      </n-gi>

      <!-- 右：订单详情 & 操作 -->
      <n-gi>
        <n-card :bordered="false" style="border-radius:12px;height:calc(100vh - 220px);overflow:auto">
          <div v-if="!selectedOrder" style="display:flex;align-items:center;justify-content:center;height:100%">
            <n-empty description="点击左侧订单查看详情" />
          </div>
          <div v-else>
            <n-descriptions :column="2" label-placement="left" bordered size="small">
              <n-descriptions-item label="订单号">{{ selectedOrder.orderNo }}</n-descriptions-item>
              <n-descriptions-item label="下单时间">{{ selectedOrder.createdAt }}</n-descriptions-item>
              <n-descriptions-item label="实付金额">¥{{ selectedOrder.totalAmount }}</n-descriptions-item>
              <n-descriptions-item label="配送地址" :span="2">
                {{ selectedOrder.addressSnapshot ? JSON.parse(selectedOrder.addressSnapshot).address : '-' }}
              </n-descriptions-item>
            </n-descriptions>

            <!-- 订单商品明细 -->
            <div style="margin-top:16px">
              <div style="font-weight:600;margin-bottom:8px">商品明细</div>
              <n-list>
                <n-list-item v-for="item in selectedOrder.items" :key="item.id">
                  <n-thing :title="item.productName">
                    <template #description>×{{ item.quantity }} · ¥{{ item.price }}</template>
                  </n-thing>
                </n-list-item>
              </n-list>
            </div>

            <!-- 操作按钮 -->
            <n-space style="margin-top:24px">
              <template v-if="selectedOrder.status === 'WAIT_MERCHANT_CONFIRM'">
                <n-button type="primary" size="large" @click="accept(selectedOrder.id)">接 单</n-button>
                <n-button type="error" size="large" @click="reject(selectedOrder.id)">拒 单</n-button>
              </template>              <n-button size="large" @click="downloadReceipt(selectedOrder)">
                🗈️ 打印小票
              </n-button>            </n-space>
          </div>
        </n-card>
      </n-gi>
    </n-grid>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useMessage } from 'naive-ui'
import { getOrders, acceptOrder, rejectOrder, getOrderDetail } from '@/api/orders'
import { printReceipt } from '@/utils/receipt'

const message = useMessage()
const activeTab = ref('WAIT_MERCHANT_CONFIRM')
const orders = ref([])
const selectedOrder = ref(null)
const loading = ref(false)

const statusTagType = {
  WAIT_MERCHANT_CONFIRM: 'warning',
  MERCHANT_CONFIRMED: 'info',
  RIDER_ACCEPTED: 'info',
  DELIVERING: 'info',
  COMPLETED: 'success',
  CANCELLED: 'error'
}
const statusLabel = {
  WAIT_MERCHANT_CONFIRM: '待接单',
  MERCHANT_CONFIRMED: '已接单',
  RIDER_ACCEPTED: '骑手已接',
  DELIVERING: '配送中',
  COMPLETED: '已完成',
  CANCELLED: '已取消'
}

const loadOrders = async () => {
  loading.value = true
  selectedOrder.value = null
  try {
    const res = await getOrders({ status: activeTab.value, page: 1, size: 50 })
    orders.value = res?.records || []
  } catch (e) {
    message.error(e.message)
  } finally {
    loading.value = false
  }
}

const selectOrder = async (order) => {
  try {
    const detail = await getOrderDetail(order.id)
    selectedOrder.value = detail || order
  } catch {
    selectedOrder.value = order
  }
}

const accept = async (id) => {
  try {
    await acceptOrder(id)
    message.success('已接单')
    loadOrders()
  } catch (e) {
    message.error(e.message)
  }
}

const reject = async (id) => {
  try {
    await rejectOrder(id, '商家已拒单')
    message.warning('已拒单')
    loadOrders()
  } catch (e) {
    message.error(e.message)
  }
}

const handleTabChange = () => loadOrders()
onMounted(loadOrders)

// ── 小票打印 ──────────────────────────────────────────
const downloadReceipt = (order) => {
  let addressText = null
  try {
    const s = JSON.parse(order.addressSnapshot || '{}')
    if (s.type !== '线下收款') {
      addressText = `${s.receiver || ''} ${s.phone || ''}  ${s.province || ''}${s.city || ''}${s.district || ''} ${s.detail || ''}`
    }
  } catch { /* ignore */ }

  printReceipt({
    orderNo:     order.orderNo,
    createdAt:   order.createdAt,
    paymentType: order.paymentType,
    addressText,
    remark:      (order.remark && order.remark !== 'POS线下订单') ? order.remark : null,
    totalAmount: order.actualAmount ?? order.totalAmount,
    deliveryFee: order.deliveryFee,
    items: (order.items || []).map(i => ({
      name:      i.productName,
      qty:       i.quantity,
      unitPrice: i.unitPrice,
      subtotal:  i.subtotal
    }))
  })
}
</script>

<style scoped>
.order-item {
  padding: 12px;
  border-radius: 8px;
  cursor: pointer;
  border: 1.5px solid transparent;
  margin-bottom: 8px;
  transition: all 0.2s;
  background: #fafafa;
}
.order-item:hover { border-color: #FFD100; background: #fffef0; }
.order-item.active { border-color: #FFD100; background: #fffacc; }
.order-no { font-weight: 600; font-size: 13px; }
</style>
