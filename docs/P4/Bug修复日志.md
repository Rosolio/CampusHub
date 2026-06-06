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

### Bug 05：CI 流水线质量门不足

**问题现象**

- GitLab CI 当前只做构建，不自动跑测试和静态检查

**根因分析**

- `.gitlab/backend.yml` 使用 `mvn clean package -DskipTests`
- `.gitlab/frontend.yml` 只做 `npm install` 和 `npm run build`

**修复方案**

- 文档上已识别缺口
- 后续应补 `mvn test`、`checkstyle`、`npm run lint` 和覆盖率统计

**验证结果**

- 本地手工验证可通过，但流水线层面仍待补强

### Bug 06：通知表缺失导致测试报错

**问题现象**

- 新增 `NotificationController` 后测试运行时提示 `notifications` 表不存在

**根因分析**

- `init_test_schema.sql` 仅包含 P0/P1 阶段表结构，未同步新增的 `notifications`、`task_favorites`、`user_verifications` 等表

**修复方案**

- 将主 `schema.sql` 中的新增表定义同步至 `init_test_schema.sql`
- 同步添加 `image_urls`、`verified_status`、`priority`、`like_count` 等新列

**验证结果**

- `mvn test` 通过，70 个测试全部成功

### Bug 07：数据库表结构固化导致新增列不生效

**问题现象**

- `schema.sql` 使用 `CREATE TABLE IF NOT EXISTS`，测试数据库已存在旧表时新列不会自动添加

**根因分析**

- `spring.sql.init.mode=always` 仅在首次建表时生效，存量库中的表结构不会变更

**修复方案**

- 使用 `DROP DATABASE IF EXISTS campusaid; CREATE DATABASE campusaid;` 重建测试数据库
- 或改用迁移脚本 `ALTER TABLE` 同步新增列

**验证结果**

- `mvn clean test` 通过，所有 70 个测试通过

### Bug 08：管理员后台用户列表 base64 头像导致响应膨胀

**问题现象**

- 管理后台用户列表返回 4MB+ 数据，页面加载极慢

**根因分析**

- SQL 查询直接包含 `avatar_url`（base64 MEDIUMTEXT），未做字段裁剪

**修复方案**

- SQL 查询中去掉 `avatar_url` 字段
- 仅在有需要时单独加载头像

**验证结果**

- 管理员用户列表响应时间恢复正常，页面加载流畅

### Bug 09：空聊天状态文本导致会话卡片折叠

**问题现象**

- 没有任何历史消息的会话在列表中高度为 0，视觉效果异常

**根因分析**

- 空白文本区域缺少 `min-height` 或占位符

**修复方案**

- 为空会话添加占位文本："暂无消息"

**验证结果**

- 空会话在消息列表中正常显示

### Bug 10：话题详情返回按钮导航到错误标签页

**问题现象**

- 从话题广场进入详情后点击返回，回到首页而非话题标签页

**根因分析**

- 返回逻辑硬编码为 `router.back()`，未保留前一路由的 tab 状态

**修复方案**

- 改为显式导航到话题标签页

**验证结果**

- 返回按钮现在正确进入话题标签页

### Bug 11：未登录用户无法访问上传的图片

**问题现象**

- 匿名用户看到的页面中图片无法加载

**根因分析**

- 图片 URL 的静态资源接口需要认证

**修复方案**

- 静态资源接口调整为允许匿名访问

**验证结果**

- 未登录用户也可以正常查看已上传的图片

### Bug 12：createTask API 类型签名缺少 imageUrls 字段

**问题现象**

- TypeScript 编译报错，前端 API 层无法传递图片 URL 数组

**根因分析**

- `createTask` 函数的 TypeScript 类型定义未包含 `imageUrls` 属性

**修复方案**

- 在前端 API 服务层补充 `imageUrls: string[]` 类型定义

**验证结果**

- 前端编译通过，图片上传功能正常

## 3. 总结

当前累计记录 12 个修复项，覆盖以下类别：

1. **旧数据兼容**（Bug 01、02）— `TaskModeResolver` 统一归一化
2. **业务权限校验**（Bug 03、04）— 重复评价拦截、越权完成校验
3. **工程化质量门**（Bug 05、06、07）— CI 配置补齐、测试 Schema 同步
4. **性能与安全**（Bug 08、11）— 大字段裁剪、匿名访问放行
5. **UI 与交互**（Bug 09、10、12）— 空状态占位、导航修复、类型补齐

前两类已通过代码与测试收敛，第三类在本次迭代中已实际解决（测试全部通过），第四、五类随 P2 功能开发同步修复。“代码完全不可用”，而是：

1. 旧数据兼容
2. 业务权限校验
3. 工程化质量门

前两类已基本通过代码与测试收敛，第三类仍是 P4 材料层面的主要风险。
