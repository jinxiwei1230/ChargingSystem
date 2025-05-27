-- 用户表数据
INSERT INTO user (username, password, phone_number, email) VALUES
('user1', '123456', '13800138001', 'user1@example.com'),
('user2', '123456', '13800138002', 'user2@example.com'),
('admin', 'admin', '13800138000', 'admin@example.com');

-- 充电桩表数据
INSERT INTO charging_pile (id, type, status, charging_power) VALUES
('A', 'FAST', 'AVAILABLE', 30),
('B', 'FAST', 'AVAILABLE', 30),
('C', 'SLOW', 'AVAILABLE', 7),
('D', 'SLOW', 'AVAILABLE', 7),
('E', 'SLOW', 'AVAILABLE', 7);

-- 充电请求表数据
INSERT INTO charging_request (user_id, car_id, status, mode, amount, battery_capacity, queue_number, queue_join_time) VALUES
(1, '京A12345', 'WAITING', 'FAST', 30, 60, 'F1', '2023-11-25 09:00:00'),
(2, '京B67890', 'WAITING', 'SLOW', 15, 40, 'T1', '2023-11-25 09:05:00'),
(1, '京A12345', 'CHARGING', 'FAST', 45, 60, 'F2', '2023-11-25 08:30:00');

-- 充电队列表数据
INSERT INTO charging_queue (pile_id, request_id, car_id, queue_type, queue_status, position) VALUES
('A', 3, '京A12345', 'FAST', 'CHARGING', 1),
('C', 2, '京B67890', 'SLOW', 'WAITING', 1),
('D', 1, '京A12345', 'FAST', 'WAITING', 2);

-- 订单表数据
INSERT INTO charging_order (order_id, user_id, car_id, request_id, pile_id, order_date, order_status, total_kwh, total_duration, total_charge_fee, total_service_fee, total_fee, start_time, end_time) VALUES
('ORD20231125F1001', 1, '京A12345', 3, 'A', '2023-11-25', 'CHARGING', 15.50, 0.52, 10.85, 12.40, 23.25, '2023-11-25 08:30:00', NULL),
('ORD20231125T2001', 2, '京B67890', 2, 'C', '2023-11-25', 'CREATED', 0, 0, 0, 0, 0, '2023-11-25 09:05:00', NULL),
('ORD20231124F1001', 1, '京A12345', 1, 'A', '2023-11-24', 'COMPLETED', 30.00, 1.00, 21.00, 24.00, 45.00, '2023-11-24 14:00:00', '2023-11-24 15:00:00');

-- 充电记录表数据
INSERT INTO charging_record (request_id, pile_id, order_id, start_time, end_time, charge_amount, charging_duration, charging_fee, service_fee) VALUES
(3, 'A', 'ORD20231125F1001', '2023-11-25 08:30:00', NULL, 15.50, 0.52, 10.85, 12.40),
(1, 'A', 'ORD20231124F1001', '2023-11-24 14:00:00', '2023-11-24 15:00:00', 30.00, 1.00, 21.00, 24.00),
(2, 'C', 'ORD20231125T2001', '2023-11-25 09:05:00', NULL, 0, 0, 0, 0);

-- 详单表数据
INSERT INTO charging_detail (order_id, period_seq, period_type, start_time, end_time, duration, kwh, charge_rate) VALUES
('ORD20231124F1001', 1, 'PEAK', '2023-11-24 14:00:00', '2023-11-24 15:00:00', 1.00, 30.00, 1.00),
('ORD20231125F1001', 1, 'STANDARD', '2023-11-25 08:30:00', '2023-11-25 09:12:00', 0.70, 21.00, 0.70),
('ORD20231125F1001', 2, 'PEAK', '2023-11-25 09:12:00', '2023-11-25 09:22:00', 0.17, 5.10, 1.00);

