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
              value-format="yyyy-MM-dd"
              :picker-options="pickerOptions"
              @change="handleDateChange">
            </el-date-picker>
            <el-select
              v-model="orderStatus"
              placeholder="订单状态"
              clearable
              style="width: 120px; margin-left: 10px"
              @change="handleStatusChange">
              <el-option
                v-for="(label, value) in OrderStatusMap"
                :key="value"
                :label="label"
                :value="value">
              </el-option>
            </el-select>
            <el-input
              v-model="searchKeyword"
              placeholder="搜索订单号/车牌号"
              style="width: 200px; margin-left: 10px"
              @input="handleSearch">
            </el-input>
            <el-button type="primary" @click="exportDetails" style="margin-left: 10px">
              导出数据
            </el-button>
          </div>
        </div>
        
        <el-table
          :data="filteredDetails"
          style="width: 100%"
          v-loading="loading">
          <el-table-column
            prop="orderId"
            label="订单编号"
            width="180">
          </el-table-column>
          <el-table-column
            prop="carId"
            label="车牌号"
            width="120">
          </el-table-column>
          <el-table-column
            prop="pileId"
            label="充电桩编号"
            width="120">
          </el-table-column>
          <el-table-column
            prop="orderStatus"
            label="订单状态"
            width="120">
            <template slot-scope="scope">
              <el-tag :type="getStatusType(scope.row.orderStatus)">
                {{ OrderStatusMap[scope.row.orderStatus] }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column
            prop="totalKwh"
            label="充电电量(度)"
            width="120">
          </el-table-column>
          <el-table-column
            prop="totalDuration"
            label="充电时长(小时)"
            width="120">
          </el-table-column>
          <el-table-column
            prop="startTime"
            label="开始时间"
            width="180">
          </el-table-column>
          <el-table-column
            prop="endTime"
            label="结束时间"
            width="180">
          </el-table-column>
          <el-table-column
            prop="totalChargeFee"
            label="充电费用(元)"
            width="120">
          </el-table-column>
          <el-table-column
            prop="totalServiceFee"
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
            width="120">
            <template slot-scope="scope">
              <el-button
                type="text"
                size="small"
                @click="showDetail(scope.row)">
                详情
              </el-button>
              <el-button
                type="text"
                size="small"
                @click="showDetailList(scope.row)">
                详单
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
        width="50%">
        <div v-if="currentOrder" class="detail-content">
          <div class="detail-item">
            <span class="label">订单编号：</span>
            <span class="value">{{ currentOrder.orderId }}</span>
          </div>
          <div class="detail-item">
            <span class="label">车牌号：</span>
            <span class="value">{{ currentOrder.carId }}</span>
          </div>
          <div class="detail-item">
            <span class="label">充电桩编号：</span>
            <span class="value">{{ currentOrder.pileId }}</span>
          </div>
          <div class="detail-item">
            <span class="label">订单状态：</span>
            <span class="value">
              <el-tag :type="getStatusType(currentOrder.orderStatus)">
                {{ OrderStatusMap[currentOrder.orderStatus] }}
              </el-tag>
            </span>
          </div>
          <div class="detail-item">
            <span class="label">充电电量：</span>
            <span class="value">{{ currentOrder.totalKwh }}度</span>
          </div>
          <div class="detail-item">
            <span class="label">充电时长：</span>
            <span class="value">{{ currentOrder.totalDuration }}小时</span>
          </div>
          <div class="detail-item">
            <span class="label">开始时间：</span>
            <span class="value">{{ currentOrder.startTime }}</span>
          </div>
          <div class="detail-item">
            <span class="label">结束时间：</span>
            <span class="value">{{ currentOrder.endTime }}</span>
          </div>
          <div class="detail-item">
            <span class="label">充电费用：</span>
            <span class="value">¥{{ currentOrder.totalChargeFee }}</span>
          </div>
          <div class="detail-item">
            <span class="label">服务费用：</span>
            <span class="value">¥{{ currentOrder.totalServiceFee }}</span>
          </div>
          <div class="detail-item">
            <span class="label">总费用：</span>
            <span class="value">¥{{ currentOrder.totalFee }}</span>
          </div>
        </div>
      </el-dialog>

      <!-- 详单列表对话框 -->
      <el-dialog
        title="充电详单列表"
        :visible.sync="detailListDialogVisible"
        width="70%">
        <el-table
          :data="detailList"
          style="width: 100%"
          v-loading="detailListLoading">
          <el-table-column
            prop="periodSeq"
            label="时段序号"
            width="100">
          </el-table-column>
          <el-table-column
            prop="periodType"
            label="时段类型"
            width="120">
            <template slot-scope="scope">
              <el-tag :type="getPeriodType(scope.row.periodType)">
                {{ scope.row.periodType }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column
            prop="startTime"
            label="开始时间"
            width="180">
          </el-table-column>
          <el-table-column
            prop="endTime"
            label="结束时间"
            width="180">
          </el-table-column>
          <el-table-column
            prop="duration"
            label="时长(小时)"
            width="120">
          </el-table-column>
          <el-table-column
            prop="kwh"
            label="电量(度)"
            width="120">
          </el-table-column>
          <el-table-column
            prop="chargeRate"
            label="充电费率"
            width="120">
          </el-table-column>
          <el-table-column
            prop="serviceRate"
            label="服务费率"
            width="120">
          </el-table-column>
          <el-table-column
            prop="chargeFee"
            label="充电费用"
            width="120">
          </el-table-column>
          <el-table-column
            prop="serviceFee"
            label="服务费用"
            width="120">
          </el-table-column>
          <el-table-column
            prop="subTotal"
            label="小计"
            width="120">
          </el-table-column>
        </el-table>
      </el-dialog>
    </div>
  </template>
  
  <script>
  import { getUserOrders, getOrderDetailList, getOrderInfo, OrderStatus, OrderStatusMap } from '@/api/order'

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
        details: [],
        orderDialogVisible: false,
        detailListDialogVisible: false,
        currentOrder: null,
        detailList: [],
        detailListLoading: false,
        OrderStatus,
        OrderStatusMap,
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
          detail.orderId.toLowerCase().includes(this.searchKeyword.toLowerCase()) ||
          detail.carId.toLowerCase().includes(this.searchKeyword.toLowerCase())
        )
      }
    },
    methods: {
      getStatusType(status) {
        const typeMap = {
          [OrderStatus.CREATED]: 'info',
          [OrderStatus.CHARGING]: 'warning',
          [OrderStatus.COMPLETED]: 'success',
          [OrderStatus.CANCELLED]: 'danger',
          [OrderStatus.FAULTED]: 'danger'
        }
        return typeMap[status] || 'info'
      },
      getPeriodType(type) {
        const typeMap = {
          'PEAK': 'danger',
          'VALLEY': 'success',
          'NORMAL': 'info'
        }
        return typeMap[type] || 'info'
      },
      handleDateChange() {
        this.fetchDetails()
      },
      handleStatusChange() {
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
          const params = {
            page: this.currentPage,
            size: this.pageSize,
            status: this.orderStatus
          }
          if (this.dateRange && this.dateRange.length === 2) {
            params.startDate = this.dateRange[0]
            params.endDate = this.dateRange[1]
          }
          
          const response = await getUserOrders(1, params) // 这里需要替换实际的用户ID
          this.details = response.data.records
          this.total = response.data.total
        } catch (error) {
          this.$message.error('获取订单列表失败：' + error.message)
        } finally {
          this.loading = false
        }
      },
      async showDetail(row) {
        try {
          const response = await getOrderInfo(row.orderId)
          this.currentOrder = response.data
          this.orderDialogVisible = true
        } catch (error) {
          this.$message.error('获取订单详情失败：' + error.message)
        }
      },
      async showDetailList(row) {
        this.detailListLoading = true
        try {
          const response = await getOrderDetailList(row.orderId)
          this.detailList = response.data
          this.detailListDialogVisible = true
        } catch (error) {
          this.$message.error('获取详单列表失败：' + error.message)
        } finally {
          this.detailListLoading = false
        }
      },
      exportDetails() {
        // TODO: 实现导出功能
        this.$message.success('数据导出成功')
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