<template>
  <view class="login-page">
    <!-- 顶部装饰 -->
    <view class="login-header">
      <view class="logo-wrap">
        <image src="/static/bowl-food.png" class="logo-icon" mode="aspectFit" />
      </view>
      <text class="app-name">FlowMeal</text>
      <text class="app-desc">智慧订餐 · 美味到家</text>
    </view>

    <!-- 登录表单 -->
    <view class="login-form">
      <view class="form-item">
        <image src="/static/User.png" class="form-icon" mode="aspectFit" />
        <input v-model="form.username" placeholder="请输入用户名" class="form-input" />
      </view>
      <view class="form-item">
        <image src="/static/lock.png" class="form-icon" mode="aspectFit" />
        <input v-model="form.password" type="password" placeholder="请输入密码" class="form-input" />
      </view>

      <view class="btn-primary login-btn" @click="handleLogin">登 录</view>

      <view class="register-link" @click="showRegister = true">
        <text>还没有账号？</text>
        <text class="text-primary">立即注册</text>
      </view>
    </view>

    <!-- 注册弹窗 -->
    <view class="mask" v-if="showRegister" @click="showRegister = false">
      <view class="register-modal" @click.stop>
        <text class="modal-title">用户注册</text>
        <view class="form-item">
          <image src="/static/User.png" class="form-icon" mode="aspectFit" />
          <input v-model="regForm.username" placeholder="用户名" class="form-input" />
        </view>
        <view class="form-item">
          <image src="/static/phone.png" class="form-icon" mode="aspectFit" />
          <input v-model="regForm.phone" placeholder="手机号" class="form-input" />
        </view>
        <view class="form-item">
          <image src="/static/lock.png" class="form-icon" mode="aspectFit" />
          <input v-model="regForm.password" type="password" placeholder="密码" class="form-input" />
        </view>
        <view class="form-item">
          <image src="/static/Smile.png" class="form-icon" mode="aspectFit" />
          <input v-model="regForm.nickname" placeholder="昵称（选填）" class="form-input" />
        </view>
        <view class="btn-primary login-btn" @click="handleRegister">注 册</view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { userLogin, userRegister } from '@/api/auth'
import { useAuthStore } from '@/stores/auth'

const authStore = useAuthStore()
const showRegister = ref(false)

const form = ref({ username: '', password: '' })
const regForm = ref({ username: '', phone: '', password: '', nickname: '' })

const handleLogin = async () => {
  if (!form.value.username || !form.value.password) {
    return uni.showToast({ title: '请填写完整', icon: 'none' })
  }
  try {
    const res = await userLogin(form.value)
    authStore.setAuth(res)
    uni.switchTab({ url: '/pages/home/index' })
  } catch (e) {}
}

const handleRegister = async () => {
  if (!regForm.value.username || !regForm.value.phone || !regForm.value.password) {
    return uni.showToast({ title: '请填写完整', icon: 'none' })
  }
  try {
    await userRegister(regForm.value)
    uni.showToast({ title: '注册成功', icon: 'success' })
    showRegister.value = false
  } catch (e) {}
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  background: #f8f9fa;
}
.login-header {
  background: linear-gradient(135deg, #FFD100 0%, #FFE550 100%);
  padding: 120rpx 60rpx 80rpx;
  border-radius: 0 0 80rpx 80rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
}
.logo-wrap {
  width: 120rpx;
  height: 120rpx;
  background: #fff;
  border-radius: 30rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 24rpx;
  box-shadow: 0 8rpx 30rpx rgba(0,0,0,0.1);
}
.logo-icon { width: 56rpx; height: 56rpx; }
.app-name { font-size: 48rpx; font-weight: bold; color: #1a1a1a; }
.app-desc { font-size: 26rpx; color: rgba(0,0,0,0.5); margin-top: 8rpx; }

.login-form { padding: 60rpx 50rpx; }
.form-item {
  display: flex;
  align-items: center;
  background: #fff;
  border-radius: 16rpx;
  padding: 0 30rpx;
  height: 100rpx;
  margin-bottom: 24rpx;
  border: 2rpx solid #f0f0f0;
}
.form-icon { width: 32rpx; height: 32rpx; margin-right: 20rpx; flex-shrink: 0; }
.form-input { flex: 1; font-size: 30rpx; }
.login-btn { margin-top: 40rpx; }
.register-link {
  text-align: center;
  margin-top: 40rpx;
  font-size: 26rpx;
  color: #999;
}

.mask {
  position: fixed;
  top: 0; left: 0; right: 0; bottom: 0;
  background: rgba(0,0,0,0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 999;
}
.register-modal {
  width: 85%;
  background: #fff;
  border-radius: 24rpx;
  padding: 50rpx 40rpx;
}
.modal-title {
  font-size: 36rpx;
  font-weight: bold;
  text-align: center;
  margin-bottom: 40rpx;
  display: block;
}
</style>
