<template>
    <div class="home-container">
      <el-row :gutter="20">
        <!-- 统计卡片 -->
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-item">
              <div class="stat-title">总用户数</div>
              <div class="stat-value">{{ statistics.totalUsers }}</div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-item">
              <div class="stat-title">今日充电次数</div>
              <div class="stat-value">{{ statistics.todayChargingCount }}</div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-item">
              <div class="stat-title">今日收入</div>
              <div class="stat-value">¥{{ statistics.todayIncome }}</div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-item">
              <div class="stat-title">在线充电桩</div>
              <div class="stat-value">{{ statistics.onlinePiles }}</div>
            </div>
          </el-card>
        </el-col>
      </el-row>
  
      <!-- 充电站状态 -->
      <el-row :gutter="20" class="mt-20">
        <el-col :span="24">
          <el-card>
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
                        <p v-if="pile.status === 'working'">
                          当前用户：{{ pile.currentUser }}
                        </p>
                        <p v-if="pile.status === 'working'">
                          已充电量：{{ pile.chargedPower }}度
                        </p>
                        <el-button 
                          v-if="pile.status === 'fault'"
                          type="primary" 
                          size="small"
                          @click="handleRepair(pile)">
                          维修
                        </el-button>
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
                        <p v-if="spot.status === 'occupied'">
                          等待用户：{{ spot.waitingUser }}
                        </p>
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
        statistics: {
          totalUsers: 100,
          todayChargingCount: 25,
          todayIncome: 1250.00,
          onlinePiles: 5
        },
        chargingPiles: [
          { 
            id: 'A', 
            type: 'fast', 
            status: 'working',
            currentUser: 'user001',
            chargedPower: 15.5
          },
          { 
            id: 'B', 
            type: 'fast', 
            status: 'idle'
          },
          { 
            id: 'C', 
            type: 'slow', 
            status: 'working',
            currentUser: 'user002',
            chargedPower: 8.2
          },
          { 
            id: 'D', 
            type: 'slow', 
            status: 'fault'
          },
          { 
            id: 'E', 
            type: 'slow', 
            status: 'idle'
          }
        ],
        waitingSpots: [
          { status: 'occupied', waitingUser: 'user003' },
          { status: 'empty' },
          { status: 'occupied', waitingUser: 'user004' },
          { status: 'empty' },
          { status: 'empty' },
          { status: 'empty' }
        ]
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
      },
      handleRepair(pile) {
        this.$confirm('确认维修该充电桩吗？', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }).then(async () => {
          try {
            // 这里应该调用API进行维修
            await new Promise(resolve => setTimeout(resolve, 1000))
            pile.status = 'idle'
            this.$message.success('维修成功')
          } catch (error) {
            this.$message.error('维修失败：' + error.message)
          }
        }).catch(() => {})
      }
    }
  }
  </script>
  
  <style scoped>
  .home-container {
    padding: 20px;
  }
  .stat-card {
    margin-bottom: 20px;
  }
  .stat-item {
    text-align: center;
    padding: 20px;
  }
  .stat-title {
    font-size: 16px;
    color: #606266;
    margin-bottom: 10px;
  }
  .stat-value {
    font-size: 24px;
    color: #303133;
    font-weight: bold;
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
  .mt-20 {
    margin-top: 20px;
  }
  </style>