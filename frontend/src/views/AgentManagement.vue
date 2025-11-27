<template>
  <div class="agent-management">
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <h3>智能体管理</h3>
          <el-button type="primary" @click="handleAddAgent">
            <el-icon><Plus /></el-icon> 添加智能体
          </el-button>
        </div>
      </template>
      
      <!-- 搜索和筛选区域 -->
      <div class="search-area">
        <el-form :inline="true" :model="searchForm" class="search-form">
          <el-form-item label="智能体名称">
            <el-input v-model="searchForm.name" placeholder="请输入智能体名称" clearable />
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="searchForm.status" placeholder="请选择状态" clearable>
              <el-option label="启用" value="1" />
              <el-option label="禁用" value="0" />
            </el-select>
          </el-form-item>
          <el-form-item label="所属分组">
            <el-input v-model="searchForm.groupName" placeholder="请输入分组名称" clearable />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSearch">搜索</el-button>
            <el-button @click="resetSearch">重置</el-button>
          </el-form-item>
        </el-form>
      </div>
      
      <!-- 智能体列表 -->
      <div class="table-area">
        <el-table :data="agentList" stripe style="width: 100%">
          <el-table-column prop="id" label="ID" width="80" />
          <el-table-column prop="name" label="智能体名称" />
          <el-table-column prop="type" label="智能体类型" />
          <el-table-column prop="apiKey" label="API密钥" width="200">
            <template #default="scope">
              <el-input v-model="scope.row.apiKey" readonly style="width: 200px" />
            </template>
          </el-table-column>
          <el-table-column prop="statusDesc" label="状态" width="100" />
          <el-table-column prop="groupName" label="所属分组" />
          <el-table-column prop="totalRequests" label="总请求数" width="120" />
          <el-table-column prop="todayRequests" label="今日请求数" width="120" />
          <el-table-column prop="lastOnlineTime" label="最后在线时间" width="180" />
          <el-table-column prop="createTime" label="创建时间" width="180" />
          <el-table-column label="操作" width="200" fixed="right">
            <template #default="scope">
              <el-button type="primary" size="small" @click="handleEditAgent(scope.row)">
                <el-icon><Edit /></el-icon> 编辑
              </el-button>
              <el-button type="success" size="small" @click="handleToggleStatus(scope.row)" :disabled="scope.row.status === 1">
                <el-icon><Check /></el-icon> 启用
              </el-button>
              <el-button type="danger" size="small" @click="handleToggleStatus(scope.row)" :disabled="scope.row.status === 0">
                <el-icon><Close /></el-icon> 禁用
              </el-button>
              <el-button type="warning" size="small" @click="handleDeleteAgent(scope.row.id)">
                <el-icon><Delete /></el-icon> 删除
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
      
      <!-- 分页 -->
      <div class="pagination-area">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>
    
    <!-- 添加/编辑智能体弹窗 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px">
      <el-form ref="agentForm" :model="agentForm" :rules="rules" label-width="100px">
        <el-form-item label="智能体名称" prop="name">
          <el-input v-model="agentForm.name" placeholder="请输入智能体名称" />
        </el-form-item>
        <el-form-item label="智能体类型" prop="type">
          <el-input v-model="agentForm.type" placeholder="请输入智能体类型" />
        </el-form-item>
        <el-form-item label="智能体描述" prop="description">
          <el-input v-model="agentForm.description" type="textarea" placeholder="请输入智能体描述" :rows="3" />
        </el-form-item>
        <el-form-item label="所属分组" prop="groupName">
          <el-input v-model="agentForm.groupName" placeholder="请输入所属分组" />
        </el-form-item>
        <el-form-item label="允许的IP列表" prop="allowedIps">
          <el-input v-model="agentForm.allowedIps" placeholder="请输入允许的IP列表，多个IP用逗号分隔" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="agentForm.status" placeholder="请选择状态">
            <el-option label="启用" value="1" />
            <el-option label="禁用" value="0" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmit">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Edit, Check, Close, Delete } from '@element-plus/icons-vue'

// 搜索表单
const searchForm = reactive({
  name: '',
  status: '',
  groupName: ''
})

// 智能体列表
const agentList = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)

// 弹窗相关
const dialogVisible = ref(false)
const dialogTitle = ref('添加智能体')
const agentForm = reactive({
  id: null,
  name: '',
  type: '',
  description: '',
  groupName: '',
  allowedIps: '',
  status: 1
})

// 表单验证规则
const rules = {
  name: [
    { required: true, message: '请输入智能体名称', trigger: 'blur' }
  ],
  type: [
    { required: true, message: '请输入智能体类型', trigger: 'blur' }
  ]
}

