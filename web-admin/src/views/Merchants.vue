<template>
  <div>
    <n-card :bordered="false" style="border-radius:12px">
      <n-space style="margin-bottom:16px">
        <n-input v-model:value="query.keyword" placeholder="商家名称 / 手机号" clearable style="width:220px" />
        <n-select v-model:value="query.status" :options="statusOptions" placeholder="审核状态" clearable style="width:140px" />
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

    <!-- 审核弹窗 -->
    <n-modal v-model:show="auditModal.show" title="商家审核" preset="card" style="width:480px" :mask-closable="false">
      <n-form :model="auditModal" label-placement="left" label-width="80">
        <n-form-item label="审核结论">
          <n-radio-group v-model:value="auditModal.status">
            <n-radio value="APPROVED">通过</n-radio>
            <n-radio value="REJECTED" style="margin-left:16px">驳回</n-radio>
          </n-radio-group>
        </n-form-item>
        <n-form-item label="备注" v-if="auditModal.status === 'REJECTED'">
          <n-input v-model:value="auditModal.remark" type="textarea" placeholder="请填写驳回原因" :rows="3" />
        </n-form-item>
      </n-form>
      <template #footer>
        <n-space justify="end">
          <n-button @click="auditModal.show = false">取消</n-button>
          <n-button type="primary" :loading="auditModal.loading" @click="submitAudit">确定</n-button>
        </n-space>
      </template>
    </n-modal>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, h } from 'vue'
import { useMessage, NButton, NTag, NSpace } from 'naive-ui'
import { getMerchants, auditMerchant } from '@/api/merchants'

const message = useMessage()
const tableData = ref([])
const loading = ref(false)
const query = reactive({ keyword: '', status: null })
const pagination = reactive({ page: 1, pageSize: 15, itemCount: 0 })

const auditModal = reactive({ show: false, id: null, status: 'APPROVED', remark: '', loading: false })

const statusOptions = [
  { label: '待审核', value: 'PENDING' },
  { label: '已通过', value: 'APPROVED' },
  { label: '已驳回', value: 'REJECTED' }
]

const statusTagType = { PENDING: 'warning', APPROVED: 'success', REJECTED: 'error' }
const statusLabel = { PENDING: '待审核', APPROVED: '已通过', REJECTED: '已驳回' }

const columns = [
  { title: 'ID', key: 'id', width: 80 },
  { title: '店铺名称', key: 'shopName' },
  { title: '联系人', key: 'contactName' },
  { title: '手机号', key: 'phone' },
  {
    title: '审核状态', key: 'auditStatus',
    render: row => h(NTag, { type: statusTagType[row.auditStatus] || 'default', size: 'small' },
      { default: () => statusLabel[row.auditStatus] || row.auditStatus })
  },
  { title: '入驻时间', key: 'createdAt' },
  {
    title: '操作', key: 'actions',
    render: row => h(NSpace, {}, {
      default: () => [
        row.auditStatus === 'PENDING'
          ? h(NButton, { size: 'small', type: 'primary', onClick: () => openAudit(row) }, { default: () => '审核' })
          : null
      ].filter(Boolean)
    })
  }
]

const openAudit = (row) => {
  auditModal.id = row.id
  auditModal.status = 'APPROVED'
  auditModal.remark = ''
  auditModal.show = true
}

const submitAudit = async () => {
  auditModal.loading = true
  try {
    await auditMerchant(auditModal.id, { status: auditModal.status, remark: auditModal.remark })
    message.success('审核完成')
    auditModal.show = false
    loadData()
  } catch (e) {
    message.error(e.message)
  } finally {
    auditModal.loading = false
  }
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await getMerchants({ ...query, page: pagination.page, size: pagination.pageSize })
    tableData.value = res?.records || []
    pagination.itemCount = res?.total || 0
  } catch (e) {
    message.error(e.message)
  } finally {
    loading.value = false
  }
}

const reset = () => { query.keyword = ''; query.status = null; pagination.page = 1; loadData() }
const handlePageChange = (page) => { pagination.page = page; loadData() }
onMounted(loadData)
</script>
