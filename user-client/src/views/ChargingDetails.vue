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
          label="详单编号"
          width="180">
        </el-table-column>
        <el-table-column
          prop="createTime"
          label="生成时间"
          width="180">
        </el-table-column>
        <el-table-column
          prop="pileId"
          label="充电桩编号"
          width="120">
        </el-table-column>
        <el-table-column
          prop="power"
          label="充电电量(度)"
          width="120">
        </el-table-column>
        <el-table-column
          prop="duration"
          label="充电时长(小时)"
          width="120">
        </el-table-column>
        <el-table-column
          prop="startTime"
          label="启动时间"
          width="180">
        </el-table-column>
        <el-table-column
          prop="endTime"
          label="停止时间"
          width="180">
        </el-table-column>
        <el-table-column
          prop="chargingFee"
          label="充电费用(元)"
          width="120">
        </el-table-column>
        <el-table-column
          prop="serviceFee"
          label="服务费用(元)"
          width="120">
        </el-table-column>
        <el-table-column
          prop="totalFee"
          label="总费用(元)"
          width="120">
        </el-table-column>
        <el-table-column
          label="操作"
          width="100">
          <template slot-scope="scope">
            <el-button
              type="text"
              size="small"
              @click="showDetail(scope.row)">
              详情
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
    filteredDetails() {
      return this.details.filter(detail => 
        detail.id.toLowerCase().includes(this.searchKeyword.toLowerCase())
      )
    }
  },
  methods: {
    handleDateChange() {
      this.fetchDetails()
    },
    handleSearch() {
      this.currentPage = 1
    },
    handleSizeChange(val) {
      this.pageSize = val
      this.fetchDetails()
    },
    handleCurrentChange(val) {
      this.currentPage = val
      this.fetchDetails()
    },
    async fetchDetails() {
      this.loading = true
      try {
        // 这里应该调用API获取详单数据
        await new Promise(resolve => setTimeout(resolve, 1000))
        this.details = [
          {
            id: 'CD001',
            createTime: '2024-03-15 10:00:00',
            pileId: 'A',
            power: 30,
            duration: 1,
            startTime: '2024-03-15 10:00:00',
            endTime: '2024-03-15 11:00:00',
            chargingFee: 30,
            serviceFee: 24,
            totalFee: 54
          }
          // 更多数据...
        ]
        this.total = this.details.length
      } catch (error) {
        this.$message.error('获取详单失败：' + error.message)
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
</style> 