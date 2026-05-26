# 测试与 CI/CD 记录

## 1. 本次验证命令

在 `dev` 分支临时 worktree 中执行了以下命令：

```bash
cd backend
mvn test
mvn verify
mvn -B checkstyle:check

cd ../frontend
npm install
npm run build
npm run lint
```

## 2. 验证结果

| 类别 | 命令 | 结果 |
|---|---|---|
| 后端测试 | `mvn test` | 通过 |
| 后端覆盖率与质量门 | `mvn verify` | 通过 |
| 后端静态检查 | `mvn -B checkstyle:check` | 通过 |
| 前端依赖安装 | `npm install` | 通过 |
| 前端构建 | `npm run build` | 通过 |
| 前端静态检查 | `npm run lint` | 通过 |

补充结果：

- 后端 JaCoCo 行覆盖率：`61.45%`
- JaCoCo 报告目录：`backend/target/site/jacoco/`
- `mvn verify` 已包含覆盖率阈值检查，当前阈值为 `LINE >= 60%`

## 3. 后端测试覆盖面

当前测试文件：

- `AuthTest`（3 个测试）
- `TaskTest`（14 个测试）
- `TaskCommentTest`（8 个测试）
- `TaskReviewTest`（3 个测试）
- `MessageTest`（4 个测试）
- `UserTest`（5 个测试）
- `TaskModeResolverTest`（4 个测试，含参数化）
- `FeedbackTest`（5 个测试）

**测试总数：`46` 个 `@Test` 方法**

### 3.1 已覆盖的正常流程

- 注册、登录、刷新令牌
- 任务创建、接单、完成
- 话题创建、评论、回复
- 双向评价与信用分更新
- 消息发送、已读、未读计数
- 用户设置与积分明细

### 3.2 已覆盖的异常/边界场景

- 重复评价被拒绝
- 非任务参与者无法确认完成
- 普通任务不能走话题评论链路
- 旧数据 `taskMode` 冲突时的兼容修正
- 话题评论奖励有每日上限
- 反馈提交需回复才能解决
- 反馈优先级按类型自动分配
- 管理员可调整反馈优先级

## 4. 集成测试判断

虽然当前测试目录未明确区分 `unit` 与 `integration`，但从测试内容看，已有测试跨越了：

- Service
- Mapper
- Spring Boot 上下文
- 数据库初始化脚本

因此这批测试实际上已经具备“轻量集成测试”特征。

### 4.1 已覆盖的完整流程

完整正常流程已被组合覆盖为：

1. 用户登录
2. 发布任务
3. 另一用户接单
4. 双方确认完成
5. 双向评价
6. 积分/信用分更新

### 4.2 已覆盖的异常流程

至少可归纳出以下 2 类异常流程：

1. 重复评价
2. 越权完成任务

另外还覆盖了：

- 任务/话题模式冲突
- 非话题内容评论失败

## 5. CI/CD 现状

### 5.1 现有文件

- `.gitlab-ci.yml`
- `.gitlab/backend.yml`
- `.gitlab/frontend.yml`

### 5.2 当前流水线行为

主流水线只负责按目录变更触发子流水线：

- `backend/**/*` 变更触发后端流水线
- `frontend/**/*` 变更触发前端流水线

后端子流水线现已执行：

```yaml
backend-checkstyle -> mvn -B checkstyle:check
backend-test -> mvn -B clean verify
backend-build -> mvn -B clean package -DskipTests
```

前端子流水线现已执行：

```yaml
frontend-lint -> npm ci && npm run lint
frontend-build -> npm ci && npm run build
```

## 6. 与课程要求的差距

### 6.1 已满足

- 自动安装依赖
- 自动运行静态检查
- 自动运行单元/集成测试
- 自动生成覆盖率报告
- 自动构建项目

### 6.2 仍待补强

- 尚未在仓库文档中附 GitLab 最近一次流水线运行截图或链接

## 7. 建议补强项

若继续完善 P4 验收证据，建议下一步做：

1. 在 GitLab 页面执行一次新流水线
2. 截图保存 checkstyle、test、coverage、build 全绿结果
3. 如答辩需要，再补一张 JaCoCo HTML 首页截图

## 8. 结论

当前系统已经完成本地等价验证，且 CI 配置已补到“检查 + 测试 + 覆盖率 + 构建”的完整链路。剩余问题主要不是配置缺失，而是 GitLab 平台侧运行记录尚未留档。
