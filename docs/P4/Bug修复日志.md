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

## 3. 总结

当前最有代表性的缺陷并不是“代码完全不可用”，而是：

1. 旧数据兼容
2. 业务权限校验
3. 工程化质量门

前两类已基本通过代码与测试收敛，第三类仍是 P4 材料层面的主要风险。
