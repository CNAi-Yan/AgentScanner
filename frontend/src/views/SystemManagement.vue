<template>
  <div class="system-management">
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <h3>系统管理</h3>
        </div>
      </template>
      
      <!-- 系统管理标签页 -->
      <div class="system-tabs">
        <el-tabs v-model="activeTab">
          <el-tab-pane label="用户管理" name="user" />
          <el-tab-pane label="系统配置" name="config" />
          <el-tab-pane label="系统监控" name="monitor" />
        </el-tabs>
      </div>
      
      <!-- 用户管理 -->
      <div v-if="activeTab === 'user'" class="tab-content">
        <div class="content-header">
          <el-button type="primary" @click="handleAddUser">
            <el-icon><Plus /></el-icon> 添加用户
          </el-button>
        </div>
        <el-table :data="userList" stripe style="width: 100%">
          <el-table-column prop="id" label="ID" width="80" />
          <el-table-column prop="username" label="用户名" />
          <el-table-column prop="role" label="角色" />
          <el-table-column prop="status" label="状态" width="100">
            <template #default="scope">
              <el-switch v-model="scope.row.status" @change="handleUserStatusChange(scope.row)" />
            </template>
          </el-table-column>
          <el-table-column prop="createTime" label="创建时间" width="180" />
          <el-table-column prop="lastLoginTime" label="最后登录时间" width="180" />
          <el-table-column label="操作" width="150" fixed="right">
            <template #default="scope">
              <el-button type="primary" size="small" @click="handleEditUser(scope.row)">
                <el-icon><Edit /></el-icon> 编辑
              </el-button>
              <el-button type="warning" size="small" @click="handleResetPassword(scope.row.id)">
                <el-icon><Key /></el-icon> 重置密码
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
      
      <!-- 系统配置 -->
      <div v-if="activeTab === 'config'" class="tab-content">
        <el-form ref="configForm" :model="configForm" label-width="150px" class="config-form">
          <el-form-item label="后端服务地址">
            <el-input v-model="configForm.backendUrl" placeholder="请输入后端服务地址" />
          </el-form-item>
          <el-form-item label="Nginx配置路径">
            <el-input v-model="configForm.nginxConfPath" placeholder="请输入Nginx配置路径" />
          </el-form-item>
          <el-form-item label="Nginx重载命令">
            <el-input v-model="configForm.nginxReloadCommand" placeholder="请输入Nginx重载命令" />
          </el-form-item>
          <el-form-item label="Lua脚本路径">
            <el-input v-model="configForm.luaScriptPath" placeholder="请输入Lua脚本路径" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSaveConfig">保存配置</el-button>
          </el-form-item>
        </el-form>
      </div>
      
      <!-- 系统监控 -->
      <div v-if="activeTab === 'monitor'" class="tab-content">
        <div class="monitor-cards">
          <el-card shadow="hover" class="monitor-card">
            <template #header>
              <div class="card-header">
                <h4>CPU使用率</h4>
              </div>
            </template>
            <div class="monitor-value">{{ systemInfo.cpuUsage }}%</div>
          </el-card>
          <el-card shadow="hover" class="monitor-card">
            <template #header>
              <div class="card-header">
                <h4>内存使用率</h4>
              </div>
            </template>
            <div class="monitor-value">{{ systemInfo.memoryUsage }}%</div>
          </el-card>
          <el-card shadow="hover" class="monitor-card">
            <template #header>
              <div class="card-header">
                <h4>磁盘使用率</h4>
              </div>
            </template>
            <div class="monitor-value">{{ systemInfo.diskUsage }}%</div>
          </el-card>
          <el-card shadow="hover" class="monitor-card">
            <template #header>
              <div class="card-header">
                <h4>在线智能体数</h4>
              </div>
            </template>
            <div class="monitor-value">{{ systemInfo.onlineAgents }}</div>
          </el-card>
        </div>
        <el-card shadow="hover" class="mt-20">
          <template #header>
            <div class="card-header">
              <h4>系统信息</h4>
            </div>
          </template>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="系统版本">{{ systemInfo.version }}</el-descriptions-item>
            <el-descriptions-item label="运行时间">{{ systemInfo.runTime }}</el-descriptions-item>
            <el-descriptions-item label="服务器IP">{{ systemInfo.serverIp }}</el-descriptions-item>
            <el-descriptions-item label="操作系统">{{ systemInfo.os }}</el-descriptions-item>
            <el-descriptions-item label="Java版本">{{ systemInfo.javaVersion }}</el-descriptions-item>
            <el-descriptions-item label="Nginx版本">{{ systemInfo.nginxVersion }}</el-descriptions-item>
          </el-descriptions>
        </el-card>
      </div>
    </el-card>
    
    <!-- 添加/编辑用户弹窗 -->
    <el-dialog v-model="userDialogVisible" :title="userDialogTitle" width="600px">
      <el-form ref="userForm" :model="userForm" :rules="userRules" label-width="100px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="userForm.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="密码" prop="password" v-if="!userForm.id">
          <el-input v-model="userForm.password" type="password" placeholder="请输入密码" show-password />
        </el-form-item>
        <el-form-item label="角色" prop="role">
          <el-select v-model="userForm.role" placeholder="请选择角色">
            <el-option label="管理员" value="admin" />
            <el-option label="操作员" value="operator" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-switch v-model="userForm.status" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="userDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSaveUser">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus, Edit, Key } from '@element-plus/icons-vue'

