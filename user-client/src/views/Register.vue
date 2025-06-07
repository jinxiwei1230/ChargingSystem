<template>
  <div class="register-container">
    <el-form ref="registerForm" :model="registerForm" :rules="registerRules" class="register-form">
      <h3 class="title">用户注册</h3>
      <el-form-item prop="username">
        <el-input v-model="registerForm.username" placeholder="用户名">
          <template slot="prepend">
            <i class="el-icon-user"></i>
          </template>
        </el-input>
      </el-form-item>
      <el-form-item prop="password">
        <el-input v-model="registerForm.password" type="password" placeholder="密码">
          <template slot="prepend">
            <i class="el-icon-lock"></i>
          </template>
        </el-input>
      </el-form-item>
      <el-form-item prop="carNumber">
        <el-input v-model="registerForm.carNumber" placeholder="车牌号">
          <template slot="prepend">
            <i class="el-icon-truck"></i>
          </template>
        </el-input>
      </el-form-item>
      <el-button :loading="loading" type="primary" style="width:100%;" @click.native.prevent="handleRegister">
        注册
      </el-button>
      <div class="login-link">
        <router-link to="/login">已有账号？立即登录</router-link>
      </div>
    </el-form>
  </div>
</template>

<script>
import { register } from '@/api/user'

export default {
  name: 'Register',
  data() {
    return {
      registerForm: {
        username: '',
        password: '',
        carNumber: ''
      },
      registerRules: {
        username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
        password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
        carNumber: [{ required: true, message: '请输入车牌号', trigger: 'blur' }]
      },
      loading: false
    }
  },
  methods: {
    handleRegister() {
      this.$refs.registerForm.validate(valid => {
        if (valid) {
          this.loading = true
          register(this.registerForm)
            .then(response => {
              if (response.data.code === 200) {
                this.$message.success('注册成功')
                this.$router.push('/login')
              } else {
                this.$message.error(response.data.message || '注册失败')
              }
            })
            .catch(error => {
              this.$message.error('注册失败：' + error.message)
            })
            .finally(() => {
              this.loading = false
            })
        }
      })
    }
  }
}
</script>

<style scoped>
.register-container {
  min-height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: #f3f3f3;
}

.register-form {
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

.login-link {
  margin-top: 20px;
  text-align: center;
}
</style> 