<template>
  <div class="log-management">
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <h3>日志管理</h3>
          <div class="header-actions">
            <el-button type="primary" @click="handleExport">
              <el-icon><Download /></el-icon> 导出日志
            </el-button>
          </div>
        </div>
      </template>
      
      <!-- 日志类型切换 -->
      <div class="log-type-tabs">
        <el-tabs v-model="activeTab" @tab-change="handleTabChange">
          <el-tab-pane label="访问日志" name="access" />
          <el-tab-pane label="审计日志" name="audit" />
        </el-tabs>
      </div>
      
      <!-- 搜索和筛选区域 -->
      <div class="search-area">
        <el-form :inline="true" :model="searchForm" class="search-form">
          <el-form-item label="智能体名称">
            <el-input v-model="searchForm.agentName" placeholder="请输入智能体名称" clearable />
          </el-form-item>
          <el-form-item label="客户端IP">
            <el-input v-model="searchForm.clientIp" placeholder="请输入客户端IP" clearable />
          </el-form-item>
          <el-form-item label="响应状态">
            <el-input-number v-model="searchForm.responseStatus" :min="100" :max="599" placeholder="请输入状态码" />
          </el-form-item>
          <el-form-item label="时间范围">
            <el-date-picker
              v-model="dateRange"
              type="daterange"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              format="YYYY-MM-DD HH:mm:ss"
              value-format="YYYY-MM-DD HH:mm:ss"
              style="width: 300px"
            />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSearch">搜索</el-button>
            <el-button @click="resetSearch">重置</el-button>
          </el-form-item>
        </el-form>
      </div>
      
      <!-- 日志列表 -->
      <div class="table-area">
        <el-table :data="logList" stripe style="width: 100%" height="600">
          <el-table-column prop="id" label="ID" width="80" />
          <el-table-column prop="agentName" label="智能体名称" />
          <el-table-column prop="clientIp" label="客户端IP" width="150" />
          <el-table-column prop="requestUrl" label="请求URL" show-overflow-tooltip />
          <el-table-column prop="httpMethod" label="HTTP方法" width="100" />
          <el-table-column prop="responseStatus" label="响应状态" width="100" />
          <el-table-column prop="responseTime" label="响应时间(ms)" width="120" />
          <el-table-column prop="logTime" label="日志时间" width="200" />
          <el-table-column label="操作" width="150" fixed="right">
            <template #default="scope">
              <el-button type="primary" size="small" @click="handleViewDetail(scope.row)">
                <el-icon><View /></el-icon> 详情
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
    
    <!-- 日志详情弹窗 -->
    <el-dialog v-model="detailVisible" title="日志详情" width="800px">
      <el-descriptions :column="1" border>
        <el-descriptions-item label="日志ID">{{ selectedLog.id }}</el-descriptions-item>
        <el-descriptions-item label="智能体名称">{{ selectedLog.agentName }}</el-descriptions-item>
        <el-descriptions-item label="客户端IP">{{ selectedLog.clientIp }}</el-descriptions-item>
        <el-descriptions-item label="请求URL">{{ selectedLog.requestUrl }}</el-descriptions-item>
        <el-descriptions-item label="HTTP方法">{{ selectedLog.httpMethod }}</el-descriptions-item>
        <el-descriptions-item label="请求参数">{{ selectedLog.requestParams }}</el-descriptions-item>
        <el-descriptions-item label="请求头">{{ selectedLog.requestHeaders }}</el-descriptions-item>
        <el-descriptions-item label="响应状态">{{ selectedLog.responseStatus }}</el-descriptions-item>
        <el-descriptions-item label="响应时间">{{ selectedLog.responseTime }} ms</el-descriptions-item>
        <el-descriptions-item label="错误信息">{{ selectedLog.errorMessage }}</el-descriptions-item>
        <el-descriptions-item label="日志时间">{{ selectedLog.logTime }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="detailVisible = false">关闭</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Download, View } from '@element-plus/icons-vue'

