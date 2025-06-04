/*
 Navicat Premium Data Transfer

 Source Server         : localhost_3306
 Source Server Type    : MySQL
 Source Server Version : 80036
 Source Host           : localhost:3306
 Source Schema         : chargingsystem

 Target Server Type    : MySQL
 Target Server Version : 80036
 File Encoding         : 65001

 Date: 01/06/2025 18:52:38
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for charging_detail
-- ----------------------------
DROP TABLE IF EXISTS `charging_detail`;
CREATE TABLE `charging_detail`  (
  `detail_id` bigint NOT NULL AUTO_INCREMENT COMMENT '详单ID',
  `order_id` varchar(24) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '关联订单ID',
  `period_seq` int NOT NULL COMMENT '计费时段顺序号(1,2,3...表示跨时段计费)',
  `period_type` enum('PEAK','STANDARD','VALLEY') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '电价时段类型',
  `start_time` datetime NOT NULL COMMENT '时段开始时间',
  `end_time` datetime NOT NULL COMMENT '时段结束时间',
  `duration` decimal(6, 2) UNSIGNED NOT NULL COMMENT '充电时长(小时)',
  `kwh` decimal(8, 2) UNSIGNED NOT NULL COMMENT '充电量(度)',
  `charge_rate` decimal(6, 2) UNSIGNED NOT NULL COMMENT '电费单价(元/度)',
  `service_rate` decimal(6, 2) UNSIGNED NOT NULL DEFAULT 0.80 COMMENT '服务费单价(元/度)',
  `charge_fee` decimal(10, 2) UNSIGNED GENERATED ALWAYS AS ((`kwh` * `charge_rate`)) STORED COMMENT '时段电费' NULL,
  `service_fee` decimal(10, 2) UNSIGNED GENERATED ALWAYS AS ((`kwh` * `service_rate`)) STORED COMMENT '时段服务费' NULL,
  `sub_total` decimal(10, 2) UNSIGNED GENERATED ALWAYS AS ((`kwh` * (`charge_rate` + `service_rate`))) STORED COMMENT '时段合计' NULL,
  PRIMARY KEY (`detail_id`) USING BTREE,
  INDEX `idx_order_periods`(`order_id` ASC, `period_seq` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '充电详单表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of charging_detail
-- ----------------------------
INSERT INTO `charging_detail` VALUES (1, 'ORD20231124F1001', 1, 'PEAK', '2023-11-24 14:00:00', '2023-11-24 15:00:00', 1.00, 30.00, 1.00, 0.80, DEFAULT, DEFAULT, DEFAULT);
INSERT INTO `charging_detail` VALUES (2, 'ORD20231125F1001', 1, 'STANDARD', '2023-11-25 08:30:00', '2023-11-25 09:12:00', 0.70, 21.00, 0.70, 0.80, DEFAULT, DEFAULT, DEFAULT);
INSERT INTO `charging_detail` VALUES (3, 'ORD20231125F1001', 2, 'PEAK', '2023-11-25 09:12:00', '2023-11-25 09:22:00', 0.17, 5.10, 1.00, 0.80, DEFAULT, DEFAULT, DEFAULT);

-- ----------------------------
-- Table structure for charging_order
-- ----------------------------
DROP TABLE IF EXISTS `charging_order`;
CREATE TABLE `charging_order`  (
  `order_id` varchar(24) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '订单号(例：ORD20231125F1001)',
  `user_id` bigint NOT NULL COMMENT '关联用户ID',
  `car_id` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '车辆ID',
  `request_id` bigint NOT NULL COMMENT '关联充电请求',
  `pile_id` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '充电桩编号',
  `order_date` date NOT NULL COMMENT '订单日期(用于按日查询)',
  `order_status` enum('CREATED','CHARGING','COMPLETED','CANCELLED','FAULTED') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'CREATED' COMMENT '订单状态',
  `total_kwh` decimal(10, 2) UNSIGNED NOT NULL COMMENT '总充电量(度)',
  `total_duration` decimal(8, 2) UNSIGNED NOT NULL COMMENT '总充电时长(小时)',
  `total_charge_fee` decimal(10, 2) UNSIGNED NOT NULL COMMENT '累计电费',
  `total_service_fee` decimal(10, 2) UNSIGNED NOT NULL COMMENT '累计服务费',
  `total_fee` decimal(10, 2) UNSIGNED NOT NULL COMMENT '订单总额(电费+服务费)',
  `start_time` datetime NOT NULL COMMENT '充电开始时间',
  `end_time` datetime NULL DEFAULT NULL COMMENT '充电结束时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '订单创建时间',
  PRIMARY KEY (`order_id`) USING BTREE,
  INDEX `user_id`(`user_id` ASC) USING BTREE,
  INDEX `request_id`(`request_id` ASC) USING BTREE,
  INDEX `pile_id`(`pile_id` ASC) USING BTREE,
  INDEX `idx_car_orders`(`car_id` ASC, `order_date` ASC) USING BTREE,
  INDEX `idx_order_date`(`order_date` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '充电订单主表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of charging_order
-- ----------------------------
INSERT INTO `charging_order` VALUES ('ORD20231124F1001', 1, '京A12345', 1, 'A', '2023-11-24', 'COMPLETED', 30.00, 1.00, 21.00, 24.00, 45.00, '2023-11-24 14:00:00', '2023-11-24 15:00:00', '2025-05-29 09:25:01');
INSERT INTO `charging_order` VALUES ('ORD20231125F1001', 1, '京A12345', 3, 'A', '2023-11-25', 'CHARGING', 15.50, 0.52, 10.85, 12.40, 23.25, '2023-11-25 08:30:00', NULL, '2025-05-29 09:25:01');
INSERT INTO `charging_order` VALUES ('ORD20231125T2001', 2, '京B67890', 2, 'C', '2023-11-25', 'CREATED', 0.00, 0.00, 0.00, 0.00, 0.00, '2023-11-25 09:05:00', NULL, '2025-05-29 09:25:01');

-- ----------------------------
-- Table structure for charging_pile
-- ----------------------------
DROP TABLE IF EXISTS `charging_pile`;
CREATE TABLE `charging_pile`  (
  `id` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '充电桩编号(如A/B/C/D/E)',
  `type` enum('FAST','SLOW') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '快充/慢充类型',
  `status` enum('AVAILABLE','IN_USE','FAULT','OFFLINE') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'AVAILABLE' COMMENT '当前状态',
  `charging_power` double NOT NULL COMMENT '充电功率(度/小时)',
  `charging_times` int NULL DEFAULT 0 COMMENT '累计充电次数',
  `total_charging_duration` double NULL DEFAULT 0 COMMENT '总充电时长(小时)',
  `total_charging_amount` double NULL DEFAULT 0 COMMENT '总充电量(度)',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '充电桩设备表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of charging_pile
-- ----------------------------
INSERT INTO `charging_pile` VALUES ('A', 'FAST', 'AVAILABLE', 30, 0, 0, 0, '2025-05-29 09:25:01', '2025-05-29 09:25:01');
INSERT INTO `charging_pile` VALUES ('B', 'FAST', 'AVAILABLE', 30, 0, 0, 0, '2025-05-29 09:25:01', '2025-05-29 09:25:01');
INSERT INTO `charging_pile` VALUES ('C', 'SLOW', 'AVAILABLE', 7, 0, 0, 0, '2025-05-29 09:25:01', '2025-05-29 09:25:01');
INSERT INTO `charging_pile` VALUES ('D', 'SLOW', 'AVAILABLE', 7, 0, 0, 0, '2025-05-29 09:25:01', '2025-05-29 09:25:01');
INSERT INTO `charging_pile` VALUES ('E', 'SLOW', 'AVAILABLE', 7, 0, 0, 0, '2025-05-29 09:25:01', '2025-05-29 09:25:01');

-- ----------------------------
-- Table structure for charging_request
-- ----------------------------
DROP TABLE IF EXISTS `charging_request`;
CREATE TABLE `charging_request`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '请求唯一ID',
  `user_id` bigint NOT NULL COMMENT '关联用户ID',
  `charging_pile_id` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '分配的充电桩ID（可为空）',
  `status` enum('WAITING_IN_WAITING_AREA','WAITING_IN_CHARGING_AREA','CHARGING','COMPLETED','CANCELED','') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'WAITING_IN_WAITING_AREA' COMMENT '请求状态',
  `mode` enum('FAST','SLOW') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '充电模式',
  `amount` double NOT NULL COMMENT '请求充电量(度)',
  `queue_number` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '排队号(F1/T2格式)',
  `queue_join_time` datetime NULL DEFAULT NULL COMMENT '加入队列时间',
  `request_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '请求发起时间',
  `charging_start_time` datetime NULL DEFAULT NULL COMMENT '开始充电时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `user_id`(`user_id` ASC) USING BTREE,
  INDEX `charging_pile_id`(`charging_pile_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 14 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '充电请求表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of charging_request
-- ----------------------------
INSERT INTO `charging_request` VALUES (11, 2, NULL, 'WAITING_IN_WAITING_AREA', 'FAST', 20, 'F1', '2025-06-01 18:50:52', '2025-06-01 18:50:52', '2025-06-01 18:50:52');
INSERT INTO `charging_request` VALUES (12, 1, NULL, 'WAITING_IN_WAITING_AREA', 'FAST', 20, 'F2', '2025-06-01 18:51:04', '2025-06-01 18:51:04', '2025-06-01 18:51:04');
INSERT INTO `charging_request` VALUES (13, 3, NULL, 'WAITING_IN_WAITING_AREA', 'SLOW', 20, 'T1', '2025-06-01 18:51:13', '2025-06-01 18:51:13', '2025-06-01 18:51:13');

-- ----------------------------
-- Table structure for user
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户唯一标识',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '登录用户名',
  `car_number` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '车牌号',
  `battery_capacity` double NULL DEFAULT NULL,
  `password` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '密码',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `username`(`username` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 13 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户信息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user
-- ----------------------------
INSERT INTO `user` VALUES (1, 'user1', '京A12345', 60, '123456');
INSERT INTO `user` VALUES (2, 'user2', '京B67890', 80, '123456');
INSERT INTO `user` VALUES (3, 'user3', '京A13422', 90, '123456');
INSERT INTO `user` VALUES (4, 'user4', '京B28271', 50, '123456');
INSERT INTO `user` VALUES (5, 'user5', '京B26271', 60, '123456');
INSERT INTO `user` VALUES (12, 'user6', '京B26232', 80, '123456');

SET FOREIGN_KEY_CHECKS = 1;
