/*
 Navicat Premium Dump SQL

 Source Server         : WSL_MySQL
 Source Server Type    : MySQL
 Source Server Version : 80045 (8.0.45-0ubuntu0.24.04.1)
 Source Host           : 127.0.0.1:3306
 Source Schema         : flowmeal

 Target Server Type    : MySQL
 Target Server Version : 80045 (8.0.45-0ubuntu0.24.04.1)
 File Encoding         : 65001

 Date: 18/03/2026 17:11:18
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for address
-- ----------------------------
DROP TABLE IF EXISTS `address`;
CREATE TABLE `address`  (
  `id` bigint NOT NULL COMMENT '地址ID',
  `user_id` bigint NOT NULL COMMENT '所属用户ID',
  `receiver` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '收货人姓名',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '收货人电话',
  `province` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '省',
  `city` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '市',
  `district` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '区/县',
  `detail` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '详细地址',
  `is_default` tinyint NOT NULL DEFAULT 0 COMMENT '是否默认: 1=是 0=否',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '收货地址表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of address
-- ----------------------------
INSERT INTO `address` VALUES (5001, 2001, '小明', '13900000001', '浙江省', '杭州市', '西湖区', '文一路1号', 1, 0, '2026-03-02 23:27:30', '2026-03-02 23:27:30');
INSERT INTO `address` VALUES (5002, 2002, '小红', '13900000002', '浙江省', '杭州市', '西湖区', '文二路2号', 1, 0, '2026-03-02 23:27:30', '2026-03-02 23:27:30');

-- ----------------------------
-- Table structure for admin
-- ----------------------------
DROP TABLE IF EXISTS `admin`;
CREATE TABLE `admin`  (
  `id` bigint NOT NULL COMMENT '管理员ID',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '登录账号',
  `password` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '密码（BCrypt加密）',
  `real_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '真实姓名',
  `avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '头像URL',
  `role` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'ADMIN' COMMENT '角色: ADMIN / SUPER_ADMIN',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态: 1=正常 0=禁用',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_username`(`username` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '管理员表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of admin
-- ----------------------------
INSERT INTO `admin` VALUES (1, 'admin', '$2a$10$EErwBWI7xVEux9i4gupiFOcAqhFQ/haVykeMxgLh3lM3Hjh81oV2K', '超级管理员', NULL, 'ADMIN', 1, 0, '2026-03-02 20:56:51', '2026-03-02 22:08:53');

-- ----------------------------
-- Table structure for cart
-- ----------------------------
DROP TABLE IF EXISTS `cart`;
CREATE TABLE `cart`  (
  `id` bigint NOT NULL COMMENT '购物车项ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `merchant_id` bigint NOT NULL COMMENT '商家ID',
  `product_id` bigint NOT NULL COMMENT '餐品ID',
  `quantity` int NOT NULL DEFAULT 1 COMMENT '数量',
  `deleted` tinyint NOT NULL DEFAULT 0,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_product`(`user_id` ASC, `product_id` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '购物车表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of cart
-- ----------------------------

-- ----------------------------
-- Table structure for coupon
-- ----------------------------
DROP TABLE IF EXISTS `coupon`;
CREATE TABLE `coupon`  (
  `id` bigint NOT NULL COMMENT '优惠券ID',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '券名称',
  `type` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'FULL_REDUCTION' COMMENT '类型: FULL_REDUCTION/DISCOUNT/NEW_USER',
  `value` decimal(10, 2) NOT NULL COMMENT '面值/折扣值',
  `min_amount` decimal(10, 2) NOT NULL DEFAULT 0.00 COMMENT '使用门槛',
  `total` int NOT NULL DEFAULT 0 COMMENT '总发行量 0=不限',
  `claimed_count` int NOT NULL DEFAULT 0 COMMENT '已领取数量',
  `start_time` datetime NULL DEFAULT NULL COMMENT '有效期开始',
  `end_time` datetime NULL DEFAULT NULL COMMENT '有效期结束',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'ACTIVE' COMMENT '状态: ACTIVE=生效 INACTIVE=停用',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '优惠券表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of coupon
-- ----------------------------
INSERT INTO `coupon` VALUES (2028648222434848769, '1', 'FULL_REDUCTION', 5.00, 20.00, 100, 0, '2026-03-03 00:00:00', '2026-04-06 00:00:00', 'ACTIVE', 0, '2026-03-03 09:46:41', '2026-03-03 09:46:41');

-- ----------------------------
-- Table structure for delivery_record
-- ----------------------------
DROP TABLE IF EXISTS `delivery_record`;
CREATE TABLE `delivery_record`  (
  `id` bigint NOT NULL COMMENT '记录ID',
  `order_id` bigint NOT NULL COMMENT '订单ID',
  `rider_id` bigint NOT NULL COMMENT '外卖员ID',
  `accept_time` datetime NULL DEFAULT NULL COMMENT '接单时间',
  `pickup_time` datetime NULL DEFAULT NULL COMMENT '取餐时间',
  `complete_time` datetime NULL DEFAULT NULL COMMENT '送达时间',
  `duration_min` int NULL DEFAULT NULL COMMENT '配送耗时（分钟）',
  `distance_km` decimal(6, 2) NULL DEFAULT NULL COMMENT '配送距离（公里）',
  `remark` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '备注',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_rider_id`(`rider_id` ASC) USING BTREE,
  INDEX `idx_order_id`(`order_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '配送记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of delivery_record
-- ----------------------------

-- ----------------------------
-- Table structure for material_report
-- ----------------------------
DROP TABLE IF EXISTS `material_report`;
CREATE TABLE `material_report`  (
  `id` bigint NOT NULL COMMENT '记录ID',
  `merchant_id` bigint NOT NULL COMMENT '商家ID',
  `material_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '原料名称',
  `consumed` int NOT NULL COMMENT '消耗量',
  `unit` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'kg' COMMENT '单位',
  `report_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '上报时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_merchant_id`(`merchant_id` ASC) USING BTREE,
  INDEX `idx_report_time`(`report_time` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '原料消耗上报记录表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of material_report
-- ----------------------------
INSERT INTO `material_report` VALUES (2028667205569499138, 1001, '1', 10, 'kg', '2026-03-03 11:02:07', 0, '2026-03-03 11:02:07', '2026-03-03 11:02:07');
INSERT INTO `material_report` VALUES (2028669348674363394, 1001, '12', 12, 'kg', '2026-03-03 11:10:38', 0, '2026-03-03 11:10:38', '2026-03-03 11:10:38');
INSERT INTO `material_report` VALUES (2028669354932264961, 1001, '12', 0, 'kg', '2026-03-03 11:10:40', 0, '2026-03-03 11:10:40', '2026-03-03 11:10:40');
INSERT INTO `material_report` VALUES (2028669372753862657, 1001, '12', 21, 'kg', '2026-03-03 11:10:44', 0, '2026-03-03 11:10:44', '2026-03-03 11:10:44');

-- ----------------------------
-- Table structure for merchant
-- ----------------------------
DROP TABLE IF EXISTS `merchant`;
CREATE TABLE `merchant`  (
  `id` bigint NOT NULL COMMENT '商家ID',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '登录账号',
  `password` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '密码（BCrypt加密）',
  `shop_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '店铺名称',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '联系电话',
  `avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '店铺Logo',
  `description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '店铺简介',
  `notice` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '店铺公告',
  `address` varchar(300) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '店铺地址',
  `latitude` decimal(10, 7) NULL DEFAULT NULL COMMENT '纬度',
  `longitude` decimal(10, 7) NULL DEFAULT NULL COMMENT '经度',
  `business_hours` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '营业时间 如: 09:00-22:00',
  `min_price` decimal(10, 2) NULL DEFAULT NULL COMMENT '起送金额',
  `delivery_fee` decimal(10, 2) NULL DEFAULT NULL COMMENT '配送费',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '营业状态: 1=营业中 0=打烊',
  `audit_status` tinyint NOT NULL DEFAULT 0 COMMENT '审核状态: 0=待审核 1=审核通过 2=审核拒绝',
  `audit_remark` varchar(300) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '审核备注',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_username`(`username` ASC) USING BTREE,
  UNIQUE INDEX `uk_phone`(`phone` ASC) USING BTREE,
  INDEX `idx_audit_status`(`audit_status` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '商家表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of merchant
-- ----------------------------
INSERT INTO `merchant` VALUES (1001, 'merchant1', '$2a$10$BNHJjyo5KRt63deDS/qjgebkJM6cKvoGmhl.7UPoGoL1K41nDIWMm', '黄焖鸡米饭', '13800138001', NULL, '正宗黄焖鸡，料足味美，现点现做', '欢迎光临，满20减5', '浙江省杭州市西湖区文三路100号', 30.2741000, 120.1551000, '09:00-22:00', 15.00, 3.00, 1, 1, NULL, 0, '2026-03-02 22:18:49', '2026-03-02 22:18:49');
INSERT INTO `merchant` VALUES (1002, 'merchant2', '$2a$10$BNHJjyo5KRt63deDS/qjgebkJM6cKvoGmhl.7UPoGoL1K41nDIWMm', '川味冒菜', '13800138002', NULL, '麻辣鲜香', '新品半价', '杭州市西湖区文二路200号', 30.2750000, 120.1500000, '09:00-23:00', 20.00, 4.00, 1, 1, NULL, 0, '2026-03-02 23:26:37', '2026-03-02 23:26:37');
INSERT INTO `merchant` VALUES (1003, 'merchant3', '$2a$10$BNHJjyo5KRt63deDS/qjgebkJM6cKvoGmhl.7UPoGoL1K41nDIWMm', '兰州拉面', '13800138003', NULL, '正宗手工拉面', '买一送一', '杭州市西湖区教工路50号', 30.2800000, 120.1400000, '08:00-21:00', 12.00, 2.00, 1, 1, NULL, 0, '2026-03-02 23:26:37', '2026-03-02 23:26:37');

-- ----------------------------
-- Table structure for order_item
-- ----------------------------
DROP TABLE IF EXISTS `order_item`;
CREATE TABLE `order_item`  (
  `id` bigint NOT NULL COMMENT '订单项ID',
  `order_id` bigint NOT NULL COMMENT '订单ID',
  `product_id` bigint NOT NULL COMMENT '餐品ID',
  `product_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '餐品名称快照',
  `product_image` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '??????',
  `unit_price` decimal(10, 2) NOT NULL COMMENT '单价快照',
  `quantity` int NOT NULL COMMENT '数量',
  `subtotal` decimal(10, 2) NOT NULL COMMENT '小计',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_order_id`(`order_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '订单详情表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of order_item
-- ----------------------------
INSERT INTO `order_item` VALUES (7001, 6001, 4001, '黄焖鸡米饭', 'https://picsum.photos/seed/food1/400/400', 18.00, 1, 18.00, '2026-03-02 23:27:36');
INSERT INTO `order_item` VALUES (2028677405680652290, 2028677405529657346, 4002, '鸡米花', 'https://picsum.photos/seed/food2/400/400', 12.00, 4, 48.00, '2026-03-03 11:42:39');
INSERT INTO `order_item` VALUES (2028677406217523201, 2028677405529657346, 4003, '冰红茶', 'https://picsum.photos/seed/drink1/400/400', 3.00, 1, 3.00, '2026-03-03 11:42:39');
INSERT INTO `order_item` VALUES (2028694808405770242, 2028694807558520833, 4001, '黄焖鸡米饭', 'https://th.bing.com/th/id/R.b0e4c264a4239f271f995e1e28188c7f?rik=Bo%2bMXpXiQG16Vg&riu=http%3a%2f%2fi2.chuimg.com%2fce36e192889d11e6a9a10242ac110002_620w_465h.jpg%3fimageView2%2f2%2fw%2f660%2finterlace%2f1%2fq%2f90&ehk=EkBsgsNoZEEx%2f%2b8jLrwZYUhidpCCLbcGJEheh9ZxBuM%3d&risl=&pid=ImgRaw&r=0', 16.00, 1, 16.00, '2026-03-03 12:51:48');
INSERT INTO `order_item` VALUES (2028722019540217857, 2028722019347279873, 4003, '冰红茶', 'https://picsum.photos/seed/drink1/400/400', 3.00, 1, 3.00, '2026-03-03 14:39:55');
INSERT INTO `order_item` VALUES (2028722110145572865, 2028722110040715266, 4002, '鸡米花', 'https://picsum.photos/seed/food2/400/400', 12.00, 1, 12.00, '2026-03-03 14:40:17');
INSERT INTO `order_item` VALUES (2028722242228400129, 2028722242161291266, 4002, '鸡米花', 'https://picsum.photos/seed/food2/400/400', 12.00, 1, 12.00, '2026-03-03 14:40:49');
INSERT INTO `order_item` VALUES (2028724137571454977, 2028724136665485314, 4002, '鸡米花', 'https://picsum.photos/seed/food2/400/400', 12.00, 1, 12.00, '2026-03-03 14:48:20');
INSERT INTO `order_item` VALUES (2028724324436086786, 2028724324331229185, 4003, '冰红茶', 'https://picsum.photos/seed/drink1/400/400', 0.01, 1, 0.01, '2026-03-03 14:49:05');
INSERT INTO `order_item` VALUES (2028727757222256641, 2028727757092233218, 4003, '冰红茶', 'https://picsum.photos/seed/drink1/400/400', 0.01, 1, 0.01, '2026-03-03 15:02:43');
INSERT INTO `order_item` VALUES (2028727973535096834, 2028727973472182274, 4003, '冰红茶', 'https://picsum.photos/seed/drink1/400/400', 0.01, 1, 0.01, '2026-03-03 15:03:35');
INSERT INTO `order_item` VALUES (2028729279406456834, 2028729279326765057, 4003, '冰红茶', 'https://picsum.photos/seed/drink1/400/400', 0.01, 1, 0.01, '2026-03-03 15:08:46');
INSERT INTO `order_item` VALUES (2028729368875155457, 2028729368795463682, 4001, '黄焖鸡米饭', 'https://th.bing.com/th/id/R.b0e4c264a4239f271f995e1e28188c7f?rik=Bo%2bMXpXiQG16Vg&riu=http%3a%2f%2fi2.chuimg.com%2fce36e192889d11e6a9a10242ac110002_620w_465h.jpg%3fimageView2%2f2%2fw%2f660%2finterlace%2f1%2fq%2f90&ehk=EkBsgsNoZEEx%2f%2b8jLrwZYUhidpCCLbcGJEheh9ZxBuM%3d&risl=&pid=ImgRaw&r=0', 16.00, 1, 16.00, '2026-03-03 15:09:07');
INSERT INTO `order_item` VALUES (2028729473023918081, 2028729472931643394, 4001, '黄焖鸡米饭', 'https://th.bing.com/th/id/R.b0e4c264a4239f271f995e1e28188c7f?rik=Bo%2bMXpXiQG16Vg&riu=http%3a%2f%2fi2.chuimg.com%2fce36e192889d11e6a9a10242ac110002_620w_465h.jpg%3fimageView2%2f2%2fw%2f660%2finterlace%2f1%2fq%2f90&ehk=EkBsgsNoZEEx%2f%2b8jLrwZYUhidpCCLbcGJEheh9ZxBuM%3d&risl=&pid=ImgRaw&r=0', 16.00, 3, 48.00, '2026-03-03 15:09:33');
INSERT INTO `order_item` VALUES (2028729473049083906, 2028729472931643394, 4002, '鸡米花', 'https://picsum.photos/seed/food2/400/400', 12.00, 2, 24.00, '2026-03-03 15:09:33');
INSERT INTO `order_item` VALUES (2028731974775320578, 2028731974494302210, 4003, '冰红茶', 'https://picsum.photos/seed/drink1/400/400', 0.01, 1, 0.01, '2026-03-03 15:19:29');
INSERT INTO `order_item` VALUES (2028732101338402818, 2028732101187407874, 4001, '黄焖鸡米饭', 'https://th.bing.com/th/id/R.b0e4c264a4239f271f995e1e28188c7f?rik=Bo%2bMXpXiQG16Vg&riu=http%3a%2f%2fi2.chuimg.com%2fce36e192889d11e6a9a10242ac110002_620w_465h.jpg%3fimageView2%2f2%2fw%2f660%2finterlace%2f1%2fq%2f90&ehk=EkBsgsNoZEEx%2f%2b8jLrwZYUhidpCCLbcGJEheh9ZxBuM%3d&risl=&pid=ImgRaw&r=0', 16.00, 1, 16.00, '2026-03-03 15:19:59');
INSERT INTO `order_item` VALUES (2028732696443031553, 2028732696380116994, 4001, '黄焖鸡米饭', 'https://th.bing.com/th/id/R.b0e4c264a4239f271f995e1e28188c7f?rik=Bo%2bMXpXiQG16Vg&riu=http%3a%2f%2fi2.chuimg.com%2fce36e192889d11e6a9a10242ac110002_620w_465h.jpg%3fimageView2%2f2%2fw%2f660%2finterlace%2f1%2fq%2f90&ehk=EkBsgsNoZEEx%2f%2b8jLrwZYUhidpCCLbcGJEheh9ZxBuM%3d&risl=&pid=ImgRaw&r=0', 16.00, 1, 16.00, '2026-03-03 15:22:21');
INSERT INTO `order_item` VALUES (2028732971568402434, 2028732971438379010, 4001, '黄焖鸡米饭', 'https://th.bing.com/th/id/R.b0e4c264a4239f271f995e1e28188c7f?rik=Bo%2bMXpXiQG16Vg&riu=http%3a%2f%2fi2.chuimg.com%2fce36e192889d11e6a9a10242ac110002_620w_465h.jpg%3fimageView2%2f2%2fw%2f660%2finterlace%2f1%2fq%2f90&ehk=EkBsgsNoZEEx%2f%2b8jLrwZYUhidpCCLbcGJEheh9ZxBuM%3d&risl=&pid=ImgRaw&r=0', 16.00, 1, 16.00, '2026-03-03 15:23:27');
INSERT INTO `order_item` VALUES (2028733248233082882, 2028733248170168321, 4001, '黄焖鸡米饭', 'https://th.bing.com/th/id/R.b0e4c264a4239f271f995e1e28188c7f?rik=Bo%2bMXpXiQG16Vg&riu=http%3a%2f%2fi2.chuimg.com%2fce36e192889d11e6a9a10242ac110002_620w_465h.jpg%3fimageView2%2f2%2fw%2f660%2finterlace%2f1%2fq%2f90&ehk=EkBsgsNoZEEx%2f%2b8jLrwZYUhidpCCLbcGJEheh9ZxBuM%3d&risl=&pid=ImgRaw&r=0', 16.00, 1, 16.00, '2026-03-03 15:24:33');
INSERT INTO `order_item` VALUES (2028733618959224833, 2028733618892115970, 4001, '黄焖鸡米饭', 'https://th.bing.com/th/id/R.b0e4c264a4239f271f995e1e28188c7f?rik=Bo%2bMXpXiQG16Vg&riu=http%3a%2f%2fi2.chuimg.com%2fce36e192889d11e6a9a10242ac110002_620w_465h.jpg%3fimageView2%2f2%2fw%2f660%2finterlace%2f1%2fq%2f90&ehk=EkBsgsNoZEEx%2f%2b8jLrwZYUhidpCCLbcGJEheh9ZxBuM%3d&risl=&pid=ImgRaw&r=0', 16.00, 1, 16.00, '2026-03-03 15:26:01');
INSERT INTO `order_item` VALUES (2028733689746493441, 2028733689616470018, 4001, '黄焖鸡米饭', 'https://th.bing.com/th/id/R.b0e4c264a4239f271f995e1e28188c7f?rik=Bo%2bMXpXiQG16Vg&riu=http%3a%2f%2fi2.chuimg.com%2fce36e192889d11e6a9a10242ac110002_620w_465h.jpg%3fimageView2%2f2%2fw%2f660%2finterlace%2f1%2fq%2f90&ehk=EkBsgsNoZEEx%2f%2b8jLrwZYUhidpCCLbcGJEheh9ZxBuM%3d&risl=&pid=ImgRaw&r=0', 16.00, 5, 80.00, '2026-03-03 15:26:18');
INSERT INTO `order_item` VALUES (2028733689809408002, 2028733689616470018, 4002, '鸡米花', 'https://picsum.photos/seed/food2/400/400', 12.00, 5, 60.00, '2026-03-03 15:26:18');
INSERT INTO `order_item` VALUES (2028733689859739649, 2028733689616470018, 4003, '冰红茶', 'https://picsum.photos/seed/drink1/400/400', 0.01, 6, 0.06, '2026-03-03 15:26:18');
INSERT INTO `order_item` VALUES (2028733744847065090, 2028733744784150529, 4003, '冰红茶', 'https://picsum.photos/seed/drink1/400/400', 0.01, 1, 0.01, '2026-03-03 15:26:31');
INSERT INTO `order_item` VALUES (2028733984652201985, 2028733984580898817, 4001, '黄焖鸡米饭', 'https://th.bing.com/th/id/R.b0e4c264a4239f271f995e1e28188c7f?rik=Bo%2bMXpXiQG16Vg&riu=http%3a%2f%2fi2.chuimg.com%2fce36e192889d11e6a9a10242ac110002_620w_465h.jpg%3fimageView2%2f2%2fw%2f660%2finterlace%2f1%2fq%2f90&ehk=EkBsgsNoZEEx%2f%2b8jLrwZYUhidpCCLbcGJEheh9ZxBuM%3d&risl=&pid=ImgRaw&r=0', 16.00, 1, 16.00, '2026-03-03 15:27:28');

-- ----------------------------
-- Table structure for orders
-- ----------------------------
DROP TABLE IF EXISTS `orders`;
CREATE TABLE `orders`  (
  `id` bigint NOT NULL COMMENT '订单ID（雪花算法）',
  `order_no` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '订单编号（展示用）',
  `user_id` bigint NOT NULL COMMENT '下单用户ID',
  `merchant_id` bigint NOT NULL COMMENT '商家ID',
  `rider_id` bigint NULL DEFAULT NULL COMMENT '接单外卖员ID',
  `address_id` bigint NOT NULL COMMENT '收货地址ID',
  `address_snapshot` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '地址快照JSON',
  `total_amount` decimal(10, 2) NOT NULL COMMENT '商品总金额',
  `delivery_fee` decimal(10, 2) NOT NULL DEFAULT 0.00 COMMENT '配送费',
  `actual_amount` decimal(10, 2) NOT NULL COMMENT '实付金额',
  `remark` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '订单备注',
  `status` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'WAIT_PAY' COMMENT '订单状态',
  `cancel_reason` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '取消原因',
  `reject_reason` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '拒单原因',
  `pay_time` datetime NULL DEFAULT NULL COMMENT '支付时间',
  `accept_time` datetime NULL DEFAULT NULL COMMENT '商家接单时间',
  `pick_up_time` datetime NULL DEFAULT NULL COMMENT '骑手接单时间',
  `deliver_time` datetime NULL DEFAULT NULL COMMENT '开始配送时间',
  `complete_time` datetime NULL DEFAULT NULL COMMENT '完成时间',
  `payment_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '支付方式: ALIPAY / WECHAT',
  `trade_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '第三方支付流水号',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_order_no`(`order_no` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_merchant_id`(`merchant_id` ASC) USING BTREE,
  INDEX `idx_rider_id`(`rider_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_created_at`(`created_at` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '订单表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of orders
-- ----------------------------
INSERT INTO `orders` VALUES (6001, 'FM202603020001', 2001, 1001, NULL, 5001, '{\"receiver\":\"小明\",\"phone\":\"13900000001\",\"address\":\"文一路1号\"}', 18.00, 3.00, 21.00, NULL, 'COMPLETED', NULL, NULL, '2026-03-02 23:27:36', NULL, NULL, NULL, NULL, 'ALIPAY', NULL, 0, '2026-03-02 23:27:36', '2026-03-02 23:27:36');
INSERT INTO `orders` VALUES (2028677405529657346, 'POS20260303114239091', 0, 1001, NULL, 0, '{\"type\":\"线下收款\"}', 51.00, 0.00, 51.00, 'POS线下订单', 'COMPLETED', NULL, NULL, '2026-03-03 11:42:39', NULL, NULL, NULL, '2026-03-03 11:42:39', 'CASH', NULL, 0, '2026-03-03 11:42:39', '2026-03-03 11:42:39');
INSERT INTO `orders` VALUES (2028694215230541825, 'POS20260303124926119', 0, 1001, NULL, 0, '{\"type\":\"线下收款\"}', 16.00, 0.00, 16.00, 'POS线下订单', 'COMPLETED', NULL, NULL, '2026-03-03 12:49:27', NULL, NULL, NULL, '2026-03-03 12:49:27', 'WECHAT', NULL, 0, '2026-03-03 12:49:27', '2026-03-03 12:49:27');
INSERT INTO `orders` VALUES (2028694224575451138, 'POS20260303124929497', 0, 1001, NULL, 0, '{\"type\":\"线下收款\"}', 16.00, 0.00, 16.00, 'POS线下订单', 'COMPLETED', NULL, NULL, '2026-03-03 12:49:29', NULL, NULL, NULL, '2026-03-03 12:49:29', 'ALIPAY', NULL, 0, '2026-03-03 12:49:29', '2026-03-03 12:49:29');
INSERT INTO `orders` VALUES (2028694247388270593, 'POS20260303124934037', 0, 1001, NULL, 0, '{\"type\":\"线下收款\"}', 16.00, 0.00, 16.00, 'POS线下订单', 'COMPLETED', NULL, NULL, '2026-03-03 12:49:35', NULL, NULL, NULL, '2026-03-03 12:49:35', 'ALIPAY', NULL, 0, '2026-03-03 12:49:35', '2026-03-03 12:49:35');
INSERT INTO `orders` VALUES (2028694279772491777, 'POS20260303124942512', 0, 1001, NULL, 0, '{\"type\":\"线下收款\"}', 16.00, 0.00, 16.00, 'POS线下订单', 'COMPLETED', NULL, NULL, '2026-03-03 12:49:42', NULL, NULL, NULL, '2026-03-03 12:49:42', 'ALIPAY', NULL, 0, '2026-03-03 12:49:42', '2026-03-03 12:49:42');
INSERT INTO `orders` VALUES (2028694807558520833, 'POS20260303125148321', 0, 1001, NULL, 0, '{\"type\":\"线下收款\"}', 16.00, 0.00, 16.00, 'POS线下订单', 'COMPLETED', NULL, NULL, '2026-03-03 12:51:48', NULL, NULL, NULL, '2026-03-03 12:51:48', 'ALIPAY', NULL, 0, '2026-03-03 12:51:48', '2026-03-03 12:51:48');
INSERT INTO `orders` VALUES (2028722019347279873, 'POS20260303143955280', 0, 1001, NULL, 0, '{\"type\":\"线下收款\"}', 3.00, 0.00, 3.00, 'POS线下订单', 'WAIT_PAY', NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'ALIPAY', NULL, 0, '2026-03-03 14:39:56', '2026-03-03 14:39:56');
INSERT INTO `orders` VALUES (2028722110040715266, 'POS20260303144017347', 0, 1001, NULL, 0, '{\"type\":\"线下收款\"}', 12.00, 0.00, 12.00, 'POS线下订单', 'WAIT_PAY', NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'ALIPAY', NULL, 0, '2026-03-03 14:40:18', '2026-03-03 14:40:18');
INSERT INTO `orders` VALUES (2028722242161291266, 'POS20260303144049901', 0, 1001, NULL, 0, '{\"type\":\"线下收款\"}', 12.00, 0.00, 12.00, 'POS线下订单', 'WAIT_PAY', NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'ALIPAY', NULL, 0, '2026-03-03 14:40:49', '2026-03-03 14:40:49');
INSERT INTO `orders` VALUES (2028724136665485314, 'POS20260303144820439', 0, 1001, NULL, 0, '{\"type\":\"线下收款\"}', 12.00, 0.00, 12.00, 'POS线下订单', 'WAIT_PAY', NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'ALIPAY', NULL, 0, '2026-03-03 14:48:21', '2026-03-03 14:48:21');
INSERT INTO `orders` VALUES (2028724324331229185, 'POS20260303144905590', 0, 1001, NULL, 0, '{\"type\":\"线下收款\"}', 0.01, 0.00, 0.01, 'POS线下订单', 'WAIT_MERCHANT_CONFIRM', NULL, NULL, '2026-03-03 14:49:28', NULL, NULL, NULL, NULL, 'ALIPAY', '2026030323001446771449755561', 0, '2026-03-03 14:49:05', '2026-03-03 14:49:28');
INSERT INTO `orders` VALUES (2028727757092233218, 'POS20260303150243678', 0, 1001, NULL, 0, '{\"type\":\"线下收款\"}', 0.01, 0.00, 0.01, 'POS线下订单', 'WAIT_MERCHANT_CONFIRM', NULL, NULL, '2026-03-03 15:03:09', NULL, NULL, NULL, NULL, 'ALIPAY', '2026030323001446771449718238', 0, '2026-03-03 15:02:44', '2026-03-03 15:03:08');
INSERT INTO `orders` VALUES (2028727973472182274, 'POS20260303150335329', 0, 1001, NULL, 0, '{\"type\":\"线下收款\"}', 0.01, 0.00, 0.01, 'POS线下订单', 'WAIT_MERCHANT_CONFIRM', NULL, NULL, '2026-03-03 15:08:00', NULL, NULL, NULL, NULL, 'WECHAT', NULL, 0, '2026-03-03 15:03:35', '2026-03-03 15:08:00');
INSERT INTO `orders` VALUES (2028729279326765057, 'POS20260303150846693', 0, 1001, NULL, 0, '{\"type\":\"线下收款\"}', 0.01, 0.00, 0.01, 'POS线下订单', 'WAIT_MERCHANT_CONFIRM', NULL, NULL, '2026-03-03 15:09:01', NULL, NULL, NULL, NULL, 'WECHAT', '4200003049202603037286535624', 0, '2026-03-03 15:08:47', '2026-03-03 15:09:00');
INSERT INTO `orders` VALUES (2028729368795463682, 'POS20260303150908828', 0, 1001, NULL, 0, '{\"type\":\"线下收款\"}', 16.00, 0.00, 16.00, 'POS线下订单', 'COMPLETED', NULL, NULL, '2026-03-03 15:09:08', NULL, NULL, NULL, '2026-03-03 15:09:08', 'CASH', NULL, 0, '2026-03-03 15:09:08', '2026-03-03 15:09:08');
INSERT INTO `orders` VALUES (2028729472931643394, 'POS20260303150932510', 0, 1001, NULL, 0, '{\"type\":\"线下收款\"}', 72.00, 0.00, 72.00, 'POS线下订单', 'COMPLETED', NULL, NULL, '2026-03-03 15:09:33', NULL, NULL, NULL, '2026-03-03 15:09:33', 'CASH', NULL, 0, '2026-03-03 15:09:33', '2026-03-03 15:09:33');
INSERT INTO `orders` VALUES (2028731974494302210, 'POS20260303151929486', 0, 1001, NULL, 0, '{\"type\":\"线下收款\"}', 0.01, 0.00, 0.01, 'POS线下订单', 'COMPLETED', NULL, NULL, '2026-03-03 15:19:29', NULL, NULL, NULL, '2026-03-03 15:19:29', 'CASH', NULL, 0, '2026-03-03 15:19:29', '2026-03-03 15:19:29');
INSERT INTO `orders` VALUES (2028732101187407874, 'POS20260303151959838', 0, 1001, NULL, 0, '{\"type\":\"线下收款\"}', 16.00, 0.00, 16.00, 'POS线下订单', 'COMPLETED', NULL, NULL, '2026-03-03 15:20:00', NULL, NULL, NULL, '2026-03-03 15:20:00', 'CASH', NULL, 0, '2026-03-03 15:20:00', '2026-03-03 15:20:00');
INSERT INTO `orders` VALUES (2028732696380116994, 'POS20260303152221981', 0, 1001, NULL, 0, '{\"type\":\"线下收款\"}', 16.00, 0.00, 16.00, 'POS线下订单', 'COMPLETED', NULL, NULL, '2026-03-03 15:22:22', NULL, NULL, NULL, '2026-03-03 15:22:22', 'CASH', NULL, 0, '2026-03-03 15:22:22', '2026-03-03 15:22:22');
INSERT INTO `orders` VALUES (2028732971438379010, 'POS20260303152327311', 0, 1001, NULL, 0, '{\"type\":\"线下收款\"}', 16.00, 0.00, 16.00, 'POS线下订单', 'COMPLETED', NULL, NULL, '2026-03-03 15:23:27', NULL, NULL, NULL, '2026-03-03 15:23:27', 'CASH', NULL, 0, '2026-03-03 15:23:27', '2026-03-03 15:23:27');
INSERT INTO `orders` VALUES (2028733248170168321, 'POS20260303152433250', 0, 1001, NULL, 0, '{\"type\":\"线下收款\"}', 16.00, 0.00, 16.00, 'POS线下订单', 'COMPLETED', NULL, NULL, '2026-03-03 15:24:33', NULL, NULL, NULL, '2026-03-03 15:24:33', 'CASH', NULL, 0, '2026-03-03 15:24:33', '2026-03-03 15:24:33');
INSERT INTO `orders` VALUES (2028733618892115970, 'POS20260303152601411', 0, 1001, NULL, 0, '{\"type\":\"线下收款\"}', 16.00, 0.00, 16.00, 'POS线下订单', 'COMPLETED', NULL, NULL, '2026-03-03 15:26:01', NULL, NULL, NULL, '2026-03-03 15:26:01', 'CASH', NULL, 0, '2026-03-03 15:26:01', '2026-03-03 15:26:01');
INSERT INTO `orders` VALUES (2028733689616470018, 'POS20260303152618047', 0, 1001, NULL, 0, '{\"type\":\"线下收款\"}', 140.06, 0.00, 140.06, 'POS线下订单', 'COMPLETED', NULL, NULL, '2026-03-03 15:26:18', NULL, NULL, NULL, '2026-03-03 15:26:18', 'CASH', NULL, 0, '2026-03-03 15:26:18', '2026-03-03 15:26:18');
INSERT INTO `orders` VALUES (2028733744784150529, 'POS20260303152631790', 0, 1001, NULL, 0, '{\"type\":\"线下收款\"}', 0.01, 0.00, 0.01, 'POS线下订单', 'WAIT_MERCHANT_CONFIRM', NULL, NULL, '2026-03-03 15:26:53', NULL, NULL, NULL, NULL, 'ALIPAY', '2026030323001446771453175718', 0, '2026-03-03 15:26:31', '2026-03-03 15:26:52');
INSERT INTO `orders` VALUES (2028733984580898817, 'POS20260303152728945', 0, 1001, NULL, 0, '{\"type\":\"线下收款\"}', 16.00, 0.00, 16.00, 'POS线下订单', 'COMPLETED', NULL, NULL, '2026-03-03 15:27:29', NULL, NULL, NULL, '2026-03-03 15:27:29', 'CASH', NULL, 0, '2026-03-03 15:27:29', '2026-03-03 15:27:29');

-- ----------------------------
-- Table structure for procurement_order
-- ----------------------------
DROP TABLE IF EXISTS `procurement_order`;
CREATE TABLE `procurement_order`  (
  `id` bigint NOT NULL COMMENT '采购单ID',
  `supplier` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '供应商',
  `material_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '原料名称',
  `quantity` int NOT NULL COMMENT '采购数量',
  `unit` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'kg' COMMENT '单位',
  `remark` varchar(300) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '备注',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'PENDING' COMMENT '状态: PENDING=待发货 SHIPPED=已发货 DELIVERED=已到货',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '采购单表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of procurement_order
-- ----------------------------
INSERT INTO `procurement_order` VALUES (2028650288595124226, '绿源食材', '1', 1, 'kg', '1', 'PENDING', 0, '2026-03-03 09:54:54', '2026-03-03 09:54:54');
INSERT INTO `procurement_order` VALUES (2028670358289473538, '1', '12', 33, 'kg', '根据近期消耗上报，累计消耗 33kg，涉及 1 家商家', 'PENDING', 0, '2026-03-03 11:14:39', '2026-03-03 11:14:39');

-- ----------------------------
-- Table structure for product
-- ----------------------------
DROP TABLE IF EXISTS `product`;
CREATE TABLE `product`  (
  `id` bigint NOT NULL COMMENT '餐品ID',
  `merchant_id` bigint NOT NULL COMMENT '所属商家ID',
  `category_id` bigint NULL DEFAULT NULL COMMENT '所属分类ID',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '餐品名称',
  `description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '餐品描述',
  `image` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '餐品图片URL',
  `price` decimal(10, 2) NOT NULL COMMENT '原价',
  `discount_price` decimal(10, 2) NULL DEFAULT NULL COMMENT '折扣价（NULL=无折扣）',
  `stock` int NOT NULL DEFAULT 999 COMMENT '库存',
  `sales` int NOT NULL DEFAULT 0 COMMENT '销量',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态: 1=上架 0=下架',
  `sort` int NOT NULL DEFAULT 0 COMMENT '排序',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_merchant_id`(`merchant_id` ASC) USING BTREE,
  INDEX `idx_category_id`(`category_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '餐品表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of product
-- ----------------------------
INSERT INTO `product` VALUES (4001, 1001, 3001, '黄焖鸡米饭', '招牌推荐', 'https://th.bing.com/th/id/R.b0e4c264a4239f271f995e1e28188c7f?rik=Bo%2bMXpXiQG16Vg&riu=http%3a%2f%2fi2.chuimg.com%2fce36e192889d11e6a9a10242ac110002_620w_465h.jpg%3fimageView2%2f2%2fw%2f660%2finterlace%2f1%2fq%2f90&ehk=EkBsgsNoZEEx%2f%2b8jLrwZYUhidpCCLbcGJEheh9ZxBuM%3d&risl=&pid=ImgRaw&r=0', 18.00, 16.00, 200, 50, 1, 0, 0, '2026-03-02 23:26:48', '2026-03-03 12:48:57');
INSERT INTO `product` VALUES (4002, 1001, 3002, '鸡米花', '香脆可口', 'https://picsum.photos/seed/food2/400/400', 12.00, NULL, 150, 30, 1, 0, 0, '2026-03-02 23:26:48', '2026-03-02 23:26:48');
INSERT INTO `product` VALUES (4003, 1001, 3003, '冰红茶', '解腻神器', 'https://picsum.photos/seed/drink1/400/400', 0.01, NULL, 500, 120, 1, 0, 0, '2026-03-02 23:26:48', '2026-03-03 14:48:59');
INSERT INTO `product` VALUES (4010, 1002, 3011, '牛肉冒菜', '川味经典', 'https://picsum.photos/seed/spicy1/400/400', 25.00, 22.00, 100, 60, 1, 0, 0, '2026-03-02 23:26:48', '2026-03-02 23:26:48');
INSERT INTO `product` VALUES (4020, 1003, 3021, '牛肉拉面', '手工拉制', 'https://picsum.photos/seed/noodle1/400/400', 15.00, NULL, 300, 80, 1, 0, 0, '2026-03-02 23:26:48', '2026-03-02 23:26:48');

-- ----------------------------
-- Table structure for product_category
-- ----------------------------
DROP TABLE IF EXISTS `product_category`;
CREATE TABLE `product_category`  (
  `id` bigint NOT NULL COMMENT '分类ID',
  `merchant_id` bigint NOT NULL COMMENT '所属商家ID',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '分类名称',
  `sort` int NOT NULL DEFAULT 0 COMMENT '排序（越小越靠前）',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_merchant_id`(`merchant_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '餐品分类表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of product_category
-- ----------------------------
INSERT INTO `product_category` VALUES (3001, 1001, '主食', 1, 0, '2026-03-02 23:26:41', '2026-03-02 23:26:41');
INSERT INTO `product_category` VALUES (3002, 1001, '小吃', 2, 0, '2026-03-02 23:26:41', '2026-03-02 23:26:41');
INSERT INTO `product_category` VALUES (3003, 1001, '饮品', 3, 0, '2026-03-02 23:26:41', '2026-03-02 23:26:41');
INSERT INTO `product_category` VALUES (3011, 1002, '冒菜', 1, 0, '2026-03-02 23:26:41', '2026-03-02 23:26:41');
INSERT INTO `product_category` VALUES (3012, 1002, '饮料', 2, 0, '2026-03-02 23:26:41', '2026-03-02 23:26:41');
INSERT INTO `product_category` VALUES (3021, 1003, '面食', 1, 0, '2026-03-02 23:26:41', '2026-03-02 23:26:41');
INSERT INTO `product_category` VALUES (3022, 1003, '凉菜', 2, 0, '2026-03-02 23:26:41', '2026-03-02 23:26:41');

-- ----------------------------
-- Table structure for rider
-- ----------------------------
DROP TABLE IF EXISTS `rider`;
CREATE TABLE `rider`  (
  `id` bigint NOT NULL COMMENT '外卖员ID',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '登录账号',
  `password` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '密码（BCrypt加密）',
  `real_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '真实姓名',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '手机号',
  `avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '头像URL',
  `id_card` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '身份证号',
  `online_status` tinyint NOT NULL DEFAULT 0 COMMENT '在线状态: 1=在线 0=离线',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '账号状态: 1=正常 0=封禁',
  `audit_status` tinyint NOT NULL DEFAULT 0 COMMENT '审核状态: 0=待审核 1=通过 2=拒绝',
  `total_orders` int NOT NULL DEFAULT 0 COMMENT '总配送单数',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_username`(`username` ASC) USING BTREE,
  UNIQUE INDEX `uk_phone`(`phone` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '外卖员表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of rider
-- ----------------------------
INSERT INTO `rider` VALUES (2028736630553407490, 'test', '$2a$10$7uURjK/l/loOKFQmj/215.9UTZ2YLXBZVQGNUxhMRDzkGJwhNDPr.', '测试', '13245455656', NULL, '1', 1, 1, 1, 0, 0, '2026-03-03 15:37:59', '2026-03-04 14:54:55');

-- ----------------------------
-- Table structure for supplier
-- ----------------------------
DROP TABLE IF EXISTS `supplier`;
CREATE TABLE `supplier`  (
  `id` bigint NOT NULL COMMENT '供应商ID',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '供应商名称',
  `contact` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '联系人',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '联系电话',
  `address` varchar(300) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '供应商地址',
  `category` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '供应类别（如：粮油/蔬菜/肉类）',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态: 1=合作中 0=停用',
  `remark` varchar(300) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '备注',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '供应商表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of supplier
-- ----------------------------
INSERT INTO `supplier` VALUES (8001, '绿源食材', '张经理', '13900001001', '杭州市余杭区仓前路88号', '蔬菜', 1, NULL, 0, '2026-03-03 00:00:00', '2026-03-03 00:00:00');
INSERT INTO `supplier` VALUES (8002, '鲜味供应商', '李总', '13900001002', '杭州市萧山区金惠路100号', '肉类', 1, NULL, 0, '2026-03-03 00:00:00', '2026-03-03 00:00:00');
INSERT INTO `supplier` VALUES (8003, '金鸿粮油', '王老板', '13900001003', '杭州市滨江区江南大道200号', '粮油', 1, NULL, 0, '2026-03-03 00:00:00', '2026-03-03 00:00:00');
INSERT INTO `supplier` VALUES (2028651365818224641, '1', '1', '1', '1', '粮油', 1, '1', 0, '2026-03-03 09:59:11', '2026-03-03 09:59:11');

-- ----------------------------
-- Table structure for user
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user`  (
  `id` bigint NOT NULL COMMENT '用户ID（雪花算法）',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户名',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '手机号',
  `password` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '密码（BCrypt加密）',
  `avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '头像URL',
  `nickname` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '昵称',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态: 1=正常 0=冻结',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0=未删 1=已删',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_phone`(`phone` ASC) USING BTREE,
  UNIQUE INDEX `uk_username`(`username` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user
-- ----------------------------
INSERT INTO `user` VALUES (2001, 'user1', '13900000001', '$2a$10$EErwBWI7xVEux9i4gupiFOcAqhFQ/haVykeMxgLh3lM3Hjh81oV2K', NULL, '小明', 1, 0, '2026-03-02 23:26:32', '2026-03-18 15:45:22');
INSERT INTO `user` VALUES (2002, 'user2', '13900000002', '$2a$10$EErwBWI7xVEux9i4gupiFOcAqhFQ/haVykeMxgLh3lM3Hjh81oV2K', NULL, '小红', 1, 0, '2026-03-02 23:26:32', '2026-03-02 23:26:32');
INSERT INTO `user` VALUES (2003, 'user3', '13900000003', '$2a$10$EErwBWI7xVEux9i4gupiFOcAqhFQ/haVykeMxgLh3lM3Hjh81oV2K', NULL, '小刚', 1, 0, '2026-03-02 23:26:32', '2026-03-02 23:26:32');
INSERT INTO `user` VALUES (2004, 'user4', '13900000004', '$2a$10$EErwBWI7xVEux9i4gupiFOcAqhFQ/haVykeMxgLh3lM3Hjh81oV2K', NULL, '小丽', 1, 0, '2026-03-02 23:26:32', '2026-03-02 23:26:32');
INSERT INTO `user` VALUES (2005, 'user5', '13900000005', '$2a$10$EErwBWI7xVEux9i4gupiFOcAqhFQ/haVykeMxgLh3lM3Hjh81oV2K', NULL, '小张', 1, 0, '2026-03-02 23:26:32', '2026-03-02 23:26:32');

SET FOREIGN_KEY_CHECKS = 1;
