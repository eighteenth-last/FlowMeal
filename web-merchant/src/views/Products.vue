<template>
  <div>
    <!-- 工具栏 -->
    <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:14px">
      <n-space>
        <n-input v-model:value="query.keyword" placeholder="商品名称" clearable style="width:200px" />
        <n-select v-model:value="query.categoryId" :options="catOptions" placeholder="全部分类" clearable style="width:150px" />
        <n-button type="primary" @click="loadData">查询</n-button>
        <n-button @click="openCreate">+ 新增商品</n-button>
      </n-space>
      <n-button secondary @click="showCatModal = true">📂 分类管理</n-button>
    </div>

    <n-card :bordered="false" style="border-radius:12px">
      <n-data-table
        :columns="columns"
        :data="tableData"
        :loading="loading"
        :pagination="pagination"
        @update:page="p => { pagination.page = p; loadData() }"
        remote
      />
    </n-card>

    <!-- 新增/编辑商品弹窗 -->
    <n-modal v-model:show="formModal.show" :title="formModal.id ? '编辑商品' : '新增商品'" preset="card" style="width:540px" :mask-closable="false">
      <n-form :model="form" label-placement="left" label-width="80" :rules="rules" ref="formRef">
        <n-form-item label="商品名称" path="name">
          <n-input v-model:value="form.name" />
        </n-form-item>
        <n-form-item label="分类" path="categoryId">
          <n-select v-model:value="form.categoryId" :options="catOptions" placeholder="请选择分类" />
        </n-form-item>
        <n-form-item label="价格(元)" path="price">
          <n-input-number v-model:value="form.price" :min="0.01" :precision="2" style="width:100%" />
        </n-form-item>
        <n-form-item label="折扣价">
          <n-input-number v-model:value="form.discountPrice" :min="0" :precision="2" placeholder="不填则无折扣" style="width:100%" />
        </n-form-item>
        <n-form-item label="封面图">
          <n-input v-model:value="form.image" placeholder="图片 URL（可选）" />
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

    <!-- 分类管理弹窗 -->
    <n-modal v-model:show="showCatModal" title="分类管理" preset="card" style="width:420px">
      <div style="display:flex;gap:8px;margin-bottom:14px">
        <n-input v-model:value="newCatName" placeholder="新分类名称" @keyup.enter="addCategory" style="flex:1" />
        <n-button type="primary" :loading="catAdding" @click="addCategory">添加</n-button>
      </div>
      <n-empty v-if="!categories.length" description="暂无分类" />
      <div v-for="cat in categories" :key="cat.id" style="display:flex;justify-content:space-between;align-items:center;padding:8px 0;border-bottom:1px solid #f0f0f0">
        <span style="font-size:14px">{{ cat.name }}</span>
        <n-popconfirm @positive-click="deleteCategory(cat.id)">
          <template #trigger>
            <n-button size="small" type="error" :ghost="true">删除</n-button>
          </template>
          确认删除分类「{{ cat.name }}」？
        </n-popconfirm>
      </div>
    </n-modal>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, h } from 'vue'
import { useMessage, NButton, NSpace, NSwitch, NPopconfirm } from 'naive-ui'
import { getProducts, getCategories, createProduct, updateProduct, toggleProductStatus, deleteProduct } from '@/api/products'
import request from '@/api/request'
import { useAuthStore } from '@/stores/auth'

const message = useMessage()
const authStore = useAuthStore()
const tableData = ref([])
const loading = ref(false)
const categories = ref([])
const catOptions = computed(() => categories.value.map(c => ({ label: c.name, value: c.id })))
const query = reactive({ keyword: '', categoryId: null })
const pagination = reactive({ page: 1, pageSize: 15, itemCount: 0 })
const formRef = ref(null)
const formModal = reactive({ show: false, id: null, loading: false })
const form = reactive({ name: '', categoryId: null, price: 9.9, discountPrice: null, image: '', description: '', stock: 99 })
const rules = {
  name: [{ required: true, message: '请填写商品名称', trigger: 'blur' }],
  price: [{ required: true, type: 'number', message: '请填写价格', trigger: 'blur' }]
}

// 分类管理
const showCatModal = ref(false)
const newCatName = ref('')
const catAdding = ref(false)

const catMap = computed(() => Object.fromEntries(categories.value.map(c => [c.id, c.name])))

const columns = [
  {
    title: '图片', key: 'image', width: 72,
    render: row => row.image
      ? h('img', { src: row.image, style: 'width:52px;height:52px;object-fit:cover;border-radius:8px;display:block' })
      : h('div', { style: 'width:52px;height:52px;background:#f5f5f5;border-radius:8px;display:flex;align-items:center;justify-content:center;font-size:22px' }, '🍱')
  },
  { title: '商品名', key: 'name' },
  {
    title: '分类', key: 'categoryId',
    render: row => catMap.value[row.categoryId] || '-'
  },
  { title: '价格', key: 'price', render: row => `¥${row.price}` },
  { title: '库存', key: 'stock', render: row => h('span', { style: row.stock < 20 ? 'color:#ff4d4f' : '' }, row.stock) },
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

const loadCategories = async () => {
  try {
    const cats = await getCategories(authStore.merchantId)
    categories.value = cats || []
  } catch {}
}

const openCreate = () => {
  formModal.id = null
  Object.assign(form, { name: '', categoryId: null, price: 9.9, discountPrice: null, image: '', description: '', stock: 99 })
  formModal.show = true
}

const openEdit = (row) => {
  formModal.id = row.id
  Object.assign(form, {
    name: row.name,
    categoryId: row.categoryId,
    price: Number(row.price),
    discountPrice: row.discountPrice ? Number(row.discountPrice) : null,
    image: row.image || '',
    description: row.description || '',
    stock: row.stock
  })
  formModal.show = true
}

const submitForm = () => {
  formRef.value?.validate(async (errors) => {
    if (errors) return
    formModal.loading = true
    try {
      const payload = { ...form, discountPrice: form.discountPrice || null }
      if (formModal.id) {
        await updateProduct(formModal.id, payload)
      } else {
        await createProduct(payload)
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

const addCategory = async () => {
  if (!newCatName.value.trim()) { message.warning('请填写分类名称'); return }
  catAdding.value = true
  try {
    await request.post('/merchant/categories', { name: newCatName.value.trim() })
    message.success('分类已添加')
    newCatName.value = ''
    await loadCategories()
  } catch (e) {
    message.error(e.message || '添加失败')
  } finally {
    catAdding.value = false
  }
}

const deleteCategory = async (id) => {
  try {
    await request.delete(`/merchant/categories/${id}`)
    message.success('已删除')
    await loadCategories()
  } catch (e) {
    message.error(e.message || '删除失败')
  }
}

onMounted(async () => {
  await loadCategories()
  loadData()
})
</script>
