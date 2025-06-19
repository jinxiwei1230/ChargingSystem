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

 Date: 10/06/2025 23:44:52
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
) ENGINE = InnoDB AUTO_INCREMENT = 65 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '充电详单表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of charging_detail
-- ----------------------------
INSERT INTO `charging_detail` VALUES (48, 'ORD202506045110', 1, 'PEAK', '2025-06-04 20:15:52', '2025-06-04 20:17:15', 1.00, 0.50, 1.00, 0.80, DEFAULT, DEFAULT, DEFAULT);
INSERT INTO `charging_detail` VALUES (49, 'ORD202506046547', 1, 'PEAK', '2025-06-04 20:16:13', '2025-06-04 20:19:50', 3.00, 1.00, 1.00, 0.80, DEFAULT, DEFAULT, DEFAULT);
INSERT INTO `charging_detail` VALUES (50, 'ORD202506046781', 1, 'PEAK', '2025-06-04 20:15:34', '2025-06-04 20:21:36', 6.00, 0.58, 1.00, 0.80, DEFAULT, DEFAULT, DEFAULT);
INSERT INTO `charging_detail` VALUES (51, 'ORD202506046114', 1, 'PEAK', '2025-06-04 20:25:06', '2025-06-04 20:29:00', 3.00, 1.50, 1.00, 0.80, DEFAULT, DEFAULT, DEFAULT);
INSERT INTO `charging_detail` VALUES (52, 'ORD202506040306', 1, 'PEAK', '2025-06-04 20:25:01', '2025-06-04 20:30:30', 5.00, 0.58, 1.00, 0.80, DEFAULT, DEFAULT, DEFAULT);
INSERT INTO `charging_detail` VALUES (53, 'ORD202506049280', 1, 'PEAK', '2025-06-04 20:25:06', '2025-06-04 20:31:42', 6.00, 3.00, 1.00, 0.80, DEFAULT, DEFAULT, DEFAULT);
INSERT INTO `charging_detail` VALUES (54, 'ORD202506040734', 1, 'PEAK', '2025-06-04 20:29:00', '2025-06-04 20:32:07', 3.00, 1.50, 1.00, 0.80, DEFAULT, DEFAULT, DEFAULT);
INSERT INTO `charging_detail` VALUES (55, 'ORD202506043313', 1, 'PEAK', '2025-06-04 20:31:42', '2025-06-04 20:39:01', 7.00, 3.50, 1.00, 0.80, DEFAULT, DEFAULT, DEFAULT);
INSERT INTO `charging_detail` VALUES (56, 'ORD202506041834', 1, 'PEAK', '2025-06-04 20:28:16', '2025-06-04 20:47:49', 19.00, 2.22, 1.00, 0.80, DEFAULT, DEFAULT, DEFAULT);
INSERT INTO `charging_detail` VALUES (57, 'ORD202506044913', 1, 'PEAK', '2025-06-04 20:48:31', '2025-06-04 20:59:40', 11.00, 5.50, 1.00, 0.80, DEFAULT, DEFAULT, DEFAULT);
INSERT INTO `charging_detail` VALUES (58, 'ORD202506041238', 1, 'PEAK', '2025-06-04 20:59:40', '2025-06-04 20:59:55', 0.00, 0.00, 1.00, 0.80, DEFAULT, DEFAULT, DEFAULT);
INSERT INTO `charging_detail` VALUES (59, 'ORD202506044124', 1, 'STANDARD', '2025-06-04 21:01:27', '2025-06-04 21:01:53', 0.00, 0.00, 0.70, 0.80, DEFAULT, DEFAULT, DEFAULT);
INSERT INTO `charging_detail` VALUES (60, 'ORD202506048696', 1, 'STANDARD', '2025-06-04 21:24:00', '2025-06-04 21:24:54', 0.00, 0.00, 0.70, 0.80, DEFAULT, DEFAULT, DEFAULT);
INSERT INTO `charging_detail` VALUES (61, 'ORD202506046622', 1, 'STANDARD', '2025-06-04 21:24:54', '2025-06-04 21:24:58', 0.00, 0.00, 0.70, 0.80, DEFAULT, DEFAULT, DEFAULT);
INSERT INTO `charging_detail` VALUES (62, 'ORD202506040089', 1, 'STANDARD', '2025-06-04 21:25:35', '2025-06-04 21:26:05', 0.00, 0.00, 0.70, 0.80, DEFAULT, DEFAULT, DEFAULT);
INSERT INTO `charging_detail` VALUES (63, 'ORD202506046989', 1, 'STANDARD', '2025-06-04 21:25:40', '2025-06-04 21:27:57', 2.00, 1.00, 0.70, 0.80, DEFAULT, DEFAULT, DEFAULT);
INSERT INTO `charging_detail` VALUES (64, 'ORD20250610354217', 1, 'STANDARD', '2025-06-10 22:34:05', '2025-06-10 22:35:04', 0.00, 0.00, 0.70, 0.80, DEFAULT, DEFAULT, DEFAULT);

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
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '充电订单主表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of charging_order
-- ----------------------------
INSERT INTO `charging_order` VALUES ('ORD20231124F1001', 1, '京A12345', 1, 'A', '2023-11-24', 'COMPLETED', 30.00, 1.00, 21.00, 24.00, 45.00, '2025-06-01 14:00:00', '2025-06-01 15:00:00', '2025-05-29 09:25:01');
INSERT INTO `charging_order` VALUES ('ORD20231125F1001', 1, '京A12345', 3, 'A', '2023-11-25', 'CHARGING', 15.50, 0.52, 10.85, 12.40, 23.25, '2023-11-25 08:30:00', NULL, '2025-05-29 09:25:01');
INSERT INTO `charging_order` VALUES ('ORD202506040089', 2, '京B67890', 161, 'A', '2025-06-04', 'FAULTED', 0.00, 0.00, 0.00, 0.00, 0.00, '2025-06-04 21:25:35', '2025-06-04 21:26:05', '2025-06-04 21:26:05');
INSERT INTO `charging_order` VALUES ('ORD202506040306', 12, '京B26232', 147, 'C', '2025-06-04', 'COMPLETED', 0.58, 5.00, 0.58, 0.47, 1.05, '2025-06-04 20:25:01', '2025-06-04 20:30:30', '2025-06-04 20:30:30');
INSERT INTO `charging_order` VALUES ('ORD202506040734', 3, '京A13422', 150, 'A', '2025-06-04', 'COMPLETED', 1.50, 3.00, 1.50, 1.20, 2.70, '2025-06-04 20:29:00', '2025-06-04 20:32:07', '2025-06-04 20:32:07');
INSERT INTO `charging_order` VALUES ('ORD202506041238', 2, '京B67890', 154, 'A', '2025-06-04', 'COMPLETED', 0.00, 0.00, 0.00, 0.00, 0.00, '2025-06-04 20:59:40', '2025-06-04 20:59:55', '2025-06-04 20:59:55');
INSERT INTO `charging_order` VALUES ('ORD202506041834', 5, '京B26271', 152, 'D', '2025-06-04', 'COMPLETED', 2.22, 19.00, 2.22, 1.77, 3.99, '2025-06-04 20:28:16', '2025-06-04 20:47:49', '2025-06-04 20:47:49');
INSERT INTO `charging_order` VALUES ('ORD202506043313', 4, '京B28271', 151, 'B', '2025-06-04', 'FAULTED', 3.50, 7.00, 3.50, 2.80, 6.30, '2025-06-04 20:31:42', '2025-06-04 20:39:01', '2025-06-04 20:39:01');
INSERT INTO `charging_order` VALUES ('ORD202506044124', 1, '京A12345', 156, 'A', '2025-06-04', 'FAULTED', 0.00, 0.00, 0.00, 0.00, 0.00, '2025-06-04 21:01:27', '2025-06-04 21:01:53', '2025-06-04 21:01:53');
INSERT INTO `charging_order` VALUES ('ORD202506044913', 1, '京A12345', 153, 'A', '2025-06-04', 'COMPLETED', 5.50, 11.00, 5.50, 4.40, 9.90, '2025-06-04 20:48:31', '2025-06-04 20:59:40', '2025-06-04 20:59:40');
INSERT INTO `charging_order` VALUES ('ORD202506045110', 1, '京A12345', 143, 'B', '2025-06-04', 'COMPLETED', 0.50, 1.00, 0.50, 0.40, 0.90, '2025-06-04 20:15:52', '2025-06-04 20:17:15', '2025-06-04 20:17:15');
INSERT INTO `charging_order` VALUES ('ORD202506046114', 1, '京A12345', 148, 'A', '2025-06-04', 'COMPLETED', 1.50, 3.00, 1.50, 1.20, 2.70, '2025-06-04 20:25:06', '2025-06-04 20:29:00', '2025-06-04 20:29:00');
INSERT INTO `charging_order` VALUES ('ORD202506046547', 2, '京B67890', 144, 'B', '2025-06-04', 'COMPLETED', 1.00, 2.00, 1.00, 0.80, 1.80, '2025-06-04 20:16:13', '2025-06-04 20:19:50', '2025-06-04 20:19:50');
INSERT INTO `charging_order` VALUES ('ORD202506046622', 2, '京B67890', 159, 'B', '2025-06-04', 'COMPLETED', 0.00, 0.00, 0.00, 0.00, 0.00, '2025-06-04 21:24:54', '2025-06-04 21:24:58', '2025-06-04 21:24:58');
INSERT INTO `charging_order` VALUES ('ORD202506046781', 12, '京B26232', 142, 'C', '2025-06-04', 'COMPLETED', 0.58, 5.00, 0.58, 0.47, 1.05, '2025-06-04 20:15:34', '2025-06-04 20:21:36', '2025-06-04 20:21:36');
INSERT INTO `charging_order` VALUES ('ORD202506046989', 3, '京A13422', 162, 'B', '2025-06-04', 'COMPLETED', 1.00, 2.00, 0.70, 0.80, 1.50, '2025-06-04 21:25:40', '2025-06-04 21:27:57', '2025-06-04 21:27:57');
INSERT INTO `charging_order` VALUES ('ORD202506048696', 1, '京A12345', 158, 'B', '2025-06-04', 'COMPLETED', 0.00, 0.00, 0.00, 0.00, 0.00, '2025-06-04 21:24:00', '2025-06-04 21:24:54', '2025-06-04 21:24:54');
INSERT INTO `charging_order` VALUES ('ORD202506049280', 2, '京B67890', 149, 'B', '2025-06-04', 'COMPLETED', 3.00, 6.00, 3.00, 2.40, 5.40, '2025-06-04 20:25:06', '2025-06-04 20:31:42', '2025-06-04 20:31:42');
INSERT INTO `charging_order` VALUES ('ORD202506071490', 1, '京A12345', 165, 'A', '2025-06-07', 'FAULTED', 0.00, 0.00, 0.00, 0.00, 0.00, '2025-06-07 23:47:26', '2025-06-07 23:47:36', '2025-06-07 23:47:36');
INSERT INTO `charging_order` VALUES ('ORD20250610354217', 3, 'V3', 178, 'D', '2025-06-10', 'FAULTED', 0.00, 0.00, 0.00, 0.00, 0.00, '2025-06-10 22:34:05', '2025-06-10 22:35:04', '2025-06-10 22:35:04');

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
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '充电桩设备表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of charging_pile
-- ----------------------------
INSERT INTO `charging_pile` VALUES ('A', 'FAST', 'AVAILABLE', 40, 8, 2.383333333333333, 71.5, '2025-05-29 09:25:01', '2025-06-10 22:44:54');
INSERT INTO `charging_pile` VALUES ('B', 'FAST', 'AVAILABLE', 30, 11, 2.15, 64.5, '2025-05-29 09:25:01', '2025-06-10 22:36:05');
INSERT INTO `charging_pile` VALUES ('C', 'SLOW', 'AVAILABLE', 7, 4, 0.8666666666666667, 6.066666666666666, '2025-05-29 09:25:01', '2025-06-10 22:44:56');
INSERT INTO `charging_pile` VALUES ('D', 'SLOW', 'AVAILABLE', 7, 3, 0.9333333333333333, 6.533333333333333, '2025-05-29 09:25:01', '2025-06-10 22:44:59');
INSERT INTO `charging_pile` VALUES ('E', 'SLOW', 'AVAILABLE', 7, 0, 0, 0, '2025-05-29 09:25:01', '2025-06-10 22:36:08');

