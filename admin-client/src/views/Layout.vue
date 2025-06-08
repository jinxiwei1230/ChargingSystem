<template>
    <el-container class="layout-container">
      <el-header>
        <div class="header-content">
          <h2>智能充电桩系统 - 管理后台</h2>
          <div class="user-info">
            <span>{{ userInfo ? userInfo.username : '' }}</span>
            <el-button type="text" @click="handleLogout">退出</el-button>
          </div>
        </div>
      </el-header>
      
      <el-container>
        <el-aside width="200px">
          <el-menu
            :router="true"
            :default-active="$route.path"
            class="el-menu-vertical">
            <el-menu-item index="/home">
              <i class="el-icon-s-home"></i>
              <span>首页</span>
            </el-menu-item>
            <el-menu-item index="/charging-piles">
              <i class="el-icon-lightning"></i>
              <span>充电桩管理</span>
            </el-menu-item>
            <el-menu-item index="/charging-queue">
              <i class="el-icon-time"></i>
              <span>等候车辆信息</span>
            </el-menu-item>
            <el-menu-item index="/charging-details">
              <i class="el-icon-document"></i>
              <span>充电详单</span>
            </el-menu-item>
            <!-- <el-menu-item index="/settings">
              <i class="el-icon-setting"></i>
              <span>系统设置</span>
            </el-menu-item> -->
          </el-menu>
        </el-aside>
        
        <el-main>
          <router-view></router-view>
        </el-main>
      </el-container>
    </el-container>
  </template>
  
  <script>
  import { mapGetters } from 'vuex'
  
  export default {
    name: 'Layout',
    computed: {
      ...mapGetters(['userInfo'])
    },
    methods: {
      handleLogout() {
        this.$confirm('确认退出登录吗？', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }).then(() => {
          this.$store.dispatch('logout')
          this.$router.push('/login')
          this.$message.success('已退出登录')
        }).catch(() => {})
      }
    }
  }
  </script>
  
  <style scoped>
  .layout-container {
    height: 100vh;
  }
  .header-content {
    display: flex;
    justify-content: space-between;
    align-items: center;
    height: 100%;
  }
  .el-header {
    background-color: #409EFF;
    color: white;
    line-height: 60px;
  }
  .el-aside {
    background-color: #f5f7fa;
  }
  .el-menu-vertical {
    height: 100%;
    border-right: none;
  }
  .user-info {
    display: flex;
    align-items: center;
    gap: 10px;
  }
  .user-info .el-button {
    color: white;
  }
  </style>