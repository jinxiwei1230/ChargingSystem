<template>
    <div class="users-container">
      <el-card>
        <div slot="header" class="header">
          <span>用户管理</span>
          <div class="header-actions">
            <el-input
              v-model="searchKeyword"
              placeholder="搜索用户名"
              style="width: 200px; margin-right: 10px"
              @input="handleSearch">
            </el-input>
            <el-button type="primary" @click="handleAdd">添加用户</el-button>
          </div>
        </div>
        
        <el-table
          :data="filteredUsers"
          style="width: 100%"
          v-loading="loading">
          <el-table-column
            prop="id"
            label="用户ID"
            width="100">
          </el-table-column>
          <el-table-column
            prop="username"
            label="用户名"
            width="150">
          </el-table-column>
          <el-table-column
            prop="registerTime"
            label="注册时间"
            width="180">
          </el-table-column>
          <el-table-column
            prop="balance"
            label="账户余额"
            width="120">
            <template slot-scope="scope">
              ¥{{ scope.row.balance }}
            </template>
          </el-table-column>
          <el-table-column
            prop="status"
            label="状态"
            width="100">
            <template slot-scope="scope">
              <el-tag :type="scope.row.status === 'active' ? 'success' : 'danger'">
                {{ scope.row.status === 'active' ? '正常' : '禁用' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column
            label="操作"
            width="250">
            <template slot-scope="scope">
              <el-button
                type="text"
                size="small"
                @click="handleEdit(scope.row)">
                编辑
              </el-button>
              <el-button
                type="text"
                size="small"
                @click="handleRecharge(scope.row)">
                充值
              </el-button>
              <el-button
                type="text"
                size="small"
                :type="scope.row.status === 'active' ? 'danger' : 'success'"
                @click="handleToggleStatus(scope.row)">
                {{ scope.row.status === 'active' ? '禁用' : '启用' }}
              </el-button>
            </template>
          </el-table-column>
        </el-table>
        
        <div class="pagination-container">
          <el-pagination
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
            :current-page="currentPage"
            :page-sizes="[10, 20, 50, 100]"
            :page-size="pageSize"
            layout="total, sizes, prev, pager, next, jumper"
            :total="total">
          </el-pagination>
        </div>
      </el-card>
  
      <!-- 用户表单对话框 -->
      <el-dialog
        :title="dialogType === 'add' ? '添加用户' : '编辑用户'"
        :visible.sync="dialogVisible"
        width="500px">
        <el-form
          :model="userForm"
          :rules="rules"
          ref="userForm"
          label-width="100px">
          <el-form-item label="用户名" prop="username">
            <el-input v-model="userForm.username" :disabled="dialogType === 'edit'"></el-input>
          </el-form-item>
          <el-form-item label="密码" prop="password" v-if="dialogType === 'add'">
            <el-input v-model="userForm.password" type="password"></el-input>
          </el-form-item>
          <el-form-item label="账户余额" prop="balance">
            <el-input-number v-model="userForm.balance" :min="0" :precision="2"></el-input-number>
          </el-form-item>
        </el-form>
        <div slot="footer" class="dialog-footer">
          <el-button @click="dialogVisible = false">取 消</el-button>
          <el-button type="primary" @click="submitForm" :loading="submitting">
            确 定
          </el-button>
        </div>
      </el-dialog>
  
      <!-- 充值对话框 -->
      <el-dialog
        title="用户充值"
        :visible.sync="rechargeDialogVisible"
        width="400px">
        <el-form
          :model="rechargeForm"
          :rules="rechargeRules"
          ref="rechargeForm"
          label-width="100px">
          <el-form-item label="充值金额" prop="amount">
            <el-input-number 
              v-model="rechargeForm.amount" 
              :min="0" 
              :precision="2"
              :step="100">
            </el-input-number>
          </el-form-item>
        </el-form>
        <div slot="footer" class="dialog-footer">
          <el-button @click="rechargeDialogVisible = false">取 消</el-button>
          <el-button type="primary" @click="submitRecharge" :loading="submitting">
            确 定
          </el-button>
        </div>
      </el-dialog>
    </div>
  </template>
  
  <script>
  export default {
    name: 'Users',
    data() {
      return {
        loading: false,
        searchKeyword: '',
        currentPage: 1,
        pageSize: 10,
        total: 0,
        users: [],
        dialogVisible: false,
        dialogType: 'add', // 'add' or 'edit'
        userForm: {
          username: '',
          password: '',
          balance: 0
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
          balance: [
            { required: true, message: '请输入账户余额', trigger: 'blur' }
          ]
        },
        submitting: false,
        rechargeDialogVisible: false,
        rechargeForm: {
          amount: 0
        },
        rechargeRules: {
          amount: [
            { required: true, message: '请输入充值金额', trigger: 'blur' }
          ]
        },
        currentUser: null
      }
    },
    computed: {
      filteredUsers() {
        return this.users.filter(user => 
          user.username.toLowerCase().includes(this.searchKeyword.toLowerCase())
        )
      }
    },
    methods: {
      handleSearch() {
        this.currentPage = 1
        this.fetchUsers()
      },
      handleSizeChange(val) {
        this.pageSize = val
        this.fetchUsers()
      },
      handleCurrentChange(val) {
        this.currentPage = val
        this.fetchUsers()
      },
      async fetchUsers() {
        this.loading = true
        try {
          // 这里应该调用API获取用户列表
          await new Promise(resolve => setTimeout(resolve, 1000))
          this.users = [
            {
              id: 'U001',
              username: 'user001',
              registerTime: '2024-03-15 10:00:00',
              balance: 100.00,
              status: 'active'
            }
            // 更多数据...
          ]
          this.total = this.users.length
        } catch (error) {
          this.$message.error('获取用户列表失败：' + error.message)
        } finally {
          this.loading = false
        }
      },
      handleAdd() {
        this.dialogType = 'add'
        this.userForm = {
          username: '',
          password: '',
          balance: 0
        }
        this.dialogVisible = true
      },
      handleEdit(row) {
        this.dialogType = 'edit'
        this.userForm = {
          username: row.username,
          balance: row.balance
        }
        this.dialogVisible = true
      },
      handleRecharge(row) {
        this.currentUser = row
        this.rechargeForm.amount = 0
        this.rechargeDialogVisible = true
      },
      async handleToggleStatus(row) {
        try {
          // 这里应该调用API修改用户状态
          await new Promise(resolve => setTimeout(resolve, 1000))
          row.status = row.status === 'active' ? 'disabled' : 'active'
          this.$message.success('状态修改成功')
        } catch (error) {
          this.$message.error('状态修改失败：' + error.message)
        }
      },
      submitForm() {
        this.$refs.userForm.validate(async valid => {
          if (valid) {
            this.submitting = true
            try {
              // 这里应该调用API保存用户信息
              await new Promise(resolve => setTimeout(resolve, 1000))
              this.$message.success(this.dialogType === 'add' ? '添加成功' : '修改成功')
              this.dialogVisible = false
              this.fetchUsers()
            } catch (error) {
              this.$message.error('操作失败：' + error.message)
            } finally {
              this.submitting = false
            }
          }
        })
      },
      submitRecharge() {
        this.$refs.rechargeForm.validate(async valid => {
          if (valid) {
            this.submitting = true
            try {
              // 这里应该调用API进行充值
              await new Promise(resolve => setTimeout(resolve, 1000))
              this.currentUser.balance += this.rechargeForm.amount
              this.$message.success('充值成功')
              this.rechargeDialogVisible = false
            } catch (error) {
              this.$message.error('充值失败：' + error.message)
            } finally {
              this.submitting = false
            }
          }
        })
      }
    },
    created() {
      this.fetchUsers()
    }
  }
  </script>
  
  <style scoped>
  .users-container {
    padding: 20px;
  }
  .header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
  .header-actions {
    display: flex;
    align-items: center;
  }
  .pagination-container {
    margin-top: 20px;
    text-align: right;
  }
  </style>