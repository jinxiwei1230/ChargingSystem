<template>
    <div class="charging-piles-container">
      <el-card>
        <div slot="header">
          <span>充电桩管理</span>
          <el-button style="float: right" type="primary" size="small" @click="handleAdd">添加充电桩</el-button>
        </div>
        
        <el-table :data="chargingPiles" v-loading="loading" border>
          <el-table-column prop="id" label="充电桩ID" width="100"></el-table-column>
          <el-table-column prop="type" label="类型" width="100">
            <template slot-scope="scope">
              <el-tag :type="scope.row.type === 'FAST' ? 'success' : 'info'">
                {{ scope.row.type === 'FAST' ? '快充' : '慢充' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="100">
            <template slot-scope="scope">
              <el-tag :type="getStatusType(scope.row.status)">
                {{ getStatusText(scope.row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="chargingPower" label="功率(kW)" width="120"></el-table-column>
          <el-table-column prop="chargingTimes" label="累计充电次数" width="120"></el-table-column>
          <el-table-column prop="totalChargingDuration" label="充电总时长(h)" width="120"></el-table-column>
          <el-table-column prop="totalChargingAmount" label="充电总电量(kWh)" width="120"></el-table-column>
          <el-table-column prop="updatedAt" label="最后更新时间" width="180">
            <template slot-scope="scope">
              {{ formatDateTime(scope.row.updatedAt) }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="280">
            <template slot-scope="scope">
              <el-button size="mini" @click="handleEdit(scope.row)" style="margin-right: 10px">编辑</el-button>
              <el-button 
                size="mini" 
                :type="scope.row.status === 'FAULT' ? 'warning' : 'primary'"
                @click="handleMaintenance(scope.row)" 
                style="margin-right: 10px"
              >{{ scope.row.status === 'FAULT' ? '维护' : '故障' }}</el-button>
              <el-button 
                size="mini" 
                :type="scope.row.status === 'AVAILABLE' ? 'danger' : 'success'"
                @click="handleToggleStatus(scope.row)"
                :disabled="scope.row.status === 'FAULT' || scope.row.status === 'MAINTENANCE'"
              >{{ scope.row.status === 'AVAILABLE' ? '关闭' : '启动' }}</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-card>
  
      <!-- 添加/编辑充电桩对话框 -->
      <el-dialog :title="dialogTitle" :visible.sync="dialogVisible" width="500px">
        <el-form :model="form" :rules="rules" ref="form" label-width="100px">
          <el-form-item label="充电桩ID">
            <el-input v-model="form.pileId" disabled></el-input>
          </el-form-item>
          <el-form-item label="充电功率(kW)" prop="chargingPower">
            <el-input-number v-model="form.chargingPower" :min="0" :max="100"></el-input-number>
          </el-form-item>
          <el-form-item v-if="dialogTitle === '编辑充电桩参数'" label="当前状态">
            <el-tag :type="getStatusType(selectedPile.status)">
              {{ getStatusText(selectedPile.status) }}
            </el-tag>
          </el-form-item>
          <el-form-item v-if="dialogTitle === '编辑充电桩参数'" label="充电次数">
            <span>{{ selectedPile.chargingTimes || 0 }}</span>
          </el-form-item>
        </el-form>
        <div slot="footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmit" :loading="submitting">确定</el-button>
        </div>
      </el-dialog>
    </div>
  </template>
  
  <script>
  import { 
    getAllChargingPiles, 
    powerOnChargingPile, 
    powerOffChargingPile, 
    setChargingPileParameters, 
    handleChargingPileFault, 
    handleChargingPileRecovery 
  } from '@/api/charge-pile'

  export default {
    name: 'ChargingPiles',
    data() {
      return {
        loading: false,
        chargingPiles: [],
        dialogVisible: false,
        dialogTitle: '',
        form: {
          pileId: '',
          chargingPower: 0
        },
        selectedPile: {}, // 用于存储当前选中的充电桩完整信息
        rules: {
          chargingPower: [
            { required: true, message: '请输入充电功率', trigger: 'blur' },
            { type: 'number', min: 0, message: '充电功率必须大于0', trigger: 'blur' }
          ]
        },
        submitting: false,
        // 添加故障处理策略选项
        strategyOptions: [
          { value: 'PRIORITY', label: '优先级调度' },
          { value: 'TIME_ORDER', label: '时间顺序调度' }
        ],
        selectedStrategy: 'TIME_ORDER'
      }
    },
    methods: {
      getStatusType(status) {
        const types = {
          'AVAILABLE': 'success',
          'IN_USE': 'warning',
          'MAINTENANCE': 'info',
          'FAULT': 'danger',
          'OFFLINE': 'info'
        }
        return types[status] || 'info'
      },
      getStatusText(status) {
        const texts = {
          'AVAILABLE': '空闲',
          'IN_USE': '使用中',
          'MAINTENANCE': '维护中',
          'FAULT': '故障',
          'OFFLINE': '离线'
        }
        return texts[status] || '未知'
      },
      formatDateTime(dateTimeStr) {
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
      handleAdd() {
        this.dialogTitle = '添加充电桩'
        this.form = {
          pileId: '',
          chargingPower: 0
        }
        this.dialogVisible = true
      },
      handleEdit(row) {
        this.dialogTitle = '编辑充电桩参数'
        this.selectedPile = { ...row }
        this.form = {
          pileId: row.id,
          chargingPower: row.chargingPower
        }
        this.dialogVisible = true
      },
      handleMaintenance(row) {
        if (row.status === 'FAULT') {
          // 故障状态转为维护状态
          this.$confirm('是否开始对该充电桩进行维护？', '提示', {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning'
          }).then(async () => {
            try {
              // 调用故障恢复API
              const response = await handleChargingPileRecovery(row.id)
              if (response.code === 200) {
                row.status = 'MAINTENANCE'
                this.$message.success(response.message || '充电桩已进入维护状态')
              } else {
                this.$message.error(response.message || '操作失败')
              }
            } catch (error) {
              console.error('处理充电桩故障恢复出错：', error)
              this.$message.error('操作失败：' + error.message)
            }
          }).catch(() => {})
        } else {
          // 非故障状态转为故障状态，需要选择调度策略
          this.$alert('', '请选择故障处理的调度策略', {
            confirmButtonText: '确定',
            showCancelButton: true,
            customClass: 'strategy-dialog',
            beforeClose: (action, instance, done) => {
              if (action === 'confirm') {
                // 确认将充电桩标记为故障状态
                this.handleFaultWithStrategy(row, instance.inputValue)
              }
              done()
            },
            dangerouslyUseHTMLString: true,
            message: this.renderStrategySelector()
          })
        }
      },
      handleToggleStatus(row) {
        const newStatus = row.status === 'AVAILABLE' ? 'OFFLINE' : 'AVAILABLE'
        const action = newStatus === 'AVAILABLE' ? '开启' : '关闭'
        const apiMethod = newStatus === 'AVAILABLE' ? powerOnChargingPile : powerOffChargingPile

        this.$confirm(`确认${action}该充电桩吗？`, '提示', {
          type: 'warning'
        }).then(async () => {
          try {
            const response = await apiMethod(row.id)
            if (response.code === 200) {
              this.$message.success(response.message)
              // 更新本地状态
              row.status = newStatus
            } else {
              this.$message.error(response.message || `${action}失败`)
            }
          } catch (error) {
            console.error(`${action}充电桩出错：`, error)
            this.$message.error(`${action}失败：` + error.message)
          }
        }).catch(() => {})
      },
      handleSubmit() {
        this.$refs.form.validate(async valid => {
          if (valid) {
            this.submitting = true
            try {
              const response = await setChargingPileParameters(this.form)
              if (response.code === 200) {
                this.$message.success(response.message)
                this.dialogVisible = false
                // 更新本地数据
                const pile = this.chargingPiles.find(p => p.id === this.form.pileId)
                if (pile) {
                  pile.chargingPower = this.form.chargingPower
                }
              } else {
                this.$message.error(response.message || '设置参数失败')
              }
            } catch (error) {
              console.error('设置充电桩参数出错：', error)
              this.$message.error('设置参数失败：' + error.message)
            } finally {
              this.submitting = false
            }
          }
        })
      },
      async fetchChargingPiles() {
        this.loading = true
        try {
          console.log('开始获取充电桩列表')
          const response = await getAllChargingPiles()
          console.log('获取到的响应数据：', response)
          if (response.code === 200) {
            this.chargingPiles = response.data
            console.log('设置充电桩数据：', this.chargingPiles)
          } else {
            this.$message.error(response.message || '获取充电桩列表失败')
          }
        } catch (error) {
          console.error('获取充电桩列表出错：', error)
          this.$message.error('获取充电桩列表失败：' + error.message)
        } finally {
          this.loading = false
        }
      },
      // 渲染策略选择器的HTML
      renderStrategySelector() {
        // 使用DOM方式创建选择器，在mounted后会被挂载
        return `
          <div>
            <p>请选择故障处理的调度策略：</p>
            <div class="el-select-container" style="margin-top: 15px;">
              <select id="strategySelect" class="el-select" style="width: 100%; height: 36px; border: 1px solid #DCDFE6; border-radius: 4px; padding: 0 15px; color: #606266;">
                <option value="PRIORITY">优先级调度</option>
                <option value="TIME_ORDER" selected>时间顺序调度</option>
              </select>
            </div>
          </div>
        `
      },
      // 使用选择的策略处理故障
      async handleFaultWithStrategy(row, strategy) {
        try {
          // 获取选择的策略
          const strategySelect = document.getElementById('strategySelect')
          const selectedStrategy = strategySelect ? strategySelect.value : 'TIME_ORDER'
          
          // 调用故障处理API
          const response = await handleChargingPileFault(row.id, selectedStrategy)
          if (response.code === 200) {
            row.status = 'FAULT'
            this.$message.success(response.message || '充电桩已标记为故障状态')
          } else {
            this.$message.error(response.message || '操作失败')
          }
        } catch (error) {
          console.error('处理充电桩故障出错：', error)
          this.$message.error('操作失败：' + error.message)
        }
      }
    },
    created() {
      this.fetchChargingPiles()
    }
  }
  </script>
  
  <style scoped>
  .charging-piles-container {
    padding: 20px;
  }
  </style>