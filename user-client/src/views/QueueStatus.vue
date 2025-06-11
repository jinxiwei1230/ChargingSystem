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
          <!-- 充电进度 -->
          <div v-if="queueInfo.status === 'CHARGING'" class="detail-item charging-progress">
            <span class="label">充电进度：</span>
            <div class="progress-container">
              <el-progress 
                :percentage="parseFloat(chargingProgress.percentage)" 
                status="success"
                :format="percentageFormat"
                style="color: black;width: 200px; height: 20px;">
                <!-- 搞了半天居然是width和height的原因？！ -->
              </el-progress>
              <div class="progress-status">
                <span>充电进度：{{ chargingProgress.percentage }}% </span>
              </div>
            </div>
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
            @click="changeQueue" 
            :disabled="queueInfo.status === 'CHARGING'">
          重新排队
          </el-button>
          <el-button 
            type="success" 
            @click="handleFinishCharging">
            取消充电
          </el-button>
          <el-button type="success" @click="getWaitingQueue">
            查看等候区排队
          </el-button>
        </div>
      </div>
    </el-card>
    
    <el-empty v-else description="暂无排队信息">
      <el-button type="primary" @click="goToChargingRequestPage">
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
  </div>
</template>

<script>
import { 
  getQueueNumber, 
  getWaitingQueue, 
  getAheadNumber, 
  modifyChargingMode, 
  modifyChargingAmount,
  cancelAndLeave,
  getUserRequests,
  submitChargingRequest,
  finishCharging,
  getChargingPower,
  cancelAndRequeue
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
      progressTimer: null,
      chargingProgress: {
        percentage: '0',
        status: 'UNSTART'
      },
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
      ]
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
    if (this.progressTimer) {
      clearInterval(this.progressTimer)
    }
  },
  methods: {
    async fetchQueueInfo() {
      // 不再使用try-catch包裹整个方法，避免API错误被自动转为消息提示
      if (!this.userId) {
        return
      }
      
      // 创建一个不会触发全局错误处理的请求版本
      const fetchRequestsSafely = async () => {
        try {
          const response = await getUserRequests(this.userId)
          return { success: true, data: response }
        } catch (error) {
          return { success: false, error }
        }
      }
      
      // 安全地获取请求列表
      const result = await fetchRequestsSafely()
      
      // 处理成功响应
      if (result.success && result.data.code === 200 && result.data.data && result.data.data.length > 0) {
        // 获取最新的请求（id最大的）
        const latestRequest = result.data.data.reduce((prev, current) => 
          (prev.id > current.id) ? prev : current
        )
        
        // 检查关键字段是否发生变化，而不仅仅是ID
        const isStatusChanged = !this.queueInfo || 
                               this.queueInfo.id !== latestRequest.id || 
                               this.queueInfo.status !== latestRequest.status ||
                               this.queueInfo.amount !== latestRequest.amount ||
                               this.queueInfo.chargingPileId !== latestRequest.chargingPileId ||
                               this.queueInfo.chargingStartTime !== latestRequest.chargingStartTime;
        
        // 如果关键字段发生变化，则更新整个queueInfo
        if (isStatusChanged) {
          // 保存之前的waitingCount值，避免被重置为0后需要等待新请求
          const prevWaitingCount = this.queueInfo ? this.queueInfo.waitingCount : 0;
          
          this.queueInfo = {
            ...latestRequest,
            waitingCount: prevWaitingCount
          }
          
          // 如果状态是充电中，开始获取充电进度
          if (latestRequest.status === 'CHARGING') {
            // 清除之前的定时器并创建新的
            if (this.progressTimer) {
              clearInterval(this.progressTimer)
            }
            this.fetchChargingProgress() // 立即获取一次
            this.progressTimer = setInterval(this.fetchChargingProgress, 1000) // 每1秒更新一次
          } else {
            // 如果不是充电中状态，清除进度定时器
            if (this.progressTimer) {
              clearInterval(this.progressTimer)
              this.progressTimer = null
            }
          }
        }
        
        // 无论关键状态是否变化，都更新waitingCount
        // 只在状态为等待时才获取前车等待数量
        if (latestRequest.status === 'WAITING_IN_WAITING_AREA' || 
            latestRequest.status === 'WAITING_IN_CHARGING_AREA') {
          // 安全获取前车等待数量
          const fetchAheadNumberSafely = async () => {
            try {
              const response = await getAheadNumber(this.userId)
              return { success: true, data: response }
            } catch (error) {
              return { success: false, error }
            }
          }
          
          const aheadResult = await fetchAheadNumberSafely()
          if (aheadResult.success && aheadResult.data.code === 200) {
            // 直接更新waitingCount，保证即使其他字段没变化，waitingCount也能更新
            if (this.queueInfo) {
              this.queueInfo.waitingCount = aheadResult.data.data
            }
          }
        }
      } else {
        // 处理请求失败或没有记录的情况
        this.queueInfo = null
        // 清除进度定时器
        if (this.progressTimer) {
          clearInterval(this.progressTimer)
          this.progressTimer = null
        }
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
    
    goToChargingRequestPage() {
      this.$router.push('/charging-request')
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
    
    async changeQueue() {
      try {
        // 根据当前状态调用不同的接口
        if (this.queueInfo.status === 'CHARGING') {
          // 如果是充电状态，先结束充电再重新排队
          const finishResponse = await finishCharging(this.userId);
          if (finishResponse.code !== 200) {
            this.$message.error(finishResponse.message || '结束充电失败');
            return;
          }
          
          // 结束充电成功后，再重新排队
          const requeueResponse = await cancelAndRequeue(this.userId);
          if (requeueResponse.code === 200) {
            this.$message.success('已结束充电并重新排队');
            this.fetchQueueInfo(); // 刷新状态
          } else {
            this.$message.error(requeueResponse.message || '重新排队失败');
          }
        } else if (this.queueInfo.status === 'WAITING_IN_CHARGING_AREA') {
          // 如果是充电区等待状态，直接重新排队
          const requeueResponse = await cancelAndRequeue(this.userId);
          if (requeueResponse.code === 200) {
            this.$message.success('已取消当前请求并重新排队');
            this.fetchQueueInfo(); // 刷新状态
          } else {
            this.$message.error(requeueResponse.message || '重新排队失败');
          }
        } else {
          this.$message.warning('当前状态无法执行此操作');
        }
      } catch (error) {
        console.log('操作失败：', error.message);
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
    async handleFinishCharging() {
      try {
        // 根据当前状态调用不同的接口
        let response;
        if (this.queueInfo.status === 'WAITING_IN_WAITING_AREA'|| this.queueInfo.status === 'WAITING_IN_CHARGING_AREA') {
          // 等待状态调用取消并离开接口
          response = await cancelAndLeave(this.userId);
          if (response.code === 200) {
            this.$message.success('已取消充电并离开系统');
            this.queueInfo = null;
          } else {
            this.$message.error(response.message || '取消失败');
          }
        } else if (this.queueInfo.status === 'CHARGING') {
          // 充电状态调用结束充电接口
          response = await finishCharging(this.userId);
          if (response.code === 200) {
            this.$message.success('充电结束');
            this.fetchQueueInfo(); // 刷新状态
          } else {
            this.$message.error(response.message || '结束充电失败');
          }
        } else {
          this.$message.warning('当前状态无法执行此操作');
        }
      } catch (error) {
        this.$message.error('操作失败：' + error.message);
      }
    },
    // 充电进度百分比格式化
    percentageFormat(percentage) {
      return percentage + '%'
    },
    
    
    // 获取充电状态文本
    getChargingStatusText(status) {
      const statusMap = {
        'CHARGING': '充电中',
        'UNSTART': '未开始',
        'FAULTED': '故障中',
        'COMPLETED': '已完成'
      }
      return statusMap[status] || status
    },
    
    // 获取充电进度
    async fetchChargingProgress() {
      if (!this.queueInfo || !this.queueInfo.id || this.queueInfo.status !== 'CHARGING') {
        return
      }
      
      try {
        const response = await getChargingPower(this.queueInfo.id)
        if (response.code === 200) {
          this.chargingProgress = response.data
          // this.chargingProgress.percentage = response.data.percentage
          // this.chargingProgress.status = response.data.status
          console.log(this.chargingProgress.percentage)
          console.log(typeof this.chargingProgress.percentage);
        }
      } catch (error) {
        console.error('获取充电进度失败:', error)
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
.no-request-content {
  text-align: center;
  font-size: 16px;
  padding: 20px;
}
.charging-progress {
  margin-top: 10px;
}
.progress-container {
  display: flex;
  align-items: center;
}
.progress-status {
  margin-left: 10px;
}
.progress-container {
  width: 100%; /* 确保有宽度 */
}
</style> 