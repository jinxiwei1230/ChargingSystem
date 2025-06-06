/*
 Navicat Premium Data Transfer

 Source Server         : jdbc
 Source Server Type    : MySQL
 Source Server Version : 80403 (8.4.3)
 Source Host           : 127.0.0.1:3306
 Source Schema         : chargingsystem

 Target Server Type    : MySQL
 Target Server Version : 80403 (8.4.3)
 File Encoding         : 65001

 Date: 04/06/2025 19:42:39
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
) ENGINE = InnoDB AUTO_INCREMENT = 48 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '充电详单表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of charging_detail
-- ----------------------------

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
INSERT INTO `charging_pile` VALUES ('A', 'FAST', 'IN_USE', 30, 4, 2.1, 63, '2025-05-29 09:25:01', '2025-06-04 19:38:18');
INSERT INTO `charging_pile` VALUES ('B', 'FAST', 'AVAILABLE', 30, 5, 1.9666666666666668, 59, '2025-05-29 09:25:01', '2025-06-04 19:36:27');
INSERT INTO `charging_pile` VALUES ('C', 'SLOW', 'IN_USE', 7, 2, 0.7, 4.9, '2025-05-29 09:25:01', '2025-06-04 19:36:48');
INSERT INTO `charging_pile` VALUES ('D', 'SLOW', 'AVAILABLE', 7, 2, 0.6166666666666667, 4.316666666666666, '2025-05-29 09:25:01', '2025-06-04 15:06:53');
INSERT INTO `charging_pile` VALUES ('E', 'SLOW', 'AVAILABLE', 7, 0, 0, 0, '2025-05-29 09:25:01', '2025-06-04 00:15:38');

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
) ENGINE = InnoDB AUTO_INCREMENT = 130 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '充电请求表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of charging_request
-- ----------------------------
INSERT INTO `charging_request` VALUES (84, 1, 'C', 'CHARGING', 'SLOW', 10, 'T1', '2025-06-03 18:01:35', '2025-06-03 18:01:35', NULL);
INSERT INTO `charging_request` VALUES (85, 1, 'A', 'COMPLETED', 'FAST', 10, 'F1', '2025-06-03 18:01:39', '2025-06-03 18:01:39', NULL);
INSERT INTO `charging_request` VALUES (86, 2, 'B', 'COMPLETED', 'FAST', 10, 'F2', '2025-06-03 18:01:42', '2025-06-03 18:01:42', NULL);
INSERT INTO `charging_request` VALUES (87, 1, 'A', 'COMPLETED', 'FAST', 10, 'F1', '2025-06-03 18:03:57', '2025-06-03 18:03:57', NULL);
INSERT INTO `charging_request` VALUES (88, 1, 'A', 'COMPLETED', 'FAST', 10, 'F1', '2025-06-03 18:07:51', '2025-06-03 18:07:51', NULL);
INSERT INTO `charging_request` VALUES (89, 1, 'A', 'COMPLETED', 'FAST', 10, 'F1', '2025-06-03 19:05:33', '2025-06-03 19:05:33', NULL);
INSERT INTO `charging_request` VALUES (90, 2, 'A', 'COMPLETED', 'FAST', 10, 'F2', '2025-06-03 19:08:48', '2025-06-03 19:08:48', NULL);
INSERT INTO `charging_request` VALUES (91, 1, 'C', 'COMPLETED', 'SLOW', 10, 'T1', '2025-06-03 19:08:53', '2025-06-03 19:08:53', NULL);
INSERT INTO `charging_request` VALUES (92, 1, 'C', 'COMPLETED', 'SLOW', 10, 'T1', '2025-06-03 19:14:43', '2025-06-03 19:14:43', NULL);
INSERT INTO `charging_request` VALUES (93, 2, 'A', 'COMPLETED', 'FAST', 10, 'F1', '2025-06-03 19:14:49', '2025-06-03 19:14:49', NULL);
INSERT INTO `charging_request` VALUES (94, 3, 'B', 'COMPLETED', 'FAST', 10, 'F2', '2025-06-03 19:14:52', '2025-06-03 19:14:52', NULL);
INSERT INTO `charging_request` VALUES (95, 12, 'D', 'COMPLETED', 'SLOW', 10, 'T2', '2025-06-03 19:15:02', '2025-06-03 19:15:02', NULL);
INSERT INTO `charging_request` VALUES (96, 4, 'A', 'COMPLETED', 'FAST', 10, 'F3', '2025-06-03 19:15:31', '2025-06-03 19:15:31', NULL);
INSERT INTO `charging_request` VALUES (97, 1, 'A', 'CHARGING', 'FAST', 10, 'F1', '2025-06-04 00:08:45', '2025-06-04 00:08:45', NULL);
INSERT INTO `charging_request` VALUES (98, 2, 'B', 'CHARGING', 'FAST', 10, 'F2', '2025-06-04 00:08:49', '2025-06-04 00:08:49', NULL);
INSERT INTO `charging_request` VALUES (99, 3, 'A', 'WAITING_IN_CHARGING_AREA', 'FAST', 10, 'F3', '2025-06-04 00:08:52', '2025-06-04 00:08:52', NULL);
INSERT INTO `charging_request` VALUES (100, 4, 'B', 'WAITING_IN_CHARGING_AREA', 'FAST', 10, 'F4', '2025-06-04 00:08:55', '2025-06-04 00:08:55', NULL);
INSERT INTO `charging_request` VALUES (101, 1, 'A', 'CHARGING', 'FAST', 10, 'F1', '2025-06-04 00:13:33', '2025-06-04 00:13:33', NULL);
INSERT INTO `charging_request` VALUES (102, 2, 'B', 'CHARGING', 'FAST', 10, 'F2', '2025-06-04 00:13:35', '2025-06-04 00:13:35', NULL);
INSERT INTO `charging_request` VALUES (103, 3, 'A', 'WAITING_IN_CHARGING_AREA', 'FAST', 10, 'F3', '2025-06-04 00:13:38', '2025-06-04 00:13:38', NULL);
INSERT INTO `charging_request` VALUES (104, 4, 'B', 'WAITING_IN_CHARGING_AREA', 'FAST', 10, 'F4', '2025-06-04 00:13:40', '2025-06-04 00:13:40', NULL);
INSERT INTO `charging_request` VALUES (105, 12, 'C', 'CHARGING', 'SLOW', 10, 'T1', '2025-06-04 00:13:43', '2025-06-04 00:13:43', NULL);
INSERT INTO `charging_request` VALUES (106, 1, 'D', 'CHARGING', 'SLOW', 10, 'T2', '2025-06-04 00:13:46', '2025-06-04 00:13:46', NULL);
INSERT INTO `charging_request` VALUES (107, 2, 'E', 'CHARGING', 'SLOW', 10, 'T3', '2025-06-04 00:13:49', '2025-06-04 00:13:49', NULL);
INSERT INTO `charging_request` VALUES (108, 3, 'C', 'WAITING_IN_CHARGING_AREA', 'SLOW', 10, 'T4', '2025-06-04 00:13:53', '2025-06-04 00:13:53', NULL);
INSERT INTO `charging_request` VALUES (110, 12, 'C', 'COMPLETED', 'SLOW', 10, 'T1', '2025-06-04 14:48:27', '2025-06-04 14:48:27', NULL);
INSERT INTO `charging_request` VALUES (111, 2, 'B', 'COMPLETED', 'FAST', 10, 'F2', '2025-06-04 14:48:29', '2025-06-04 14:48:29', NULL);
INSERT INTO `charging_request` VALUES (112, 3, 'A', 'WAITING_IN_CHARGING_AREA', 'FAST', 10, 'F3', '2025-06-04 14:48:32', '2025-06-04 14:48:32', NULL);
INSERT INTO `charging_request` VALUES (113, 4, 'B', 'COMPLETED', 'FAST', 10, 'F4', '2025-06-04 14:48:34', '2025-06-04 14:48:34', NULL);
INSERT INTO `charging_request` VALUES (114, 5, 'B', 'CHARGING', 'FAST', 10, 'F5', '2025-06-04 14:56:22', '2025-06-04 14:56:22', NULL);
INSERT INTO `charging_request` VALUES (115, 5, 'D', 'COMPLETED', 'SLOW', 10, 'T2', '2025-06-04 14:56:40', '2025-06-04 14:56:40', NULL);
INSERT INTO `charging_request` VALUES (116, 12, 'C', 'CHARGING', 'SLOW', 10, 'T1', '2025-06-04 15:09:11', '2025-06-04 15:09:11', NULL);
INSERT INTO `charging_request` VALUES (117, 1, 'A', 'COMPLETED', 'FAST', 10, 'F1', '2025-06-04 15:09:14', '2025-06-04 15:09:14', NULL);
INSERT INTO `charging_request` VALUES (118, 2, 'B', 'COMPLETED', 'FAST', 10, 'F2', '2025-06-04 15:09:16', '2025-06-04 15:09:16', NULL);
INSERT INTO `charging_request` VALUES (119, 3, 'A', 'COMPLETED', 'FAST', 10, 'F3', '2025-06-04 15:09:19', '2025-06-04 15:09:19', NULL);
INSERT INTO `charging_request` VALUES (120, 4, 'B', 'COMPLETED', 'FAST', 10, 'F4', '2025-06-04 15:09:21', '2025-06-04 15:09:21', NULL);
INSERT INTO `charging_request` VALUES (121, 5, NULL, 'WAITING_IN_WAITING_AREA', 'FAST', 10, 'F5', '2025-06-04 15:09:23', '2025-06-04 15:09:23', NULL);
INSERT INTO `charging_request` VALUES (122, 12, 'C', 'CHARGING', 'SLOW', 10, 'T1', '2025-06-04 19:30:56', '2025-06-04 19:30:56', NULL);
INSERT INTO `charging_request` VALUES (123, 1, 'A', 'CHARGING', 'FAST', 10, 'F1', '2025-06-04 19:31:01', '2025-06-04 19:31:01', NULL);
INSERT INTO `charging_request` VALUES (124, 2, 'B', 'CHARGING', 'FAST', 10, 'F2', '2025-06-04 19:31:03', '2025-06-04 19:31:03', NULL);
INSERT INTO `charging_request` VALUES (125, 3, 'A', 'WAITING_IN_CHARGING_AREA', 'FAST', 10, 'F3', '2025-06-04 19:31:06', '2025-06-04 19:31:06', NULL);
INSERT INTO `charging_request` VALUES (126, 4, 'B', 'WAITING_IN_CHARGING_AREA', 'FAST', 10, 'F4', '2025-06-04 19:31:09', '2025-06-04 19:31:09', NULL);
INSERT INTO `charging_request` VALUES (127, 5, NULL, 'WAITING_IN_WAITING_AREA', 'FAST', 10, 'F5', '2025-06-04 19:31:12', '2025-06-04 19:31:12', NULL);
INSERT INTO `charging_request` VALUES (128, 12, 'C', 'CHARGING', 'SLOW', 10, 'T1', '2025-06-04 19:36:45', '2025-06-04 19:36:45', NULL);
INSERT INTO `charging_request` VALUES (129, 1, 'A', 'CHARGING', 'FAST', 10, 'F1', '2025-06-04 19:38:16', '2025-06-04 19:38:16', NULL);

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
INSERT INTO `user` VALUES (1, 'user1', '京A12345', 60, '123456');
INSERT INTO `user` VALUES (2, 'user2', '京B67890', 80, '123456');
INSERT INTO `user` VALUES (3, 'user3', '京A13422', 90, '123456');
INSERT INTO `user` VALUES (4, 'user4', '京B28271', 50, '123456');
INSERT INTO `user` VALUES (5, 'user5', '京B26271', 60, '123456');
INSERT INTO `user` VALUES (12, 'user6', '京B26232', 80, '123456');

SET FOREIGN_KEY_CHECKS = 1;
