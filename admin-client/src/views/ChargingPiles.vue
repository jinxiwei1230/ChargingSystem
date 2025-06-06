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
              <el-tag :type="scope.row.type === 'fast' ? 'success' : 'info'">
                {{ scope.row.type === 'fast' ? '快充' : '慢充' }}
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
          <el-table-column prop="power" label="功率(kW)" width="100"></el-table-column>
          <el-table-column prop="totalChargingTime" label="总充电时长(h)" width="120"></el-table-column>
          <el-table-column prop="totalChargingAmount" label="总充电量(kWh)" width="120"></el-table-column>
          <el-table-column prop="lastMaintenanceTime" label="上次维护时间" width="180"></el-table-column>
          <el-table-column label="操作" width="280">
            <template slot-scope="scope">
              <el-button size="mini" @click="handleEdit(scope.row)" style="margin-right: 10px">编辑</el-button>
              <el-button size="mini" type="warning" @click="handleMaintenance(scope.row)" style="margin-right: 10px">维护</el-button>
              <el-button size="mini" type="danger" @click="handleDelete(scope.row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-card>
  
      <!-- 添加/编辑充电桩对话框 -->
      <el-dialog :title="dialogTitle" :visible.sync="dialogVisible" width="500px">
        <el-form :model="form" :rules="rules" ref="form" label-width="100px">
          <el-form-item label="充电桩类型" prop="type">
            <el-select v-model="form.type" placeholder="请选择充电桩类型">
              <el-option label="快充" value="fast"></el-option>
              <el-option label="慢充" value="slow"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="功率(kW)" prop="power">
            <el-input-number v-model="form.power" :min="1" :max="100"></el-input-number>
          </el-form-item>
          <el-form-item label="状态" prop="status">
            <el-select v-model="form.status" placeholder="请选择状态">
              <el-option label="空闲" value="idle"></el-option>
              <el-option label="使用中" value="in_use"></el-option>
              <el-option label="维护中" value="maintenance"></el-option>
              <el-option label="故障" value="fault"></el-option>
            </el-select>
          </el-form-item>
        </el-form>
        <div slot="footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmit" :loading="submitting">确定</el-button>
        </div>
      </el-dialog>
  
      <!-- 维护记录对话框 -->
      <el-dialog title="维护记录" :visible.sync="maintenanceDialogVisible" width="600px">
        <el-table :data="maintenanceRecords" border>
          <el-table-column prop="time" label="维护时间" width="180"></el-table-column>
          <el-table-column prop="type" label="维护类型" width="120">
            <template slot-scope="scope">
              <el-tag :type="scope.row.type === 'regular' ? 'info' : 'warning'">
                {{ scope.row.type === 'regular' ? '定期维护' : '故障维修' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="description" label="维护内容"></el-table-column>
          <el-table-column prop="operator" label="维护人员" width="120"></el-table-column>
        </el-table>
      </el-dialog>
    </div>
  </template>
  
  <script>
  export default {
    name: 'ChargingPiles',
    data() {
      return {
        loading: false,
        chargingPiles: [
          {
            id: 'CP001',
            type: 'fast',
            status: 'idle',
            power: 60,
            totalChargingTime: 1200,
            totalChargingAmount: 72000,
            lastMaintenanceTime: '2024-01-15 10:00:00'
          },
          {
            id: 'CP002',
            type: 'slow',
            status: 'in_use',
            power: 7,
            totalChargingTime: 800,
            totalChargingAmount: 5600,
            lastMaintenanceTime: '2024-01-10 14:30:00'
          }
        ],
        dialogVisible: false,
        dialogTitle: '',
        form: {
          type: '',
          power: 0,
          status: ''
        },
        rules: {
          type: [{ required: true, message: '请选择充电桩类型', trigger: 'change' }],
          power: [{ required: true, message: '请输入功率', trigger: 'blur' }],
          status: [{ required: true, message: '请选择状态', trigger: 'change' }]
        },
        submitting: false,
        maintenanceDialogVisible: false,
        maintenanceRecords: [
          {
            time: '2024-01-15 10:00:00',
            type: 'regular',
            description: '定期检查和清洁',
            operator: '张工'
          },
          {
            time: '2024-01-10 14:30:00',
            type: 'repair',
            description: '更换充电接口',
            operator: '李工'
          }
        ]
      }
    },
    methods: {
      getStatusType(status) {
        const types = {
          idle: 'success',
          in_use: 'warning',
          maintenance: 'info',
          fault: 'danger'
        }
        return types[status] || 'info'
      },
      getStatusText(status) {
        const texts = {
          idle: '空闲',
          in_use: '使用中',
          maintenance: '维护中',
          fault: '故障'
        }
        return texts[status] || '未知'
      },
      handleAdd() {
        this.dialogTitle = '添加充电桩'
        this.form = {
          type: '',
          power: 0,
          status: 'idle'
        }
        this.dialogVisible = true
      },
      handleEdit(row) {
        this.dialogTitle = '编辑充电桩'
        this.form = { ...row }
        this.dialogVisible = true
      },
      handleMaintenance(row) {
        this.maintenanceDialogVisible = true
      },
      handleDelete(row) {
        this.$confirm('确认删除该充电桩吗？', '提示', {
          type: 'warning'
        }).then(() => {
          // 这里应该调用API删除充电桩
          this.$message.success('删除成功')
        }).catch(() => {})
      },
      handleSubmit() {
        this.$refs.form.validate(valid => {
          if (valid) {
            this.submitting = true
            // 这里应该调用API保存充电桩信息
            setTimeout(() => {
              this.submitting = false
              this.dialogVisible = false
              this.$message.success('保存成功')
            }, 1000)
          }
        })
      }
    }
  }
  </script>
  
  <style scoped>
  .charging-piles-container {
    padding: 20px;
  }
  </style>