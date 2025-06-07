<template>
  <div class="home-container">
    <el-row :gutter="20">
      <el-col :span="24">
        <el-card class="status-card">
          <div slot="header">
            <span>充电站实时状态</span>
          </div>
          <div class="station-status">
            <div class="charging-area">
              <h3>充电区</h3>
              <el-row :gutter="20">
                <el-col :span="4" v-for="pile in chargingPiles" :key="pile.id" class="pile-item">
                  <div class="pile-container">
                    <span class="pile-id">{{ pile.id }}</span>
                    <img
                      :src="pile.type === 'fast' ? require('../assets/fast-name.png') : require('../assets/slow-name.png')"
                      alt="Charging Pile"
                      class="pile-image"
                    />
                    
                  </div>
                  <div class="car-spots">
                    <div v-for="spot in pile.spots" :key="spot.id" class="car-spot">
                      <img v-if="spot.occupied" src="../assets/汽车.png" alt="Car" class="car-image">
                      <div v-else class="empty-spot"></div>
                    </div>
                  </div>
                  <p class="pile-status">{{ getStatusText(pile.status) }}</p>
                </el-col>
              </el-row>
            </div>
            
            <div class="waiting-area">
              <h3>等候区</h3>
              <el-row :gutter="20">
                <el-col :span="6" v-for="(spot, index) in waitingSpots" :key="index" class="waiting-spot-item">
                  <el-card class="spot-card empty">
                    <div class="spot-info">
                      <h4>{{ spot.id }}号车位</h4>
                       <div class="car-spots-waiting">
                         <div v-for="carIndex in spot.cars" :key="carIndex" class="car-spot-waiting">
                           <img src="../assets/汽车.png" alt="Car" class="car-image">
                         </div>
                         <div v-if="spot.cars === 0" class="empty-spot-waiting">空闲</div>
                       </div>
                    </div>
                  </el-card>
                </el-col>
              </el-row>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script>
export default {
  name: 'Home',
  data() {
    return {
      chargingPiles: [
        { id: 'A', type: 'fast', status: 'working', spots: [{ id: 1, occupied: true }] },
        { id: 'B', type: 'fast', status: 'working', spots: [{ id: 1, occupied: true }] },
        { id: 'C', type: 'slow', status: 'working', spots: [{ id: 1, occupied: true }, { id: 2, occupied: true }] },
        { id: 'D', type: 'slow', status: 'working', spots: [{ id: 1, occupied: true }] },
        { id: 'E', type: 'slow', status: 'idle', spots: [{ id: 1, occupied: false }] }
      ],
      waitingSpots: [
        { id: 'F1', cars: 1 },
        { id: 'F2', cars: 1 },
        { id: 'T1', cars: 1 },
        { id: 'T2', cars: 1 },
        { id: 'T3', cars: 1 },
        { id: 'T4', cars: 1 }
      ]
    }
  },
  methods: {
    getStatusText(status) {
      const statusMap = {
        working: '工作中',
        idle: '空闲',
        fault: '故障'
      }
      return statusMap[status] || status
    }
  }
}
</script>

<style scoped>
.home-container {
  padding: 20px;
}
.status-card {
  margin-bottom: 20px;
}
.station-status {
  display: flex;
  flex-direction: column;
  gap: 20px;
}
.charging-area, .waiting-area {
  margin-bottom: 20px;
}
h3 {
  margin-bottom: 15px;
  color: #303133;
}
h4 {
  margin: 0 0 10px 0;
  color: #606266;
}

.pile-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 20px;
}
.pile-container {
  position: relative;
  width: 120px; /* 增加宽度以容纳ID */
  height: 120px;
  display: flex;
  align-items: center;
}
.pile-image {
  width: 80px;
  height: 120px;
  object-fit: contain;
  margin-left: 25px; /* 添加左边距 */
}
.pile-id {
  position: absolute;
  left: 0;
  font-size: 24px;
  font-weight: bold;
  color: #333;
  z-index: 2;
  margin-right: 15px; /* 添加右边距 */
}
.car-spots {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-top: 10px;
}
.car-spot {
  width: 100px;
  height: 55px;
  border: 1px dashed #666; /* 加粗边框并加深颜色 */
  margin-bottom: 5px;
  display: flex;
  justify-content: center;
  align-items: center;
  overflow: hidden; /* 防止内容溢出 */
  position: relative; /* 添加相对定位 */
}
.car-image {
  width: 90%;
  height: 90%;
  object-fit: contain;
  transform: scale(1); /* 增加图片尺寸 */
}
.empty-spot {
   width: 100%;
  height: 100%;
}

.pile-status {
  margin-top: 10px;
  font-size: 14px;
  color: #606266;
}

.waiting-spot-item .el-card__body {
    padding: 10px;
}

.waiting-area .el-col {
    margin-bottom: 20px;
}

.car-spots-waiting {
    display: flex;
    flex-wrap: wrap;
    justify-content: center;
    gap: 5px;
}

.car-spot-waiting {
    width: 100px; /* Adjust size as needed */
    height: 60px; /* Adjust size as needed */
     display: flex;
  justify-content: center;
  align-items: center;
}

.empty-spot-waiting {
    text-align: center;
    color: #909399;
}

/* Remove default card styles that conflict with custom layout */
.pile-card,
.spot-card {
  border: none;
  box-shadow: none;
}
</style> 