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
              <div class="stat-value">{{ chargingPiles.length }}</div>
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
              <div class="charging-area" v-loading="pileLoading">
                <h3>充电区</h3>
                <el-row :gutter="20">
                  <el-col :span="8" v-for="pile in chargingPiles" :key="pile.id">
                    <el-card :class="['pile-card', getPileStatusClass(pile.status)]">
                      <div class="pile-info">
                        <h4>{{ pile.id }}号充电桩</h4>
                        <p>类型：{{ pile.type === 'FAST' ? '快充' : '慢充' }}</p>
                        <p>状态：{{ getPileStatusText(pile.status) }}</p>
                        <p>功率：{{ pile.chargingPower }} kW</p>
                        <p>累计充电次数：{{ pile.chargingTimes }} 次</p>
                        <p>累计充电时长：{{ pile.totalChargingDuration.toFixed(1) }} 小时</p>
                        <p>累计充电量：{{ pile.totalChargingAmount.toFixed(1) }} 度</p>
                        <el-button 
                          v-if="pile.status === 'FAULT'"
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
  import { getAllChargingPiles, handleChargingPileFault, handleChargingPileRecovery } from '@/api/charge-pile'

  export default {
    name: 'Home',
    data() {
      return {
        loading: false,
        pileLoading: false,
        settingsDialogVisible: false,
        statistics: {
          monthChargingCount: 0,
          monthIncome: 0,
        },
        chargingPiles: [],
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
      
      getPileStatusClass(status) {
        const classMap = {
          'AVAILABLE': 'available',
          'IN_USE': 'working',
          'MAINTENANCE': 'maintenance',
          'FAULT': 'fault',
          'OFFLINE': 'offline'
        }
        return classMap[status] || 'unknown'
      },
      
      getPileStatusText(status) {
        const statusMap = {
          'AVAILABLE': '空闲',
          'IN_USE': '使用中',
          'MAINTENANCE': '维护中',
          'FAULT': '故障',
          'OFFLINE': '离线'
        }
        return statusMap[status] || '未知'
      },
      
      handleRepair(pile) {
        this.$confirm('确认维修该充电桩吗？', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }).then(async () => {
          try {
            // 调用故障处理API
            if (pile.status === 'FAULT') {
              // 询问用户选择调度策略
              this.$confirm('请选择调度策略', '调度策略选择', {
                confirmButtonText: '优先级调度',
                cancelButtonText: '时间顺序调度',
                distinguishCancelAndClose: true,
                type: 'info'
              }).then(async () => {
                // 优先级调度
                const response = await handleChargingPileFault(pile.id, 'PRIORITY')
                if (response.code === 200) {
                  this.$message.success('故障已处理，采用优先级调度')
                  this.fetchChargingPiles() // 刷新充电桩状态
                } else {
                  this.$message.error(response.message || '故障处理失败')
                }
              }).catch(action => {
                if (action === 'cancel') {
                  // 时间顺序调度
                  handleChargingPileFault(pile.id, 'TIME_ORDER').then(response => {
                    if (response.code === 200) {
                      this.$message.success('故障已处理，采用时间顺序调度')
                      this.fetchChargingPiles() // 刷新充电桩状态
                    } else {
                      this.$message.error(response.message || '故障处理失败')
                    }
                  }).catch(error => {
                    this.$message.error('故障处理失败：' + error.message)
                  })
                }
              })
            } else if (pile.status === 'MAINTENANCE') {
              // 处理维护完成，恢复充电桩
              const response = await handleChargingPileRecovery(pile.id)
              if (response.code === 200) {
                this.$message.success('充电桩已恢复正常')
                this.fetchChargingPiles() // 刷新充电桩状态
              } else {
                this.$message.error(response.message || '恢复充电桩失败')
              }
            }
          } catch (error) {
            this.$message.error('操作失败：' + error.message)
          }
        }).catch(() => {})
      },

      // 获取充电桩信息
      async fetchChargingPiles() {
        this.pileLoading = true
        try {
          const response = await getAllChargingPiles()
          if (response.code === 200) {
            this.chargingPiles = response.data
          } else {
            this.$message.error(response.message || '获取充电桩信息失败')
          }
        } catch (error) {
          console.error('获取充电桩信息出错：', error)
          this.$message.error('获取充电桩信息失败：' + error.message)
        } finally {
          this.pileLoading = false
        }
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
      this.fetchChargingPiles()
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
  .charging-area {
    margin-bottom: 20px;
  }
  .pile-card {
    margin-bottom: 20px;
    transition: all 0.3s;
  }
  .pile-card.working {
    border-left: 4px solid #E6A23C;
  }
  .pile-card.available {
    border-left: 4px solid #67C23A;
  }
  .pile-card.fault {
    border-left: 4px solid #F56C6C;
  }
  .pile-card.maintenance {
    border-left: 4px solid #409EFF;
  }
  .pile-card.offline {
    border-left: 4px solid #909399;
  }
  .pile-info {
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