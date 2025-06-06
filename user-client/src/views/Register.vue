<template>
    <div class="register-container">
      <el-card class="register-card">
        <div slot="header">
          <h2>智能充电桩系统 - 用户注册</h2>
        </div>
        <el-form 
          :model="registerForm" 
          :rules="rules" 
          ref="registerForm" 
          label-width="80px">
          <el-form-item label="用户名" prop="username">
            <el-input 
              v-model="registerForm.username" 
              placeholder="请输入用户名">
            </el-input>
          </el-form-item>
          <el-form-item label="密码" prop="password">
            <el-input 
              v-model="registerForm.password" 
              type="password" 
              placeholder="请输入密码">
            </el-input>
          </el-form-item>
          <el-form-item label="确认密码" prop="confirmPassword">
            <el-input 
              v-model="registerForm.confirmPassword" 
              type="password" 
              placeholder="请再次输入密码">
            </el-input>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleRegister" :loading="loading">
              注册
            </el-button>
            <el-button @click="goToLogin">返回登录</el-button>
          </el-form-item>
        </el-form>
      </el-card>
    </div>
  </template>
  
  <script>
  export default {
    name: 'Register',
    data() {
      const validateConfirmPassword = (rule, value, callback) => {
        if (value !== this.registerForm.password) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      }
      
      return {
        registerForm: {
          username: '',
          password: '',
          confirmPassword: ''
        },
        rules: {
          username: [
            { required: true, message: '请输入用户名', trigger: 'blur' },
            { min: 3, max: 20, message: '长度在 3 到 20 个字符', trigger: 'blur' }
          ],
          password: [
            { required: true, message: '请输入密码', trigger: 'blur' },
            { min: 6, message: '密码长度不能小于6位', trigger: 'blur' }
          ],
          confirmPassword: [
            { required: true, message: '请再次输入密码', trigger: 'blur' },
            { validator: validateConfirmPassword, trigger: 'blur' }
          ]
        },
        loading: false
      }
    },
    methods: {
      handleRegister() {
        this.$refs.registerForm.validate(async valid => {
          if (valid) {
            this.loading = true
            try {
              // 这里应该调用API进行注册
              await new Promise(resolve => setTimeout(resolve, 1000))
              this.$message.success('注册成功')
              this.$router.push('/login')
            } catch (error) {
              this.$message.error('注册失败：' + error.message)
            } finally {
              this.loading = false
            }
          }
        })
      },
      goToLogin() {
        this.$router.push('/login')
      }
    }
  }
  </script>
  
  <style scoped>
  .register-container {
    height: 100vh;
    display: flex;
    justify-content: center;
    align-items: center;
    background-color: #f5f7fa;
  }
  .register-card {
    width: 400px;
  }
  </style> 