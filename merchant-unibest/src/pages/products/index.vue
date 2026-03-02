<template>
  <view class="products-page">
    <!-- Header -->
    <view class="page-header">
      <text class="header-title font-bold">餐品管理</text>
      <view class="add-btn" @click="goAdd">
        <text class="fa fa-plus" style="font-size:22rpx;"></text>
        <text>添加餐品</text>
      </view>
    </view>

    <!-- 搜索 -->
    <view class="search-bar">
      <text class="fa fa-magnifying-glass" style="color:#ccc;font-size:26rpx;"></text>
      <input v-model="keyword" placeholder="搜索餐品名称" class="search-input" @confirm="loadProducts" />
    </view>

    <!-- 布局：分类侧栏 + 商品列表 -->
    <view class="content-wrap">
      <!-- 分类侧栏 -->
      <scroll-view scroll-y class="category-sidebar">
        <view
          v-for="cat in categories" :key="cat.id"
          :class="['cat-item', activeCat === cat.id ? 'cat-active' : '']"
          @click="selectCat(cat)"
        >
          <text>{{ cat.name }}</text>
          <text class="cat-count text-gray">{{ cat.productCount || 0 }}</text>
        </view>
        <view class="cat-item cat-add" @click="showAddCat = true">
          <text class="fa fa-plus" style="font-size:20rpx;color:#999;"></text>
          <text class="text-gray" style="font-size:22rpx;">添加分类</text>
        </view>
      </scroll-view>

      <!-- 餐品列表 -->
      <scroll-view scroll-y class="product-list">
        <view class="product-card" v-for="p in filteredProducts" :key="p.id">
          <view class="product-img">
            <text class="fa fa-image" style="font-size:40rpx;color:#ddd;"></text>
          </view>
          <view class="product-info">
            <text class="product-name font-bold">{{ p.name }}</text>
            <text class="product-desc text-gray">{{ p.description || '暂无描述' }}</text>
            <view class="product-bottom">
              <text class="product-price">¥{{ p.price }}</text>
              <text class="product-stock text-gray">库存 {{ p.stock }}</text>
            </view>
          </view>
          <view class="product-actions">
            <view class="p-action" @click="toggleStatus(p)">
              <text :class="'fa ' + (p.status === 1 ? 'fa-eye' : 'fa-eye-slash')"
                :style="{ color: p.status === 1 ? '#22c55e' : '#999', fontSize: '28rpx' }"></text>
            </view>
            <view class="p-action" @click="goEdit(p)">
              <text class="fa fa-pen" style="color:#3b82f6;font-size:24rpx;"></text>
            </view>
            <view class="p-action" @click="handleDelete(p)">
              <text class="fa fa-trash" style="color:#ef4444;font-size:24rpx;"></text>
            </view>
          </view>
        </view>

        <view v-if="filteredProducts.length === 0" class="empty-tip">
          <text class="fa fa-box-open" style="font-size:64rpx;color:#ddd;"></text>
          <text class="text-gray">暂无餐品</text>
        </view>
      </scroll-view>
    </view>

    <!-- 添加分类弹窗 -->
    <view class="mask" v-if="showAddCat" @click="showAddCat = false">
      <view class="cat-modal" @click.stop>
        <text class="modal-title">添加分类</text>
        <view class="form-item" style="margin-bottom:30rpx;">
          <input v-model="newCatName" placeholder="分类名称" class="form-input" />
        </view>
        <view class="btn-dark" style="padding:20rpx 0;" @click="handleAddCat">确 定</view>
      </view>
    </view>

    <CustomTabBar :current="1" />
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { getProducts, deleteProduct, updateProduct } from '@/api/product'
import { getCategories, addCategory } from '@/api/product'
import CustomTabBar from '@/components/CustomTabBar.vue'

const keyword = ref('')
const categories = ref([])
const products = ref([])
const activeCat = ref(null)
const showAddCat = ref(false)
const newCatName = ref('')

const filteredProducts = computed(() => {
  let list = products.value
  if (activeCat.value) list = list.filter(p => p.categoryId === activeCat.value)
  if (keyword.value) list = list.filter(p => p.name?.includes(keyword.value))
  return list
})

const selectCat = (cat) => { activeCat.value = activeCat.value === cat.id ? null : cat.id }

const loadCategories = async () => {
  try { categories.value = await getCategories() || [] } catch (e) {}
}

const loadProducts = async () => {
  try {
    const res = await getProducts({ page: 1, size: 200 })
    products.value = res?.records || res || []
  } catch (e) {}
}

const toggleStatus = async (p) => {
  try {
    await updateProduct(p.id, { ...p, status: p.status === 1 ? 0 : 1 })
    uni.showToast({ title: p.status === 1 ? '已下架' : '已上架', icon: 'none' })
    loadProducts()
  } catch (e) {}
}