// 活跃标签页
const activeTab = ref('access')

// 搜索表单
const searchForm = reactive({
  agentName: '',
  clientIp: '',
  responseStatus: null
})

// 日期范围
const dateRange = ref([])

// 日志列表
const logList = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)

// 详情弹窗
const detailVisible = ref(false)
const selectedLog = ref({})

// 模拟数据
const mockAccessLogs = [
  {
    id: 1,
    type: 1,
    agentId: 1,
    agentName: '智能体A',
    clientIp: '192.168.1.100',
    requestUrl: '/api/v1/chat',
    httpMethod: 'POST',
    requestParams: '{"message":"你好"}',
    requestHeaders: '{"Content-Type":"application/json","Authorization":"Bearer token123"}',
    responseStatus: 200,
    responseTime: 150,
    errorMessage: '',
    logTime: '2024-01-02 14:30:00'
  },
  {
    id: 2,
    type: 1,
    agentId: 2,
    agentName: '智能体B',
    clientIp: '192.168.1.101',
    requestUrl: '/api/v1/data',
    httpMethod: 'GET',
    requestParams: '{"id":123}',
    requestHeaders: '{"Authorization":"Bearer token456"}',
    responseStatus: 404,
    responseTime: 50,
    errorMessage: '资源不存在',
    logTime: '2024-01-02 14:31:00'
  },
  {
    id: 3,
    type: 1,
    agentId: 1,
    agentName: '智能体A',
    clientIp: '192.168.1.100',
    requestUrl: '/api/v1/chat',
    httpMethod: 'POST',
    requestParams: '{"message":"再见"}',
    requestHeaders: '{"Content-Type":"application/json","Authorization":"Bearer token123"}',
    responseStatus: 200,
    responseTime: 120,
    errorMessage: '',
    logTime: '2024-01-02 14:32:00'
  }
]

const mockAuditLogs = [
  {
    id: 1001,
    type: 2,
    operator: 'admin',
    operationContent: '添加了智能体C',
    logTime: '2024-01-02 14:25:00'
  },
  {
    id: 1002,
    type: 2,
    operator: 'admin',
    operationContent: '更新了防护策略1',
    logTime: '2024-01-02 14:26:00'
  },
  {
    id: 1003,
    type: 2,
    operator: 'admin',
    operationContent: '删除了智能体D',
    logTime: '2024-01-02 14:27:00'
  }
]

// 初始化数据
onMounted(() => {
  loadLogList()
})

// 加载日志列表
const loadLogList = () => {
  // 模拟API请求
  setTimeout(() => {
    if (activeTab.value === 'access') {
      logList.value = mockAccessLogs
      total.value = mockAccessLogs.length
    } else {
      logList.value = mockAuditLogs
      total.value = mockAuditLogs.length
    }
  }, 500)
}

// 标签页切换
const handleTabChange = () => {
  currentPage.value = 1
  loadLogList()
}

// 搜索
const handleSearch = () => {
  currentPage.value = 1
  loadLogList()
}

// 重置搜索
const resetSearch = () => {
  searchForm.agentName = ''
  searchForm.clientIp = ''
  searchForm.responseStatus = null
  dateRange.value = []
  currentPage.value = 1
  loadLogList()
}

// 分页大小变化
const handleSizeChange = (size) => {
  pageSize.value = size
  loadLogList()
}

// 当前页码变化
const handleCurrentChange = (page) => {
  currentPage.value = page
  loadLogList()
}

// 查看日志详情
const handleViewDetail = (row) => {
  selectedLog.value = row
  detailVisible.value = true
}

// 导出日志
const handleExport = () => {
  // 模拟导出操作
  ElMessage.success('日志导出成功')
}
</script>

<style scoped>
.log-management {
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

.log-type-tabs {
  margin-bottom: 20px;
}

.search-area {
  margin-bottom: 20px;
}

.search-form {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
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