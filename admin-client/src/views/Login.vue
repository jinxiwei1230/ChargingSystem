<template>
    <div class="login-container">
      <el-card class="login-card">
        <div slot="header">
          <h2>智能充电桩系统 - 管理员登录</h2>
        </div>
        <el-form :model="loginForm" :rules="rules" ref="loginForm" label-width="80px">
          <el-form-item label="用户名" prop="username">
            <el-input v-model="loginForm.username" placeholder="请输入管理员用户名"></el-input>
          </el-form-item>
          <el-form-item label="密码" prop="password">
            <el-input v-model="loginForm.password" type="password" placeholder="请输入密码"></el-input>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleLogin" :loading="loading">登录</el-button>
          </el-form-item>
        </el-form>
      </el-card>
    </div>
  </template>
  
  <script>
  import { mapActions } from 'vuex'
  
  export default {
    name: 'Login',
    data() {
      return {
        loginForm: {
          username: 'admin',
          password: 'admin'
        },
        rules: {
          username: [
            { required: true, message: '请输入用户名', trigger: 'blur' }
          ],
          password: [
            { required: true, message: '请输入密码', trigger: 'blur' }
          ]
        },
        loading: false
      }
    },
    methods: {
      ...mapActions(['login']),
      async handleLogin() {
        this.$refs.loginForm.validate(async valid => {
          if (valid) {
            this.loading = true
            try {
              await this.login(this.loginForm)
              this.$message.success('登录成功')
              this.$router.push('/')
            } catch (error) {
              this.$message.error('登录失败：' + error.message)
            } finally {
              this.loading = false
            }
          }
        })
      }
    }
  }
  </script>
  
  <style scoped>
  .login-container {
    height: 100vh;
    display: flex;
    justify-content: center;
    align-items: center;
    background-color: #f5f7fa;
  }
  .login-card {
    width: 400px;
  }
  .login-tip {
    text-align: center;
    color: #909399;
    font-size: 14px;
    margin-top: 10px;
  }
  </style>