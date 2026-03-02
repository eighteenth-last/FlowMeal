# FlowMeal 智慧外卖平台

一个完整的外卖订餐系统，包含用户端、商家端、配送端和管理后台。

## 项目简介

FlowMeal 是一款智慧外卖平台，采用微服务架构设计，包含以下核心模块：

- **用户端小程序** - 顾客浏览商家、餐品，下单购物
- **商家端小程序** - 商家管理店铺、餐品、订单
- **配送端小程序** - 外卖员接单、配送
- **管理后台** (Web) - 平台管理员进行系统管理
- **后端服务** (Spring Boot) - 提供 RESTful API 服务

## 技术栈

### 后端

- **框架**: Spring Boot 3.2.3
- **持久层**: MyBatis-Plus 3.5.5
- **数据库**: MySQL 8.0
- **缓存**: Redis
- **认证**: Sa-Token 1.38.0
- **API文档**: Knife4j 4.4.0
- **支付**: 支付宝沙箱支付
- **其他**: WebSocket、Hutool、 Lombok

### 前端

- **小程序框架**: UniApp + Vue 3
- **状态管理**: Pinia
- **构建工具**: Vite 5.2.8

## 项目结构

```
FlowMeal/
├── backend/                 # Spring Boot 后端服务
│   ├── src/main/java/com/flowmeal/
│   │   ├── common/          # 公共模块（配置、异常、结果封装）
│   │   └── module/          # 业务模块
│   │       ├── admin/       # 管理员模块
│   │       ├── auth/        # 认证模块
│   │       ├── cart/        # 购物车模块
│   │       ├── merchant/    # 商家模块
│   │       ├── order/       # 订单模块
│   │       ├── payment/     # 支付模块
│   │       ├── product/     # 餐品模块
│   │       ├── rider/       # 骑手模块
│   │       └── user/        # 用户模块
│   └── src/main/resources/
│       ├── sql/             # 数据库脚本
│       └── application.yml  # 配置文件
│
├── food-delivery-unibest/   # 外卖员端小程序 (UniApp)
├── merchant-unibest/         # 商家端小程序 (UniApp)
├── user-unibest/            # 用户端小程序 (UniApp)
├── web-admin/               # 管理后台 (Vue)
└── web-merchant/           # 商家PC端 (Vue)
```

## 核心功能

### 用户端

- 用户注册/登录
- 浏览商家列表和餐品
- 购物车管理
- 下单支付（支付宝）
- 订单管理
- 收货地址管理

### 商家端

- 商家注册/登录
- 店铺信息管理
- 餐品分类和餐品管理
- 接收新订单
- 订单处理（接单/拒单）
- 店铺统计

### 配送端

- 骑手注册/登录
- 接收配送订单
- 配送状态更新
- 配送记录查看

### 管理后台

- 商家审核管理
- 骑手审核管理
- 订单管理
- 数据统计

## 数据库表

| 表名 | 说明 |
|------|------|
| user | 用户表 |
| merchant | 商家表 |
| rider | 骑手表 |
| admin | 管理员表 |
| product | 餐品表 |
| product_category | 餐品分类表 |
| orders | 订单表 |
| order_item | 订单详情表 |
| cart | 购物车表 |
| address | 收货地址表 |
| delivery_record | 配送记录表 |

## 快速开始

### 环境要求

- JDK 17+
- Maven 3.8+
- MySQL 8.0+
- Redis 6.0+
- Node.js 16+ (前端开发)

### 后端启动

1. 创建数据库 `flowmeal` 并导入 `backend/src/main/resources/sql/flowmeal.sql`

2. 修改配置文件 `application.yml` 中的数据库和Redis连接信息

3. 编译运行：

```bash
cd backend
mvn clean install
mvn spring-boot:run
```

4. 访问 API 文档：http://localhost:8080/api/doc.html

### 前端启动

```bash
# 用户端小程序
cd food-delivery-unibest
npm install
npm run dev:h5

# 商家端小程序
cd merchant-unibest
npm install
npm run dev:h5

# 管理后台
cd web-admin
npm install
npm run dev
```

## 默认账号

| 角色 | 用户名 | 密码 |
|------|--------|------|
| 管理员 | admin | admin123 |
| 商家 | merchant1 | 123456 |
| 用户 | user1 | 123456 |
| 骑手 | rider1 | 123456 |

## API 接口概览

- `/api/auth/*` - 认证相关（登录、注册）
- `/api/user/*` - 用户相关
- `/api/merchant/*` - 商家相关
- `/api/product/*` - 餐品相关
- `/api/cart/*` - 购物车相关
- `/api/order/*` - 订单相关
- `/api/payment/*` - 支付相关
- `/api/rider/*` - 骑手相关
- `/api/admin/*` - 管理员相关

## 订单状态流转

```
WAIT_PAY (待支付)
    ↓
PAID (已支付) → 待商家接单
    ↓
ACCEPTED (已接单) → 待取餐
    ↓
PICKED_UP (已取餐) → 配送中
    ↓
DELIVERING (配送中) → 待送达
    ↓
COMPLETED (已完成)
```

## 许可证

MIT License
