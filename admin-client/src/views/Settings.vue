<template>
    <div class="settings-container">
      <el-row :gutter="20">
        <el-col :span="12">
          <el-card>
            <div slot="header">
              <span>电价设置</span>
            </div>
            <el-form
              :model="priceForm"
              :rules="priceRules"
              ref="priceForm"
              label-width="120px">
              <el-form-item label="平时电价(元/度)" prop="normalPrice">
                <el-input-number
                  v-model="priceForm.normalPrice"
                  :min="0"
                  :precision="2"
                  :step="0.1">
                </el-input-number>
              </el-form-item>
              <el-form-item label="峰时电价(元/度)" prop="peakPrice">
                <el-input-number
                  v-model="priceForm.peakPrice"
                  :min="0"
                  :precision="2"
                  :step="0.1">
                </el-input-number>
              </el-form-item>
              <el-form-item label="谷时电价(元/度)" prop="valleyPrice">
                <el-input-number
                  v-model="priceForm.valleyPrice"
                  :min="0"
                  :precision="2"
                  :step="0.1">
                </el-input-number>
              </el-form-item>
              <el-form-item label="服务费(元/度)" prop="servicePrice">
                <el-input-number
                  v-model="priceForm.servicePrice"
                  :min="0"
                  :precision="2"
                  :step="0.1">
                </el-input-number>
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="savePriceSettings" :loading="loading">
                  保存设置
                </el-button>
              </el-form-item>
            </el-form>
          </el-card>
        </el-col>
        
        <el-col :span="12">
          <el-card>
            <div slot="header">
              <span>时段设置</span>
            </div>
            <el-form
              :model="timeForm"
              :rules="timeRules"
              ref="timeForm"
              label-width="120px">
              <el-form-item label="峰时时段">
                <el-time-picker
                  v-model="timeForm.peakStart"
                  placeholder="开始时间"
                  format="HH:mm">
                </el-time-picker>
                <span class="time-separator">至</span>
                <el-time-picker
                  v-model="timeForm.peakEnd"
                  placeholder="结束时间"
                  format="HH:mm">
                </el-time-picker>
              </el-form-item>
              <el-form-item label="谷时时段">
                <el-time-picker
                  v-model="timeForm.valleyStart"
                  placeholder="开始时间"
                  format="HH:mm">
                </el-time-picker>
                <span class="time-separator">至</span>
                <el-time-picker
                  v-model="timeForm.valleyEnd"
                  placeholder="结束时间"
                  format="HH:mm">
                </el-time-picker>
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="saveTimeSettings" :loading="loading">
                  保存设置
                </el-button>
              </el-form-item>
            </el-form>
          </el-card>
          
          <el-card class="mt-20">
            <div slot="header">
              <span>系统参数</span>
            </div>
            <el-form
              :model="systemForm"
              :rules="systemRules"
              ref="systemForm"
              label-width="120px">
              <el-form-item label="最大等待数量" prop="maxWaitingCount">
                <el-input-number
                  v-model="systemForm.maxWaitingCount"
                  :min="1"
                  :max="100">
                </el-input-number>
              </el-form-item>
              <el-form-item label="快充功率(kW)" prop="fastChargingPower">
                <el-input-number
                  v-model="systemForm.fastChargingPower"
                  :min="1"
                  :max="100">
                </el-input-number>
              </el-form-item>
              <el-form-item label="慢充功率(kW)" prop="slowChargingPower">
                <el-input-number
                  v-model="systemForm.slowChargingPower"
                  :min="1"
                  :max="100">
                </el-input-number>
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="saveSystemSettings" :loading="loading">
                  保存设置
                </el-button>
              </el-form-item>
            </el-form>
          </el-card>
        </el-col>
      </el-row>
    </div>
  </template>
  
  <script>
  export default {
    name: 'Settings',
    data() {
      return {
        loading: false,
        priceForm: {
          normalPrice: 0.7,
          peakPrice: 1.0,
          valleyPrice: 0.4,
          servicePrice: 0.8
        },
        priceRules: {
          normalPrice: [
            { required: true, message: '请输入平时电价', trigger: 'blur' }
          ],
          peakPrice: [
            { required: true, message: '请输入峰时电价', trigger: 'blur' }
          ],
          valleyPrice: [
            { required: true, message: '请输入谷时电价', trigger: 'blur' }
          ],
          servicePrice: [
            { required: true, message: '请输入服务费', trigger: 'blur' }
          ]
        },
        timeForm: {
          peakStart: new Date(2000, 0, 1, 10, 0),
          peakEnd: new Date(2000, 0, 1, 15, 0),
          valleyStart: new Date(2000, 0, 1, 23, 0),
          valleyEnd: new Date(2000, 0, 1, 7, 0)
        },
        timeRules: {
          peakStart: [
            { required: true, message: '请选择峰时开始时间', trigger: 'change' }
          ],
          peakEnd: [
            { required: true, message: '请选择峰时结束时间', trigger: 'change' }
          ],
          valleyStart: [
            { required: true, message: '请选择谷时开始时间', trigger: 'change' }
          ],
          valleyEnd: [
            { required: true, message: '请选择谷时结束时间', trigger: 'change' }
          ]
        },
        systemForm: {
          maxWaitingCount: 10,
          fastChargingPower: 30,
          slowChargingPower: 7
        },
        systemRules: {
          maxWaitingCount: [
            { required: true, message: '请输入最大等待数量', trigger: 'blur' }
          ],
          fastChargingPower: [
            { required: true, message: '请输入快充功率', trigger: 'blur' }
          ],
          slowChargingPower: [
            { required: true, message: '请输入慢充功率', trigger: 'blur' }
          ]
        }
      }
    },
    methods: {
      async savePriceSettings() {
        this.$refs.priceForm.validate(async valid => {
          if (valid) {
            this.loading = true
            try {
              // 这里应该调用API保存电价设置
              await new Promise(resolve => setTimeout(resolve, 1000))
              this.$message.success('电价设置保存成功')
            } catch (error) {
              this.$message.error('保存失败：' + error.message)
            } finally {
              this.loading = false
            }
          }
        })
      },
      async saveTimeSettings() {
        this.$refs.timeForm.validate(async valid => {
          if (valid) {
            this.loading = true
            try {
              // 这里应该调用API保存时段设置
              await new Promise(resolve => setTimeout(resolve, 1000))
              this.$message.success('时段设置保存成功')
            } catch (error) {
              this.$message.error('保存失败：' + error.message)
            } finally {
              this.loading = false
            }
          }
        })
      },
      async saveSystemSettings() {
        this.$refs.systemForm.validate(async valid => {
          if (valid) {
            this.loading = true
            try {
              // 这里应该调用API保存系统参数
              await new Promise(resolve => setTimeout(resolve, 1000))
              this.$message.success('系统参数保存成功')
            } catch (error) {
              this.$message.error('保存失败：' + error.message)
            } finally {
              this.loading = false
            }
          }
        })
      }
    }
  }
  </script>
  
  <style scoped>
  .settings-container {
    padding: 20px;
  }
  .time-separator {
    margin: 0 10px;
  }
  .mt-20 {
    margin-top: 20px;
  }
  </style>