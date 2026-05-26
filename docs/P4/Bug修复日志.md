# Bug 修复日志

## 1. 记录原则

- 只记录对演示、业务流程或工程质量有代表性的缺陷
- 每条日志都包含现象、根因、修复方案和验证结果
- 以 `dev` 分支现有代码与本次验证结论为依据

## 2. Bug 清单

### Bug 01：旧数据话题帖模式冲突

**问题现象**

- 某些旧帖子本质是话题帖，但 `taskMode` 存成了 `task`
- 导致评论、点赞或页面行为与预期不一致

**根因分析**

- 早期数据字段设计或迁移阶段，没有统一对 `category`、`badgeSecondary`、`impactText` 和 `taskMode` 做归一化

**修复方案**

- 引入 `TaskModeResolver`
- 在读取或处理任务前根据旧字段推断并修正真实模式

**验证结果**

- `TaskModeResolverTest` 通过
- `TaskCommentTest` 中旧数据兼容场景通过
- `TaskTest` 中旧话题帖点赞兼容场景通过

### Bug 02：普通任务误进入话题评论链路

**问题现象**

- 跑腿任务若未做类型校验，可能被当作话题帖评论

**根因分析**

- 任务与话题共用 `tasks` 表，若缺少模式判断，评论服务无法正确限制入口

**修复方案**

- 评论创建前增加“当前内容是否为话题帖”的业务校验

**验证结果**

- `TaskCommentTest.testTaskModeCannotComment` 通过
- 异常信息为“当前内容不是话题帖”

### Bug 03：重复评价导致业务数据失真

**问题现象**

- 同一用户可能对同一任务重复提交评价，造成积分和信用分重复结算

**根因分析**

- 若只靠前端限制按钮状态，无法防止重复提交或并发重试

**修复方案**

- Service 层增加重复评价判断
- 数据层增加唯一约束兜底

**验证结果**

- `TaskReviewTest.testCannotReviewSameTaskTwice` 通过

### Bug 04：非参与者越权确认完成

**问题现象**

- 与任务无关的用户若能调用完成接口，会破坏订单状态流转

**根因分析**

- 任务完成逻辑若仅校验登录，不校验参与身份，就会出现越权

**修复方案**

- 完成任务前校验调用者必须是发布者或接单者

**验证结果**

- `TaskTest.testOnlyTaskParticipantsCanCompleteTask` 通过

### Bug 05：CI 流水线质量门不足 ✅ 已修复

**问题现象**

- GitLab CI 当前只做构建，不自动跑测试和静态检查

**根因分析**

- `.gitlab/backend.yml` 使用 `mvn clean package -DskipTests`
- `.gitlab/frontend.yml` 只做 `npm install` 和 `npm run build`

**修复方案**

- 后端子流水线补 `mvn -B checkstyle:check`、`mvn -B clean verify`（含 JaCoCo 覆盖率）
- 前端子流水线补 `npm ci && npm run lint`
- 新增 GitHub Actions 镜像 CI 配置

**验证结果**

- GitLab CI 后端流水线：checkstyle → test → build 三阶段完整
- GitLab CI 前端流水线：lint → build 两阶段完整
- GitHub Actions 同样覆盖 checkstyle、test、build 和 lint、build

### Bug 06：注册限频绕过

**问题现象**

- 用户注册时未调用登录限频检查，可通过重复注册绕过 `auth:login_attempts` 限频

**根因分析**

- `AuthService` 注册流程中隐式登录路径缺少 `enforceLoginRateLimit()` 调用

**修复方案**

- 在注册流程的隐式登录路径中增加限频检查

**验证结果**

- 限频逻辑统一应用于登录和注册两条路径

### Bug 07：JWT token type 缺失导致 refresh token 混用

**问题现象**

- access token 可被当作 refresh token 使用，绕过过期校验

**根因分析**

- `JwtUtil.generateToken()` 和 `generateRefreshToken()` 未在 JWT claims 中区分 token 类型

**修复方案**

- `generateToken()` 写入 `"type": "access"`
- `generateRefreshToken()` 写入 `"type": "refresh"`
- `refreshToken()` 校验 `getTokenType()` 必须为 `"refresh"`

**验证结果**

- `AuthTest.testRefreshToken` 通过

### Bug 08：密码哈希通过 API 泄露

**问题现象**

- `/users/me`、`/admin/users` 等接口直接返回 `User` 实体，JSON 序列化包含 `password` 字段

**根因分析**

- `User` 实体未对 `password` 字段做 `@JsonIgnore`，且未使用安全 DTO

**修复方案**

- 创建 `UserVO` DTO，排除 `password` 和 `disabledReason` 字段
- 所有 Controller 返回 `UserVO` 而非 `User`
- `User` 实体的 `getPassword()` 方法加 `@JsonIgnore` 兜底

**验证结果**

- API 响应不再包含密码哈希

### Bug 09：JWT 过滤器硬编码角色

**问题现象**

- `JwtAuthenticationFilter` 对所有用户硬编码 `SimpleGrantedAuthority("USER")`，管理员无法被正确识别

**根因分析**

- 过滤器未查询数据库获取用户实际角色

**修复方案**

- 注入 `UserMapper`，调用 `selectRoleById(userId)` 获取实际角色
- 根据角色设置 `"ADMIN"` 或 `"USER"` authority

**验证结果**

- `SecurityConfig` 中 `.requestMatchers("/admin/**").hasAuthority("ADMIN")` 生效

### Bug 10：管理员路径无权限保护

**问题现象**

- `/admin/**` 接口无 Spring Security 权限校验，普通用户可访问

**根因分析**

- `SecurityConfig` 未对 `/admin/**` 路径添加 `hasAuthority` 限制

**修复方案**

- 添加 `.requestMatchers("/admin/**").hasAuthority("ADMIN")`

**验证结果**

- 普通用户访问 `/admin/**` 返回 403

### Bug 11：GlobalExceptionHandler 不返回 403 状态码

**问题现象**

- "无管理员权限"和"账号已被禁用"等权限类异常返回 400 而非 403

**根因分析**

- `GlobalExceptionHandler` 统一将所有 `RuntimeException` 映射为 400

**修复方案**

- 新增 `AUTHORITY_ERROR_CODES` 映射表，将特定中文错误消息映射为 HTTP 403
- 增加 SLF4J 日志记录

**验证结果**

- 权限类异常正确返回 403 FORBIDDEN

## 3. 总结

当前最有代表性的缺陷并不是“代码完全不可用”，而是：

1. 旧数据兼容
2. 业务权限校验
3. 工程化质量门

前两类已基本通过代码与测试收敛，第三类已通过补充 CI 配置解决。

此外，安全加固（commit `62aa029`）修复了 6 个安全相关缺陷（Bug 06-11），涵盖认证绕过、数据泄露和权限控制。
