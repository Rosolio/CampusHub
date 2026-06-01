# 测试与 CI/CD 记录

## 1. 本次验证命令

在 `dev` 分支执行了以下命令：

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
| 后端测试 | `mvn test` | ✅ 通过 |
| 后端覆盖率与质量门 | `mvn verify` | ✅ 通过 |
| 后端静态检查 | `mvn -B checkstyle:check` | ✅ 通过 |
| 前端依赖安装 | `npm install` | ✅ 通过 |
| 前端构建 | `npm run build` | ✅ 通过 |
| 前端静态检查 | `npm run lint` | ✅ 通过 |

- 后端 JaCoCo 行覆盖率：>= 61%
- JaCoCo 报告目录：`backend/target/site/jacoco/`
- `mvn verify` 已包含覆盖率阈值检查（`LINE >= 60%`）

## 3. 后端测试覆盖面

当前测试文件（9 个测试类，55 个 `@Test` 方法）：

| 测试类 | 测试数 | 覆盖内容 |
|------|------|---------|
| `AuthTest` | 3 | 注册、登录、刷新令牌 |
| `TaskTest` | 15 | 任务创建/接单/完成/取消/删除、旧数据兼容、权限校验、评价积分 |
| `TaskCommentTest` | 8 | 评论/回复/删除、模式校验、奖励上限、旧数据兼容 |
| `TaskReviewTest` | 3 | 双向评价、重复评价限制、低分扣分 |
| `MessageTest` | 4 | 消息发送、已读、未读计数 |
| `UserTest` | 5 | 用户信息、设置、积分明细 |
| `TaskModeResolverTest` | 6 | 任务/话题模式推断（含参数化测试） |
| `FeedbackTest` | 5 | 反馈提交、管理处理、优先级 |
| `TaskPerformanceTest` | 6 | 分页正确性、taskMode/topic 筛选、缓存一致性、推荐模式 |

### 3.1 已覆盖的正常流程

- 注册、登录、刷新令牌
- 任务创建、接单、完成
- 话题创建、评论、回复
- 双向评价与信用分更新
- 消息发送、已读、未读计数、批量已读
- 用户设置与积分明细
- 分页查询与缓存命中

### 3.2 已覆盖的异常/边界场景

- 重复评价被拒绝
- 非任务参与者无法确认完成
- 普通任务不能走话题评论链路
- 旧数据 `taskMode` 冲突时的兼容修正
- 话题评论奖励有每日上限
- 低分评价扣分机制
- 反馈提交需回复才能解决
- 分页不重叠（page1 IDs ∩ page2 IDs = ∅）
- taskMode 筛选互斥（task 不含 topic，topic 不含 task）

## 4. 集成测试

已有测试跨越 Service、Mapper、Spring Boot 上下文和数据库，实际上已具备轻量集成测试特征。

完整正常流程已被组合覆盖为：
1. 用户登录
2. 发布任务
3. 另一用户接单
4. 双方确认完成
5. 双向评价
6. 积分/信用分更新

至少覆盖以下异常流程：
1. 重复评价
2. 越权完成任务
3. 任务/话题模式冲突
4. 非话题内容评论失败

## 5. CI/CD

### 5.1 现有文件

- `.gitlab-ci.yml`
- `.gitlab/backend.yml`
- `.gitlab/frontend.yml`
- `.github/workflows/`

### 5.2 流水线行为

后端子流水线：
```yaml
backend-checkstyle → mvn -B checkstyle:check
backend-test      → mvn -B clean verify
backend-build     → mvn -B clean package -DskipTests
```

前端子流水线：
```yaml
frontend-lint  → npm ci && npm run lint
frontend-build → npm ci && npm run build
```

## 6. 与课程要求的对照

| 要求 | 状态 |
|------|------|
| 自动安装依赖 | ✅ |
| 自动运行静态检查 | ✅ |
| 自动运行单元/集成测试 | ✅ |
| 自动生成覆盖率报告 | ✅ |
| 自动构建项目 | ✅ |
| 核心模块覆盖率 >= 60% | ✅ >= 61% |
| 集成测试覆盖 1 正常 + 2 异常流程 | ✅ |
| CI/CD 运行记录截图 | ⚠️ 建议补 GitLab 截图 |

## 7. 结论

质量保障体系已完整覆盖检查、测试、覆盖率、构建四道门，55 个测试全部通过，满足 P4 验收标准。
