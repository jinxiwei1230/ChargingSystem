<template>
  <div class="charging-details-container">
    <el-card>
      <div slot="header" class="header">
        <span>充电详单</span>
        <div class="header-actions">
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
            placeholder="搜索详单编号"
            style="width: 200px; margin-left: 10px"
            @input="handleSearch">
          </el-input>
        </div>
      </div>
      
      <el-table
        :data="filteredDetails"
        style="width: 100%"
        v-loading="loading">
        <el-table-column
          prop="id"
          label="请求编号"
          width="150">
        </el-table-column>
        <el-table-column
          prop="chargingPileId"
          label="充电桩编号"
          width="150">
          <template slot-scope="scope">
            {{ scope.row.chargingPileId || '未分配' }}
          </template>
        </el-table-column>
        <el-table-column
          prop="mode"
          label="充电模式"
          width="150">
          <template slot-scope="scope">
            {{ scope.row.mode === 'FAST' ? '快充' : '慢充' }}
          </template>
        </el-table-column>
        <el-table-column
          prop="amount"
          label="充电量(度)"
          width="150">
        </el-table-column>
        <el-table-column
          prop="queueNumber"
          label="排队号码"
          width="180">
        </el-table-column>
        <el-table-column
          prop="status"
          label="状态"
          width="200">
          <template slot-scope="scope">
            <el-tag :type="getStatusType(scope.row.status)">
              {{ getStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column
          prop="queueJoinTime"
          label="排队时间"
          width="180">
          <template slot-scope="scope">
            {{ formatDateTime(scope.row.queueJoinTime) }}
          </template>
        </el-table-column>
        <el-table-column
          prop="requestTime"
          label="请求时间"
          width="180">
          <template slot-scope="scope">
            {{ formatDateTime(scope.row.requestTime) }}
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
    
    <!-- 详情对话框 -->
    <el-dialog
      title="充电详单详情"
      :visible.sync="dialogVisible"
      width="50%">
      <div v-if="currentDetail" class="detail-content">
        <div class="detail-item">
          <span class="label">详单编号：</span>
          <span class="value">{{ currentDetail.id }}</span>
        </div>
        <div class="detail-item">
          <span class="label">充电桩编号：</span>
          <span class="value">{{ currentDetail.pileId }}</span>
        </div>
        <div class="detail-item">
          <span class="label">充电电量：</span>
          <span class="value">{{ currentDetail.power }}度</span>
        </div>
        <div class="detail-item">
          <span class="label">充电时长：</span>
          <span class="value">{{ currentDetail.duration }}小时</span>
        </div>
        <div class="detail-item">
          <span class="label">启动时间：</span>
          <span class="value">{{ currentDetail.startTime }}</span>
        </div>
        <div class="detail-item">
          <span class="label">停止时间：</span>
          <span class="value">{{ currentDetail.endTime }}</span>
        </div>
        <div class="detail-item">
          <span class="label">充电费用：</span>
          <span class="value">¥{{ currentDetail.chargingFee }}</span>
        </div>
        <div class="detail-item">
          <span class="label">服务费用：</span>
          <span class="value">¥{{ currentDetail.serviceFee }}</span>
        </div>
        <div class="detail-item">
          <span class="label">总费用：</span>
          <span class="value">¥{{ currentDetail.totalFee }}</span>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { getUserRequests } from '@/api/schedule'
import { mapGetters } from 'vuex'

export default {
  name: 'ChargingDetails',
  data() {
    return {
      loading: false,
      dateRange: [],
      searchKeyword: '',
      currentPage: 1,
      pageSize: 10,
      total: 0,
      details: [],
      dialogVisible: false,
      currentDetail: null,
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
    ...mapGetters(['userId']),
    filteredDetails() {
      let result = this.details
      
      // 按关键词搜索
      if (this.searchKeyword) {
        result = result.filter(detail => 
          detail.id.toString().includes(this.searchKeyword) ||
          detail.queueNumber.includes(this.searchKeyword)
        )
      }
      
      // 按日期范围筛选
      if (this.dateRange && this.dateRange.length === 2) {
        const startDate = new Date(this.dateRange[0])
        const endDate = new Date(this.dateRange[1])
        endDate.setHours(23, 59, 59, 999)
        
        result = result.filter(detail => {
          const requestTime = new Date(detail.requestTime)
          return requestTime >= startDate && requestTime <= endDate
        })
      }
      
      return result
    }
  },
  methods: {
    getStatusText(status) {
      const statusMap = {
        'WAITING_IN_WAITING_AREA': '等候区等待',
        'WAITING_IN_CHARGING_AREA': '充电区等待',
        'CHARGING': '充电中',
        'COMPLETED': '已完成',
        'CANCELED': '已取消'
      }
      return statusMap[status] || status
    },
    getStatusType(status) {
      const typeMap = {
        'WAITING_IN_WAITING_AREA': 'warning',
        'WAITING_IN_CHARGING_AREA': 'info',
        'CHARGING': 'success',
        'COMPLETED': '',
        'CANCELED': 'danger'
      }
      return typeMap[status] || ''
    },
    formatDateTime(dateTimeStr) {
      if (!dateTimeStr) return ''
      const date = new Date(dateTimeStr)
      return date.toLocaleString()
    },
    handleDateChange() {
      this.currentPage = 1
    },
    handleSearch() {
      this.currentPage = 1
    },
    handleSizeChange(val) {
      this.pageSize = val
    },
    handleCurrentChange(val) {
      this.currentPage = val
    },
    async fetchDetails() {
      this.loading = true
      try {
        if (!this.userId) {
          this.$message.error('用户未登录')
          return
        }
        
        const response = await getUserRequests(this.userId)
        if (response.code === 200) {
          this.details = response.data
          this.total = response.data.length
        } else {
          this.$message.error(response.message || '获取充电请求列表失败')
        }
      } catch (error) {
        this.$message.error('获取充电请求列表失败：' + error.message)
      } finally {
        this.loading = false
      }
    },
    showDetail(row) {
      this.currentDetail = row
      this.dialogVisible = true
    }
  },
  created() {
    this.fetchDetails()
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
.detail-item {
  margin: 15px 0;
  font-size: 16px;
}
.label {
  color: #606266;
  margin-right: 10px;
}
.value {
  color: #303133;
  font-weight: bold;
}
.el-tag {
  margin-right: 10px;
}
</style> 