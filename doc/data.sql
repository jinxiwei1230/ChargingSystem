-- ----------------------------
-- 用户表：存储注册用户信息
-- ----------------------------
CREATE TABLE user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用户唯一标识',
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '登录用户名',
    password VARCHAR(100) NOT NULL COMMENT '密码',
--     phone_number VARCHAR(20) COMMENT '联系电话',
--     email VARCHAR(100) COMMENT '电子邮箱',
--     created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
--     updated_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间'
) ENGINE=InnoDB COMMENT='用户信息表';

-- ----------------------------
-- 充电桩表：充电设备基本信息
-- ----------------------------
CREATE TABLE charging_pile (
    id VARCHAR(10) PRIMARY KEY COMMENT '充电桩编号(如A/B/C/D/E)',
    type ENUM('FAST', 'SLOW') NOT NULL COMMENT '快充/慢充类型',
    status ENUM('AVAILABLE', 'IN_USE', 'FAULT', 'OFFLINE') NOT NULL DEFAULT 'AVAILABLE' COMMENT '当前状态',
    charging_power DOUBLE NOT NULL COMMENT '充电功率(度/小时)',
    charging_times INT DEFAULT 0 COMMENT '累计充电次数',
    total_charging_duration DOUBLE DEFAULT 0 COMMENT '总充电时长(小时)',
    total_charging_amount DOUBLE DEFAULT 0 COMMENT '总充电量(度)',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB COMMENT='充电桩设备表';

-- ----------------------------
-- 充电请求表：充电请求核心数据
-- ----------------------------
CREATE TABLE charging_request (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '请求唯一ID',
    user_id BIGINT NOT NULL COMMENT '关联用户ID',
    car_id VARCHAR(20) NOT NULL COMMENT '车辆唯一标识',
    charging_pile_id VARCHAR(10) COMMENT '分配的充电桩ID（可为空）',
    status ENUM('WAITING', 'CHARGING', 'COMPLETED', 'CANCELED') NOT NULL DEFAULT 'WAITING' COMMENT '请求状态',
    mode ENUM('FAST', 'SLOW') NOT NULL COMMENT '充电模式',
    amount DOUBLE NOT NULL COMMENT '请求充电量(度)',
    battery_capacity DOUBLE NOT NULL COMMENT '车辆电池总容量(度)',
    queue_number VARCHAR(10) COMMENT '排队号(F1/T2格式)',
    queue_join_time DATETIME COMMENT '加入队列时间',
    request_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '请求发起时间',
    INDEX idx_status (status),
    INDEX idx_car_id (car_id),
    FOREIGN KEY (user_id) REFERENCES user(id),
    FOREIGN KEY (charging_pile_id) REFERENCES charging_pile(id)
) ENGINE=InnoDB COMMENT='充电请求表';

-- ----------------------------
-- 充电队列表：实时队列管理
-- ----------------------------
CREATE TABLE charging_queue (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '队列条目ID',
    pile_id VARCHAR(10) NOT NULL COMMENT '充电桩ID',
    request_id BIGINT NOT NULL COMMENT '充电请求ID',
    car_id VARCHAR(20) NOT NULL COMMENT '车辆ID',
    queue_type ENUM('FAST', 'SLOW') NOT NULL COMMENT '队列类型',
    queue_status ENUM('WAITING', 'CHARGING') NOT NULL DEFAULT 'WAITING' COMMENT '队列状态',
    position TINYINT NOT NULL DEFAULT 1 COMMENT '队列位置(1-2)',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '加入队列时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
    FOREIGN KEY (pile_id) REFERENCES charging_pile(id),
    FOREIGN KEY (request_id) REFERENCES charging_request(id),
    INDEX idx_pile_status (pile_id, queue_status)
) ENGINE=InnoDB COMMENT='充电实时队列表';


-- ----------------------------
-- 订单表(主表)：充电订单汇总信息
-- ----------------------------
CREATE TABLE charging_order (
    order_id VARCHAR(24) PRIMARY KEY COMMENT '订单号(例：ORD20231125F1001)',
    user_id BIGINT NOT NULL COMMENT '关联用户ID',
    car_id VARCHAR(20) NOT NULL COMMENT '车辆ID',
    request_id BIGINT NOT NULL COMMENT '关联充电请求',
    pile_id VARCHAR(10) NOT NULL COMMENT '充电桩编号',
    order_date DATE NOT NULL COMMENT '订单日期(用于按日查询)',
    order_status ENUM('CREATED', 'CHARGING', 'COMPLETED', 'CANCELLED', 'FAULTED') NOT NULL DEFAULT 'CREATED' COMMENT '订单状态',
    total_kwh DECIMAL(10,2) UNSIGNED NOT NULL COMMENT '总充电量(度)',
    total_duration DECIMAL(8,2) UNSIGNED NOT NULL COMMENT '总充电时长(小时)',
    total_charge_fee DECIMAL(10,2) UNSIGNED NOT NULL COMMENT '累计电费',
    total_service_fee DECIMAL(10,2) UNSIGNED NOT NULL COMMENT '累计服务费',
    total_fee DECIMAL(10,2) UNSIGNED NOT NULL COMMENT '订单总额(电费+服务费)',
    start_time DATETIME NOT NULL COMMENT '充电开始时间',
    end_time DATETIME COMMENT '充电结束时间',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '订单创建时间',
    FOREIGN KEY (user_id) REFERENCES user(id),
    FOREIGN KEY (request_id) REFERENCES charging_request(id),
    FOREIGN KEY (pile_id) REFERENCES charging_pile(id),
    INDEX idx_car_orders (car_id, order_date),
    INDEX idx_order_date (order_date)
) ENGINE=InnoDB COMMENT='充电订单主表';


-- ----------------------------
-- 充电记录表：充电过程明细
-- ----------------------------
CREATE TABLE charging_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '记录ID',
    request_id BIGINT NOT NULL COMMENT '关联充电请求',
    pile_id VARCHAR(10) NOT NULL COMMENT '使用的充电桩',
		order_id VARCHAR(24) COMMENT '关联订单ID',
    start_time DATETIME NOT NULL COMMENT '充电开始时间',
    end_time DATETIME COMMENT '充电结束时间',
    charge_amount DECIMAL(10,2) NOT NULL COMMENT '实际充电量(度)',
    charging_duration DOUBLE NOT NULL COMMENT '充电时长(小时)',
    charging_fee DECIMAL(10,2) NOT NULL COMMENT '电费金额',
    service_fee DECIMAL(10,2) NOT NULL COMMENT '服务费金额',
    total_fee DECIMAL(10,2) GENERATED ALWAYS AS (charging_fee + service_fee) STORED COMMENT '总费用',
    FOREIGN KEY (request_id) REFERENCES charging_request(id),
    FOREIGN KEY (pile_id) REFERENCES charging_pile(id),
		FOREIGN KEY (order_id) REFERENCES charging_order(order_id),
    INDEX idx_start_time (start_time)
) ENGINE=InnoDB COMMENT='充电明细记录表';


