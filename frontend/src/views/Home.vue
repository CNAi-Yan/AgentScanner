<template>
  <div class="home-container">
    <!-- 侧边栏 -->
    <aside class="sidebar">
      <div class="sidebar-header">
        <h2>智能体安全网关</h2>
      </div>
      <el-menu
        :default-active="activeMenu"
        class="sidebar-menu"
        router
        :unique-opened="true"
        background-color="#001529"
        text-color="#fff"
        active-text-color="#ffd04b"
      >
        <el-menu-item index="/agent">
          <template #icon>
            <el-icon><Cpu /></el-icon>
          </template>
          <span>智能体管理</span>
        </el-menu-item>
        <el-menu-item index="/policy">
          <template #icon>
            <el-icon><Shield /></el-icon>
          </template>
          <span>防护策略</span>
        </el-menu-item>
        <el-menu-item index="/log">
          <template #icon>
            <el-icon><Document /></el-icon>
          </template>
          <span>日志管理</span>
        </el-menu-item>
        <el-menu-item index="/system">
          <template #icon>
            <el-icon><Setting /></el-icon>
          </template>
          <span>系统管理</span>
        </el-menu-item>
        <el-menu-item index="/nginx">
          <template #icon>
            <el-icon><Server /></el-icon>
          </template>
          <span>Nginx配置</span>
        </el-menu-item>
      </el-menu>
    </aside>
    
    <!-- 主内容区域 -->
    <main class="main-content">
      <!-- 顶部导航栏 -->
      <header class="top-header">
        <div class="header-left">
          <el-button type="text" @click="toggleSidebar">
            <el-icon><Menu /></el-icon>
          </el-button>
        </div>
        <div class="header-right">
          <el-dropdown>
            <span class="user-info">
              <el-icon><User /></el-icon>
              {{ username }}
              <el-icon class="el-icon--right"><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="handleLogout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </header>
      
      <!-- 内容区域 -->
      <div class="content-wrapper">
        <router-view />
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Cpu, Shield, Document, Setting, Server, Menu, User, ArrowDown } from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()

const username = ref('管理员')
const isSidebarCollapsed = ref(false)

// 计算当前激活的菜单
const activeMenu = computed(() => {
  return route.path
})

// 切换侧边栏折叠状态
const toggleSidebar = () => {
  isSidebarCollapsed.value = !isSidebarCollapsed.value
}

// 退出登录
const handleLogout = () => {
  localStorage.removeItem('token')
  router.push('/')
  ElMessage.success('退出登录成功')
}
</script>

<style scoped>
.home-container {
  display: flex;
  width: 100%;
  height: 100vh;
  overflow: hidden;
}

/* 侧边栏样式 */
.sidebar {
  width: 200px;
  height: 100%;
  background-color: #001529;
  color: #fff;
  transition: width 0.3s;
  overflow: hidden;
}

.sidebar.collapsed {
  width: 64px;
}

.sidebar-header {
  padding: 20px;
  text-align: center;
  border-bottom: 1px solid #1890ff;
}

.sidebar-header h2 {
  margin: 0;
  font-size: 18px;
  color: #fff;
}

.sidebar-menu {
  border-right: none;
  height: calc(100% - 70px);
}

/* 主内容区域样式 */
.main-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

/* 顶部导航栏样式 */
.top-header {
  height: 60px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
  background-color: #fff;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  z-index: 100;
}

.header-left {
  display: flex;
  align-items: center;
}

.header-right {
  display: flex;
  align-items: center;
}

.user-info {
  display: flex;
  align-items: center;
  cursor: pointer;
  padding: 8px 12px;
  border-radius: 4px;
  transition: background-color 0.3s;
}

.user-info:hover {
  background-color: #f5f7fa;
}

.user-info .el-icon {
  margin-right: 8px;
}

/* 内容区域样式 */
.content-wrapper {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
  background-color: #f5f7fa;
}
</style>