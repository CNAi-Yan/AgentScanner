<template>
  <div class="policy-management">
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <h3>防护策略管理</h3>
          <el-button type="primary" @click="handleAddPolicy">
            <el-icon><Plus /></el-icon> 添加策略
          </el-button>
        </div>
      </template>
      
      <!-- 搜索和筛选区域 -->
      <div class="search-area">
        <el-form :inline="true" :model="searchForm" class="search-form">
          <el-form-item label="策略名称">
            <el-input v-model="searchForm.name" placeholder="请输入策略名称" clearable />
          </el-form-item>
          <el-form-item label="策略类型">
            <el-select v-model="searchForm.type" placeholder="请选择策略类型" clearable>
              <el-option label="访问控制" value="1" />
              <el-option label="流量控制" value="2" />
              <el-option label="内容过滤" value="3" />
            </el-select>
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="searchForm.status" placeholder="请选择状态" clearable>
              <el-option label="启用" value="1" />
              <el-option label="禁用" value="0" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSearch">搜索</el-button>
            <el-button @click="resetSearch">重置</el-button>
          </el-form-item>
        </el-form>
      </div>
      
      <!-- 策略列表 -->
      <div class="table-area">
        <el-table :data="policyList" stripe style="width: 100%">
          <el-table-column prop="id" label="ID" width="80" />
          <el-table-column prop="name" label="策略名称" />
          <el-table-column prop="typeDesc" label="策略类型" width="120" />
          <el-table-column prop="description" label="策略描述" />
          <el-table-column prop="priority" label="优先级" width="100" />
          <el-table-column prop="statusDesc" label="状态" width="100" />
          <el-table-column prop="applicableGroups" label="适用分组" />
          <el-table-column prop="createTime" label="创建时间" width="180" />
          <el-table-column label="操作" width="200" fixed="right">
            <template #default="scope">
              <el-button type="primary" size="small" @click="handleEditPolicy(scope.row)">
                <el-icon><Edit /></el-icon> 编辑
              </el-button>
              <el-button type="success" size="small" @click="handleToggleStatus(scope.row)" :disabled="scope.row.status === 1">
                <el-icon><Check /></el-icon> 启用
              </el-button>
              <el-button type="danger" size="small" @click="handleToggleStatus(scope.row)" :disabled="scope.row.status === 0">
                <el-icon><Close /></el-icon> 禁用
              </el-button>
              <el-button type="warning" size="small" @click="handleDeletePolicy(scope.row.id)">
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
    
    <!-- 添加/编辑策略弹窗 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="800px">
      <el-form ref="policyForm" :model="policyForm" :rules="rules" label-width="100px">
        <el-form-item label="策略名称" prop="name">
          <el-input v-model="policyForm.name" placeholder="请输入策略名称" />
        </el-form-item>
        <el-form-item label="策略类型" prop="type">
          <el-select v-model="policyForm.type" placeholder="请选择策略类型">
            <el-option label="访问控制" value="1" />
            <el-option label="流量控制" value="2" />
            <el-option label="内容过滤" value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="策略描述" prop="description">
          <el-input v-model="policyForm.description" type="textarea" placeholder="请输入策略描述" :rows="3" />
        </el-form-item>
        <el-form-item label="策略内容" prop="content">
          <el-input v-model="policyForm.content" type="textarea" placeholder="请输入策略内容（JSON格式）" :rows="6" />
        </el-form-item>
        <el-form-item label="优先级" prop="priority">
          <el-input-number v-model="policyForm.priority" :min="1" :max="100" placeholder="请输入优先级" />
        </el-form-item>
        <el-form-item label="适用分组" prop="applicableGroups">
          <el-input v-model="policyForm.applicableGroups" placeholder="请输入适用分组，多个分组用逗号分隔" />
        </el-form-item>
        <el-form-item label="适用智能体" prop="applicableAgents">
          <el-input v-model="policyForm.applicableAgents" placeholder="请输入适用智能体ID，多个ID用逗号分隔" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="policyForm.status" placeholder="请选择状态">
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
  type: '',
  status: ''
})

// 策略列表
const policyList = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)

// 弹窗相关
const dialogVisible = ref(false)
const dialogTitle = ref('添加策略')
const policyForm = reactive({
  id: null,
  name: '',
  type: 1,
  description: '',
  content: '',
  priority: 50,
  status: 1,
  applicableGroups: '',
  applicableAgents: ''
})

// 表单验证规则
const rules = {
  name: [
    { required: true, message: '请输入策略名称', trigger: 'blur' }
  ],
  type: [
    { required: true, message: '请选择策略类型', trigger: 'change' }
  ],
  content: [
    { required: true, message: '请输入策略内容', trigger: 'blur' }
  ]
}

