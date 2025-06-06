<template>
    <div class="profile-container">
      <el-row :gutter="20">
        <el-col :span="24">
          <el-card>
            <div slot="header">
              <span>个人信息</span>
              <el-button 
                type="primary" 
                size="small" 
                style="float: right"
                @click="showPasswordDialog">
                修改密码
              </el-button>
            </div>
            <div class="user-info">
              <div class="info-item">
                <span class="label">用户名：</span>
                <span class="value">{{ userInfo.username }}</span>
              </div>
              <div class="info-item">
                <span class="label">注册时间：</span>
                <span class="value">{{ userInfo.registerTime }}</span>
              </div>
              <div class="info-item">
                <span class="label">账户余额：</span>
                <span class="value">¥{{ userInfo.balance }}</span>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
  
      <!-- 修改密码对话框 -->
      <el-dialog
        title="修改密码"
        :visible.sync="passwordDialogVisible"
        width="500px"
        :close-on-click-modal="false">
        <el-form 
          :model="passwordForm" 
          :rules="passwordRules" 
          ref="passwordForm" 
          label-width="100px">
          <el-form-item label="原密码" prop="oldPassword">
            <el-input 
              v-model="passwordForm.oldPassword" 
              type="password" 
              placeholder="请输入原密码">
            </el-input>
          </el-form-item>
          <el-form-item label="新密码" prop="newPassword">
            <el-input 
              v-model="passwordForm.newPassword" 
              type="password" 
              placeholder="请输入新密码">
            </el-input>
          </el-form-item>
          <el-form-item label="确认密码" prop="confirmPassword">
            <el-input 
              v-model="passwordForm.confirmPassword" 
              type="password" 
              placeholder="请再次输入新密码">
            </el-input>
          </el-form-item>
        </el-form>
        <div slot="footer" class="dialog-footer">
          <el-button @click="passwordDialogVisible = false">取 消</el-button>
          <el-button type="primary" @click="changePassword" :loading="loading">
            确 定
          </el-button>
        </div>
      </el-dialog>
    </div>
  </template>
  
  <script>
  export default {
    name: 'Profile',
    data() {
      const validateConfirmPassword = (rule, value, callback) => {
        if (value !== this.passwordForm.newPassword) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      }
      
      return {
        userInfo: {
          username: 'test_user',
          registerTime: '2024-03-15',
          balance: 100.00
        },
        passwordForm: {
          oldPassword: '',
          newPassword: '',
          confirmPassword: ''
        },
        passwordRules: {
          oldPassword: [
            { required: true, message: '请输入原密码', trigger: 'blur' }
          ],
          newPassword: [
            { required: true, message: '请输入新密码', trigger: 'blur' },
            { min: 6, message: '密码长度不能小于6位', trigger: 'blur' }
          ],
          confirmPassword: [
            { required: true, message: '请再次输入新密码', trigger: 'blur' },
            { validator: validateConfirmPassword, trigger: 'blur' }
          ]
        },
        loading: false,
        passwordDialogVisible: false
      }
    },
    methods: {
      showPasswordDialog() {
        this.passwordDialogVisible = true
        this.$nextTick(() => {
          this.$refs.passwordForm && this.$refs.passwordForm.resetFields()
        })
      },
      changePassword() {
        this.$refs.passwordForm.validate(async valid => {
          if (valid) {
            this.loading = true
            try {
              // 这里应该调用API修改密码
              await new Promise(resolve => setTimeout(resolve, 1000))
              this.$message.success('密码修改成功')
              this.passwordDialogVisible = false
              this.$refs.passwordForm.resetFields()
            } catch (error) {
              this.$message.error('密码修改失败：' + error.message)
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
  .profile-container {
    padding: 20px;
  }
  .user-info {
    padding: 20px;
  }
  .info-item {
    margin-bottom: 15px;
    font-size: 16px;
  }
  .label {
    color: #606266;
    margin-right: 10px;
  }
  .value {
    color: #303133;
    font-weight: bold;
  }
  </style>