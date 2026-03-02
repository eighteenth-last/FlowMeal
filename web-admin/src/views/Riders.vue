<template>
  <div>
    <n-card :bordered="false" style="border-radius:12px">
      <n-space style="margin-bottom:16px">
        <n-input v-model:value="query.keyword" placeholder="姓名 / 手机号" clearable style="width:200px" />
        <n-select v-model:value="query.auditStatus" :options="auditOptions" placeholder="审核状态" clearable style="width:140px" />
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
      />
    </n-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, h } from 'vue'
import { useMessage, NButton, NTag, NSpace, NPopconfirm } from 'naive-ui'
import { getRiders, auditRider } from '@/api/riders'

const message = useMessage()
const tableData = ref([])
const loading = ref(false)
const query = reactive({ keyword: '', auditStatus: null })
const pagination = reactive({ page: 1, pageSize: 15, itemCount: 0 })

const auditOptions = [
  { label: '待审核', value: 'PENDING' },
  { label: '已通过', value: 'APPROVED' },
  { label: '已驳回', value: 'REJECTED' }
]

const tagType = { PENDING: 'warning', APPROVED: 'success', REJECTED: 'error' }
const tagLabel = { PENDING: '待审核', APPROVED: '已通过', REJECTED: '已驳回' }

const columns = [
  { title: 'ID', key: 'id', width: 80 },
  { title: '姓名', key: 'realName' },
  { title: '手机号', key: 'phone' },
  { title: '身份证', key: 'idCard' },
  {
    title: '审核状态', key: 'auditStatus',
    render: row => h(NTag, { type: tagType[row.auditStatus] || 'default', size: 'small' },
      { default: () => tagLabel[row.auditStatus] || row.auditStatus })
  },
  { title: '注册时间', key: 'createdAt' },
  {
    title: '操作', key: 'actions',
    render: row => h(NSpace, {}, {
      default: () => row.auditStatus === 'PENDING' ? [
        h(NPopconfirm, { onPositiveClick: () => doAudit(row.id, 'APPROVED') },
          { trigger: () => h(NButton, { size: 'small', type: 'primary' }, { default: () => '通过' }), default: () => '确认通过审核？' }),
        h(NPopconfirm, { onPositiveClick: () => doAudit(row.id, 'REJECTED') },
          { trigger: () => h(NButton, { size: 'small', type: 'error' }, { default: () => '驳回' }), default: () => '确认驳回该骑手？' })
      ] : []
    })
  }
]

const doAudit = async (id, status) => {
  try {
    await auditRider(id, status)
    message.success(status === 'APPROVED' ? '已通过' : '已驳回')
    loadData()
  } catch (e) {
    message.error(e.message)
  }
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await getRiders({ ...query, page: pagination.page, size: pagination.pageSize })
    tableData.value = res?.records || []
    pagination.itemCount = res?.total || 0
  } catch (e) {
    message.error(e.message)
  } finally {
    loading.value = false
  }
}

const reset = () => { query.keyword = ''; query.auditStatus = null; pagination.page = 1; loadData() }
const handlePageChange = (page) => { pagination.page = page; loadData() }
onMounted(loadData)
</script>
