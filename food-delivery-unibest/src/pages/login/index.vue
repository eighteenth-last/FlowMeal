<template>
  <view class="login-page">
    <!-- 顶部深色装饰 -->
    <view class="login-header">
      <view class="logo-wrap">
        <text class="fa fa-motorcycle logo-icon"></text>
      </view>
      <text class="app-name">FlowMeal 骑手</text>
      <text class="app-desc">配送接单，高效赚钱</text>
    </view>

    <!-- 登录表单 -->
    <view class="login-form">
      <view class="form-item">
        <text class="fa fa-user form-icon"></text>
        <input v-model="form.username" placeholder="请输入账号" class="form-input" />
      </view>
      <view class="form-item">
        <text class="fa fa-lock form-icon"></text>
        <input v-model="form.password" type="password" placeholder="请输入密码" class="form-input" />
      </view>

      <view class="btn-dark login-btn" @click="handleLogin">登 录</view>

      <view class="register-link" @click="showRegister = true">
        <text>还没有账号？</text>
        <text style="color:#FFD100;font-weight:bold;">申请入驻</text>
      </view>
    </view>

    <!-- 注册弹窗 -->
    <view class="mask" v-if="showRegister" @click="showRegister = false">
      <view class="register-modal" @click.stop>
        <text class="modal-title">骑手注册</text>
        <view class="form-item">
          <text class="fa fa-user form-icon"></text>
          <input v-model="regForm.username" placeholder="登录账号" class="form-input" />
        </view>
        <view class="form-item">
          <text class="fa fa-lock form-icon"></text>
          <input v-model="regForm.password" type="password" placeholder="密码" class="form-input" />
        </view>
        <view class="form-item">
          <text class="fa fa-id-card form-icon"></text>
          <input v-model="regForm.realName" placeholder="真实姓名" class="form-input" />
        </view>
        <view class="form-item">
          <text class="fa fa-phone form-icon"></text>
          <input v-model="regForm.phone" placeholder="手机号" class="form-input" />
        </view>
        <view class="form-item">
          <text class="fa fa-address-card form-icon"></text>
          <input v-model="regForm.idCard" placeholder="身份证号（选填）" class="form-input" />
        </view>
        <view class="btn-dark login-btn" @click="handleRegister">提交申请</view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { riderLogin, riderRegister } from '@/api/auth'
import { useAuthStore } from '@/stores/auth'

const authStore = useAuthStore()
const showRegister = ref(false)

const form = ref({ username: '', password: '' })
const regForm = ref({ username: '', password: '', realName: '', phone: '', idCard: '' })

const handleLogin = async () => {
  if (!form.value.username || !form.value.password) {
    return uni.showToast({ title: '请填写完整', icon: 'none' })
  }
  try {
    const res = await riderLogin(form.value)
    authStore.setAuth(res)
    uni.switchTab({ url: '/pages/hall/index' })
  } catch (e) {}
}

const handleRegister = async () => {
  if (!regForm.value.username || !regForm.value.password || !regForm.value.realName || !regForm.value.phone) {
    return uni.showToast({ title: '请填写完整', icon: 'none' })
  }
  try {
    await riderRegister(regForm.value)
    uni.showToast({ title: '提交成功，等待审核', icon: 'success' })
    showRegister.value = false
  } catch (e) {}
}
</script>

<style scoped>
.login-page { min-height: 100vh; background: #f5f5f5; }
.login-header {
  background: #1a1a1a;
  padding: 120rpx 60rpx 80rpx;
  border-radius: 0 0 80rpx 80rpx;
  display: flex; flex-direction: column; align-items: center;
}
.logo-wrap {
  width: 120rpx; height: 120rpx; background: #FFD100; border-radius: 30rpx;
  display: flex; align-items: center; justify-content: center;
  margin-bottom: 24rpx; box-shadow: 0 8rpx 30rpx rgba(255,209,0,0.3);
}
.logo-icon { font-size: 56rpx; color: #1a1a1a; }
.app-name { font-size: 48rpx; font-weight: bold; color: #fff; }
.app-desc { font-size: 26rpx; color: rgba(255,255,255,0.5); margin-top: 8rpx; }

.login-form { padding: 60rpx 50rpx; }
.form-item {
  display: flex; align-items: center; background: #fff; border-radius: 16rpx;
  padding: 0 30rpx; height: 100rpx; margin-bottom: 24rpx; border: 2rpx solid #f0f0f0;
}
.form-icon { font-size: 32rpx; color: #ccc; margin-right: 20rpx; width: 40rpx; text-align: center; }
.form-input { flex: 1; font-size: 30rpx; }
.login-btn { margin-top: 40rpx; }
.register-link { text-align: center; margin-top: 40rpx; font-size: 26rpx; color: #999; }

.mask {
  position: fixed; top: 0; left: 0; right: 0; bottom: 0;
  background: rgba(0,0,0,0.5); display: flex; align-items: center; justify-content: center; z-index: 999;
}
.register-modal { width: 85%; background: #fff; border-radius: 24rpx; padding: 50rpx 40rpx; }
.modal-title { font-size: 36rpx; font-weight: bold; text-align: center; margin-bottom: 40rpx; display: block; }
</style>