const handleDelete = (p) => {
  uni.showModal({
    title: '确认删除',
    content: `删除餐品 "${p.name}"？`,
    success: async (res) => {
      if (res.confirm) {
        try {
          await deleteProduct(p.id)
          uni.showToast({ title: '已删除', icon: 'success' })
          loadProducts()
        } catch (e) {}
      }
    }
  })
}

const handleAddCat = async () => {
  if (!newCatName.value.trim()) return uni.showToast({ title: '请输入分类名称', icon: 'none' })
  try {
    await addCategory({ name: newCatName.value.trim() })
    uni.showToast({ title: '添加成功', icon: 'success' })
    showAddCat.value = false
    newCatName.value = ''
    loadCategories()
  } catch (e) {}
}

const goAdd = () => uni.navigateTo({ url: '/pages/product-edit/index' })
const goEdit = (p) => uni.navigateTo({ url: `/pages/product-edit/index?id=${p.id}` })

onMounted(() => {
  const token = uni.getStorageSync('fm_merchant_token')
  if (!token) { uni.reLaunch({ url: '/pages/login/index' }); return }
  loadCategories()
  loadProducts()
})
</script>

<style scoped>
.products-page { min-height: 100vh; background: #f5f5f5; display: flex; flex-direction: column; padding-bottom: 120rpx; }

.page-header {
  background: #fff; padding: 80rpx 30rpx 20rpx;
  display: flex; justify-content: space-between; align-items: center;
  border-bottom: 2rpx solid #f0f0f0;
}
.header-title { font-size: 36rpx; }
.add-btn {
  background: #FFD100; color: #1a1a1a; padding: 12rpx 24rpx;
  border-radius: 10rpx; font-size: 24rpx; font-weight: bold;
  display: flex; align-items: center; gap: 8rpx;
}

.search-bar {
  display: flex; align-items: center; gap: 12rpx; margin: 16rpx 30rpx;
  background: #fff; border-radius: 12rpx; padding: 16rpx 24rpx;
}
.search-input { flex: 1; font-size: 28rpx; }

.content-wrap { flex: 1; display: flex; overflow: hidden; }
.category-sidebar {
  width: 180rpx; background: #f0f0f0; height: calc(100vh - 300rpx);
}
.cat-item {
  padding: 24rpx 16rpx; font-size: 24rpx; text-align: center;
  border-left: 6rpx solid transparent;
  display: flex; flex-direction: column; align-items: center; gap: 4rpx;
}
.cat-active {
  background: #fff; border-left-color: #FFD100; font-weight: bold;
}
.cat-count { font-size: 20rpx; }
.cat-add { border-top: 2rpx solid #e8e8e8; }

.product-list {
  flex: 1; background: #fff; padding: 16rpx;
  height: calc(100vh - 300rpx);
}

.product-card {
  display: flex; gap: 16rpx; padding: 20rpx 0;
  border-bottom: 2rpx solid #f8f8f8; align-items: center;
}
.product-img {
  width: 120rpx; height: 120rpx; background: #f8f8f8; border-radius: 16rpx;
  display: flex; align-items: center; justify-content: center; flex-shrink: 0;
}
.product-info { flex: 1; display: flex; flex-direction: column; gap: 6rpx; overflow: hidden; }
.product-name { font-size: 28rpx; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.product-desc { font-size: 22rpx; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.product-bottom { display: flex; align-items: center; gap: 16rpx; }
.product-price { font-size: 28rpx; color: #ef4444; font-weight: bold; }
.product-stock { font-size: 22rpx; }
.product-actions { display: flex; flex-direction: column; gap: 12rpx; flex-shrink: 0; }
.p-action {
  width: 56rpx; height: 56rpx; background: #f8f8f8; border-radius: 12rpx;
  display: flex; align-items: center; justify-content: center;
}

.empty-tip { text-align: center; padding: 120rpx 0; display: flex; flex-direction: column; align-items: center; gap: 20rpx; }

.mask {
  position: fixed; top: 0; left: 0; right: 0; bottom: 0;
  background: rgba(0,0,0,0.5); display: flex; align-items: center; justify-content: center; z-index: 999;
}
.cat-modal { width: 80%; background: #fff; border-radius: 24rpx; padding: 40rpx; }
.modal-title { font-size: 34rpx; font-weight: bold; text-align: center; margin-bottom: 30rpx; display: block; }
.form-item {
  display: flex; align-items: center; background: #f5f5f5; border-radius: 12rpx;
  padding: 0 24rpx; height: 90rpx;
}
.form-input { flex: 1; font-size: 28rpx; }
</style>
