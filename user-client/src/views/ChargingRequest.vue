<template>
  <div class="charging-request-container">
    <el-card>
      <div slot="header">
        <span>充电请求</span>
      </div>
      
      <el-form :model="requestForm" :rules="rules" ref="requestForm" label-width="100px">
        <el-form-item label="充电模式" prop="mode">
          <el-radio-group v-model="requestForm.mode">
            <el-radio label="fast">快充</el-radio>
            <el-radio label="slow">慢充</el-radio>
          </el-radio-group>
        </el-form-item>
        
        <el-form-item label="充电量(度)" prop="amount">
          <el-input-number 
            v-model="requestForm.amount" 
            :min="1" 
            :max="100"
            :step="1"
            :precision="1">
          </el-input-number>
        </el-form-item>
        
        <el-form-item label="预计时长">
          <span>{{ calculateDuration }}小时</span>
        </el-form-item>
        
        <el-form-item label="预计费用">
          <span>¥{{ calculateCost }}</span>
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" @click="submitRequest" :loading="loading">
            {{ isInQueue ? '修改请求' : '提交请求' }}
          </el-button>
          <el-button v-if="isInQueue" type="danger" @click="cancelRequest">
            取消请求
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script>
export default {
  name: 'ChargingRequest',
  data() {
    return {
      requestForm: {
        mode: 'fast',
        amount: 10
      },
      rules: {
        mode: [
          { required: true, message: '请选择充电模式', trigger: 'change' }
        ],
        amount: [
          { required: true, message: '请输入充电量', trigger: 'blur' }
        ]
      },
      loading: false,
      isInQueue: false
    }
  },
  computed: {
    calculateDuration() {
      const power = this.requestForm.mode === 'fast' ? 30 : 7
      return (this.requestForm.amount / power).toFixed(1)
    },
    calculateCost() {
      const currentHour = new Date().getHours()
      let unitPrice = 0.7 // 默认平时电价
      
      // 判断当前时段电价
      if ((currentHour >= 10 && currentHour < 15) || 
          (currentHour >= 18 && currentHour < 21)) {
        unitPrice = 1.0 // 峰时
      } else if (currentHour >= 23 || currentHour < 7) {
        unitPrice = 0.4 // 谷时
      }
      
      const servicePrice = 0.8 // 服务费单价
      const totalCost = (unitPrice + servicePrice) * this.requestForm.amount
      return totalCost.toFixed(2)
    }
  },
  methods: {
    submitRequest() {
      this.$refs.requestForm.validate(async valid => {
        if (valid) {
          this.loading = true
          try {
            // 这里应该调用API提交请求
            await new Promise(resolve => setTimeout(resolve, 1000))
            this.$message.success(this.isInQueue ? '修改成功' : '请求已提交')
            this.isInQueue = true
          } catch (error) {
            this.$message.error('操作失败：' + error.message)
          } finally {
            this.loading = false
          }
        }
      })
    },
    cancelRequest() {
      this.$confirm('确认取消充电请求吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          // 这里应该调用API取消请求
          await new Promise(resolve => setTimeout(resolve, 1000))
          this.isInQueue = false
          this.$message.success('已取消充电请求')
        } catch (error) {
          this.$message.error('取消失败：' + error.message)
        }
      }).catch(() => {})
    }
  }
}
</script>

<style scoped>
.charging-request-container {
  padding: 20px;
}
.el-input-number {
  width: 180px;
}
</style> 