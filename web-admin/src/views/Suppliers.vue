<template>
  <div class="page-wrap">
    <!-- 操作栏 -->
    <div class="toolbar">
      <n-input v-model:value="searchText" placeholder="搜索供应商名称 / 联系人" clearable style="width:260px">
        <template #prefix><n-icon :component="SearchOutline" /></template>
      </n-input>
      <n-button type="primary" @click="openCreate">+ 新增供应商</n-button>
    </div>

    <!-- 表格 -->
    <n-card :bordered="false">
      <n-data-table
        :columns="columns"
        :data="filteredList"
        :loading="loading"
        :pagination="{ pageSize: 10 }"
        size="small"
      />
    </n-card>

    <!-- 新增 / 编辑弹窗 -->
    <n-modal v-model:show="showModal" :title="editId ? '编辑供应商' : '新增供应商'" preset="card" style="width:520px">
      <n-form :model="form" label-placement="left" label-width="80px">
        <n-form-item label="供应商名称" required>
          <n-input v-model:value="form.name" placeholder="如：绿源食材" />
        </n-form-item>
        <n-form-item label="联系人">
          <n-input v-model:value="form.contact" placeholder="联系人姓名" />
        </n-form-item>
        <n-form-item label="联系电话">
          <n-input v-model:value="form.phone" placeholder="手机号" />
        </n-form-item>
        <n-form-item label="供应类别">
          <n-select v-model:value="form.category" :options="categoryOptions" clearable placeholder="选择类别" />
        </n-form-item>
        <n-form-item label="供应商地址">
          <n-input v-model:value="form.address" placeholder="详细地址" />
        </n-form-item>
        <n-form-item label="备注">
          <n-input v-model:value="form.remark" type="textarea" :rows="2" placeholder="可选备注" />
        </n-form-item>
      </n-form>
      <template #footer>
        <n-space justify="end">
          <n-button @click="showModal = false">取消</n-button>
          <n-button type="primary" :loading="submitLoading" @click="handleSubmit">确认</n-button>
        </n-space>
      </template>
    </n-modal>
  </div>
</template>

<script setup>
import { ref, computed, h, onMounted, reactive } from 'vue'
import { useMessage, NTag, NButton, NSpace, NPopconfirm } from 'naive-ui'
import { SearchOutline } from '@vicons/ionicons5'
import request from '@/api/request'

const message = useMessage()
const loading = ref(false)
const submitLoading = ref(false)
const showModal = ref(false)
const searchText = ref('')
const editId = ref(null)
const suppliers = ref([])

const categoryOptions = [
  { label: '粮油', value: '粮油' },
  { label: '蔬菜', value: '蔬菜' },
  { label: '肉类', value: '肉类' },
  { label: '调料', value: '调料' },
  { label: '饮品', value: '饮品' },
  { label: '其他', value: '其他' }
]

const emptyForm = { name: '', contact: '', phone: '', address: '', category: null, remark: '' }
const form = reactive({ ...emptyForm })

const filteredList = computed(() => {
  if (!searchText.value) return suppliers.value
  const kw = searchText.value.toLowerCase()
  return suppliers.value.filter(s =>
    (s.name || '').toLowerCase().includes(kw) ||
    (s.contact || '').toLowerCase().includes(kw)
  )
})

const columns = [
  { title: '供应商名称', key: 'name' },
  { title: '联系人', key: 'contact' },
  { title: '联系电话', key: 'phone' },
  { title: '供应类别', key: 'category' },
  { title: '地址', key: 'address', ellipsis: { tooltip: true } },
  {
    title: '状态', key: 'status',
    render: row => h(NTag,
      { type: row.status === 1 ? 'success' : 'default', size: 'small' },
      { default: () => row.status === 1 ? '合作中' : '已停用' }
    )
  },
  {
    title: '操作', key: 'actions', width: 200,
    render: row => h(NSpace, { wrap: false, size: 'small' }, {
      default: () => [
        h(NButton, { size: 'small', type: 'info', ghost: true, onClick: () => openEdit(row) }, { default: () => '编辑' }),
        row.status === 1
          ? h(NButton, { size: 'small', type: 'warning', ghost: true, onClick: () => toggleStatus(row.id, 0) }, { default: () => '停用' })
          : h(NButton, { size: 'small', type: 'success', ghost: true, onClick: () => toggleStatus(row.id, 1) }, { default: () => '启用' }),
        h(NPopconfirm, { onPositiveClick: () => handleDelete(row.id) }, {
          default: () => '确认删除该供应商？',
          trigger: () => h(NButton, { size: 'small', type: 'error', ghost: true }, { default: () => '删除' })
        })
      ]
    })
  },
  { title: '创建时间', key: 'createdAt' }
]

const loadData = async () => {
  loading.value = true
  try {
    suppliers.value = await request.get('/admin/suppliers') || []
  } catch (e) {
    message.error(e.message || '加载失败')
  } finally {
    loading.value = false
  }
}

const openCreate = () => {
  editId.value = null
  Object.assign(form, emptyForm)
  showModal.value = true
}

const openEdit = (row) => {
  editId.value = row.id
  Object.assign(form, {
    name: row.name,
    contact: row.contact,
    phone: row.phone,
    address: row.address,
    category: row.category,
    remark: row.remark
  })
  showModal.value = true
}

const handleSubmit = async () => {
  if (!form.name.trim()) { message.warning('请填写供应商名称'); return }
  submitLoading.value = true
  try {
    if (editId.value) {
      await request.put(`/admin/suppliers/${editId.value}`, { ...form })
      message.success('修改成功')
    } else {
      await request.post('/admin/suppliers', { ...form })
      message.success('新增成功')
    }
    showModal.value = false
    loadData()
  } catch (e) {
    message.error(e.message || '操作失败')
  } finally {
    submitLoading.value = false
  }
}

const toggleStatus = async (id, status) => {
  try {
    await request.put(`/admin/suppliers/${id}/status`, null, { params: { status } })
    message.success(status === 1 ? '已启用' : '已停用')
    loadData()
  } catch (e) {
    message.error(e.message)
  }
}

const handleDelete = async (id) => {
  try {
    await request.delete(`/admin/suppliers/${id}`)
    message.success('已删除')
    loadData()
  } catch (e) {
    message.error(e.message)
  }
}

onMounted(loadData)
</script>

<style scoped>
.page-wrap { padding: 0; }
.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}
</style>
