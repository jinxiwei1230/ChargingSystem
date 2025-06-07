<template>
  <div class="queue-status-container">
    <el-card v-if="queueInfo">
      <div slot="header">
        <span>排队状态</span>
      </div>
      
      <div class="queue-info">
        <div class="queue-number">
          <h3>您的排队号码</h3>
          <div class="number">{{ queueInfo.queueNumber }}</div>
        </div>
        
        <el-divider></el-divider>
        
        <div class="queue-details">
          <div class="detail-item">
            <span class="label">充电模式：</span>
            <span class="value">{{ queueInfo.mode === 'FAST' ? '快充' : '慢充' }}</span>
          </div>
          <div class="detail-item">
            <span class="label">充电量：</span>
            <span class="value">{{ queueInfo.amount }}度</span>
          </div>
          <div class="detail-item">
            <span class="label">充电桩：</span>
            <span class="value">{{ queueInfo.chargingPileId || '未分配' }}</span>
          </div>
          <div class="detail-item">
            <span class="label">状态：</span>
            <span class="value">
              <el-tag :type="getStatusType(queueInfo.status)">
                {{ getStatusText(queueInfo.status) }}
              </el-tag>
            </span>
          </div>
          <div class="detail-item">
            <span class="label">排队时间：</span>
            <span class="value">{{ formatDateTime(queueInfo.queueJoinTime) }}</span>
          </div>
          <div class="detail-item">
            <span class="label">请求时间：</span>
            <span class="value">{{ formatDateTime(queueInfo.requestTime) }}</span>
          </div>
          <div class="detail-item">
            <span class="label">开始充电时间：</span>
            <span class="value">{{ queueInfo.chargingStartTime ? formatDateTime(queueInfo.chargingStartTime) : '未开始' }}</span>
          </div>
          <div class="detail-item">
            <span class="label">前车等待数量：</span>
            <span class="value">{{ queueInfo.waitingCount }}辆</span>
          </div>
        </div>
        
        <el-divider></el-divider>
        
        <div class="queue-actions">
          <el-button 
            type="primary" 
            @click="goToChargingRequest"
            :disabled="!canModifyRequest">
            修改请求
          </el-button>
          <el-button 
            type="danger" 
            @click="showCancelDialog">
            取消排队
          </el-button>
          <el-button type="success" @click="getWaitingQueue">
            查看排队情况
          </el-button>
        </div>
      </div>
    </el-card>
    
    <el-empty v-else description="暂无排队信息">
      <el-button type="primary" @click="goToChargingRequest">
        去提交充电请求
      </el-button>
    </el-empty>

    <!-- 添加排队情况表格对话框 -->
    <el-dialog
      title="当前排队情况"
      :visible.sync="dialogVisible"
      width="70%">
      <el-table
        :data="waitingQueueData"
        style="width: 100%">
        <el-table-column
          prop="userId"
          label="用户ID"
          width="100">
        </el-table-column>
        <el-table-column
          prop="queueNumber"
          label="排队号码"
          width="120">
        </el-table-column>
        <el-table-column
          prop="mode"
          label="充电模式"
          width="120">
          <template slot-scope="scope">
            {{ scope.row.mode === 'FAST' ? '快充' : '慢充' }}
          </template>
        </el-table-column>
        <el-table-column
          prop="amount"
          label="充电量(kWh)"
          width="120">
        </el-table-column>
        <el-table-column
          prop="queueJoinTime"
          label="排队时间"
          width="180">
          <template slot-scope="scope">
            {{ formatDateTime(scope.row.queueJoinTime) }}
          </template>
        </el-table-column>
        <el-table-column
          prop="status"
          label="状态"
          width="120">
          <template slot-scope="scope">
            {{ getStatusText(scope.row.status) }}
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>

    <!-- 修改充电请求对话框 -->
    <el-dialog
      title="修改充电请求"
      :visible.sync="modifyDialogVisible"
      width="30%">
      <el-form :model="modifyForm" label-width="100px">
        <el-form-item label="充电模式" v-if="canModifyMode">
          <el-select v-model="modifyForm.mode" placeholder="请选择充电模式">
            <el-option
              v-for="item in modeOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value">
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="充电量(度)">
          <el-input-number 
            v-model="modifyForm.amount" 
            :min="0" 
            :precision="1"
            :step="0.5">
          </el-input-number>
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button @click="modifyDialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="handleModify">确 定</el-button>
      </span>
    </el-dialog>

    <!-- 取消充电对话框 -->
    <el-dialog
      title="取消充电"
      :visible.sync="cancelDialogVisible"
      width="30%">
      <div class="cancel-options">
        <p>请选择取消后的操作：</p>
        <el-button 
          type="primary" 
          @click="showNewRequestDialog"
          :loading="cancelLoading">
          重新排队
        </el-button>
        <el-button 
          type="danger" 
          @click="handleCancelAndLeave"
          :loading="cancelLoading">
          离开系统
        </el-button>
      </div>
    </el-dialog>

    <!-- 新的充电请求对话框 -->
    <el-dialog
      title="提交充电请求"
      :visible.sync="newRequestDialogVisible"
      width="30%">
      <el-form 
        :model="newRequestForm" 
        :rules="newRequestRules"
        ref="newRequestForm" 
        label-width="100px">
        <el-form-item label="充电模式" prop="mode">
          <el-select v-model="newRequestForm.mode" placeholder="请选择充电模式">
            <el-option
              v-for="item in modeOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value">
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="充电量(度)" prop="requestAmount">
          <el-input-number 
            v-model="newRequestForm.requestAmount" 
            :min="0" 
            :precision="1"
            :step="0.5">
          </el-input-number>
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button @click="newRequestDialogVisible = false">取 消</el-button>
        <el-button 
          type="primary" 
          @click="handleNewRequest"
          :loading="newRequestLoading">
          提 交
        </el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import { 
  getQueueNumber, 
  getWaitingQueue, 
  getAheadNumber, 
  modifyChargingMode, 
  modifyChargingAmount,
  cancelAndRequeue,
  cancelAndLeave,
  getUserRequests,
  submitChargingRequest
} from '@/api/schedule'
import { mapGetters } from 'vuex'

