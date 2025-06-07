<template>
  <div class="charging-queue-container">
    <el-card>
      <div slot="header" style="height: 35px;">
        <span>充电桩等候服务车辆信息</span>
        <el-select v-model="selectedPileId" placeholder="选择充电桩" style="float: right; width: 180px" @change="handlePileChange">
          <el-option label="全部充电桩" value="all"></el-option>
          <el-option v-for="pile in chargingPiles" :key="pile.id" :label="pile.id" :value="pile.id"></el-option>
        </el-select>
      </div>
      
      <div v-if="loading" class="loading-container">
        <el-skeleton :rows="6" animated />
      </div>
      
      <div v-else>
        <div v-if="selectedPileId === 'all'">
          <!-- 显示所有充电桩的队列信息 -->
          <div v-for="(queueList, pileId) in queueInfo" :key="pileId" class="pile-queue-section">
            <div class="pile-header">
              <h3>充电桩: {{ pileId }}</h3>
              <el-tag :type="getPileStatusType(pileId)" size="small">{{ getPileStatusText(pileId) }}</el-tag>
            </div>
            
            <el-table v-if="queueList && queueList.length > 0" :data="queueList" border stripe size="small">
              <el-table-column prop="userId" label="用户ID" width="100"></el-table-column>
              <el-table-column prop="batteryCapacity" label="电池容量(度)" width="120"></el-table-column>
              <el-table-column prop="requestAmount" label="请求充电量(度)" width="130"></el-table-column>
              <el-table-column prop="queueNumber" label="队列号" width="100"></el-table-column>
              <el-table-column label="等待时长" width="180">
                <template slot-scope="scope">
                  {{ formatWaitingDuration(scope.row.waitingDuration) }}
                </template>
              </el-table-column>
              <el-table-column label="加入队列时间" width="180">
                <template slot-scope="scope">
                  {{ formatDateTime(scope.row.queueJoinTime) }}
                </template>
              </el-table-column>
              <el-table-column prop="requestId" label="请求ID" width="100"></el-table-column>
            </el-table>
            <el-empty v-else description="暂无等候车辆"></el-empty>
          </div>
        </div>
        <div v-else>
          <!-- 显示单个充电桩的队列详细信息 -->
          <el-table v-if="queueDetails && queueDetails.length > 0" :data="queueDetails" border>
            <el-table-column prop="userId" label="用户ID" width="100"></el-table-column>
            <el-table-column prop="batteryCapacity" label="电池容量(度)" width="120"></el-table-column>
            <el-table-column prop="requestAmount" label="请求充电量(度)" width="130"></el-table-column>
            <el-table-column prop="queueNumber" label="队列号" width="100"></el-table-column>
            <el-table-column label="等待时长" width="180">
              <template slot-scope="scope">
                {{ formatWaitingDuration(scope.row.waitingDuration) }}
              </template>
            </el-table-column>
            <el-table-column label="加入队列时间" width="180">
              <template slot-scope="scope">
                {{ formatDateTime(scope.row.queueJoinTime) }}
              </template>
            </el-table-column>
            <el-table-column prop="requestId" label="请求ID" width="100"></el-table-column>
            <el-table-column prop="pileId" label="充电桩ID" width="100"></el-table-column>
          </el-table>
          <el-empty v-else description="暂无等候车辆"></el-empty>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script>
import { getAllChargingPiles, getAllPileQueueInfo, getChargingQueueDetails } from '@/api/charge-pile'

