<template>
  <div class="home-container">
    <el-row :gutter="20">
      <el-col :span="24">
        <el-card class="status-card">
          <div slot="header">
            <span>充电站实时状态</span>
          </div>
          <div class="station-status">
            <div class="charging-area">
              <h3>充电区</h3>
              <el-row :gutter="20">
                <el-col :span="8" v-for="pile in chargingPiles" :key="pile.id">
                  <el-card :class="['pile-card', pile.status]">
                    <div class="pile-info">
                      <h4>{{ pile.id }}号充电桩</h4>
                      <p>类型：{{ pile.type === 'fast' ? '快充' : '慢充' }}</p>
                      <p>状态：{{ getStatusText(pile.status) }}</p>
                    </div>
                  </el-card>
                </el-col>
              </el-row>
            </div>
            
            <div class="waiting-area">
              <h3>等候区</h3>
              <el-row :gutter="20">
                <el-col :span="8" v-for="(spot, index) in waitingSpots" :key="index">
                  <el-card :class="['spot-card', spot.status]">
                    <div class="spot-info">
                      <h4>{{ index + 1 }}号车位</h4>
                      <p>状态：{{ spot.status === 'empty' ? '空闲' : '占用' }}</p>
                    </div>
                  </el-card>
                </el-col>
              </el-row>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script>
export default {
  name: 'Home',
  data() {
    return {
      chargingPiles: [
        { id: 'A', type: 'fast', status: 'working' },
        { id: 'B', type: 'fast', status: 'working' },
        { id: 'C', type: 'slow', status: 'working' },
        { id: 'D', type: 'slow', status: 'working' },
        { id: 'E', type: 'slow', status: 'working' }
      ],
      waitingSpots: Array(6).fill({ status: 'empty' })
    }
  },
  methods: {
    getStatusText(status) {
      const statusMap = {
        working: '工作中',
        idle: '空闲',
        fault: '故障'
      }
      return statusMap[status] || status
    }
  }
}
</script>

<style scoped>
.home-container {
  padding: 20px;
}
.status-card {
  margin-bottom: 20px;
}
.station-status {
  display: flex;
  flex-direction: column;
  gap: 20px;
}
.charging-area, .waiting-area {
  margin-bottom: 20px;
}
.pile-card, .spot-card {
  margin-bottom: 20px;
  transition: all 0.3s;
}
.pile-card.working {
  border-left: 4px solid #67C23A;
}
.pile-card.idle {
  border-left: 4px solid #909399;
}
.pile-card.fault {
  border-left: 4px solid #F56C6C;
}
.spot-card.empty {
  border-left: 4px solid #909399;
}
.spot-card.occupied {
  border-left: 4px solid #E6A23C;
}
.pile-info, .spot-info {
  text-align: center;
}
h3 {
  margin-bottom: 15px;
  color: #303133;
}
h4 {
  margin: 0 0 10px 0;
  color: #606266;
}
p {
  margin: 5px 0;
  color: #909399;
}
</style> 