export default {
  name: 'QueueStatus',
  data() {
    return {
      queueInfo: {
        mode:'',
        waitingCount: 0,
        queueNumber: '',
        amount: 0
      },
      timer: null,
      dialogVisible: false,
      waitingQueueData: [],
      modifyDialogVisible: false,
      modifyForm: {
        mode: '',
        amount: 0
      },
      modeOptions: [
        { label: '快充', value: 'FAST' },
        { label: '慢充', value: 'SLOW' }
      ],
      cancelDialogVisible: false,
      cancelLoading: false,
      newRequestDialogVisible: false,
      newRequestLoading: false,
      newRequestForm: {
        mode: 'FAST',
        requestAmount: 10.0
      },
      newRequestRules: {
        mode: [
          { required: true, message: '请选择充电模式', trigger: 'change' }
        ],
        requestAmount: [
          { required: true, message: '请输入充电量', trigger: 'blur' },
          { type: 'number', min: 0, message: '充电量必须大于0', trigger: 'blur' }
        ]
      }
    }
  },
  computed: {
    ...mapGetters(['userId']),
    // 判断是否可以修改请求
    canModifyRequest() {
      if (!this.queueInfo) return false
      return this.queueInfo.status === 'WAITING_IN_WAITING_AREA'
    },
    // 判断是否可以修改充电模式
    canModifyMode() {
      if (!this.queueInfo) return false
      return this.queueInfo.status === 'WAITING_IN_WAITING_AREA'
    }
  },
  created() {
    this.fetchQueueInfo()
    // 每1秒刷新一次排队信息
    this.timer = setInterval(this.fetchQueueInfo, 1000)
  },
  beforeDestroy() {
    if (this.timer) {
      clearInterval(this.timer)
    }
  },
  methods: {
    async fetchQueueInfo() {
      try {
        if (!this.userId) {
          this.$message.error('用户未登录')
          return
        }
        
        // 获取用户充电请求列表
        const response = await getUserRequests(this.userId)
        if (response.code === 200 && response.data && response.data.length > 0) {
          // 获取最新的请求（id最大的）
          const latestRequest = response.data.reduce((prev, current) => 
            (prev.id > current.id) ? prev : current
          )
          
          // 只有当状态发生变化时才更新整个queueInfo
          if (!this.queueInfo || this.queueInfo.id !== latestRequest.id) {
            this.queueInfo = {
              ...latestRequest,
              waitingCount: 0
            }
            
            // 只在状态为等待时才获取前车等待数量
            if (latestRequest.status === 'WAITING_IN_WAITING_AREA' || 
                latestRequest.status === 'WAITING_IN_CHARGING_AREA') {
              const aheadResponse = await getAheadNumber(this.userId)
              if (aheadResponse.code === 200) {
                this.queueInfo.waitingCount = aheadResponse.data
              }
            }
          }
        } else {
          this.queueInfo = null
        }
      } catch (error) {
        this.$message.error('获取排队信息失败：' + error.message)
      }
    },
    showCancelDialog() {
      this.cancelDialogVisible = true
    },
    
    async handleCancelAndRequeue() {
      try {
        this.cancelLoading = true
        const response = await cancelAndRequeue(this.userId)
        if (response.code === 200) {
          this.$message.success('已取消充电并重新排队')
          this.cancelDialogVisible = false
          this.fetchQueueInfo() // 刷新状态
        } else {
          this.$message.error(response.message || '取消失败')
        }
      } catch (error) {
        this.$message.error('取消失败：' + error.message)
      } finally {
        this.cancelLoading = false
      }
    },
    
    async handleCancelAndLeave() {
      try {
        this.cancelLoading = true
        const response = await cancelAndLeave(this.userId)
        if (response.code === 200) {
          this.$message.success('已取消充电并离开系统')
          this.cancelDialogVisible = false
          this.queueInfo = null
        } else {
          this.$message.error(response.message || '取消失败')
        }
      } catch (error) {
        this.$message.error('取消失败：' + error.message)
      } finally {
        this.cancelLoading = false
      }
    },
    
    goToChargingRequest() {
      if (!this.canModifyRequest) {
        this.$message.warning('当前状态不允许修改请求')
        return
      }
      this.modifyDialogVisible = true
      // 初始化表单数据
      this.modifyForm = {
        mode: this.queueInfo.mode,
        amount: this.queueInfo.amount
      }
    },
    
    async getWaitingQueue() {
      try {
        const response = await getWaitingQueue()
        if (response.code === 200) {
          this.waitingQueueData = response.data
          this.dialogVisible = true
        } else {
          this.$message.error(response.message || '获取排队情况失败')
        }
      } catch (error) {
        this.$message.error('获取排队情况失败：' + error.message)
      }
    },
    formatDateTime(dateTimeStr) {
      if (!dateTimeStr) return ''
      const date = new Date(dateTimeStr)
      return date.toLocaleString()
    },
    getStatusText(status) {
      const statusMap = {
        'WAITING_IN_WAITING_AREA': '等候区等待',
        'WAITING_IN_CHARGING_AREA': '充电区等待',
        'CHARGING': '充电中',
        'COMPLETED': '已完成',
        'CANCELED': '已取消'
      }
      return statusMap[status] || status
    },
    getStatusType(status) {
      const typeMap = {
        'WAITING_IN_WAITING_AREA': 'warning',
        'WAITING_IN_CHARGING_AREA': 'info',
        'CHARGING': 'success',
        'COMPLETED': '',
        'CANCELED': 'danger'
      }
      return typeMap[status] || ''
    },
    async handleModify() {
      try {
        // 修改充电模式
        if (this.canModifyMode && this.modifyForm.mode !== this.queueInfo.mode) {
          const modeResponse = await modifyChargingMode({
            userId: this.userId,
            mode: this.modifyForm.mode
          })
          if (modeResponse.code === 200) {
            this.$message.success('充电模式修改成功')
          } else {
            this.$message.error(modeResponse.message || '充电模式修改失败')
            return
          }
        }

        // 修改充电量
        if (this.modifyForm.amount !== this.queueInfo.amount) {
          const amountResponse = await modifyChargingAmount({
            userId: this.userId,
            requestAmount: this.modifyForm.amount
          })
          if (amountResponse.code === 200) {
            this.$message.success('充电量修改成功')
          } else {
            this.$message.error(amountResponse.message || '充电量修改失败')
            return
          }
        }

        this.modifyDialogVisible = false
        this.fetchQueueInfo() // 刷新状态
      } catch (error) {
        this.$message.error('修改失败：' + error.message)
      }
    },
    showNewRequestDialog() {
      this.cancelDialogVisible = false
      this.newRequestDialogVisible = true
      // 初始化表单数据
      this.newRequestForm = {
        mode: 'FAST',
        requestAmount: 10.0
      }
    },
    
    async handleNewRequest() {
      try {
        this.$refs.newRequestForm.validate(async valid => {
          if (valid) {
            this.newRequestLoading = true
            // 先取消当前请求
            const cancelResponse = await cancelAndRequeue(this.userId)
            if (cancelResponse.code === 200) {
              // 提交新的充电请求
              const response = await submitChargingRequest({
                userId: this.userId,
                mode: this.newRequestForm.mode,
                requestAmount: this.newRequestForm.requestAmount
              })
              
              if (response.code === 200) {
                this.$message.success('充电请求提交成功')
                this.newRequestDialogVisible = false
                this.fetchQueueInfo() // 刷新状态
              } else {
                this.$message.error(response.message || '充电请求提交失败')
              }
            } else {
              this.$message.error(cancelResponse.message || '取消当前请求失败')
            }
          }
        })
      } catch (error) {
        this.$message.error('操作失败：' + error.message)
      } finally {
        this.newRequestLoading = false
      }
    }
  }
}
</script>

<style scoped>
.queue-status-container {
  padding: 20px;
}
.queue-info {
  text-align: center;
}
.queue-number {
  margin-bottom: 20px;
}
.number {
  font-size: 48px;
  font-weight: bold;
  color: #409EFF;
  margin: 20px 0;
}
.queue-details {
  text-align: left;
  max-width: 400px;
  margin: 0 auto;
}
.detail-item {
  margin: 15px 0;
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
.queue-actions {
  margin-top: 20px;
}
.cancel-options {
  text-align: center;
  padding: 20px;
}
.cancel-options .el-button {
  margin: 10px;
}
</style> 