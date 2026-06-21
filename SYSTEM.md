# CampusHub 校园互助平台 — 系统文档

## 项目概述

CampusHub 是一个前后端分离的校园互助服务平台。

- **前端：** Vue 3 + TypeScript + Vite + UnoCSS
- **后端：** Spring Boot 3.2 + Java 17 + MyBatis-Plus + MySQL + Redis
- **部署：** Nginx（静态资源）+ Systemd（后端服务）

**源码仓库：** <https://github.com/Rosolio/CampusHub>
**线上地址：** <https://campushub.icu>

---

## 目录

1. [技术栈](#1-技术栈)
2. [项目结构](#2-项目结构)
3. [快速启动](#3-快速启动)
4. [数据库设计](#4-数据库设计)
5. [API 文档](#5-api-文档)
6. [前端架构](#6-前端架构)
7. [部署](#7-部署)
8. [CI/CD](#8-cicd)
9. [性能优化](#9-性能优化)

---

## 1. 技术栈

### 后端

| 组件 | 技术 | 版本 |
|------|------|------|
| 框架 | Spring Boot | 3.2.4 |
| 语言 | Java | 17 |
| ORM | MyBatis-Plus | 3.5.5 |
| 数据库 | MySQL | 8.0+ |
| 缓存 | Redis | 6.0+ |
| 认证 | JWT (jjwt) | 0.12.5 |
| 构建 | Maven | 3.9+ |
| 代码质量 | Checkstyle | 10.12+ |
| 覆盖率 | JaCoCo | 0.8.12 |

### 前端

| 组件 | 技术 | 版本 |
|------|------|------|
| 框架 | Vue | 3.5+ |
| 路由 | Vue Router | 4.x |
| 构建 | Vite + Rolldown | 6.x |
| 语言 | TypeScript | 5.x |
| 样式 | UnoCSS | 0.65+ |
| HTTP | Axios | 1.x |
| 图标 | Material Symbols (Iconify) | — |

---

## 2. 项目结构

```
/
├── backend/                        # Spring Boot 后端
│   ├── src/main/java/com/campushub/
│   │   ├── CampusHubApplication.java    # 启动入口
│   │   ├── config/                      # 配置（Security/JWT/WebSocket/Redis）
│   │   ├── controller/                  # REST 控制器（16 个）
│   │   ├── dto/                         # 数据传输对象
│   │   ├── entity/                      # 数据实体
│   │   ├── mapper/                      # MyBatis Mapper 接口
│   │   ├── service/                     # 业务逻辑层
│   │   └── util/                        # 工具类
│   ├── src/main/resources/
│   │   ├── mapper/                      # MyBatis XML（15 个）
│   │   ├── schema.sql                   # 数据库建表脚本
│   │   ├── data.sql                     # 种子数据
│   │   └── application.yml             # 后端配置
│   ├── sql/                             # 数据库迁移脚本
│   └── pom.xml
│
├── frontend/                       # Vue 3 前端
│   ├── src/
│   │   ├── pages/                       # 页面组件
│   │   │   ├── admin/                   # 后台管理页面
│   │   │   └── settings/                # 设置页面
│   │   ├── components/                  # 通用组件
│   │   ├── composables/                 # 组合式函数
│   │   ├── router/                      # 路由配置
│   │   ├── services/                    # API 服务
│   │   ├── utils/                       # 工具函数
│   │   └── constants/                   # 常量
│   ├── uno.config.ts                   # UnoCSS 设计令牌
│   └── package.json
│
├── docs/                           # 课程交付文档
│   ├── p0/                          # Phase 0 文档
│   ├── p1/                          # Phase 1 文档
│   └── P4/                          # Phase 4 文档
│
├── .github/workflows/              # GitHub Actions CI
├── .gitlab/                         # GitLab CI 配置
├── deploy.sh                        # 部署脚本
├── start.sh                         # 本地启动脚本
├── USER_GUIDE.md                    # 用户手册
└── SYSTEM.md                        # 本文档
```

---

## 3. 快速启动

### 前置依赖

- Node.js 20+
- Java 17+
- Maven 3.9+
- MySQL 8+
- Redis 6+

### 本地开发

```bash
# 1. 配置数据库
mysql -u root -p
> CREATE DATABASE campushub CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

# 2. 启动后端
cd backend
mvn spring-boot:run

# 3. 启动前端（新终端）
cd frontend
npm install
npm run dev
```

### 快捷脚本

```bash
./start.sh    # 同时启动前后端
./stop.sh     # 停止
./restart.sh  # 重启
```

### 测试账号

| 角色 | 学号 | 密码 |
|------|------|------|
| 普通用户 | `20230001` | `123456` |
| 管理员 | `admin` | 需在 data.sql 中确认 |

---

## 4. 数据库设计

### ER 概览

核心实体关系：

```
users (1) ──→ tasks (N)          用户发布任务
users (1) ──→ task_participants   用户参与任务
tasks (1) ──→ task_comments       任务评论
tasks (1) ──→ task_reviews        任务评价
users (1) ──→ messages            私信
users (1) ──→ notifications       通知
users (1) ──→ feedback            反馈
users (1) ──→ user_verifications  校园认证
tasks (1) ──→ task_favorites      收藏
```

### 核心表结构

| 表名 | 说明 | 关键字段 |
|------|------|---------|
| `users` | 用户 | student_id, name, email, password, avatar_url, role, status, score, points |
| `tasks` | 任务/话题 | requester_id, title, description, category, task_mode, status, expires_at |
| `task_participants` | 参与者 | task_id, participant_id, role, status |
| `task_comments` | 评论 | task_id, author_id, parent_id, content |
| `task_reviews` | 评价 | task_id, reviewer_id, reviewee_id, rating |
| `messages` | 私信 | sender_id, receiver_id, task_id, content, status |
| `notifications` | 通知 | user_id, type, title, content, reference_type, is_read |
| `feedback` | 反馈 | user_id, type, title, content, status, priority |
| `user_verifications` | 认证 | user_id, real_name, student_id, image_urls, status |
| `task_favorites` | 收藏 | user_id, task_id |

### 任务状态机

```
[open] → [accepted] → [completion_pending] → [completed]
   ↓          ↓                                  ↓
 canceled   canceled                        → review flow
```

- **open:** 待接单
- **accepted:** 已被接单
- **completion_pending:** 一方已确认完成，等待另一方
- **completed:** 双方确认完成
- **canceled:** 已取消/已过期

### 话题与任务区别

| 维度 | 任务 (task) | 话题帖 (topic) |
|------|------------|---------------|
| 分类 | 跑腿代办、学习辅导 | 二手闲置、恋爱交友、打听求助、兼职招聘 |
| 接单流程 | ✅ 有人接单 → 确认完成 | ❌ 无接单，直接评论互动 |
| 评价 | ✅ 双向评价 | ❌ 无评价 |
| 截止时间 | 必须设置 | 可选，可设为长期有效 |
| 积分 | 评价获得 | 发布+5，评论+5 |

---

## 5. API 文档

### 认证

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| POST | `/auth/register` | 注册 | ❌ |
| POST | `/auth/login` | 登录，返回 token + user | ❌ |
| POST | `/auth/refresh` | 刷新令牌 | ❌ |

### 用户

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/users/me` | 获取当前用户信息 |
| PUT | `/users/me` | 修改个人资料 |
| GET | `/users/me/points/records` | 积分明细 |
| GET | `/users/leaderboard?limit=20` | 积分排行榜 |
| GET | `/users/settings` | 获取用户设置 |
| PUT | `/users/settings` | 修改用户设置 |

### 任务

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/tasks` | 任务列表（支持筛选/分页/推荐） |
| GET | `/tasks/{id}` | 任务详情 |
| POST | `/tasks` | 创建任务 |
| POST | `/tasks/{id}/accept` | 接单 |
| POST | `/tasks/{id}/unaccept` | 取消接单 |
| POST | `/tasks/{id}/complete` | 确认完成 |
| POST | `/tasks/{id}/cancel` | 取消任务 |
| POST | `/tasks/{id}/like` | 点赞 |
| DELETE | `/tasks/{id}/like` | 取消点赞 |
| POST | `/tasks/{id}/favorite` | 收藏 |
| DELETE | `/tasks/{id}/favorite` | 取消收藏 |
| GET | `/tasks/favorites` | 收藏列表 |
| GET | `/tasks/my` | 我发布的任务 |
| GET | `/tasks/my/accepted` | 我接单的任务 |
| DELETE | `/tasks/{id}` | 删除任务 |

### 评论

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/tasks/{taskId}/comments` | 获取评论列表 |
| POST | `/tasks/{taskId}/comments` | 发表评论 |
| DELETE | `/tasks/{taskId}/comments/{commentId}` | 删除评论 |
| POST | `/tasks/{taskId}/comments/{commentId}/like` | 评论点赞 |

### 评价

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/tasks/{taskId}/reviews` | 获取评价 |
| POST | `/tasks/{taskId}/reviews` | 提交评价 |

### 消息

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/messages` | 获取消息列表 |
| POST | `/messages` | 发送消息 |
| PUT | `/messages/{id}/read` | 标记已读 |
| PUT | `/messages/read` | 批量标记已读 |
| GET | `/messages/unread/count` | 未读消息数 |
| GET | `/messages/stream` | SSE 实时推送（预留） |

### 通知

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/notifications` | 通知列表 |
| GET | `/notifications/unread-count` | 未读通知数 |
| PUT | `/notifications/{id}/read` | 标记已读 |
| PUT | `/notifications/read-all` | 全部已读 |

### 搜索

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/search?q=&mode=` | 全局搜索 |

### 仪表盘

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/dashboard` | 个人主页聚合数据 |

### 反馈

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/feedback` | 提交反馈 |
| GET | `/feedback/my` | 我的反馈 |
| DELETE | `/feedback/{id}` | 撤回反馈 |

### 认证

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/users/me/verification` | 获取认证信息 |
| POST | `/users/me/verification` | 提交认证 |

### 文件

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/files/upload` | 上传文件 |

### 管理后台

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/admin/dashboard` | 后台概览 |
| GET | `/admin/users` | 用户管理 |
| PUT | `/admin/users/{id}/status` | 禁用/启用用户 |
| GET | `/admin/tasks` | 任务审核列表 |
| PUT | `/admin/tasks/{id}/review` | 审核任务 |
| GET | `/admin/announcements` | 公告列表 |
| POST | `/admin/announcements` | 发布公告 |
| GET | `/admin/feedback` | 反馈管理 |
| PUT | `/admin/feedback/{id}` | 处理反馈 |
| GET | `/admin/verifications` | 认证审核列表 |
| PUT | `/admin/verifications/{id}/review` | 审核认证 |

---

## 6. 前端架构

### 路由结构

```
/                  → 重定向到 /home 或 /auth
/home              → 社区首页（Discovery.vue）
/detail/:id        → 任务/话题详情
/detail/:id/review → 任务评价
/profile           → 个人中心
/messages          → 消息
/publish           → 发布
/notifications     → 通知
/search            → 搜索
/feedback          → 反馈
/verification      → 校园认证
/settings/*        → 设置各页面
/admin/*           → 管理后台各页面
/auth              → 登录/注册
```

### 关键组件

| 组件 | 用途 |
|------|------|
| `AppTopNav.vue` | 顶部导航栏 |
| `AppBottomNav.vue` | 移动端底部导航 |
| `PageBackHeader.vue` | 返回按钮 |
| `EmojiPicker.vue` | Emoji 选择器 |
| `ImageUploader.vue` | 图片上传组件 |
| `SettingsToggleItem.vue` | 开关设置项 |

### 状态管理

当前版本不使用 Pinia/Vuex，状态通过以下方式管理：

- **用户认证状态：** `utils/auth.ts` — 基于 localStorage，通过 `CustomEvent` 跨组件通信
- **本地偏好：** `composables/usePreferences.ts` — 主题、语言
- **组件内部状态：** 各页面通过 `ref` / `reactive` 自行管理

---

## 7. 部署

### 生产环境架构

```
用户 → HTTPS → Nginx (443)
                  ├─ /api/* → proxy_pass → Backend (:8080)
                  └─ /*     → serve static → /var/www/campushub/
```

### 部署步骤

```bash
# 服务器上执行
./deploy.sh              # 前端 + 后端
./deploy.sh frontend     # 仅前端
./deploy.sh all          # 全量部署
```

部署脚本会自动：
1. 备份工作区改动
2. 从 Git 拉取最新代码
3. 构建前端 → rsync 到 `/var/www/campushub/`
4. 构建后端 → 重启 `campushub-backend` systemd 服务

### Systemd 服务

```ini
[Unit]
Description=CampusHub Backend
After=network.target mysql.service redis-server.service

[Service]
Type=simple
User=ubuntu
WorkingDirectory=/home/ubuntu/apps/SECII-CampusHub/backend
Environment=DB_URL=jdbc:mysql://127.0.0.1:3306/campusaid
Environment=DB_USERNAME=campusaid
Environment=DB_PASSWORD=***
Environment=JWT_SECRET=***
ExecStart=/usr/bin/java -jar target/campushub-backend-1.0.0.jar
Restart=always
RestartSec=5
```

---

## 8. CI/CD

### GitHub Actions

| 工作流 | 触发条件 | 任务 |
|--------|---------|------|
| `backend.yml` | `backend/**` 变更 | Checkstyle → Test (MySQL+Redis) → Build |
| `frontend.yml` | `frontend/**` 变更 | Lint → Build |

### GitLab CI

- `.gitlab-ci.yml` 作为父流水线，按目录变更触发子流水线
- `.gitlab/backend.yml`：checkstyle → test → package
- `.gitlab/frontend.yml`：lint → build

### 质量门禁

- **Checkstyle：** 自定义规则集（`backend/checkstyle.xml`），0 违规通过
- **JaCoCo：** 行覆盖率 ≥ 60%
- **前端 lint：** ESLint flat config

---

## 9. 性能优化

### 已实施

| 优化 | 说明 | 文件 |
|------|------|------|
| Redis 缓存 | 用户信息缓存 30 分钟，任务列表缓存 2 分钟 | `UserService.java`, `TaskService.java` |
| SQL 索引下推 | 分页查询在 SQL 层完成，减少内存排序 | `TaskMapper.xml` |
| Feed 缓存失效 | 创建/接单/完成时自动失效缓存 | `TaskService.java` |
| 头像过滤 | 排行榜排除 base64 内联头像 | `UserMapper.xml` |
| 索引 | `idx_users_points` 加速排行榜排序 | `schema.sql` |