export default {
  name: 'ChargingQueue',
  data() {
    return {
      loading: false,
      chargingPiles: [], // 所有充电桩
      selectedPileId: 'all', // 默认显示所有充电桩
      queueInfo: {}, // 所有充电桩的队列信息
      queueDetails: [], // 单个充电桩的队列详细信息
      pilesStatus: {} // 充电桩状态信息
    }
  },
  methods: {
    // 获取所有充电桩信息
    async fetchAllChargingPiles() {
      try {
        const response = await getAllChargingPiles()
        if (response.code === 200) {
          this.chargingPiles = response.data
          // 创建充电桩状态映射
          this.chargingPiles.forEach(pile => {
            this.pilesStatus[pile.id] = pile.status
          })
        } else {
          this.$message.error(response.message || '获取充电桩列表失败')
        }
      } catch (error) {
        console.error('获取充电桩列表出错：', error)
        this.$message.error('获取充电桩列表失败：' + error.message)
      }
    },
    
    // 获取所有充电桩的队列信息
    async fetchAllPileQueueInfo() {
      this.loading = true
      try {
        const response = await getAllPileQueueInfo()
        if (response.code === 200) {
          this.queueInfo = response.data
        } else {
          this.$message.error(response.message || '获取队列信息失败')
        }
      } catch (error) {
        console.error('获取队列信息出错：', error)
        this.$message.error('获取队列信息失败：' + error.message)
      } finally {
        this.loading = false
      }
    },
    
    // 获取单个充电桩的队列详细信息
    async fetchChargingQueueDetails(pileId) {
      this.loading = true
      try {
        const response = await getChargingQueueDetails(pileId)
        if (response.code === 200) {
          this.queueDetails = response.data
        } else {
          this.$message.error(response.message || '获取队列详情失败')
        }
      } catch (error) {
        console.error('获取队列详情出错：', error)
        this.$message.error('获取队列详情失败：' + error.message)
      } finally {
        this.loading = false
      }
    },
    
    // 处理充电桩选择变化
    handlePileChange(pileId) {
      if (pileId === 'all') {
        this.fetchAllPileQueueInfo()
      } else {
        this.fetchChargingQueueDetails(pileId)
      }
    },
    
    // 格式化等待时长（秒转为时分秒）
    formatWaitingDuration(seconds) {
      if (seconds == null || seconds === undefined) return '未知'
      
      const hours = Math.floor(seconds / 3600)
      const minutes = Math.floor((seconds % 3600) / 60)
      const remainSeconds = seconds % 60
      
      return `${hours}小时${minutes}分钟${remainSeconds}秒`
    },
    
    // 格式化日期时间
    formatDateTime(dateTimeStr) {
      if (!dateTimeStr) return '未知'
      
      const date = new Date(dateTimeStr)
      return date.toLocaleString('zh-CN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit',
        second: '2-digit',
        hour12: false
      })
    },
    
    // 获取充电桩状态类型
    getPileStatusType(pileId) {
      const status = this.pilesStatus[pileId]
      const types = {
        'AVAILABLE': 'success',
        'IN_USE': 'warning',
        'MAINTENANCE': 'info',
        'FAULT': 'danger',
        'OFFLINE': 'info'
      }
      return types[status] || 'info'
    },
    
    // 获取充电桩状态文本
    getPileStatusText(pileId) {
      const status = this.pilesStatus[pileId]
      const texts = {
        'AVAILABLE': '空闲',
        'IN_USE': '使用中',
        'MAINTENANCE': '维护中',
        'FAULT': '故障',
        'OFFLINE': '离线'
      }
      return texts[status] || '未知'
    },
    
    // 初始化数据
    async initData() {
      await this.fetchAllChargingPiles()
      this.fetchAllPileQueueInfo()
    }
  },
  created() {
    this.initData()
  }
}
</script>

<style scoped>
.charging-queue-container {
  padding: 20px;
}

.loading-container {
  padding: 20px 0;
}

.pile-queue-section {
  margin-bottom: 20px;
  padding-bottom: 20px;
  border-bottom: 1px dashed #eee;
}

.pile-queue-section:last-child {
  border-bottom: none;
}

.pile-header {
  display: flex;
  align-items: center;
  margin-bottom: 10px;
}

.pile-header h3 {
  margin: 0;
  margin-right: 10px;
}
</style> 