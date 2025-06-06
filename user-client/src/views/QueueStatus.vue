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
            <span class="value">{{ queueInfo.mode === 'fast' ? '快充' : '慢充' }}</span>
          </div>
          <div class="detail-item">
            <span class="label">前车等待数量：</span>
            <span class="value">{{ queueInfo.waitingCount }}辆</span>
          </div>
          <div class="detail-item">
            <span class="label">预计等待时间：</span>
            <span class="value">{{ queueInfo.estimatedTime }}分钟</span>
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
        </div>
      </div>
    </el-card>
    
    <el-empty v-else description="暂无排队信息">
      <el-button type="primary" @click="goToChargingRequest">
        去提交充电请求
      </el-button>
    </el-empty>
  </div>
</template>

<script>
export default {
  name: 'QueueStatus',
  data() {
    return {
      queueInfo: {
        queueNumber: 'F001',
        mode: 'fast',
        waitingCount: 2,
        estimatedTime: 30
      }
    }
  },
  methods: {
    goToChargingRequest() {
      this.$router.push('/charging-request')
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