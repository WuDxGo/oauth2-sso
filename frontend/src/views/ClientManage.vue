<template>
  <div class="client-manage">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>OAuth2 客户端管理</span>
          <el-button type="primary" @click="handleAdd">
            <el-icon><Plus /></el-icon>
            新增客户端
          </el-button>
        </div>
      </template>

      <el-table :data="clientList" v-loading="loading" style="width: 100%">
        <el-table-column prop="clientId" label="客户端 ID" width="150" />
        <el-table-column prop="clientName" label="客户端名称" width="150" />
        <el-table-column label="授权模式" width="200">
          <template #default="{ row }">
            <el-tag
              v-for="type in row.authorizationGrantTypes"
              :key="type"
              size="small"
              style="margin-right: 5px; margin-bottom: 5px;"
            >
              {{ getGrantTypeLabel(type) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="授权范围" width="200">
          <template #default="{ row }">
            <el-tag
              v-for="scope in row.scopes"
              :key="scope"
              size="small"
              type="info"
              style="margin-right: 5px; margin-bottom: 5px;"
            >
              {{ scope }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="handleEdit(row)">
              编辑
            </el-button>
            <el-button
              type="danger"
              size="small"
              @click="handleDelete(row)"
              :disabled="row.clientId === 'gateway-client'"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 新增/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="900px"
      @close="handleDialogClose"
      class="client-dialog"
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="rules"
        label-width="160px"
        label-position="left"
      >
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="客户端 ID" prop="clientId">
              <el-input
                v-model="formData.clientId"
                :disabled="!!isEdit"
                placeholder="如：order-service"
                size="large"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="客户端名称" prop="clientName">
              <el-input
                v-model="formData.clientName"
                placeholder="如：订单服务"
                size="large"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="客户端密钥" prop="clientSecret">
              <el-input
                v-model="formData.clientSecret"
                type="password"
                show-password
                :placeholder="isEdit ? '不修改请留空' : '请输入密钥'"
                size="large"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="需要授权同意" prop="requireConsent">
              <el-switch
                v-model="formData.requireConsent"
                active-text="开启"
                inactive-text="关闭"
                style="margin-top: 10px;"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-divider content-position="left">认证配置</el-divider>

        <el-form-item label="认证方式" prop="clientAuthenticationMethods">
          <el-checkbox-group v-model="formData.clientAuthenticationMethods" class="checkbox-group">
            <el-checkbox
              label="client_secret_basic"
              border
              size="large"
            >
              client_secret_basic
            </el-checkbox>
            <el-checkbox
              label="client_secret_post"
              border
              size="large"
            >
              client_secret_post
            </el-checkbox>
          </el-checkbox-group>
          <div class="form-tip">
            client_secret_basic: 通过 HTTP Basic Auth 传递密钥（推荐）
            <br>
            client_secret_post: 通过 POST 表单参数传递密钥
          </div>
        </el-form-item>

        <el-divider content-position="left">授权配置</el-divider>

        <el-form-item label="授权模式" prop="authorizationGrantTypes">
          <el-checkbox-group v-model="formData.authorizationGrantTypes" class="checkbox-group">
            <el-checkbox
              label="authorization_code"
              border
              size="large"
            >
              authorization_code
            </el-checkbox>
            <el-checkbox
              label="password"
              border
              size="large"
            >
              password
            </el-checkbox>
            <el-checkbox
              label="client_credentials"
              border
              size="large"
            >
              client_credentials
            </el-checkbox>
            <el-checkbox
              label="refresh_token"
              border
              size="large"
            >
              refresh_token
            </el-checkbox>
          </el-checkbox-group>
          <div class="form-tip">
            authorization_code: 授权码模式（Web 应用，最安全）
            <br>
            password: 密码模式（可信应用，如网关）
            <br>
            client_credentials: 客户端凭证模式（服务间调用）
            <br>
            refresh_token: 刷新 Token（通常与其他模式配合使用）
          </div>
        </el-form-item>

        <el-form-item label="重定向 URI" prop="redirectUris">
          <el-select
            v-model="formData.redirectUris"
            multiple
            allow-create
            default-first-option
            placeholder="请输入重定向 URI，按回车添加"
            size="large"
            style="width: 100%"
          >
          </el-select>
          <div class="form-tip">
            授权成功后跳转的地址，如：http://localhost:8081/login/oauth2/code/gateway-client
          </div>
        </el-form-item>

        <el-form-item label="授权范围" prop="scopes">
            <el-checkbox-group v-model="formData.scopes" class="checkbox-group">
              <el-checkbox
                label="openid"
                border
                size="large"
              >
                openid
              </el-checkbox>
              <el-checkbox
                label="profile"
                border
                size="large"
              >
                profile
              </el-checkbox>
              <el-checkbox
                label="read"
                border
                size="large"
              >
                read
              </el-checkbox>
              <el-checkbox
                label="write"
                border
                size="large"
              >
                write
              </el-checkbox>
            </el-checkbox-group>
            <div class="form-tip">
              openid: 获取用户身份信息（OIDC）
              <br>
              profile: 获取用户基本信息
              <br>
              read: 读取权限
              <br>
              write: 写入权限
              <br>
              --------------------------------------------------
            </div>
        </el-form-item>
      </el-form>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogVisible = false" size="large">取消</el-button>
          <el-button type="primary" @click="handleSubmit" :loading="submitting" size="large">
            确定
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import type { Client } from '@/api/client'
import { getClientList, createClient, updateClient, deleteClient } from '@/api/client'

const loading = ref(false)
const submitting = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('')
const isEdit = ref(false)
const formRef = ref<FormInstance>()

const clientList = ref<Client[]>([])
const formData = reactive<Partial<Client>>({
  clientId: '',
  clientName: '',
  clientSecret: '',
  clientAuthenticationMethods: ['client_secret_basic'],
  authorizationGrantTypes: [],
  redirectUris: [],
  scopes: ['read', 'write'],
  requireConsent: false
})

const rules: FormRules = {
  clientId: [
    { required: true, message: '请输入客户端 ID', trigger: 'blur' },
    { pattern: /^[a-zA-Z0-9_-]+$/, message: '只能包含字母、数字、下划线和短横线', trigger: 'blur' }
  ],
  clientName: [{ required: true, message: '请输入客户端名称', trigger: 'blur' }],
  clientSecret: [{ required: true, message: '请输入客户端密钥', trigger: 'blur' }]
}

// 授权模式中文映射
const grantTypeLabels: Record<string, string> = {
  authorization_code: '授权码模式',
  password: '密码模式',
  client_credentials: '客户端凭证',
  refresh_token: '刷新 Token'
}

const getGrantTypeLabel = (type: string) => {
  return grantTypeLabels[type] || type
}

const fetchClients = async () => {
  loading.value = true
  try {
    const res = await getClientList()
    clientList.value = res.data || []
  } catch (error: any) {
    ElMessage.error(error.message || '获取客户端列表失败')
  } finally {
    loading.value = false
  }
}

const handleAdd = () => {
  dialogTitle.value = '新增客户端'
  isEdit.value = false
  formData.clientId = ''
  formData.clientName = ''
  formData.clientSecret = ''
  formData.clientAuthenticationMethods = ['client_secret_basic']
  formData.authorizationGrantTypes = []
  formData.redirectUris = []
  formData.scopes = ['read', 'write']
  formData.requireConsent = false
  dialogVisible.value = true
}

const handleEdit = (row: Client) => {
  dialogTitle.value = '编辑客户端'
  isEdit.value = true
  Object.assign(formData, {
    ...row,
    clientSecret: '' // 编辑时不显示密钥
  })
  dialogVisible.value = true
}

const handleDelete = async (row: Client) => {
  try {
    await ElMessageBox.confirm(
      `确认删除客户端 "${row.clientName}" 吗？删除后该客户端将无法访问系统！`,
      '警告',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    await deleteClient(row.clientId)
    ElMessage.success('删除成功')
    fetchClients()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '删除失败')
    }
  }
}

const handleDialogClose = () => {
  formRef.value?.resetFields()
  isEdit.value = false
}

const handleSubmit = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (!valid) return

    submitting.value = true
    try {
      // 编辑时如果密钥为空则不传递
      const submitData: Partial<Client> = { ...formData }
      if (isEdit.value && !submitData.clientSecret) {
        delete submitData.clientSecret
      }

      if (isEdit.value && formData.clientId) {
        await updateClient(formData.clientId, submitData as Client)
        ElMessage.success('更新成功')
      } else {
        await createClient(submitData as Client)
        ElMessage.success('创建成功')
      }
      dialogVisible.value = false
      fetchClients()
    } catch (error: any) {
      ElMessage.error(error.message || '操作失败')
    } finally {
      submitting.value = false
    }
  })
}

