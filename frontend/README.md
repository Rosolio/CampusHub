# CampusHub Frontend

前端基于 Vue 3、Vite、TypeScript、Vue Router、Axios 和 UnoCSS。已完成学生端全部核心页面、管理后台、校园认证、智能推荐展示和性能优化。

## 项目结构

- `src/main.ts`：应用入口。
- `src/router/index.ts`：路由配置，含登录守卫和管理员守卫。
- `src/services/api.ts`：Axios 实例、JWT 自动刷新、全部业务 API 封装。
- `src/pages`：学生端页面。
- `src/pages/admin`：管理后台页面。
- `src/pages/settings`：设置子页面。
- `src/components`：顶部导航、底部导航、Toast、确认弹窗等。
- `src/composables`：偏好设置、确认弹窗、Toast 等组合式逻辑。
- `src/utils`：认证工具、任务模式工具。

## 学生端页面

| 页面 | 路径 | 说明 |
|------|------|------|
| Auth | `/auth` | 登录/注册 |
| HomeGateway / Discovery | `/home` | 社区首页（需求广场 + 话题广场，双 Tab） |
| TopicSquare | `/topics` | 话题广场独立页 |
| RequestDetail | `/detail/:id` | 任务/话题详情 |
| Publish | `/publish` | 发布需求/话题 |
| Messages | `/messages` | 消息列表（智能轮询） |
| Profile | `/profile` | 个人中心（我的需求/话题帖/服务） |
| FeedbackPage | `/feedback` | 提交反馈 |
| VerificationPage | `/verification` | 校园身份认证 |
| Settings | `/settings` | 设置入口 |
| UserAgreementPage | `/agreement` | 用户协议（独立页，无需登录） |
| PrivacyPolicyPage | `/privacy` | 隐私政策（独立页，无需登录） |
| NotFound | `/:pathMatch(.*)*` | 404 页面 |

## 管理后台页面

| 页面 | 路径 | 说明 |
|------|------|------|
| AdminLayout | `/admin` | 管理后台布局 |
| AdminOverviewPage | `/admin` | 数据概览 |
| AdminUsersPage | `/admin/users` | 用户管理 |
| AdminModerationPage | `/admin/tasks` | 内容审核 |
| AdminCommunityFeedPage | `/admin/community` | 社区内容管理 |
| AdminVerificationPage | `/admin/verifications` | 认证审核 |
| AdminProfilePage | `/admin/profile` | 管理员个人资料 |

## 已完成功能

### 智能推荐展示
- 首页双 Tab（需求广场 / 话题广场），胶囊按钮切换
- 推荐模式切换：智能推荐 / 最新发布
- 分类筛选、位置输入、时间选项
- 任务卡片展示匹配度分数和推荐理由
- 话题帖独立浏览，支持分类 + 关键词搜索
- 话题热榜（Top 10，按评论×3 + 点赞×2 热度计算）

### 校园认证
- 认证申请页（上传证件照片 + 填写姓名学号）
- 认证状态实时展示（未认证 / 审核中 / 已认证）
- 管理员审核页（查看照片、通过/驳回/撤销）

### 性能优化
- 服务端分页（`taskMode`、`page`、`size` 参数），前端不再全量拉取
- 需求 Tab 和话题 Tab 各取所需，不再重复请求
- 消息页轮询改为轻量未读计数，仅在计数变化时拉全量
- 标记已读改为批量接口（一次请求代替 N 次）
- 详情页加载状态占位，避免话题/任务布局闪烁

### UI 一致性
- Tab 按钮统一胶囊风格，独立间距
- 顶部/底部导航统一交互
- Toast、确认弹窗全局可用

## 运行

```bash
npm install
npm run dev      # 开发模式
npm run build    # 生产构建
```
