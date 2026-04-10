<template>
  <div class="login-container">
    <div class="login-box">
      <h1 class="login-title">OAuth2 SSO 系统</h1>
      <p class="login-subtitle">企业级单点登录系统</p>
      
      <el-form
        ref="formRef"
        :model="loginForm"
        :rules="rules"
        class="login-form"
        @keyup.enter="handleLogin"
      >
        <el-form-item prop="username">
          <el-input
            v-model="loginForm.username"
            placeholder="请输入用户名"
            size="large"
            prefix-icon="User"
          />
        </el-form-item>
        
        <el-form-item prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="请输入密码"
            size="large"
            prefix-icon="Lock"
            show-password
          />
        </el-form-item>
        
        <el-form-item>
          <el-button
            type="primary"
            size="large"
            :loading="loading"
            class="login-btn"
            @click="handleLogin"
          >
            登录
          </el-button>
        </el-form-item>
      </el-form>
      
      <div class="login-tips">
        <p>测试账号：</p>
        <p>管理员：admin / 123456</p>
        <p>普通用户：user / 123456</p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const authStore = useAuthStore()
const formRef = ref<FormInstance>()
const loading = ref(false)

const loginForm = reactive({
  username: '',
  password: ''
})

const rules: FormRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' }
  ]
}

const handleLogin = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    
    loading.value = true
    try {
      // 调用简单登录接口
      const response = await fetch('/api/login', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({
          username: loginForm.username,
          password: loginForm.password
        })
      })
      
      if (!response.ok) {
        const error = await response.json()
        throw new Error(error.message || '登录失败')
      }
      
      const tokenData = await response.json()
      console.log('登录响应:', tokenData)

      // 后端返回的字段是 accessToken（驼峰），不是 access_token
      const token = tokenData.accessToken || tokenData.access_token
      const expiresIn = tokenData.expiresIn || tokenData.expires_in || 7200
      const tokenType = tokenData.tokenType || tokenData.token_type || 'Bearer'

      if (!token) {
        console.error('登录响应缺少 token:', tokenData)
        throw new Error('登录响应格式错误')
      }

      authStore.setToken(token, tokenType, expiresIn)

      // 验证 Token 是否保存成功
      console.log('Token 已保存:', authStore.accessToken)

      // 获取用户信息
      const userInfoResponse = await fetch('/api/users/me', {
        headers: {
          'Authorization': `Bearer ${authStore.accessToken}`
        }
      })
      
      console.log('用户信息响应状态:', userInfoResponse.status)

      if (!userInfoResponse.ok) {
        const errText = await userInfoResponse.text()
        console.error('用户信息响应:', errText)
        // 如果获取用户信息失败，使用登录用户名作为临时用户信息
        authStore.setUser({
          username: loginForm.username,
          nickname: loginForm.username,
          roles: [],
          authorities: []
        })
        ElMessage.warning('获取用户信息失败，使用临时用户信息')
        ElMessage.success('登录成功')
        router.push('/')
        return
      }

      const userInfo = await userInfoResponse.json()
      console.log('用户信息:', userInfo)
      // 处理 Result<T> 格式
      if (userInfo.code === 200 || userInfo.code === 0) {
        authStore.setUser(userInfo.data || userInfo)
      } else if (userInfo.username) {
        // 直接返回用户对象
        authStore.setUser(userInfo)
      } else {
        throw new Error(userInfo.message || '获取用户信息失败')
      }
      
      ElMessage.success('登录成功')
      router.push('/')
    } catch (error: any) {
      console.error('登录错误:', error)
      ElMessage.error(error.message || '登录失败，请检查用户名和密码')
    } finally {
      loading.value = false
    }
  })
}
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.login-box {
  width: 400px;
  padding: 40px;
  background: #fff;
  border-radius: 10px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.1);
}

.login-title {
  text-align: center;
  color: #333;
  margin-bottom: 10px;
  font-size: 28px;
}

.login-subtitle {
  text-align: center;
  color: #666;
  margin-bottom: 30px;
  font-size: 14px;
}

.login-form {
  margin-top: 20px;
}

.login-btn {
  width: 100%;
}

.login-tips {
  margin-top: 20px;
  padding: 15px;
  background: #f5f7fa;
  border-radius: 5px;
  font-size: 13px;
  color: #666;
}

.login-tips p {
  margin: 5px 0;
}
</style>