onMounted(() => {
  fetchClients()
})
</script>

<style scoped>
.client-manage {
  padding: 24px;
  background-color: #f5f7fa;
  min-height: calc(100vh - 84px);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header span {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

/* 表格样式优化 */
:deep(.el-table) {
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
}

:deep(.el-table th) {
  background-color: #f5f7fa;
  color: #606266;
  font-weight: 600;
}

:deep(.el-table--striped .el-table__body tr.el-table__row--striped td) {
  background: #fafafa;
}

:deep(.el-table__row:hover) {
  background-color: #f5f7fa !important;
}

/* 对话框样式优化 */
.client-dialog :deep(.el-dialog__header) {
  padding: 20px 24px;
  border-bottom: 1px solid #ebeef5;
}

.client-dialog :deep(.el-dialog__title) {
  font-size: 20px;
  font-weight: 600;
  color: #303133;
}

.client-dialog :deep(.el-dialog__body) {
  padding: 24px;
}

.client-dialog :deep(.el-dialog__footer) {
  padding: 16px 24px;
  border-top: 1px solid #ebeef5;
}

/* 表单样式优化 */
:deep(.el-form-item) {
  margin-bottom: 24px;
}

:deep(.el-form-item__label) {
  font-weight: 500;
  color: #606266;
  font-size: 15px;
}

:deep(.el-input__wrapper) {
  border-radius: 6px;
}

:deep(.el-input.is-disabled .el-input__wrapper) {
  background-color: #f5f7fa;
}

/* 复选框组样式 */
.checkbox-group {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.checkbox-group :deep(.el-checkbox) {
  margin-right: 0;
  margin-bottom: 8px;
}

.checkbox-group :deep(.el-checkbox__label) {
  font-size: 14px;
}

/* 分割线样式 */
:deep(.el-divider) {
  margin: 24px 0;
  background-color: #e4e7ed;
}

:deep(.el-divider__text) {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
}

/* 提示信息样式 */
.form-tip {
  font-size: 13px;
  color: #909399;
  line-height: 1.8;
  margin-top: 8px;
  padding: 8px 12px;
  background-color: #f4f4f5;
  border-radius: 4px;
}

.form-tip .el-icon {
  margin-right: 6px;
  vertical-align: middle;
  color: #409eff;
}

/* 按钮样式 */
.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

:deep(.el-button--large) {
  padding: 12px 24px;
  font-size: 15px;
}

/* Switch 样式 */
:deep(.el-form-item:last-child) {
  margin-bottom: 24px;
}
</style>
