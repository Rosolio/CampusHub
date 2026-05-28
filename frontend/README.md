# CampusHub Frontend

前端基于 Vue 3、Vite、TypeScript、Vue Router、Axios 和 UnoCSS。当前 `dev` 基线已经完成学生端核心页面和管理端基础页面。

## 现有结构

- `src/main.ts`：前端应用入口。
- `src/router/index.ts`：路由配置与登录/管理员守卫。
- `src/services/api.ts`：Axios 实例、JWT 自动刷新和业务 API 封装。
- `src/pages`：学生端页面，包括登录注册、首页、话题广场、详情、发布、消息、个人中心、反馈、设置。
- `src/pages/admin`：管理后台页面。
- `src/components`：顶部导航、底部导航、Toast、确认弹窗、设置项、表单组件等。
- `src/composables`：偏好设置、确认弹窗、Toast 等组合式逻辑。
- `src/utils`：认证与任务模式工具。

## 当前完成度

- 首页 `Discovery.vue` 已能拉取 `taskApi.getTasks()`，再在前端区分任务和话题。
- 首页任务区当前只支持“全部任务、跑腿代办、学习辅导”分类筛选。
- 话题热榜当前在前端按评论数和点赞数计算热度。
- 当前任务列表没有展示推荐分、推荐理由，也没有向后端传递位置、可用时间或推荐模式参数。

## 智能匹配与推荐需求

### 目标

让首页任务流变成“智能推荐任务流”：默认优先展示更适合当前用户接单的任务，并保留手动分类筛选能力。

### 页面范围

第一阶段主要改造：

- `src/pages/Discovery.vue`：首页任务列表、筛选、推荐状态展示。
- `src/services/api.ts`：扩展任务列表 API 参数。
- `src/utils/tasks.ts`：补充推荐字段归一化逻辑。

详情页 `RequestDetail.vue` 暂不需要参与推荐计算，只负责展示被点击任务。

### 前端交互逻辑

首页任务区增加推荐相关状态：

- 默认模式：`recommended`，文案可表现为“智能推荐”。
- 可切换为：`latest`，用于按最新发布查看。
- 分类筛选继续保留：`全部任务`、`跑腿代办`、`学习辅导`。
- 位置输入或选择：第一阶段可使用文本输入，例如“南门”“图书馆”“宿舍区”。
- 可用时间：第一阶段可提供简单选项，如“现在”“今天内”“明天”，最终转换为后端可识别的时间参数。

前端请求参数建议：

```ts
taskApi.getTasks({
  mode: recommendationMode,
  category: activeCategory === '全部任务' ? undefined : activeCategory,
  location: selectedLocation || undefined,
  availableAt: selectedAvailableAt || undefined
})
```

### 展示规则

任务卡片继续显示原有信息：

- 分类
- 紧急/普通标签
- 状态
- 标题与描述
- 地点
- 截止时间
- 发布者
- 奖励

当后端返回推荐字段时，额外展示：

- `matchScore`：推荐匹配度，可显示为 `匹配度 86` 或进度条。
- `matchReasons`：最多展示 2 条理由，如“常接跑腿代办”“地点接近”。
- 推荐模式标签：当 `mode = recommended` 时显示“智能推荐”；当 `mode = latest` 时显示“最新发布”。

如果后端暂未返回推荐字段，前端应保持兼容，不显示推荐区块。

### 数据归一化

前端任务卡片映射需要扩展字段：

- `matchScore?: number`
- `matchReasons?: string[]`
- `recommendationMode?: 'recommended' | 'latest'`

`mapTaskToCard` 需要保证：

- `matchScore` 不存在时不参与前端排序。
- `matchReasons` 非数组时归一为空数组。
- 前端不再自行按推荐分排序，推荐排序以服务端返回顺序为准。

### 空状态与异常

- 新用户无历史记录时，仍展示后端返回的基础匹配结果。
- 筛选后无结果时，提示“当前没有匹配任务”，并提供清空筛选操作。
- 推荐接口失败时，沿用当前错误提示，不影响公告和话题热榜加载。

### 验收标准

- 首页默认请求推荐模式任务列表。
- 用户切换分类后，请求参数带上分类，并只展示对应分类结果。
- 用户输入位置或选择时间后，任务列表刷新。
- 后端返回 `matchScore` 和 `matchReasons` 时，任务卡片能正确展示。
- 后端不返回推荐字段时，页面仍能按现有方式工作。
- 前端不破坏话题广场、发布页、详情页、消息和个人中心现有流程。
