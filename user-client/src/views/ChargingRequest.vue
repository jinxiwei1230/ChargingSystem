<template>
  <div class="charging-request-container">
    <el-card>
      <div slot="header">
        <span>充电请求</span>
      </div>
      
      <el-form :model="requestForm" :rules="rules" ref="requestForm" label-width="100px">
        <el-form-item label="充电模式" prop="mode">
          <el-radio-group v-model="requestForm.mode">
            <el-radio label="FAST">快充</el-radio>
            <el-radio label="SLOW">慢充</el-radio>
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
        
        <el-form-item label="费用明细">
          <div class="cost-details">
            <div>充电费用：¥{{ chargingCost }} = {{ amount_cost }}度 × ¥{{ currentUnitPrice }}/度</div>
            <div>服务费用：¥{{ serviceCost }} = {{ amount_cost }}度 × ¥0.8/度</div>
            <div class="total-cost">总费用：¥{{ calculateCost }} = 充电费用 + 服务费用</div>
          </div>
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
import { submitChargingRequest, cancelAndLeave } from '@/api/schedule'
import { Message, MessageBox } from 'element-ui'
import { mapGetters } from 'vuex'

export default {
  name: 'ChargingRequest',
  data() {
    return {
      requestForm: {
        mode: 'FAST',
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
      isInQueue: false,
      form: {
        amount: 0,
        power: 0
      }
    }
  },
  computed: {
    ...mapGetters(['userId']),
    // 根据当前时间计算电价
    currentUnitPrice() {
      const currentHour = new Date().getHours()
      if ((currentHour >= 10 && currentHour < 15) || 
          (currentHour >= 18 && currentHour < 21)) {
        return 1.0 // 峰时
      } else if (currentHour >= 23 || currentHour < 7) {
        return 0.4 // 谷时
      } else {
        return 0.7 // 平时
      }
    },
    // 充电费用 = 单位电价 * 充电度数
    chargingCost(){
      return this.requestForm.amount * this.currentUnitPrice
    },
    // 服务费用 = 服务费单价 * 充电度数
    serviceCost(){
      return this.requestForm.amount * 0.8
    }, 
    // 总费用
    calculateCost() {
      return this.chargingCost + this.serviceCost
    },
    // 计算充电时长
    chargingDuration() {
      if (!this.form.power) return 0
      return (this.form.amount / this.form.power).toFixed(1)
    },
    calculateDuration() {
      const power = this.requestForm.mode === 'FAST' ? 30 : 7
      return (this.requestForm.amount / power).toFixed(1)
    },
    amount_cost() {
      return this.requestForm.amount
    }
  },
  methods: {
    async submitRequest() {
      this.$refs.requestForm.validate(async valid => {
        if (valid) {
          this.loading = true
          try {
            const requestData = {
              userId: this.userId,
              requestAmount: this.requestForm.amount,
              mode: this.requestForm.mode
            }
            console.log(this.userId)
            const response = await submitChargingRequest(requestData)
            if (response.code === 200) {
              Message.success(response.message || '充电申请提交成功')
              this.isInQueue = true
            } else {
              Message.error(response.message || '提交失败')
            }
          } catch (error) {
            Message.error('操作失败：' + (error.message || '未知错误'))
          } finally {
            this.loading = false
          }
        }
      })
    },
    cancelRequest() {
      MessageBox.confirm('确认取消充电请求吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          const response = await cancelAndLeave(this.userId)
          if (response.code === 200) {
            this.isInQueue = false
            Message.success(response.message || '已取消充电请求')
          } else {
            Message.error(response.message || '取消失败')
          }
        } catch (error) {
          Message.error('取消失败：' + (error.message || '未知错误'))
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
.cost-details {
  padding: 10px;
  background-color: #f8f9fa;
  border-radius: 4px;
}

.cost-details div {
  margin: 5px 0;
  color: #606266;
}

.total-cost {
  margin-top: 10px !important;
  font-weight: bold;
  color: #409EFF !important;
  border-top: 1px solid #dcdfe6;
  padding-top: 10px;
}
</style> 