-- ----------------------------
-- Table structure for charging_request
-- ----------------------------
DROP TABLE IF EXISTS `charging_request`;
CREATE TABLE `charging_request`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '请求唯一ID',
  `user_id` bigint NOT NULL COMMENT '关联用户ID',
  `charging_pile_id` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '分配的充电桩ID（可为空）',
  `status` enum('WAITING_IN_WAITING_AREA','WAITING_IN_CHARGING_AREA','CHARGING','COMPLETED','CANCELED') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'WAITING_IN_WAITING_AREA' COMMENT '请求状态',
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
) ENGINE = InnoDB AUTO_INCREMENT = 190 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '充电请求表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of charging_request
-- ----------------------------

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
) ENGINE = InnoDB AUTO_INCREMENT = 13 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户信息表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of user
-- ----------------------------
INSERT INTO `user` VALUES (1, 'user1', 'V1', 150, '123456');
INSERT INTO `user` VALUES (2, 'user2', 'V2', 150, '123456');
INSERT INTO `user` VALUES (3, 'user3', 'V3', 150, '123456');
INSERT INTO `user` VALUES (4, 'user4', 'V4', 150, '123456');
INSERT INTO `user` VALUES (5, 'user5', 'V5', 150, '123456');
INSERT INTO `user` VALUES (6, 'user6', 'V6', 150, '123456');
INSERT INTO `user` VALUES (7, 'user7', 'V7', 150, '123456');
INSERT INTO `user` VALUES (8, 'user8', 'V8', 150, '123546');

SET FOREIGN_KEY_CHECKS = 1;