// 活跃标签页
const activeTab = ref('user')

// 用户管理相关
const userList = ref([])
const userDialogVisible = ref(false)
const userDialogTitle = ref('添加用户')
const userForm = reactive({
  id: null,
  username: '',
  password: '',
  role: 'operator',
  status: true
})

const userRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' }
  ],
  role: [
    { required: true, message: '请选择角色', trigger: 'change' }
  ]
}

// 系统配置相关
const configForm = reactive({
  backendUrl: 'http://localhost:8080/api',
  nginxConfPath: '/usr/local/nginx/conf/nginx.conf',
  nginxReloadCommand: '/usr/local/nginx/sbin/nginx -s reload',
  luaScriptPath: '/usr/local/nginx/lua/'
})

// 系统监控相关
const systemInfo = reactive({
  cpuUsage: 45,
  memoryUsage: 60,
  diskUsage: 75,
  onlineAgents: 10,
  version: '1.0.0',
  runTime: '2天12小时30分钟',
  serverIp: '192.168.1.100',
  os: 'Linux Ubuntu 20.04',
  javaVersion: '17.0.1',
  nginxVersion: '1.24.0'
})

// 模拟用户数据
const mockUsers = [
  {
    id: 1,
    username: 'admin',
    role: 'admin',
    status: true,
    createTime: '2024-01-01 10:00:00',
    lastLoginTime: '2024-01-02 14:30:00'
  },
  {
    id: 2,
    username: 'operator',
    role: 'operator',
    status: true,
    createTime: '2024-01-01 11:00:00',
    lastLoginTime: '2024-01-02 13:00:00'
  }
]

// 初始化数据
onMounted(() => {
  userList.value = mockUsers
})

// 添加用户
const handleAddUser = () => {
  userDialogTitle.value = '添加用户'
  Object.assign(userForm, {
    id: null,
    username: '',
    password: '',
    role: 'operator',
    status: true
  })
  userDialogVisible.value = true
}

// 编辑用户
const handleEditUser = (row) => {
  userDialogTitle.value = '编辑用户'
  Object.assign(userForm, row)
  userDialogVisible.value = true
}

// 保存用户
const handleSaveUser = () => {
  // 模拟API请求
  setTimeout(() => {
    if (userForm.id) {
      // 编辑操作
      const index = userList.value.findIndex(user => user.id === userForm.id)
      if (index !== -1) {
        userList.value[index] = { ...userForm }
      }
      ElMessage.success('编辑用户成功')
    } else {
      // 添加操作
      const newUser = {
        ...userForm,
        id: Date.now(),
        createTime: new Date().toLocaleString(),
        lastLoginTime: ''
      }
      userList.value.unshift(newUser)
      ElMessage.success('添加用户成功')
    }
    userDialogVisible.value = false
  }, 500)
}

// 切换用户状态
const handleUserStatusChange = (row) => {
  // 模拟API请求
  setTimeout(() => {
    ElMessage.success('用户状态更新成功')
  }, 300)
}

// 重置密码
const handleResetPassword = (id) => {
  // 模拟API请求
  setTimeout(() => {
    ElMessage.success('密码重置成功，新密码为：123456')
  }, 500)
}

// 保存配置
const handleSaveConfig = () => {
  // 模拟API请求
  setTimeout(() => {
    ElMessage.success('配置保存成功')
  }, 500)
}
</script>

<style scoped>
.system-management {
  width: 100%;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.system-tabs {
  margin-bottom: 20px;
}

.tab-content {
  padding: 10px 0;
}

.content-header {
  margin-bottom: 20px;
  display: flex;
  justify-content: flex-end;
}

.config-form {
  max-width: 600px;
}

.monitor-cards {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-bottom: 20px;
}

.monitor-card {
  text-align: center;
}

.monitor-value {
  font-size: 36px;
  font-weight: bold;
  color: #1890ff;
  margin: 20px 0;
}

.mt-20 {
  margin-top: 20px;
}
</style>