// 模拟数据
const mockPolicies = [
  {
    id: 1,
    name: 'IP白名单策略',
    type: 1,
    typeDesc: '访问控制',
    description: '只允许指定IP访问',
    content: '{"whiteList":["192.168.1.1","192.168.1.2"]}',
    priority: 10,
    status: 1,
    statusDesc: '启用',
    applicableGroups: '客服组,数据分析组',
    applicableAgents: '1,2',
    createTime: '2024-01-01 10:00:00',
    updateTime: '2024-01-01 10:00:00'
  },
  {
    id: 2,
    name: '速率限制策略',
    type: 2,
    typeDesc: '流量控制',
    description: '限制每秒请求数',
    content: '{"rateLimit":100,"burst":200}',
    priority: 20,
    status: 1,
    statusDesc: '启用',
    applicableGroups: '所有分组',
    applicableAgents: '',
    createTime: '2024-01-02 11:00:00',
    updateTime: '2024-01-02 11:00:00'
  },
  {
    id: 3,
    name: '关键词过滤策略',
    type: 3,
    typeDesc: '内容过滤',
    description: '过滤敏感关键词',
    content: '{"keywords":["敏感词1","敏感词2"]}',
    priority: 30,
    status: 0,
    statusDesc: '禁用',
    applicableGroups: '所有分组',
    applicableAgents: '',
    createTime: '2024-01-03 12:00:00',
    updateTime: '2024-01-03 12:00:00'
  }
]

// 初始化数据
onMounted(() => {
  loadPolicyList()
})

// 加载策略列表
const loadPolicyList = () => {
  // 模拟API请求
  setTimeout(() => {
    policyList.value = mockPolicies
    total.value = mockPolicies.length
  }, 500)
}

// 搜索
const handleSearch = () => {
  currentPage.value = 1
  loadPolicyList()
}

// 重置搜索
const resetSearch = () => {
  searchForm.name = ''
  searchForm.type = ''
  searchForm.status = ''
  currentPage.value = 1
  loadPolicyList()
}

// 分页大小变化
const handleSizeChange = (size) => {
  pageSize.value = size
  loadPolicyList()
}

// 当前页码变化
const handleCurrentChange = (page) => {
  currentPage.value = page
  loadPolicyList()
}

// 添加策略
const handleAddPolicy = () => {
  dialogTitle.value = '添加策略'
  Object.assign(policyForm, {
    id: null,
    name: '',
    type: 1,
    description: '',
    content: '',
    priority: 50,
    status: 1,
    applicableGroups: '',
    applicableAgents: ''
  })
  dialogVisible.value = true
}

// 编辑策略
const handleEditPolicy = (row) => {
  dialogTitle.value = '编辑策略'
  Object.assign(policyForm, row)
  dialogVisible.value = true
}

// 切换策略状态
const handleToggleStatus = (row) => {
  const newStatus = row.status === 1 ? 0 : 1
  const statusText = newStatus === 1 ? '启用' : '禁用'
  
  ElMessageBox.confirm(`确定要${statusText}该策略吗？`, '提示', {
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

// 删除策略
const handleDeletePolicy = (id) => {
  ElMessageBox.confirm('确定要删除该策略吗？删除后不可恢复', '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'error'
  }).then(() => {
    // 模拟API请求
    setTimeout(() => {
      policyList.value = policyList.value.filter(policy => policy.id !== id)
      total.value = policyList.value.length
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
    if (policyForm.id) {
      // 编辑操作
      const index = policyList.value.findIndex(policy => policy.id === policyForm.id)
      if (index !== -1) {
        policyList.value[index] = {
          ...policyForm,
          typeDesc: getTypeDesc(policyForm.type),
          statusDesc: policyForm.status === 1 ? '启用' : '禁用'
        }
      }
      ElMessage.success('编辑成功')
    } else {
      // 添加操作
      const newPolicy = {
        ...policyForm,
        id: Date.now(),
        typeDesc: getTypeDesc(policyForm.type),
        statusDesc: policyForm.status === 1 ? '启用' : '禁用',
        createTime: new Date().toLocaleString(),
        updateTime: new Date().toLocaleString()
      }
      policyList.value.unshift(newPolicy)
      total.value++
      ElMessage.success('添加成功')
    }
    dialogVisible.value = false
  }, 500)
}

// 获取策略类型描述
const getTypeDesc = (type) => {
  switch (type) {
    case 1:
      return '访问控制'
    case 2:
      return '流量控制'
    case 3:
      return '内容过滤'
    default:
      return '未知类型'
  }
}
</script>

<style scoped>
.policy-management {
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