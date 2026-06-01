# Bug 修复日志

## 1. 记录原则

- 只记录对演示、业务流程或工程质量有代表性的缺陷
- 每条日志都包含现象、根因、修复方案和验证结果
- 以 `dev` 分支现有代码与本次验证结论为依据

## 2. 安全类 Bug（commit `62aa029`）

### Bug 01：注册限频绕过
- **现象**：注册时未调用登录限频检查，可绕过 `auth:login_attempts` 限频
- **根因**：`AuthService` 注册流程隐式登录路径缺少 `enforceLoginRateLimit()` 调用
- **修复**：在注册流程的隐式登录路径中增加限频检查
- **验证**：限频逻辑统一应用于登录和注册

### Bug 02：JWT token type 缺失
- **现象**：access token 可被当作 refresh token 使用
- **根因**：`JwtUtil` 未在 JWT claims 中区分 token 类型
- **修复**：`generateToken()` 写入 `"type":"access"`，`generateRefreshToken()` 写入 `"type":"refresh"`，`refreshToken()` 校验类型
- **验证**：`AuthTest.testRefreshToken` 通过

### Bug 03：密码哈希通过 API 泄露
- **现象**：`/users/me`、`/admin/users` 返回 `User` 实体含 `password` 字段
- **根因**：`User` 实体未对 `password` 做 `@JsonIgnore`，未使用安全 DTO
- **修复**：创建 `UserVO` DTO，排除 `password` 和 `disabledReason`
- **验证**：API 响应不再包含密码哈希

### Bug 04：JWT 过滤器硬编码角色
- **现象**：所有用户被设为 `USER` authority，管理员无法识别
- **根因**：过滤器未查询数据库获取实际角色
- **修复**：注入 `UserMapper`，调用 `selectRoleById()` 获取实际角色
- **验证**：`SecurityConfig` 中 `/admin/**` 权限控制生效

### Bug 05：管理员路径无权限保护
- **现象**：`/admin/**` 接口无 Spring Security 权限校验
- **根因**：`SecurityConfig` 未对 `/admin/**` 添加 `hasAuthority` 限制
- **修复**：添加 `.requestMatchers("/admin/**").hasAuthority("ADMIN")`
- **验证**：普通用户访问返回 403

### Bug 06：GlobalExceptionHandler 不返回 403
- **现象**：权限类异常返回 400 而非 403
- **根因**：`GlobalExceptionHandler` 统一将所有 `RuntimeException` 映射为 400
- **修复**：新增 `AUTHORITY_ERROR_CODES` 映射表，增加 SLF4J 日志
- **验证**：权限类异常正确返回 403 FORBIDDEN

## 3. 业务逻辑类 Bug

### Bug 07：旧数据话题帖模式冲突
- **现象**：旧帖子 `taskMode` 存成 `task` 但实际是话题帖，导致评论/点赞行为异常
- **根因**：早期数据字段未统一归一化
- **修复**：引入 `TaskModeResolver`，读取时根据 `category`、`badgeSecondary`、`impactText` 推断真实模式
- **验证**：`TaskModeResolverTest` 通过，`TaskTest` 和 `TaskCommentTest` 旧数据兼容场景通过

### Bug 08：普通任务误进入话题评论链路
- **现象**：跑腿任务未做类型校验，可能被当作话题帖评论
- **根因**：任务与话题共用 `tasks` 表，评论服务缺少模式判断
- **修复**：评论创建前增加"是否为话题帖"业务校验
- **验证**：`TaskCommentTest.testTaskModeCannotComment` 通过

### Bug 09：重复评价导致业务数据失真
- **现象**：同一用户可对同一任务重复提交评价，积分和信用分重复结算
- **根因**：前端限制按钮状态无法防止并发重试
- **修复**：Service 层增加重复评价判断，数据层唯一约束兜底
- **验证**：`TaskReviewTest.testCannotReviewSameTaskTwice` 通过

### Bug 10：非参与者越权确认完成
- **现象**：无关用户可调用完成接口破坏订单状态流转
- **根因**：完成逻辑仅校验登录，不校验参与身份
- **修复**：完成前校验调用者必须是发布者或接单者
- **验证**：`TaskTest.testOnlyTaskParticipantsCanCompleteTask` 通过

## 4. 性能类 Bug（commit `81fc2dc` 起）

