<template>
  <div class="nginx-config">
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <h3>Nginx配置管理</h3>
          <div class="header-actions">
            <el-button type="primary" @click="handleReloadNginx">
              重载Nginx
            </el-button>
            <el-button type="success" @click="handleTestConfig">
              测试配置
            </el-button>
          </div>
        </div>
      </template>
      
      <!-- Nginx配置标签页 -->
      <div class="nginx-tabs">
        <el-tabs v-model="activeTab">
          <el-tab-pane label="主配置文件" name="main" />
          <el-tab-pane label="代理配置" name="proxy" />
          <el-tab-pane label="Lua脚本" name="lua" />
          <el-tab-pane label="Nginx状态" name="status" />
        </el-tabs>
      </div>
      
      <!-- 主配置文件 -->
      <div v-if="activeTab === 'main'" class="tab-content">
        <div class="content-header">
          <el-button type="primary" @click="handleSaveConfig">
            保存配置
          </el-button>
        </div>
        <el-input
          v-model="mainConfigContent"
          type="textarea"
          placeholder="请输入Nginx主配置内容"
          :rows="20"
          class="config-textarea"
        />
      </div>
      
      <!-- 代理配置 -->
      <div v-if="activeTab === 'proxy'" class="tab-content">
        <div class="content-header">
          <el-button type="primary" @click="handleAddProxyConfig">
            添加代理配置
          </el-button>
          <el-button type="primary" @click="handleSaveConfig">
            保存配置
          </el-button>
        </div>
        <el-table :data="proxyConfigs" stripe style="width: 100%">
          <el-table-column prop="id" label="ID" width="80" />
          <el-table-column prop="name" label="配置名称" />
          <el-table-column prop="type" label="代理类型" width="120" />
          <el-table-column prop="listenPort" label="监听端口" width="120" />
          <el-table-column prop="backendUrl" label="后端地址" show-overflow-tooltip />
          <el-table-column prop="status" label="状态" width="100">
            <template #default="scope">
              <el-switch v-model="scope.row.status" @change="handleProxyStatusChange(scope.row)" />
            </template>
          </el-table-column>
          <el-table-column label="操作" width="150" fixed="right">
            <template #default="scope">
              <el-button type="primary" size="small" @click="handleEditProxyConfig(scope.row)">
                编辑
              </el-button>
              <el-button type="warning" size="small" @click="handleDeleteProxyConfig(scope.row.id)">
                删除
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
      
      <!-- Lua脚本 -->
      <div v-if="activeTab === 'lua'" class="tab-content">
        <div class="content-header">
          <el-button type="primary" @click="handleAddLuaScript">
            添加脚本
          </el-button>
          <el-button type="primary" @click="handleSaveLuaScript">
            保存脚本
          </el-button>
        </div>
        <div class="lua-content">
          <div class="lua-sidebar">
            <el-tree
              v-model="selectedLuaScript"
              :data="luaScripts"
              :props="luaTreeProps"
              @node-click="handleLuaScriptClick"
              class="lua-tree"
            />
          </div>
          <div class="lua-editor">
            <el-input
              v-model="currentLuaContent"
              type="textarea"
              placeholder="请输入Lua脚本内容"
              :rows="25"
              class="lua-textarea"
            />
          </div>
        </div>
      </div>
      
      <!-- Nginx状态 -->
      <div v-if="activeTab === 'status'" class="tab-content">
        <el-card shadow="hover" class="status-card">
          <template #header>
            <div class="card-header">
              <h4>Nginx运行状态</h4>
            </div>
          </template>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="进程ID">{{ nginxStatus.pid }}</el-descriptions-item>
            <el-descriptions-item label="运行时间">{{ nginxStatus.runTime }}</el-descriptions-item>
            <el-descriptions-item label="版本">{{ nginxStatus.version }}</el-descriptions-item>
            <el-descriptions-item label="启动时间">{{ nginxStatus.startTime }}</el-descriptions-item>
            <el-descriptions-item label="总连接数">{{ nginxStatus.totalConnections }}</el-descriptions-item>
            <el-descriptions-item label="当前连接数">{{ nginxStatus.currentConnections }}</el-descriptions-item>
            <el-descriptions-item label="请求数">{{ nginxStatus.requests }}</el-descriptions-item>
            <el-descriptions-item label="接受连接数">{{ nginxStatus.acceptedConnections }}</el-descriptions-item>
          </el-descriptions>
        </el-card>
        
        <el-card shadow="hover" class="status-card mt-20">
          <template #header>
            <div class="card-header">
              <h4>工作进程状态</h4>
            </div>
          </template>
          <el-table :data="workerProcesses" stripe style="width: 100%">
            <el-table-column prop="pid" label="进程ID" width="100" />
            <el-table-column prop="connections" label="连接数" width="100" />
            <el-table-column prop="status" label="状态" width="120" />
            <el-table-column prop="cpuUsage" label="CPU使用率" width="120" />
            <el-table-column prop="memoryUsage" label="内存使用率" width="120" />
          </el-table>
        </el-card>
      </div>
    </el-card>
    
    <!-- 添加/编辑代理配置弹窗 -->
    <el-dialog v-model="proxyDialogVisible" :title="proxyDialogTitle" width="800px">
      <el-form ref="proxyForm" :model="proxyForm" :rules="proxyRules" label-width="120px">
        <el-form-item label="配置名称" prop="name">
          <el-input v-model="proxyForm.name" placeholder="请输入配置名称" />
        </el-form-item>
        <el-form-item label="代理类型" prop="type">
          <el-select v-model="proxyForm.type" placeholder="请选择代理类型">
            <el-option label="HTTP" value="http" />
            <el-option label="HTTPS" value="https" />
            <el-option label="TCP" value="tcp" />
            <el-option label="UDP" value="udp" />
          </el-select>
        </el-form-item>
        <el-form-item label="监听端口" prop="listenPort">
          <el-input-number v-model="proxyForm.listenPort" :min="1" :max="65535" placeholder="请输入监听端口" />
        </el-form-item>
        <el-form-item label="后端地址" prop="backendUrl">
          <el-input v-model="proxyForm.backendUrl" placeholder="请输入后端地址" />
        </el-form-item>
        <el-form-item label="配置内容" prop="configContent">
          <el-input v-model="proxyForm.configContent" type="textarea" placeholder="请输入详细配置内容" :rows="6" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-switch v-model="proxyForm.status" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="proxyDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSaveProxyConfig">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'