-- ----------------------------
-- 详单表(子表)：分时段计费明细
-- ----------------------------
CREATE TABLE charging_detail (
    detail_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '详单ID',
    order_id VARCHAR(24) NOT NULL COMMENT '关联订单ID',
    period_seq INT NOT NULL COMMENT '计费时段顺序号(1,2,3...表示跨时段计费)',
    period_type ENUM('PEAK', 'STANDARD', 'VALLEY') NOT NULL COMMENT '电价时段类型',
    start_time DATETIME NOT NULL COMMENT '时段开始时间',
    end_time DATETIME NOT NULL COMMENT '时段结束时间',
    duration DECIMAL(6,2) UNSIGNED NOT NULL COMMENT '充电时长(小时)',
    kwh DECIMAL(8,2) UNSIGNED NOT NULL COMMENT '充电量(度)',
    charge_rate DECIMAL(6,2) UNSIGNED NOT NULL COMMENT '电费单价(元/度)',
    service_rate DECIMAL(6,2) UNSIGNED NOT NULL DEFAULT 0.8 COMMENT '服务费单价(元/度)',
    charge_fee DECIMAL(10,2) UNSIGNED GENERATED ALWAYS AS (kwh * charge_rate) STORED COMMENT '时段电费',
    service_fee DECIMAL(10,2) UNSIGNED GENERATED ALWAYS AS (kwh * service_rate) STORED COMMENT '时段服务费',
    sub_total DECIMAL(10,2) UNSIGNED GENERATED ALWAYS AS (kwh * (charge_rate + service_rate)) STORED COMMENT '时段合计',
    FOREIGN KEY (order_id) REFERENCES charging_order(order_id) ON DELETE CASCADE,
    INDEX idx_order_periods (order_id, period_seq)
) ENGINE=InnoDB COMMENT='充电详单表';


