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
            <span class="label">前车等待数量：</span>
            <span class="value">{{ queueInfo.waitingCount }}辆</span>
          </div>
        </div>
        
        <el-divider></el-divider>
        
        <div class="queue-actions">
          <el-button type="primary" @click="goToChargingRequest">
            修改请求
          </el-button>
          <el-button type="danger" @click="cancelQueue">
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
        <el-form-item label="充电模式">
          <el-select v-model="modifyForm.mode" placeholder="请选择充电模式">
            <el-option
              v-for="item in modeOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value">
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="充电量(kWh)">
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
import { getQueueNumber, getWaitingQueue, getAheadNumber, modifyChargingMode, modifyChargingAmount } from '@/api/schedule'
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
      ]
    }
  },
  computed: {
    ...mapGetters(['userId'])
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
        const response1 = await getQueueNumber(this.userId)
        if (response1.code === 200) {
          // 如果返回null，说明没有排队信息
          if (response1.data === null) {
            this.queueInfo = null
          } else {
            this.queueInfo.queueNumber = response1.data
            // 获取前车等待数量
            const response2 = await getAheadNumber(this.userId)
            if (response2.code === 200) {
              this.queueInfo.waitingCount = response2.data
            } else {
              this.$message.error(response2.message || '获取前车等待数量失败')
            }
          }
        } else {
          this.$message.error(response1.message || '获取排队信息失败')
        }
      } catch (error) {
        this.$message.error('获取排队信息失败：' + error.message)
      }
    },
    goToChargingRequest() {
      this.modifyDialogVisible = true
      // 初始化表单数据
      this.modifyForm = {
        mode: this.queueInfo.mode,
        amount: this.queueInfo.amount
      }
    },
    cancelQueue() {
      this.$confirm('确认取消排队吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          // 这里应该调用API取消排队
          await new Promise(resolve => setTimeout(resolve, 1000))
          this.queueInfo = null
          this.$message.success('已取消排队')
        } catch (error) {
          this.$message.error('取消失败：' + error.message)
        }
      }).catch(() => {})
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
        'CHARGING': '充电中',
        'COMPLETED': '已完成',
        'CANCELLED': '已取消'
      }
      return statusMap[status] || status
    },
    async handleModify() {
      try {
        // 修改充电模式
        if (this.modifyForm.mode !== this.queueInfo.mode) {
          const modeResponse = await modifyChargingMode({
            userId: this.userId,
            mode: this.modifyForm.mode
          })
          if (modeResponse.code === 200) {
            this.queueInfo.mode = this.modifyForm.mode
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
            this.queueInfo.amount = this.modifyForm.amount
            this.$message.success('充电量修改成功')
          } else {
            this.$message.error(amountResponse.message || '充电量修改失败')
            return
          }
        }

        this.modifyDialogVisible = false
      } catch (error) {
        this.$message.error('修改失败：' + error.message)
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
</style> 