<template>
  <div class="login-container">
    <el-card class="login-card" shadow="hover">
      <template #header>
        <div class="login-header">
          <h2>智能体安全网关</h2>
          <p>请登录您的账户</p>
        </div>
      </template>
      <el-form ref="formRef" :model="loginForm" :rules="rules" label-width="80px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="loginForm.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="loginForm.password" type="password" placeholder="请输入密码" show-password />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleLogin" :loading="loading" class="login-btn">登录</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { login } from '../api/login'

const router = useRouter()
const formRef = ref(null)
const loginForm = reactive({
  username: '',
  password: ''
})

const loading = ref(false)

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' }
  ]
}

const handleLogin = async () => {
  loading.value = true
  try {
    // 调用登录API
    const response = await login(loginForm)
    
    // 保存token和用户信息到localStorage
    localStorage.setItem('token', response.data.token)
    localStorage.setItem('userInfo', JSON.stringify(response.data.userInfo))
    
    // 跳转到首页
    router.push('/home')
    ElMessage.success('登录成功')
  } catch (error) {
    // 处理登录失败
    ElMessage.error(error.response?.data?.message || '登录失败，请重试')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-container {
  width: 100%;
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.login-card {
  width: 400px;
  border-radius: 10px;
}

.login-header {
  text-align: center;
}

.login-header h2 {
  margin-bottom: 10px;
  color: #333;
}

.login-header p {
  color: #666;
  margin: 0;
}

.login-btn {
  width: 100%;
}
</style>