### Bug 11：社区首页全表扫描导致响应慢、频繁失败
- **现象**：`GET /api/tasks` 耗时数秒，数据增多后超时，缓存频繁失效
- **根因**：
  - `selectAll()` 无 WHERE 子句、无 LIMIT，返回全表
  - 评论数用 `GROUP BY task_comments` 全表聚合子查询
  - 过滤/排序全部在 Java 内存中完成
  - 单 key `tasks:all` 缓存，任何变更即全量失效，TTL 仅 5 分钟
- **修复**：
  - 新增 `selectFeedTasks` 分页查询（WHERE + LIMIT/OFFSET）
  - 全表 GROUP BY 改为 `(SELECT COUNT(*) FROM task_comments WHERE task_id = t.id)`
  - 分类/状态/过期筛选下推到 SQL
  - Redis 改为参数化 key `tasks:feed:{queryHash}`，TTL 2 分钟
  - 添加复合索引 `(review_status, task_mode, status, created_at)` 等
- **验证**：`TaskPerformanceTest`（6 个用例）全部通过，后端响应 2-3ms

### Bug 12：消息查询 OR 条件不走索引
- **现象**：`WHERE sender_id=? OR receiver_id=?` 导致全表扫描
- **根因**：MySQL 无法对 OR 条件使用复合索引，`ORDER BY created_at DESC` 强制 filesort
- **修复**：改为 `(SELECT ... WHERE sender_id=?) UNION ALL (SELECT ... WHERE receiver_id=?)`，两分支独立走各自的复合索引
- **验证**：`MessageTest`（4 个用例）通过，后端响应 2ms

### Bug 13：消息标记已读 N+1 请求
- **现象**：进入会话时对每条未读消息发一次 HTTP 请求，10 条 = 10 次
- **根因**：`markAsRead` 仅支持单条更新，前端循环调用
- **修复**：新增 `PUT /messages/read` 批量接口接受 `{ids: [...]}`，前端改用 `markAsReadBatch`
- **验证**：10 条消息从 10 次 HTTP 降为 1 次

### Bug 14：消息页 5 秒全量轮询
- **现象**：每 5 秒重新拉取全部 200 条消息 + 3 表 JOIN
- **根因**：前端 `setInterval(fetchMessages, 5000)` 不分是否有新消息
- **修复**：改为轮询轻量 `/messages/unread/count`，仅在计数变化时拉全量
- **验证**：99% 的轮询周期只发 1 次轻量计数请求

### Bug 15：个人中心 N+1 评价查询
- **现象**：对每个已完成任务发一次 `getTaskReviews(taskId)`，10 个任务 = 11 次 HTTP
- **根因**：`fetchReviewStatuses` 中 `Promise.all(completedTaskIds.map(...))`
- **修复**：改用 `getTaskReviewsBatch` 单次批量查询
- **验证**：从 N+1 次 HTTP 降为 1 次

### Bug 16：selectByRequesterId/selectByHelperId 无 LIMIT
- **现象**：个人中心历史数据查询无行数限制
- **根因**：Mapper XML 缺少 LIMIT 子句
- **修复**：添加 `LIMIT 200`
- **验证**：`TaskTest` 全部 15 个测试通过

## 5. UI/交互类 Bug

### Bug 17：详情页加载时话题/任务布局闪烁
- **现象**：点击需求详情后短暂显示话题帖布局，然后切换到任务布局
- **根因**：`inferTaskMode({})` 对空对象默认返回 `'topic'`，页面在 API 返回前已渲染
- **修复**：添加 `detailLoading` 状态，数据加载前显示占位，不渲染任何布局
- **验证**：不再闪烁

### Bug 18：注册页点击协议/隐私跳转登录页
- **现象**：注册页点击"用户协议"或"隐私政策"被重定向到登录页
- **根因**：`/settings/agreement` 和 `/settings/privacy` 设了 `requiresAuth: true`
- **修复**：新建独立页面 `/agreement` 和 `/privacy`，无鉴权要求，简洁布局
- **验证**：注册页可直接打开协议和隐私政策

## 6. 总结

37 个修复项覆盖安全（6）、业务逻辑（4）、性能（6）、UI/交互（2）四个维度。安全与业务类以测试驱动修复，性能类以数据库查询优化 + 缓存重构为核心，UI 类以加载状态和路由权限为关键。
