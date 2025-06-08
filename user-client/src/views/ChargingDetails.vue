<template>
  <div class="charging-details-container">
    <el-card>
      <div slot="header" class="header">
        <span>充电订单列表</span>
        <div class="header-actions">
          <el-select v-model="orderStatus" placeholder="订单状态" style="width: 120px; margin-right: 10px">
            <el-option label="全部" value=""></el-option>
            <el-option label="已创建" value="CREATED"></el-option>
            <el-option label="充电中" value="CHARGING"></el-option>
            <el-option label="已完成" value="COMPLETED"></el-option>
            <el-option label="已取消" value="CANCELLED"></el-option>
            <el-option label="故障" value="FAULTED"></el-option>
          </el-select>
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            :picker-options="pickerOptions"
            @change="handleDateChange">
          </el-date-picker>
          <el-input
            v-model="searchKeyword"
            placeholder="搜索订单编号"
            style="width: 200px; margin-left: 10px"
            @input="handleSearch">
          </el-input>
        </div>
      </div>
      
      <el-table
        :data="ordersList"
        style="width: 100%"
        v-loading="loading"
        @row-click="showOrderDetail">
        <el-table-column
          prop="orderId"
          label="订单编号"
          width="180">
        </el-table-column>
        <el-table-column
          prop="pileId"
          label="充电桩编号"
          width="120">
        </el-table-column>
        <el-table-column
          prop="carId"
          label="车牌号"
          width="120">
        </el-table-column>
        <el-table-column
          prop="orderStatus"
          label="订单状态"
          width="100">
          <template slot-scope="scope">
            <el-tag :type="getStatusType(scope.row.orderStatus)">
              {{ getStatusText(scope.row.orderStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column
          prop="totalKwh"
          label="充电量(度)"
          width="100">
        </el-table-column>
        <el-table-column
          prop="totalDuration"
          label="充电时长(分钟)"
          width="120">
        </el-table-column>
        <el-table-column
          prop="totalFee"
          label="总费用(元)"
          width="120">
          <template slot-scope="scope">
            {{ scope.row.totalFee ? '¥' + scope.row.totalFee : '-' }}
          </template>
        </el-table-column>
        <el-table-column
          prop="startTime"
          label="开始时间"
          width="180">
          <template slot-scope="scope">
            {{ formatDateTime(scope.row.startTime) }}
          </template>
        </el-table-column>
        <el-table-column
          prop="endTime"
          label="结束时间"
          width="180">
          <template slot-scope="scope">
            {{ formatDateTime(scope.row.endTime) }}
          </template>
        </el-table-column>
        <el-table-column
          label="操作"
          width="120">
          <template slot-scope="scope">
            <el-button 
              type="text" 
              size="small"
              @click.stop="showOrderDetail(scope.row)">
              查看详单
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <div class="pagination-container">
        <el-pagination
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
          :current-page="currentPage"
          :page-sizes="[10, 20, 50, 100]"
          :page-size="pageSize"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total">
        </el-pagination>
      </div>
    </el-card>
    
    <!-- 订单详情对话框 -->
    <el-dialog
      title="订单详情"
      :visible.sync="orderDialogVisible"
      width="60%">
      <div v-if="currentOrder" class="detail-content">
        <h3>基本信息</h3>
        <div class="detail-grid">
        <div class="detail-item">
            <span class="label">订单编号：</span>
            <span class="value">{{ currentOrder.orderId }}</span>
        </div>
        <div class="detail-item">
          <span class="label">充电桩编号：</span>
            <span class="value">{{ currentOrder.pileId }}</span>
          </div>
          <div class="detail-item">
            <span class="label">车牌号：</span>
            <span class="value">{{ currentOrder.carId }}</span>
          </div>
          <div class="detail-item">
            <span class="label">订单状态：</span>
            <span class="value">
              <el-tag :type="getStatusType(currentOrder.orderStatus)">
                {{ getStatusText(currentOrder.orderStatus) }}
              </el-tag>
            </span>
        </div>
        <div class="detail-item">
          <span class="label">充电电量：</span>
            <span class="value">{{ currentOrder.totalKwh }}度</span>
        </div>
        <div class="detail-item">
          <span class="label">充电时长：</span>
            <span class="value">{{ currentOrder.totalDuration }}分钟</span>
        </div>
        <div class="detail-item">
          <span class="label">充电费用：</span>
            <span class="value">¥{{ currentOrder.totalChargeFee || '-' }}</span>
        </div>
        <div class="detail-item">
          <span class="label">服务费用：</span>
            <span class="value">¥{{ currentOrder.totalServiceFee || '-' }}</span>
        </div>
        <div class="detail-item">
          <span class="label">总费用：</span>
            <span class="value">¥{{ currentOrder.totalFee || '-' }}</span>
          </div>
          <div class="detail-item">
            <span class="label">开始时间：</span>
            <span class="value">{{ formatDateTime(currentOrder.startTime) }}</span>
          </div>
          <div class="detail-item">
            <span class="label">结束时间：</span>
            <span class="value">{{ formatDateTime(currentOrder.endTime) }}</span>
          </div>
          <div class="detail-item">
            <span class="label">创建时间：</span>
            <span class="value">{{ formatDateTime(currentOrder.createTime) }}</span>
          </div>
        </div>
        
        <div v-if="orderDetails.length > 0" class="detail-list">
          <h3>详单列表</h3>
          <el-table :data="orderDetails" border>
            <el-table-column prop="periodSeq" label="时段序号" width="100"></el-table-column>
            <el-table-column prop="periodType" label="时段类型" width="120">
              <template slot-scope="scope">
                {{ getPeriodTypeText(scope.row.periodType) }}
              </template>
            </el-table-column>
            <el-table-column label="开始时间" width="180">
              <template slot-scope="scope">
                {{ formatDateTime(scope.row.startTime) }}
              </template>
            </el-table-column>
            <el-table-column label="结束时间" width="180">
              <template slot-scope="scope">
                {{ formatDateTime(scope.row.endTime) }}
              </template>
            </el-table-column>
            <el-table-column prop="duration" label="时长(分钟)" width="120"></el-table-column>
            <el-table-column prop="kwh" label="充电量(度)" width="120"></el-table-column>
            <el-table-column prop="chargeRate" label="充电费率" width="120"></el-table-column>
            <el-table-column prop="serviceRate" label="服务费率" width="120"></el-table-column>
            <el-table-column prop="chargeFee" label="充电费(元)" width="120"></el-table-column>
            <el-table-column prop="serviceFee" label="服务费(元)" width="120"></el-table-column>
            <el-table-column prop="subTotal" label="小计(元)" width="120"></el-table-column>
          </el-table>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { getUserOrders, getOrderInfo, getOrderDetailList } from '@/api/user-bill'
import { mapGetters } from 'vuex'

export default {
  name: 'ChargingDetails',
  data() {
    return {
      loading: false,
      dateRange: [],
      searchKeyword: '',
      orderStatus: '',
      currentPage: 1,
      pageSize: 10,
      total: 0,
      ordersList: [],
      allOrders: [],
      orderDialogVisible: false,
      currentOrder: null,
      orderDetails: [],
      pickerOptions: {
        shortcuts: [{
          text: '最近一周',
          onClick(picker) {
            const end = new Date()
            const start = new Date()
            start.setTime(start.getTime() - 3600 * 1000 * 24 * 7)
            picker.$emit('pick', [start, end])
          }
        }, {
          text: '最近一个月',
          onClick(picker) {
            const end = new Date()
            const start = new Date()
            start.setTime(start.getTime() - 3600 * 1000 * 24 * 30)
            picker.$emit('pick', [start, end])
          }
        }]
      }
    }
  },
  computed: {
    ...mapGetters(['userId'])
  },
  methods: {
    getPeriodTypeText(type) {
      const typeMap = {
        'PEAK': '峰时',
        'STANDARD': '平时',
        'VALLEY': '谷时'
      }
      return typeMap[type] || type
    },
    getStatusText(status) {
      const statusMap = {
        'CREATED': '已创建',
        'CHARGING': '充电中',
        'COMPLETED': '已完成',
        'CANCELLED': '已取消',
        'FAULTED': '故障'
      }
      return statusMap[status] || status
    },
    getStatusType(status) {
      const typeMap = {
        'CREATED': 'info',
        'CHARGING': 'success',
        'COMPLETED': '',
        'CANCELLED': 'danger',
        'FAULTED': 'warning'
      }
      return typeMap[status] || ''
    },
    formatDateTime(dateTimeStr) {
      if (!dateTimeStr) return '-'
      const date = new Date(dateTimeStr)
      return date.toLocaleString()
    },
    handleDateChange() {
      this.applyFilters()
    },
    handleSearch() {
      this.applyFilters()
    },
    handleSizeChange(val) {
      this.pageSize = val
      this.applyFilters()
    },
    handleCurrentChange(val) {
      this.currentPage = val
      this.applyFilters()
    },
    async fetchOrders() {
      this.loading = true
      try {
        if (!this.userId) {
          this.$message.error('用户未登录')
          return
        }
        
        const params = {
          page: this.currentPage,
          size: this.pageSize
        }
        
        if (this.orderStatus) {
          params.status = this.orderStatus
        }
        
        if (this.dateRange && this.dateRange.length === 2) {
          params.startDate = this.formatDate(this.dateRange[0])
          params.endDate = this.formatDate(this.dateRange[1])
        }
        
        const response = await getUserOrders(this.userId, params)
        if (response.code === 200) {
          this.ordersList = response.data.records
          this.total = response.data.total
        } else {
          this.$message.error(response.message || '获取订单列表失败')
        }
      } catch (error) {
        this.$message.error('获取订单列表失败：' + error.message)
      } finally {
        this.loading = false
      }
    },
    formatDate(date) {
      const d = new Date(date)
      let month = '' + (d.getMonth() + 1)
      let day = '' + d.getDate()
      const year = d.getFullYear()
      
      if (month.length < 2) month = '0' + month
      if (day.length < 2) day = '0' + day
      
      return [year, month, day].join('-')
    },
    applyFilters() {
      this.fetchOrders()
    },
    async showOrderDetail(row) {
      this.loading = true
      try {
        const [orderResponse, detailsResponse] = await Promise.all([
          getOrderInfo(row.orderId),
          getOrderDetailList(row.orderId)
        ])
        
        if (orderResponse.code === 200) {
          this.currentOrder = orderResponse.data
        } else {
          this.$message.error(orderResponse.message || '获取订单详情失败')
        }
        
        if (detailsResponse.code === 200) {
          this.orderDetails = detailsResponse.data
        } else {
          this.orderDetails = []
        }
        
        this.orderDialogVisible = true
      } catch (error) {
        this.$message.error('获取订单详情失败：' + error.message)
      } finally {
        this.loading = false
      }
    }
  },
  created() {
    this.fetchOrders()
  }
}
</script>

<style scoped>
.charging-details-container {
  padding: 20px;
}
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.header-actions {
  display: flex;
  align-items: center;
}
.pagination-container {
  margin-top: 20px;
  text-align: right;
}
.detail-content {
  padding: 20px;
}
.detail-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 15px;
  margin-bottom: 30px;
}
.detail-item {
  margin: 10px 0;
  font-size: 14px;
}
.label {
  color: #606266;
  margin-right: 10px;
}
.value {
  color: #303133;
  font-weight: bold;
}
.detail-list {
  margin-top: 20px;
}
.el-tag {
  margin-right: 10px;
}
</style> 