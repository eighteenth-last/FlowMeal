<template>
  <div>
    <n-card :bordered="false" style="border-radius:12px">
      <!-- 搜索栏 -->
      <n-space style="margin-bottom:16px">
        <n-input v-model:value="query.keyword" placeholder="手机号 / 昵称" clearable style="width:200px" />
        <n-select v-model:value="query.status" :options="statusOptions" placeholder="账号状态" clearable style="width:120px" />
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
import { getUsers, toggleUserStatus } from '@/api/users'

const message = useMessage()
const tableData = ref([])
const loading = ref(false)
const query = reactive({ keyword: '', status: null })

const pagination = reactive({ page: 1, pageSize: 15, itemCount: 0, showSizePicker: true, pageSizes: [15, 30, 50] })

const statusOptions = [
  { label: '正常', value: 0 },
  { label: '封禁', value: 1 }
]

const columns = [
  { title: 'ID', key: 'id', width: 80 },
  { title: '昵称', key: 'nickname' },
  { title: '手机号', key: 'phone' },
  {
    title: '状态', key: 'status',
    render: row => h(NTag, { type: row.status === 0 ? 'success' : 'error', size: 'small' },
      { default: () => row.status === 0 ? '正常' : '封禁' })
  },
  { title: '注册时间', key: 'createdAt', width: 180 },
  {
    title: '操作', key: 'actions', width: 120,
    render: row => h(NPopconfirm,
      { onPositiveClick: () => handleToggle(row) },
      {
        trigger: () => h(NButton, { size: 'small', type: row.status === 0 ? 'warning' : 'primary' },
          { default: () => row.status === 0 ? '封禁' : '解封' }),
        default: () => row.status === 0 ? '确定封禁该用户？' : '确定解封该用户？'
      })
  }
]

const loadData = async () => {
  loading.value = true
  try {
    const res = await getUsers({ ...query, page: pagination.page, size: pagination.pageSize })
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
  query.status = null
  pagination.page = 1
  loadData()
}

const handleToggle = async (row) => {
  try {
    await toggleUserStatus(row.id, row.status === 0 ? 1 : 0)
    message.success('操作成功')
    loadData()
  } catch (e) {
    message.error(e.message)
  }
}

const handlePageChange = (page) => {
  pagination.page = page
  loadData()
}

onMounted(loadData)
</script>
