<template>
  <div class="login-container">
    <el-form ref="loginForm" :model="loginForm" :rules="loginRules" class="login-form">
      <h3 class="title">用户登录</h3>
      <el-form-item prop="username">
        <el-input v-model="loginForm.username" placeholder="用户名">
          <template slot="prepend">
            <i class="el-icon-user"></i>
          </template>
        </el-input>
      </el-form-item>
      <el-form-item prop="password">
        <el-input v-model="loginForm.password" type="password" placeholder="密码">
          <template slot="prepend">
            <i class="el-icon-lock"></i>
          </template>
        </el-input>
      </el-form-item>
      <el-button :loading="loading" type="primary" style="width:100%;" @click.native.prevent="handleLogin">
        登录
      </el-button>
      <div class="register-link">
        <router-link to="/register">没有账号？立即注册</router-link>
      </div>
    </el-form>
  </div>
</template>
<script>
import { mapActions } from 'vuex'
export default {
  name: 'Login',
  data() {
    return {
      loginForm: {
        username: '',
        password: ''
      },
      loginRules: {
        username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
        password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
      },
      loading: false
    }
  },
  methods: {
    ...mapActions(['login']),
    handleLogin() {
      this.$refs.loginForm.validate(async valid => {
        if (valid) {
          this.loading = true
          try {
            await this.login(this.loginForm)
            this.$message.success('登录成功')
            this.$router.replace('/')
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
  min-height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: #f3f3f3;
}
.login-form {
  width: 400px;
  padding: 30px;
  background: #fff;
  border-radius: 6px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}
.title {
  text-align: center;
  margin-bottom: 30px;
  color: #333;
}
.register-link {
  margin-top: 20px;
  text-align: center;
}
</style> 