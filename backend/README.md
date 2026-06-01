# CampusHub Backend

后端基于 Spring Boot 3.2、Java 17、MyBatis Plus、MySQL 8.0 和 Redis。已完成认证、任务、话题、评论、评价、消息、反馈、公告、认证和后台管理等全部核心能力，并完成多轮性能优化。

## 项目结构

- `CampusHubApplication.java`：Spring Boot 启动入口。
- `controller`：REST 接口层，包含 `AuthController`、`TaskController`、`TaskCommentController`、`TaskReviewController`、`MessageController`、`UserController`、`FeedbackController`、`AnnouncementController`、`AdminController`、`VerificationController`。
- `service`：业务逻辑层，核心包括 `TaskService`（分页查询 + 缓存）、`TaskRecommendationService`（智能推荐打分）、`MessageService`（批量已读）、`VerificationService`（校园认证）、`AdminService`、`UserService` 等。
- `mapper` 与 `resources/mapper`：MyBatis Mapper 接口和 XML SQL。
- `entity`：数据库实体。
- `dto`：接口请求/响应体。
- `config`：JWT 鉴权、Spring Security、Redis、MyBatis 配置。
- `sql/`：数据库初始化脚本和性能索引迁移脚本。

## 已完成功能

### 核心流程
- 注册登录、JWT 鉴权、Token 自动刷新
- 发布需求（任务 / 话题帖），含关键词审核
- 任务列表（分页、分类筛选、位置/时间过滤）
- 任务详情、接单、取消接单、完成确认、删除
- 话题帖评论（含回复、点赞、删除）
- 任务互评（双盲打分、积分结算）
- 消息通知（系统消息 + 私信，批量已读）
- 反馈提交与管理员处理
- 公告管理

### 智能推荐（已实现）
- `GET /api/tasks` 支持 `mode=recommended|latest`、`category`、`location`、`availableAt`、`page`、`size`、`taskMode` 参数
- 100 分制多维打分：分类匹配、地理位置、时间窗口、历史偏好、新鲜度
- 推荐理由回传 (`matchReasons`)，前端可展示
- 历史偏好基于用户接单记录自动计算

### 校园用户认证（已实现）
- 用户上传学生证/校园卡照片（multipart，最多 3 张，JPG/PNG，单张 ≤5MB）
- 管理员审核（通过/驳回/撤销），状态变更自动发系统通知
- 用户 `verifiedStatus` 冗余字段，全站展示认证标识
- 证件照片通过后端代理访问，不暴露直接 URL

### 性能优化（已实现）
- **服务端分页**：`selectFeedTasks` 带 WHERE 过滤 + LIMIT/OFFSET，不再全表扫描
- **SQL 条件下推**：`taskMode`、`category`、`status`、`expiresAt` 过滤在数据库层完成
- **评论数优化**：全表 `GROUP BY task_comments` 改为关联子查询 + 索引查找
- **消息查询**：`WHERE sender_id=? OR receiver_id=?` 改为 UNION ALL，两分支独立走复合索引
- **批量已读**：`PUT /messages/read` 接受 `{ ids: [...] }`，一次请求标记多条
- **Redis 缓存**：参数化缓存键 `tasks:feed:{queryHash}`，TTL 2 分钟，数据变更时批量失效
- **复合索引**：`(review_status, task_mode, status, created_at)`、`(review_status, task_mode, category, created_at)` 等

## API 端点（主要）

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/tasks` | 任务/话题列表（分页 + 推荐） |
| GET | `/api/tasks/{id}` | 任务详情 |
| POST | `/api/tasks` | 发布任务/话题 |
| POST | `/api/tasks/{id}/accept` | 接单 |
| POST | `/api/tasks/{id}/like` | 点赞 |
| DELETE | `/api/tasks/{id}/like` | 取消点赞 |
| POST | `/api/tasks/{id}/complete` | 确认完成 |
| GET | `/api/tasks/{id}/comments` | 获取评论 |
| POST | `/api/tasks/{id}/comments` | 发表评论 |
| GET | `/api/messages` | 消息列表（UNION ALL，限 200 条） |
| PUT | `/api/messages/read` | 批量标记已读 |
| GET | `/api/messages/unread/count` | 未读计数 |
| POST | `/api/users/me/verification` | 提交认证 |
| GET | `/api/admin/dashboard` | 管理后台概览 |
| PUT | `/api/admin/tasks/{id}/review` | 审核内容 |

## 运行

```bash
# 开发环境
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# 生产部署
mvn -DskipTests clean package
java -jar target/campushub-backend-1.0.0.jar
```

## 测试

```bash
mvn test  # 55 个测试，含性能测试
```