// 活跃标签页
const activeTab = ref('main')

// 主配置文件内容
const mainConfigContent = ref(`worker_processes  1;

events {
    worker_connections  1024;
}

http {
    include       mime.types;
    default_type  application/octet-stream;
    
    sendfile        on;
    keepalive_timeout  65;
    
    server {
        listen       80;
        server_name  localhost;
        
        location / {
            root   html;
            index  index.html index.htm;
        }
        
        error_page   500 502 503 504  /50x.html;
        location = /50x.html {
            root   html;
        }
    }
}`)

// 代理配置相关
const proxyConfigs = ref([])
const proxyDialogVisible = ref(false)
const proxyDialogTitle = ref('添加代理配置')
const proxyForm = reactive({
  id: null,
  name: '',
  type: 'http',
  listenPort: 8080,
  backendUrl: '',
  configContent: '',
  status: true
})

const proxyRules = {
  name: [
    { required: true, message: '请输入配置名称', trigger: 'blur' }
  ],
  type: [
    { required: true, message: '请选择代理类型', trigger: 'change' }
  ],
  listenPort: [
    { required: true, message: '请输入监听端口', trigger: 'blur' }
  ],
  backendUrl: [
    { required: true, message: '请输入后端地址', trigger: 'blur' }
  ]
}

// Lua脚本相关
const luaScripts = ref([
  {
    id: 1,
    label: '认证脚本',
    children: [
      { id: 11, label: 'auth.lua' },
      { id: 12, label: 'jwt.lua' }
    ]
  },
  {
    id: 2,
    label: '策略脚本',
    children: [
      { id: 21, label: 'policy.lua' },
      { id: 22, label: 'rate_limit.lua' }
    ]
  },
  {
    id: 3,
    label: '日志脚本',
    children: [
      { id: 31, label: 'log.lua' }
    ]
  }
])

const luaTreeProps = {
  children: 'children',
  label: 'label'
}

const selectedLuaScript = ref(null)
const currentLuaContent = ref('-- Lua脚本内容')

// Nginx状态相关
const nginxStatus = reactive({
  pid: 1234,
  runTime: '2天12小时30分钟',
  version: '1.24.0',
  startTime: '2024-01-01 10:00:00',
  totalConnections: 10000,
  currentConnections: 100,
  requests: 50000,
  acceptedConnections: 10000
})

const workerProcesses = ref([
  { pid: 1234, connections: 25, status: 'running', cpuUsage: '5%', memoryUsage: '10%' },
  { pid: 1235, connections: 30, status: 'running', cpuUsage: '6%', memoryUsage: '12%' },
  { pid: 1236, connections: 20, status: 'running', cpuUsage: '4%', memoryUsage: '9%' },
  { pid: 1237, connections: 25, status: 'running', cpuUsage: '5%', memoryUsage: '11%' }
])

