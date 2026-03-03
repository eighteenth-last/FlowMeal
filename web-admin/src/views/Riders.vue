<template>
  <div>
    <n-card :bordered="false" style="border-radius:12px">
      <n-space style="margin-bottom:16px">
        <n-input v-model:value="query.keyword" placeholder="姓名 / 手机号" clearable style="width:200px" />
        <n-select v-model:value="query.auditStatus" :options="auditOptions" placeholder="审核状态" clearable style="width:140px" />
        <n-select v-model:value="query.onlineStatus" :options="onlineOptions" placeholder="在线状态" clearable style="width:140px" />
        <n-button type="primary" @click="loadData">查询</n-button>
        <n-button @click="reset">重置</n-button>
      </n-space>

      <n-data-table
        :columns="columns"
        :data="tableData"
        :loading="loading"
        :pagination="pagination"
        @update:page="handlePageChange"
        remote
        :scroll-x="1000"
      />
    </n-card>

    <!-- 订单调度弹窗 -->
    <n-modal v-model:show="dispatchModal.visible"
      :title="`调度订单 — ${dispatchModal.riderName}`"
      style="width:820px" preset="card" :bordered="false">
      <n-spin :show="dispatchModal.loading">
        <template v-if="!dispatchModal.loading && dispatchModal.orders.length === 0">
          <n-empty description="该骑手目前没有进行中的订单" style="padding:40px 0" />
        </template>
        <n-table v-else :bordered="false" :single-line="false" size="small">
          <thead>
            <tr>
              <th>订单号</th>
              <th>状态</th>
              <th>金额</th>
              <th>下单时间</th>
              <th style="width:220px">转派给</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="order in dispatchModal.orders" :key="order.id">
              <td style="font-size:12px;word-break:break-all">{{ order.orderNo }}</td>
              <td>
                <n-tag :type="orderStatusType[order.status] || 'default'" size="small">
                  {{ orderStatusLabel[order.status] || order.status }}
                </n-tag>
              </td>
              <td>¥{{ order.actualAmount }}</td>
              <td style="font-size:12px">{{ order.createdAt }}</td>
              <td>
                <n-select
                  v-model:value="dispatchModal.targetRiders[order.id]"
                  :options="onlineRiderOptions.filter(r => r.value !== dispatchModal.riderId)"
                  placeholder="选择骑手"
                  size="small"
                  clearable
                />
              </td>
              <td>
                <n-button size="small" type="warning"
                  :disabled="!dispatchModal.targetRiders[order.id]"
                  :loading="dispatchModal.reassigning[order.id]"
                  @click="doReassign(order.id)">
                  转派
                </n-button>
              </td>
            </tr>
          </tbody>
        </n-table>
      </n-spin>
    </n-modal>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, h } from 'vue'
import { useMessage, NButton, NTag, NSpace, NPopconfirm } from 'naive-ui'
import { getRiders, auditRider, setRiderOnline, getRiderActiveOrders, reassignOrder } from '@/api/riders'

const message = useMessage()
const tableData = ref([])
const loading = ref(false)
const query = reactive({ keyword: '', auditStatus: null, onlineStatus: null })
const pagination = reactive({ page: 1, pageSize: 15, itemCount: 0 })
const onlineRiderOptions = ref([])

const auditOptions = [
  { label: '待审核', value: 0 },
  { label: '已通过', value: 1 },
  { label: '已驳回', value: 2 }
]
const onlineOptions = [
  { label: '在线', value: 1 },
  { label: '离线', value: 0 }
]

const auditTagType  = { 0: 'warning', 1: 'success', 2: 'error' }
const auditTagLabel = { 0: '待审核', 1: '已通过', 2: '已驳回' }
const orderStatusType  = { RIDER_ACCEPTED: 'info', DELIVERING: 'warning' }
const orderStatusLabel = { RIDER_ACCEPTED: '已接单', DELIVERING: '配送中' }

const dispatchModal = reactive({
  visible: false,
  loading: false,
  riderId: null,
  riderName: '',
  orders: [],
  targetRiders: {},
  reassigning: {}
})

