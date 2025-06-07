<template>
    <div class="home-container">
      <el-row :gutter="20">
        <!-- 统计卡片 -->
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-item">
              <div class="stat-title">本月充电次数</div>
              <div class="stat-value">{{ statistics.monthChargingCount }}</div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-item">
              <div class="stat-title">本月收入</div>
              <div class="stat-value">¥{{ statistics.monthIncome }}</div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-item">
              <div class="stat-title">充电桩数量</div>
              <div class="stat-value">{{ 5 }}</div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card clickable">
            <div class="stat-item">
              <div class="stat-title">系统设置</div>
              <el-button type="primary" size="small" @click="openSettingsDialog">
                查看设置
              </el-button>
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

      <!-- 系统设置对话框 -->
      <el-dialog title="系统设置" :visible.sync="settingsDialogVisible" width="40%" :before-close="handleDialogClose">
        <el-tabs>
          <el-tab-pane label="电价设置">
            <div class="settings-content">
              <div class="setting-item">
                <div class="setting-label">平时电价</div>
                <div class="setting-value">{{ priceForm.normalPrice }} <span class="unit">元/度</span></div>
              </div>
              <div class="setting-item">
                <div class="setting-label">峰时电价</div>
                <div class="setting-value">{{ priceForm.peakPrice }} <span class="unit">元/度</span></div>
              </div>
              <div class="setting-item">
                <div class="setting-label">谷时电价</div>
                <div class="setting-value">{{ priceForm.valleyPrice }} <span class="unit">元/度</span></div>
              </div>
              <div class="setting-item">
                <div class="setting-label">服务费</div>
                <div class="setting-value">{{ priceForm.servicePrice }} <span class="unit">元/度</span></div>
              </div>
            </div>
          </el-tab-pane>
          
          <el-tab-pane label="时段设置">
            <div class="settings-content">
              <div class="setting-item">
                <div class="setting-label">峰时时段</div>
                <div class="setting-value">
                  {{ formatTime(timeForm.peakStart) }} - {{ formatTime(timeForm.peakEnd) }}
                </div>
              </div>
              <div class="setting-item">
                <div class="setting-label">谷时时段</div>
                <div class="setting-value">
                  {{ formatTime(timeForm.valleyStart) }} - {{ formatTime(timeForm.valleyEnd) }}
                </div>
              </div>
            </div>
          </el-tab-pane>
          
          <el-tab-pane label="系统参数">
            <div class="settings-content">
              <div class="setting-item">
                <div class="setting-label">最大等待数量</div>
                <div class="setting-value">{{ systemForm.maxWaitingCount }} <span class="unit">辆</span></div>
              </div>
              <div class="setting-item">
                <div class="setting-label">快充功率</div>
                <div class="setting-value">{{ systemForm.fastChargingPower }} <span class="unit">kW/h</span></div>
              </div>
              <div class="setting-item">
                <div class="setting-label">慢充功率</div>
                <div class="setting-value">{{ systemForm.slowChargingPower }} <span class="unit">kW/h</span></div>
              </div>
            </div>
          </el-tab-pane>
        </el-tabs>
      </el-dialog>
    </div>
  </template>
  
  <script>
  import { getChargingSystemSummaryReport } from '@/api/tables'

  export default {
    name: 'Home',
    data() {
      return {
        loading: false,
        settingsDialogVisible: false,
        statistics: {
          monthChargingCount: 0,
          monthIncome: 0,
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
        ],
        // 系统设置相关数据
        priceForm: {
          normalPrice: 0.7,
          peakPrice: 1.0,
          valleyPrice: 0.4,
          servicePrice: 0.8
        },
        timeForm: {
          peakStart: new Date(2000, 0, 1, 10, 0),
          peakEnd: new Date(2000, 0, 1, 15, 0),
          valleyStart: new Date(2000, 0, 1, 23, 0),
          valleyEnd: new Date(2000, 0, 1, 7, 0)
        },
        systemForm: {
          maxWaitingCount: 10,
          fastChargingPower: 30,
          slowChargingPower: 7
        }
      }
    },
    methods: {
      openSettingsDialog() {
        this.settingsDialogVisible = true
      },
      
      handleDialogClose() {
        this.settingsDialogVisible = false
      },
      
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
      },

      // 获取本月统计数据
      async fetchMonthlyStatistics() {
        try {
          const response = await getChargingSystemSummaryReport({ timeType: 'month' })
          if (response.code === 200 && response.data && response.data.summary) {
            const summary = response.data.summary
            this.statistics.monthChargingCount = summary.totalChargingCount || 0
            this.statistics.monthIncome = summary.totalFee || 0
          }
        } catch (error) {
          console.error('获取月度统计数据失败：', error)
        }
      },

      // 格式化时间显示
      formatTime(time) {
        if (!time) return '--:--'
        const hours = time.getHours().toString().padStart(2, '0')
        const minutes = time.getMinutes().toString().padStart(2, '0')
        return `${hours}:${minutes}`
      }
    },
    created() {
      this.fetchMonthlyStatistics()
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
  .time-separator {
    margin: 0 10px;
  }
  .clickable {
    cursor: pointer;
    transition: all 0.3s;
  }
  .clickable:hover {
    box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  }
  .settings-content {
    padding: 10px 0;
  }
  .setting-item {
    display: flex;
    justify-content: space-between;
    padding: 15px 20px;
    border-bottom: 1px solid #f0f0f0;
    font-size: 16px;
  }
  .setting-item:last-child {
    border-bottom: none;
  }
  .setting-label {
    color: #606266;
    font-weight: 500;
  }
  .setting-value {
    color: #303133;
    font-weight: bold;
  }
  .unit {
    font-size: 14px;
    color: #909399;
    font-weight: normal;
    margin-left: 4px;
  }
  </style>