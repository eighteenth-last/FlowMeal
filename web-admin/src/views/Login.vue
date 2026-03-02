<template>
  <div class="login-page">
    <div class="login-card">
      <div class="login-header">
        <div class="brand">
          <span class="brand-icon">🍜</span>
          <span class="brand-name">FlowMeal</span>
        </div>
        <p class="login-sub">管理员后台</p>
      </div>

      <n-form ref="formRef" :model="form" :rules="rules" size="large">
        <n-form-item path="username" label="账号">
          <n-input v-model:value="form.username" placeholder="请输入管理员账号" clearable />
        </n-form-item>
        <n-form-item path="password" label="密码">
          <n-input
            v-model:value="form.password"
            type="password"
            placeholder="请输入密码"
            show-password-on="click"
            @keyup.enter="handleLogin"
          />
        </n-form-item>
      </n-form>

      <n-button
        type="primary"
        block
        size="large"
        :loading="loading"
        @click="handleLogin"
        style="margin-top:8px"
      >
        登 录
      </n-button>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useMessage } from 'naive-ui'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const message = useMessage()
const authStore = useAuthStore()

const formRef = ref(null)
const loading = ref(false)
const form = ref({ username: '', password: '' })

const rules = {
  username: [{ required: true, message: '请输入账号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const handleLogin = () => {
  formRef.value?.validate(async (errors) => {
    if (errors) return
    loading.value = true
    try {
      await authStore.adminLogin(form.value)
      message.success('登录成功')
      router.push({ name: 'Dashboard' })
    } catch (e) {
      message.error(e.message || '登录失败')
    } finally {
      loading.value = false
    }
  })
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #18181c 0%, #2a2a2e 100%);
}
.login-card {
  width: 400px;
  background: #fff;
  border-radius: 16px;
  padding: 40px;
  box-shadow: 0 20px 60px rgba(0,0,0,0.3);
}
.login-header { text-align: center; margin-bottom: 32px; }
.brand { display: flex; align-items: center; justify-content: center; gap: 10px; }
.brand-icon { font-size: 32px; }
.brand-name { font-size: 26px; font-weight: 800; color: #18181c; }
.login-sub { margin: 8px 0 0; color: #999; font-size: 14px; }
</style>
