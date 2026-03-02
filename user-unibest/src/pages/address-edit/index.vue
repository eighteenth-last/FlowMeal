<template>
  <view class="edit-page">
    <view class="card">
      <view class="form-item">
        <text class="form-label">收货人</text>
        <input v-model="form.receiver" placeholder="请输入姓名" class="form-input" />
      </view>
      <view class="form-item">
        <text class="form-label">手机号</text>
        <input v-model="form.phone" placeholder="请输入手机号" type="number" class="form-input" />
      </view>
      <view class="form-item">
        <text class="form-label">省</text>
        <input v-model="form.province" placeholder="省" class="form-input" />
      </view>
      <view class="form-item">
        <text class="form-label">市</text>
        <input v-model="form.city" placeholder="市" class="form-input" />
      </view>
      <view class="form-item">
        <text class="form-label">区/县</text>
        <input v-model="form.district" placeholder="区/县" class="form-input" />
      </view>
      <view class="form-item">
        <text class="form-label">详细地址</text>
        <input v-model="form.detail" placeholder="楼栋门牌号等" class="form-input" />
      </view>
      <view class="form-item" @click="form.isDefault = form.isDefault === 1 ? 0 : 1">
        <text class="form-label">设为默认</text>
        <text :class="'fa ' + (form.isDefault === 1 ? 'fa-toggle-on text-primary' : 'fa-toggle-off text-gray')" style="font-size:48rpx;"></text>
      </view>
    </view>

    <view class="btn-primary save-btn" @click="handleSave">保 存</view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { addAddress, updateAddress, getAddresses } from '@/api/user'

const isEdit = ref(false)
const addrId = ref(null)
const form = ref({
  receiver: '', phone: '', province: '', city: '', district: '', detail: '', isDefault: 0
})

const handleSave = async () => {
  if (!form.value.receiver || !form.value.phone || !form.value.detail) {
    return uni.showToast({ title: '请填写完整', icon: 'none' })
  }
  try {
    if (isEdit.value) {
      await updateAddress(addrId.value, form.value)
    } else {
      await addAddress(form.value)
    }
    uni.showToast({ title: '保存成功', icon: 'success' })
    setTimeout(() => uni.navigateBack(), 800)
  } catch (e) {}
}

onMounted(async () => {
  const pages = getCurrentPages()
  const currentPage = pages[pages.length - 1]
  const id = currentPage.options?.id
  if (id) {
    isEdit.value = true
    addrId.value = Number(id)
    try {
      const list = await getAddresses()
      const addr = (list || []).find(a => a.id == id)
      if (addr) form.value = { ...addr }
    } catch (e) {}
  }
})
</script>

<style scoped>
.edit-page { min-height: 100vh; background: #f8f9fa; padding: 20rpx 30rpx; }
.form-item {
  display: flex; align-items: center; padding: 28rpx 0;
  border-bottom: 2rpx solid #f8f8f8;
}
.form-item:last-child { border-bottom: none; }
.form-label { width: 160rpx; font-size: 28rpx; color: #666; flex-shrink: 0; }
.form-input { flex: 1; font-size: 28rpx; }
.save-btn { margin-top: 60rpx; }
</style>