// 模拟代理配置数据
const mockProxyConfigs = [
  {
    id: 1,
    name: 'HTTP代理配置',
    type: 'http',
    listenPort: 8080,
    backendUrl: 'http://localhost:8000',
    configContent: `location / {
    proxy_pass http://localhost:8000;
    proxy_set_header Host $host;
    proxy_set_header X-Real-IP $remote_addr;
}`,
    status: true
  },
  {
    id: 2,
    name: 'HTTPS代理配置',
    type: 'https',
    listenPort: 8443,
    backendUrl: 'https://localhost:9000',
    configContent: `location / {
    proxy_pass https://localhost:9000;
    proxy_set_header Host $host;
    proxy_set_header X-Real-IP $remote_addr;
}`,
    status: true
  }
]

// 初始化数据
onMounted(() => {
  proxyConfigs.value = mockProxyConfigs
})

// 保存配置
const handleSaveConfig = () => {
  // 模拟API请求
  setTimeout(() => {
    ElMessage.success('配置保存成功')
  }, 500)
}

// 重载Nginx
const handleReloadNginx = () => {
  // 模拟API请求
  setTimeout(() => {
    ElMessage.success('Nginx重载成功')
  }, 1000)
}

// 测试配置
const handleTestConfig = () => {
  // 模拟API请求
  setTimeout(() => {
    ElMessage.success('配置测试通过')
  }, 500)
}

// 添加代理配置
const handleAddProxyConfig = () => {
  proxyDialogTitle.value = '添加代理配置'
  Object.assign(proxyForm, {
    id: null,
    name: '',
    type: 'http',
    listenPort: 8080,
    backendUrl: '',
    configContent: '',
    status: true
  })
  proxyDialogVisible.value = true
}

// 编辑代理配置
const handleEditProxyConfig = (row) => {
  proxyDialogTitle.value = '编辑代理配置'
  Object.assign(proxyForm, row)
  proxyDialogVisible.value = true
}

// 保存代理配置
const handleSaveProxyConfig = () => {
  // 模拟API请求
  setTimeout(() => {
    if (proxyForm.id) {
      // 编辑操作
      const index = proxyConfigs.value.findIndex(config => config.id === proxyForm.id)
      if (index !== -1) {
        proxyConfigs.value[index] = { ...proxyForm }
      }
      ElMessage.success('代理配置编辑成功')
    } else {
      // 添加操作
      const newConfig = {
        ...proxyForm,
        id: Date.now()
      }
      proxyConfigs.value.unshift(newConfig)
      ElMessage.success('代理配置添加成功')
    }
    proxyDialogVisible.value = false
  }, 500)
}

// 切换代理配置状态
const handleProxyStatusChange = (row) => {
  // 模拟API请求
  setTimeout(() => {
    ElMessage.success('代理配置状态更新成功')
  }, 300)
}

// 删除代理配置
const handleDeleteProxyConfig = (id) => {
  // 模拟API请求
  setTimeout(() => {
    proxyConfigs.value = proxyConfigs.value.filter(config => config.id !== id)
    ElMessage.success('代理配置删除成功')
  }, 500)
}

// 添加Lua脚本
const handleAddLuaScript = () => {
  ElMessage.info('添加Lua脚本功能开发中')
}

// 保存Lua脚本
const handleSaveLuaScript = () => {
  // 模拟API请求
  setTimeout(() => {
    ElMessage.success('Lua脚本保存成功')
  }, 500)
}

// 点击Lua脚本树节点
const handleLuaScriptClick = (data) => {
  selectedLuaScript.value = data.id
  currentLuaContent.value = `-- ${data.label} 脚本内容\n-- 这里是脚本的具体实现`
}
</script>

<style scoped>
.nginx-config {
  width: 100%;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-actions {
  display: flex;
  gap: 10px;
}

.nginx-tabs {
  margin-bottom: 20px;
}

.tab-content {
  padding: 10px 0;
}

.content-header {
  margin-bottom: 20px;
  display: flex;
  gap: 10px;
}

.config-textarea,
.lua-textarea {
  font-family: 'Consolas', 'Monaco', 'Courier New', monospace;
  font-size: 14px;
}

.lua-content {
  display: flex;
  gap: 20px;
}

.lua-sidebar {
  width: 250px;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  overflow: hidden;
}

.lua-tree {
  height: 500px;
  overflow-y: auto;
}

.lua-editor {
  flex: 1;
}

.status-card {
  margin-bottom: 20px;
}

.mt-20 {
  margin-top: 20px;
}
</style>