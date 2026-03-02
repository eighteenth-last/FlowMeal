<template>
  <view class="edit-page">
    <!-- Header -->
    <view class="edit-header">
      <view class="back-btn" @click="goBack">
        <text class="fa fa-chevron-left" style="font-size:28rpx;"></text>
      </view>
      <text class="font-bold" style="font-size:32rpx;">{{ isEdit ? '编辑餐品' : '添加餐品' }}</text>
      <view style="width:60rpx;"></view>
    </view>

    <scroll-view scroll-y class="edit-body">
      <!-- 图片 -->
      <view class="section">
        <text class="section-title font-bold">餐品图片</text>
        <view class="img-upload" @click="chooseImage">
          <text class="fa fa-camera" style="font-size:48rpx;color:#ccc;"></text>
          <text class="text-gray" style="font-size:22rpx;">点击上传</text>
        </view>
      </view>

      <!-- 基本信息 -->
      <view class="section">
        <text class="section-title font-bold">基本信息</text>
        <view class="field">
          <text class="field-label">名称</text>
          <input v-model="form.name" placeholder="餐品名称" class="field-input" />
        </view>
        <view class="field">
          <text class="field-label">分类</text>
          <picker :range="catNames" @change="onCatChange" class="field-picker">
            <text :class="form.categoryId ? '' : 'text-gray'">
              {{ selectedCatName || '选择分类' }}
            </text>
          </picker>
        </view>
        <view class="field">
          <text class="field-label">价格</text>
          <input v-model="form.price" type="digit" placeholder="0.00" class="field-input" />
        </view>
        <view class="field">
          <text class="field-label">库存</text>
          <input v-model="form.stock" type="number" placeholder="0" class="field-input" />
        </view>
        <view class="field">
          <text class="field-label">描述</text>
          <textarea v-model="form.description" placeholder="餐品描述（选填）" class="field-textarea" :maxlength="200" />
        </view>
      </view>

      <!-- 状态 -->
      <view class="section">
        <view class="toggle-row">
          <text class="font-bold">立即上架</text>
          <switch :checked="form.status === 1" color="#FFD100" @change="form.status = $event.detail.value ? 1 : 0" />
        </view>
      </view>
    </scroll-view>

    <!-- 提交按钮 -->
    <view class="submit-area">
      <view class="btn-dark" @click="handleSubmit">{{ isEdit ? '保存修改' : '添加餐品' }}</view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { getProducts, addProduct, updateProduct } from '@/api/product'
import { getCategories } from '@/api/product'

const isEdit = ref(false)
const productId = ref(null)
const categories = ref([])
const form = ref({
  name: '', categoryId: null, price: '', stock: '',
  description: '', status: 1, image: ''
})

const catNames = computed(() => categories.value.map(c => c.name))
const selectedCatName = computed(() => {
  const cat = categories.value.find(c => c.id === form.value.categoryId)
  return cat?.name || ''
})

const onCatChange = (e) => {
  const idx = e.detail.value
  form.value.categoryId = categories.value[idx]?.id
}

const chooseImage = () => {
  uni.chooseImage({
    count: 1,
    success: (res) => {
      form.value.image = res.tempFilePaths[0]
      uni.showToast({ title: '图片已选择', icon: 'none' })
    }
  })
}

const handleSubmit = async () => {
  if (!form.value.name || !form.value.price) {
    return uni.showToast({ title: '请填写名称和价格', icon: 'none' })
  }
  try {
    const data = { ...form.value, price: parseFloat(form.value.price), stock: parseInt(form.value.stock) || 0 }
    if (isEdit.value) {
      await updateProduct(productId.value, data)
      uni.showToast({ title: '修改成功', icon: 'success' })
    } else {
      await addProduct(data)
      uni.showToast({ title: '添加成功', icon: 'success' })
    }
    setTimeout(() => uni.navigateBack(), 800)
  } catch (e) {}
}

const goBack = () => uni.navigateBack()

onMounted(async () => {
  try { categories.value = await getCategories() || [] } catch (e) {}

  const pages = getCurrentPages()
  const currentPage = pages[pages.length - 1]
  const id = currentPage.options?.id || currentPage.$page?.options?.id
  if (id) {
    isEdit.value = true
    productId.value = id
    try {
      const res = await getProducts({ page: 1, size: 200 })
      const list = res?.records || res || []
      const p = list.find(item => String(item.id) === String(id))
      if (p) {
        form.value = { name: p.name, categoryId: p.categoryId, price: String(p.price), stock: String(p.stock), description: p.description || '', status: p.status, image: p.image || '' }
      }
    } catch (e) {}
  }
})
</script>

<style scoped>
.edit-page { min-height: 100vh; background: #f5f5f5; display: flex; flex-direction: column; }

.edit-header {
  background: #fff; padding: 80rpx 30rpx 20rpx;
  display: flex; justify-content: space-between; align-items: center;
  border-bottom: 2rpx solid #f0f0f0;
}
.back-btn {
  width: 60rpx; height: 60rpx; display: flex;
  align-items: center; justify-content: center;
}

.edit-body { flex: 1; padding: 20rpx 30rpx 200rpx; }

.section { background: #fff; border-radius: 20rpx; padding: 24rpx; margin-bottom: 20rpx; }
.section-title { font-size: 28rpx; margin-bottom: 20rpx; display: block; }

.img-upload {
  width: 200rpx; height: 200rpx; background: #f8f8f8; border-radius: 16rpx;
  display: flex; flex-direction: column; align-items: center; justify-content: center;
  gap: 8rpx; border: 2rpx dashed #ddd;
}

.field { padding: 20rpx 0; border-bottom: 2rpx solid #f8f8f8; }
.field:last-child { border-bottom: none; }
.field-label { font-size: 26rpx; color: #666; display: block; margin-bottom: 12rpx; }
.field-input { font-size: 30rpx; width: 100%; }
.field-picker { font-size: 30rpx; padding: 8rpx 0; }
.field-textarea {
  width: 100%; height: 160rpx; font-size: 28rpx;
  background: #f8f8f8; border-radius: 12rpx; padding: 16rpx; box-sizing: border-box;
}

.toggle-row { display: flex; justify-content: space-between; align-items: center; }

.submit-area {
  position: fixed; bottom: 0; left: 0; right: 0;
  background: #fff; padding: 20rpx 30rpx; padding-bottom: calc(20rpx + env(safe-area-inset-bottom));
  box-shadow: 0 -4rpx 20rpx rgba(0,0,0,0.06);
}
</style>