const columns = [
  { title: 'ID', key: 'id', width: 180, ellipsis: { tooltip: true } },
  { title: '姓名', key: 'realName', width: 90 },
  { title: '手机号', key: 'phone', width: 120 },
  { title: '身份证', key: 'idCard', width: 160, ellipsis: { tooltip: true } },
  {
    title: '在线状态', key: 'onlineStatus', width: 90,
    render: row => h(NTag,
      { type: row.onlineStatus === 1 ? 'success' : 'default', size: 'small' },
      { default: () => row.onlineStatus === 1 ? '在线' : '离线' }
    )
  },
  {
    title: '审核状态', key: 'auditStatus', width: 90,
    render: row => h(NTag,
      { type: auditTagType[row.auditStatus] ?? 'default', size: 'small' },
      { default: () => auditTagLabel[row.auditStatus] ?? row.auditStatus }
    )
  },
  { title: '注册时间', key: 'createdAt', width: 170 },
  {
    title: '操作', key: 'actions', fixed: 'right', width: 280,
    render: row => h(NSpace, { size: 4 }, {
      default: () => {
        const btns = []
        if (row.auditStatus === 0) {
          btns.push(
            h(NPopconfirm, { onPositiveClick: () => doAudit(row.id, 1) }, {
              trigger: () => h(NButton, { size: 'small', type: 'primary' }, { default: () => '通过' }),
              default: () => '确认通过审核？'
            }),
            h(NPopconfirm, { onPositiveClick: () => doAudit(row.id, 2) }, {
              trigger: () => h(NButton, { size: 'small', type: 'error' }, { default: () => '驳回' }),
              default: () => '确认驳回？'
            })
          )
        }
        if (row.auditStatus === 1) {
          if (row.onlineStatus === 1) {
            btns.push(
              h(NPopconfirm, { onPositiveClick: () => doToggleOnline(row, 0) }, {
                trigger: () => h(NButton, { size: 'small' }, { default: () => '强制下线' }),
                default: () => '确认强制下线该骑手？'
              }),
              h(NButton, { size: 'small', type: 'warning', onClick: () => openDispatch(row) },
                { default: () => '调度订单' }
              )
            )
          } else {
            btns.push(
              h(NButton, { size: 'small', type: 'success', onClick: () => doToggleOnline(row, 1) },
                { default: () => '强制上线' }
              )
            )
          }
        }
        return btns
      }
    })
  }
]

const doAudit = async (id, status) => {
  try {
    await auditRider(id, status)
    message.success(status === 1 ? '已通过' : '已驳回')
    loadData()
  } catch (e) { message.error(e.message) }
}

const doToggleOnline = async (row, onlineStatus) => {
  try {
    await setRiderOnline(row.id, onlineStatus)
    message.success(onlineStatus === 1 ? `已将 ${row.realName} 上线` : `已将 ${row.realName} 下线`)
    loadData()
  } catch (e) { message.error(e.message) }
}

const openDispatch = async (row) => {
  dispatchModal.visible = true
  dispatchModal.loading = true
  dispatchModal.riderId = row.id
  dispatchModal.riderName = row.realName
  dispatchModal.orders = []
  dispatchModal.targetRiders = {}
  dispatchModal.reassigning = {}
  try {
    const [orders] = await Promise.all([
      getRiderActiveOrders(row.id),
      loadOnlineRiders()
    ])
    dispatchModal.orders = orders || []
  } catch (e) {
    message.error(e.message)
  } finally {
    dispatchModal.loading = false
  }
}

const loadOnlineRiders = async () => {
  try {
    const res = await getRiders({ auditStatus: 1, onlineStatus: 1, page: 1, size: 100 })
    onlineRiderOptions.value = (res?.records || []).map(r => ({
      label: `${r.realName}（${r.phone}）`,
      value: r.id
    }))
  } catch {}
}

const doReassign = async (orderId) => {
  const toRiderId = dispatchModal.targetRiders[orderId]
  if (!toRiderId) return
  dispatchModal.reassigning[orderId] = true
  try {
    await reassignOrder(orderId, toRiderId)
    message.success('转派成功')
    dispatchModal.orders = dispatchModal.orders.filter(o => o.id !== orderId)
  } catch (e) {
    message.error(e.message)
  } finally {
    dispatchModal.reassigning[orderId] = false
  }
}

const loadData = async () => {
  loading.value = true
  try {
    const params = { ...query, page: pagination.page, size: pagination.pageSize }
    Object.keys(params).forEach(k => params[k] == null && delete params[k])
    const res = await getRiders(params)
    tableData.value = res?.records || []
    pagination.itemCount = res?.total || 0
  } catch (e) {
    message.error(e.message)
  } finally {
    loading.value = false
  }
}

const reset = () => {
  query.keyword = ''
  query.auditStatus = null
  query.onlineStatus = null
  pagination.page = 1
  loadData()
}
const handlePageChange = (page) => { pagination.page = page; loadData() }
onMounted(loadData)
</script>