// 模拟数据
const mockAgents = [
  {
    id: 1,
    name: '智能体A',
    type: '聊天机器人',
    description: '用于客服的聊天机器人',
    apiKey: '1234567890abcdef1234567890abcdef',
    status: 1,
    statusDesc: '启用',
    groupName: '客服组',
    allowedIps: '192.168.1.1,192.168.1.2',
    createTime: '2024-01-01 10:00:00',
    updateTime: '2024-01-01 10:00:00',
    lastOnlineTime: '2024-01-02 14:30:00',
    totalRequests: 1000,
    todayRequests: 100
  },
  {
    id: 2,
    name: '智能体B',
    type: '数据分析',
    description: '用于数据分析的智能体',
    apiKey: 'abcdef1234567890abcdef1234567890',
    status: 0,
    statusDesc: '禁用',
    groupName: '数据分析组',
    allowedIps: '192.168.2.1,192.168.2.2',
    createTime: '2024-01-02 11:00:00',
    updateTime: '2024-01-02 11:00:00',
    lastOnlineTime: '2024-01-02 12:00:00',
    totalRequests: 500,
    todayRequests: 50
  }
]

// 初始化数据
onMounted(() => {
  loadAgentList()
})

// 加载智能体列表
const loadAgentList = () => {
  // 模拟API请求
  setTimeout(() => {
    agentList.value = mockAgents
    total.value = mockAgents.length
  }, 500)
}

// 搜索
const handleSearch = () => {
  currentPage.value = 1
  loadAgentList()
}

// 重置搜索
const resetSearch = () => {
  searchForm.name = ''
  searchForm.status = ''
  searchForm.groupName = ''
  currentPage.value = 1
  loadAgentList()
}

// 分页大小变化
const handleSizeChange = (size) => {
  pageSize.value = size
  loadAgentList()
}

// 当前页码变化
const handleCurrentChange = (page) => {
  currentPage.value = page
  loadAgentList()
}

// 添加智能体
const handleAddAgent = () => {
  dialogTitle.value = '添加智能体'
  Object.assign(agentForm, {
    id: null,
    name: '',
    type: '',
    description: '',
    groupName: '',
    allowedIps: '',
    status: 1
  })
  dialogVisible.value = true
}

// 编辑智能体
const handleEditAgent = (row) => {
  dialogTitle.value = '编辑智能体'
  Object.assign(agentForm, row)
  dialogVisible.value = true
}

// 切换智能体状态
const handleToggleStatus = (row) => {
  const newStatus = row.status === 1 ? 0 : 1
  const statusText = newStatus === 1 ? '启用' : '禁用'
  
  ElMessageBox.confirm(`确定要${statusText}该智能体吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    // 模拟API请求
    setTimeout(() => {
      row.status = newStatus
      row.statusDesc = newStatus === 1 ? '启用' : '禁用'
      ElMessage.success(`${statusText}成功`)
    }, 500)
  }).catch(() => {
    // 取消操作
  })
}

// 删除智能体
const handleDeleteAgent = (id) => {
  ElMessageBox.confirm('确定要删除该智能体吗？删除后不可恢复', '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'error'
  }).then(() => {
    // 模拟API请求
    setTimeout(() => {
      agentList.value = agentList.value.filter(agent => agent.id !== id)
      total.value = agentList.value.length
      ElMessage.success('删除成功')
    }, 500)
  }).catch(() => {
    // 取消操作
  })
}

// 提交表单
const handleSubmit = () => {
  // 模拟API请求
  setTimeout(() => {
    if (agentForm.id) {
      // 编辑操作
      const index = agentList.value.findIndex(agent => agent.id === agentForm.id)
      if (index !== -1) {
        agentList.value[index] = { ...agentForm, statusDesc: agentForm.status === 1 ? '启用' : '禁用' }
      }
      ElMessage.success('编辑成功')
    } else {
      // 添加操作
      const newAgent = {
        ...agentForm,
        id: Date.now(),
        apiKey: 'new-api-key-' + Date.now(),
        createTime: new Date().toLocaleString(),
        updateTime: new Date().toLocaleString(),
        lastOnlineTime: new Date().toLocaleString(),
        totalRequests: 0,
        todayRequests: 0,
        statusDesc: agentForm.status === 1 ? '启用' : '禁用'
      }
      agentList.value.unshift(newAgent)
      total.value++
      ElMessage.success('添加成功')
    }
    dialogVisible.value = false
  }, 500)
}
</script>

<style scoped>
.agent-management {
  width: 100%;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.search-area {
  margin-bottom: 20px;
}

.search-form {
  display: flex;
  align-items: center;
  gap: 10px;
}

.table-area {
  margin-bottom: 20px;
}

.pagination-area {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}
</style>