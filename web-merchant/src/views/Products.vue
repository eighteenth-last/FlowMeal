<template>
  <div>
    <n-card :bordered="false" style="border-radius:12px">
      <n-space style="margin-bottom:16px">
        <n-input v-model:value="query.keyword" placeholder="商品名称" clearable style="width:200px" />
        <n-select v-model:value="query.categoryId" :options="catOptions" placeholder="分类" clearable style="width:140px" />
        <n-button type="primary" @click="loadData">查询</n-button>
        <n-button @click="openCreate">+ 新增商品</n-button>
      </n-space>

      <n-data-table
        :columns="columns"
        :data="tableData"
        :loading="loading"
        :pagination="pagination"
        @update:page="p => { pagination.page = p; loadData() }"
        remote
      />
    </n-card>

    <!-- 新增/编辑弹窗 -->
    <n-modal v-model:show="formModal.show" :title="formModal.id ? '编辑商品' : '新增商品'" preset="card" style="width:540px" :mask-closable="false">
      <n-form :model="form" label-placement="left" label-width="80" :rules="rules" ref="formRef">
        <n-form-item label="商品名称" path="name">
          <n-input v-model:value="form.name" />
        </n-form-item>
        <n-form-item label="分类" path="categoryId">
          <n-select v-model:value="form.categoryId" :options="catOptions" />
        </n-form-item>
        <n-form-item label="价格(元)" path="price">
          <n-input-number v-model:value="form.price" :min="0.01" :precision="2" style="width:100%" />
        </n-form-item>
        <n-form-item label="封面图">
          <n-input v-model:value="form.imageUrl" placeholder="图片 URL（可选）" />
        </n-form-item>
        <n-form-item label="商品描述">
          <n-input v-model:value="form.description" type="textarea" :rows="2" />
        </n-form-item>
        <n-form-item label="库存">
          <n-input-number v-model:value="form.stock" :min="0" style="width:100%" />
        </n-form-item>
      </n-form>
      <template #footer>
        <n-space justify="end">
          <n-button @click="formModal.show = false">取消</n-button>
          <n-button type="primary" :loading="formModal.loading" @click="submitForm">保 存</n-button>
        </n-space>
      </template>
    </n-modal>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, h } from 'vue'
import { useMessage, NButton, NTag, NSpace, NSwitch, NPopconfirm } from 'naive-ui'
import { getProducts, getCategories, createProduct, updateProduct, toggleProductStatus, deleteProduct } from '@/api/products'
import { useAuthStore } from '@/stores/auth'

const message = useMessage()
const authStore = useAuthStore()
const tableData = ref([])
const loading = ref(false)
const categories = ref([])
const query = reactive({ keyword: '', categoryId: null })
const pagination = reactive({ page: 1, pageSize: 15, itemCount: 0 })
const formRef = ref(null)
const formModal = reactive({ show: false, id: null, loading: false })
const form = reactive({ name: '', categoryId: null, price: 9.9, imageUrl: '', description: '', stock: 99 })
const rules = {
  name: [{ required: true, message: '请填写商品名称', trigger: 'blur' }],
  price: [{ required: true, type: 'number', message: '请填写价格', trigger: 'blur' }]
}

const catOptions = ref([])

const columns = [
  { title: '商品名', key: 'name' },
  { title: '分类', key: 'categoryName' },
  { title: '价格', key: 'price', render: row => `¥${row.price}` },
  { title: '库存', key: 'stock' },
  {
    title: '上架', key: 'status',
    render: row => h(NSwitch, {
      value: row.status === 1,
      onUpdateValue: (v) => handleToggle(row, v ? 1 : 0)
    })
  },
  {
    title: '操作', key: 'actions',
    render: row => h(NSpace, {}, {
      default: () => [
        h(NButton, { size: 'small', onClick: () => openEdit(row) }, { default: () => '编辑' }),
        h(NPopconfirm, { onPositiveClick: () => handleDelete(row.id) },
          { trigger: () => h(NButton, { size: 'small', type: 'error' }, { default: () => '删除' }), default: () => '确认删除？' })
      ]
    })
  }
]

const loadData = async () => {
  loading.value = true
  try {
    const res = await getProducts({ ...query, page: pagination.page, size: pagination.pageSize })
    tableData.value = res?.records || []
    pagination.itemCount = res?.total || 0
  } catch (e) {
    message.error(e.message)
  } finally {
    loading.value = false
  }
}

const openCreate = () => {
  formModal.id = null
  Object.assign(form, { name: '', categoryId: null, price: 9.9, imageUrl: '', description: '', stock: 99 })
  formModal.show = true
}

const openEdit = (row) => {
  formModal.id = row.id
  Object.assign(form, { name: row.name, categoryId: row.categoryId, price: row.price, imageUrl: row.imageUrl, description: row.description, stock: row.stock })
  formModal.show = true
}

const submitForm = () => {
  formRef.value?.validate(async (errors) => {
    if (errors) return
    formModal.loading = true
    try {
      if (formModal.id) {
        await updateProduct(formModal.id, form)
      } else {
        await createProduct(form)
      }
      message.success('保存成功')
      formModal.show = false
      loadData()
    } catch (e) {
      message.error(e.message)
    } finally {
      formModal.loading = false
    }
  })
}

const handleToggle = async (row, status) => {
  try {
    await toggleProductStatus(row.id, status)
    row.status = status
  } catch (e) {
    message.error(e.message)
  }
}

const handleDelete = async (id) => {
  try {
    await deleteProduct(id)
    message.success('已删除')
    loadData()
  } catch (e) {
    message.error(e.message)
  }
}

onMounted(async () => {
  try {
    const cats = await getCategories(authStore.merchantId)
    categories.value = cats || []
    catOptions.value = cats.map(c => ({ label: c.name, value: c.id }))
  } catch {}
  loadData()
})
</script>
