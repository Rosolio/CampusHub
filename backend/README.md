# CampusHub Backend

后端基于 Spring Boot 3.2、Java 17、MyBatis Plus、MySQL 和 Redis。当前 `dev` 基线已经完成认证、用户资料、任务发布与接单、评论互动、评价积分、消息通知、反馈、公告和后台管理等核心能力。

## 现有结构

- `CampusHubApplication.java`：Spring Boot 启动入口。
- `controller`：REST 接口层，包含 `AuthController`、`TaskController`、`TaskCommentController`、`TaskReviewController`、`MessageController`、`UserController`、`FeedbackController`、`AnnouncementController`、`AdminController`。
- `service`：业务逻辑层，目前任务列表由 `TaskService#getTasks` 汇总并过滤可见内容。
- `mapper` 与 `resources/mapper`：MyBatis Mapper 接口和 XML SQL。
- `entity`：数据库实体，`Task` 已包含分类、地点文本、时间文本、状态、点赞数、评论数、发布者/接单者信息等字段。
- `dto`：接口请求体。
- `config`：JWT 鉴权、Spring Security、Redis、MyBatis 配置。
- `resources/schema.sql` 与 `resources/data.sql`：数据库初始化。

## 当前完成度

- 已完成 P0 主流程：注册登录、JWT 鉴权、发布需求、列表/详情、接单、取消、完成、删除、消息联动。
- 已完成或基本完成 P1 能力：评论回复、点赞、评价积分、反馈、公告、管理后台。
- 当前任务列表接口 `GET /api/tasks` 返回审核通过的社区内容，排序主要来自 `TaskMapper.selectAll` 的 `created_at DESC`，个性化推荐尚未实现。

## 智能匹配与推荐需求

### 目标

为普通用户提供“更适合我接单/参与”的任务排序，而不是只按发布时间展示。第一阶段不引入复杂机器学习模型，采用可解释的规则打分，便于测试、演示和课程验收。

### 范围

推荐对象以 `task_mode = 'task'` 的可接单需求为主，包括“跑腿代办”和“学习辅导”。话题帖仍保留现有社区热榜逻辑，不纳入接单推荐主排序。

推荐入口优先复用 `GET /api/tasks`，通过可选查询参数启用推荐排序：

- `mode`：`latest` 或 `recommended`，默认建议为 `recommended`。
- `category`：分类筛选，如 `跑腿代办`、`学习辅导`。
- `location`：用户当前或选择的校内位置文本。
- `availableAt`：用户可服务时间，ISO datetime 字符串。
- `limit`：返回数量上限。

如果前端暂不传参数，后端应保持兼容，仍能返回现有列表。

### 候选集规则

后端先筛选候选任务，再计算分数：

- 只展示 `review_status = 'approved'` 的内容。
- 普通首页推荐优先包含 `task_mode = 'task'` 且 `status = 'pending'` 的任务。
- 可保留 `status = 'accepted'` 任务用于详情或历史展示，但不应排在推荐接单列表前列。
- 排除当前用户自己发布的任务，避免推荐给发布者本人接单。
- 排除已取消、已完成、已过期的任务。
- 管理员账号不参与普通推荐，沿用现有普通社区限制。

### 基础匹配打分

推荐分数采用 100 分制，后端可在服务层计算，不要求第一阶段新增数据库字段。

| 维度 | 权重 | 实现逻辑 |
|---|---:|---|
| 分类匹配 | 35 | 用户显式选择分类完全匹配得满分；未选择分类时，根据用户历史接单分类偏好计分。 |
| 地理位置匹配 | 20 | 用 `locationText` 做校内文本匹配，完全包含、同楼/同区关键词命中得高分；无法解析时得中性分。 |
| 时间匹配 | 20 | `availableAt` 或当前时间与 `expiresAt`、`timeText` 匹配；未截止且时间越近得分越高，已过期不得推荐。 |
| 历史行为偏好 | 20 | 统计当前用户历史接单并完成的任务分类，常接分类加权。例如经常接“跑腿代办”的用户优先看到快递/代取类需求。 |
| 新鲜度与质量 | 5 | 较新的任务、发布者信用分较高的任务获得小幅加分。 |

### 历史行为偏好

第一阶段使用已有表 `task_participants` 和 `tasks` 计算，不新增行为日志表：

- 统计当前用户作为 `helper` 且状态为 `completed` 或非 `canceled` 的任务。
- 按 `tasks.category` 聚合计数，得到用户偏好分类。
- 分类偏好分 = 当前任务分类历史次数 / 用户历史接单总次数。
- 当历史为空时，不做偏好惩罚，使用基础匹配和新鲜度排序。

后续如需扩展点击、浏览、收藏行为，可再新增 `user_task_events` 表，但不作为本阶段必需项。

### 排序规则

最终排序按以下优先级：

1. 推荐总分 `matchScore` 降序。
2. `status = 'pending'` 优先于其他状态。
3. 未过期且截止时间更近的任务优先。
4. 发布时间 `createdAt` 倒序。
5. `id` 倒序，保证结果稳定。

### 返回字段

建议在任务响应中补充推荐解释字段。可通过扩展 `Task` 非持久字段或新增响应 DTO 实现。

- `matchScore`：整数，0-100。
- `matchReasons`：字符串数组，如 `["常接跑腿代办", "地点接近", "今天可完成"]`。
- `matchedCategory`：命中的推荐分类。
- `recommendationMode`：`recommended` 或 `latest`。

如果为了降低改动，也可以先只返回排序后的任务列表，前端不展示理由；但验收可解释性会变弱。

### 后端实现拆分

建议新增或调整：

- `dto/TaskRecommendationQuery.java`：承接 `mode/category/location/availableAt/limit`。
- `dto/TaskRecommendationResponse.java` 或在 `Task` 添加非数据库字段：承接 `matchScore`、`matchReasons`。
- `service/TaskRecommendationService.java`：封装候选过滤、用户画像统计、打分、排序。
- `mapper/TaskMapper.java` 与 `TaskMapper.xml`：新增候选任务查询和用户历史分类统计 SQL。
- `TaskController#getTasks`：读取查询参数和当前用户，调用推荐服务。
- `TaskService#getTasks`：保留兼容逻辑，或迁移到推荐服务内部复用。

### 验收标准

- 用户访问首页时，待接单任务不再只按发布时间排列，而是按推荐分排序。
- 选择分类后，分类匹配任务稳定排在前面。
- 提供位置参数时，同地点或相近地点任务优先。
- 有历史接单记录的用户，历史高频分类任务优先。
- 无历史记录的新用户仍能看到合理排序，不能出现空白或异常。
- 后端测试覆盖分类匹配、历史偏好、过期排除和稳定排序。
