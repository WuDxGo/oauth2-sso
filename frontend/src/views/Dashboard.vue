<template>
  <div class="dashboard">
    <el-row :gutter="20">
      <el-col :span="8">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon user">
              <el-icon :size="40"><User /></el-icon>
            </div>
            <div class="stat-info">
              <p class="stat-label">用户数量</p>
              <p class="stat-value">{{ stats.userCount }}</p>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="8">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon order">
              <el-icon :size="40"><Document /></el-icon>
            </div>
            <div class="stat-info">
              <p class="stat-label">订单数量</p>
              <p class="stat-value">{{ stats.orderCount }}</p>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="8">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon amount">
              <el-icon :size="40"><Money /></el-icon>
            </div>
            <div class="stat-info">
              <p class="stat-label">订单总额</p>
              <p class="stat-value">¥{{ stats.totalAmount }}</p>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
    
    <el-card class="welcome-card" style="margin-top: 20px;">
      <template #header>
        <div class="card-header">
          <span>欢迎使用</span>
        </div>
      </template>
      <div class="welcome-content">
        <h2>欢迎，{{ authStore.nickname || authStore.username }}！</h2>
        <p>您已成功登录 OAuth2 SSO 系统</p>
        <el-divider />
        <div class="user-info">
          <p><strong>用户名：</strong>{{ authStore.username }}</p>
          <p><strong>角色：</strong>{{ authStore.roles.join(', ') }}</p>
          <p><strong>权限：</strong>{{ authStore.user?.authorities?.join(', ') || '无' }}</p>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { getUserList } from '@/api/auth'
import { getOrderList } from '@/api/order'

const authStore = useAuthStore()

const stats = ref({
  userCount: 0,
  orderCount: 0,
  totalAmount: 0
})

onMounted(async () => {
  try {
    const [users, orders] = await Promise.all([
      getUserList(),
      getOrderList()
    ])
    stats.value.userCount = users.length
    stats.value.orderCount = orders.length
    stats.value.totalAmount = orders.reduce((sum, order) => sum + order.amount, 0).toFixed(2)
  } catch (error) {
    console.error('获取统计数据失败:', error)
  }
})
</script>

<style scoped>
.dashboard {
  padding: 20px;
}

.stat-card {
  margin-bottom: 20px;
}

.stat-content {
  display: flex;
  align-items: center;
}

.stat-icon {
  width: 80px;
  height: 80px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  margin-right: 20px;
}

.stat-icon.user {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.stat-icon.order {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
}

.stat-icon.amount {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
}

.stat-info {
  flex: 1;
}

.stat-label {
  color: #666;
  font-size: 14px;
  margin-bottom: 5px;
}

.stat-value {
  color: #333;
  font-size: 24px;
  font-weight: bold;
  margin: 0;
}

.welcome-card {
  margin-top: 20px;
}

.welcome-content h2 {
  margin-bottom: 10px;
  color: #333;
}

.welcome-content p {
  color: #666;
  margin-bottom: 15px;
}

.user-info p {
  margin: 10px 0;
  color: #666;
}
</style>
