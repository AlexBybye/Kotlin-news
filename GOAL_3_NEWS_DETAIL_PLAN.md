# 目标 3 详细开发计划：新闻详情页

## 1. 目标说明

本阶段目标是在现有首页新闻列表基础上，完成“新闻详情页”的完整展示链路，实现用户点击首页新闻卡片后进入详情页面，查看文章标题、来源、时间、封面图、正文、相关推荐与基础操作区域。

本阶段重点覆盖以下能力：

- `Navigation Component` 二级页面跳转
- 首页列表项点击进入详情页
- 详情页 UI 搭建与数据绑定
- `Safe Args` 或统一参数传递方案
- 详情页 `ViewModel` 状态管理
- 详情数据的 `Mock / Remote` 可切换结构
- 为后续收藏、原文跳转、历史记录保留扩展入口

本阶段不包含：

- Room 收藏持久化
- 浏览历史落库
- 评论系统
- 真正的 WebView 原文阅读
- 多级相关推荐跳转链路优化

## 2. 当前现状分析

结合当前项目现状，详情页相关基础如下：

- [HomeFragment.kt](file:///Users/bytedance/AndroidStudioProjects/Homework/app/src/main/java/com/example/homework/ui/home/HomeFragment.kt) 已有列表点击回调，但目前只弹出提示，还未真正导航。
- [nav_graph.xml](file:///Users/bytedance/AndroidStudioProjects/Homework/app/src/main/res/navigation/nav_graph.xml) 目前只有 4 个一级页面，尚未声明详情页 destination。
- [NewsArticle.kt](file:///Users/bytedance/AndroidStudioProjects/Homework/app/src/main/java/com/example/homework/model/NewsArticle.kt) 已形成首页文章业务模型，可直接复用于详情页。
- 当前数据层已具备 `Mock / Remote` 可切换思路，可沿用到详情页数据获取。

因此，目标 3 的核心是补齐“点击新闻 -> 参数传递 -> 请求详情数据 / 复用摘要数据 -> 渲染详情页”这条用户路径。

## 3. 设计原则

### 3.1 优先打通用户路径

- 第一优先级是首页点击后能进入详情页并展示完整内容。
- 第二优先级才是详情页功能的丰富度，如收藏、分享、原文跳转。

### 3.2 保持与目标 2 一致的 Mock / Remote 切换策略

- 详情页同样必须支持先使用 Mock 数据跑通。
- 后续切真实接口时，只改数据源实现，不改 UI 层结构。

### 3.3 参数传递只传关键标识，不传整块业务对象

- 推荐优先传 `newsId`、`title` 等轻量信息。
- 详情页根据 `newsId` 通过 repository 获取详情数据。
- 这样更适合后续切到真实接口，也避免大对象序列化带来的复杂度。

## 4. 本阶段产出物

完成后，项目应新增以下产出：

- 一个详情页 Fragment：`NewsDetailFragment`
- 一个详情页 ViewModel：`NewsDetailViewModel`
- 一个详情页布局：`fragment_news_detail.xml`
- 详情页 UI 状态模型：`NewsDetailUiState`
- 详情页业务模型：`NewsDetail`
- 详情页 DTO：`NewsDetailDto`
- 详情数据源与仓库能力扩展
- 导航图中新增详情页 destination 与 action
- 首页点击事件改为真实导航

## 5. 推荐包结构

建议在当前结构上新增以下模块：

```text
com.example.homework
├── data
│   ├── remote
│   │   ├── dto
│   │   │   └── NewsDetailDto.kt
│   │   ├── datasource
│   │   │   ├── MockNewsDataSource.kt
│   │   │   ├── RemoteNewsDataSource.kt
│   │   │   └── NewsDataSource.kt
│   │   └── api
│   │       └── NewsApiService.kt
│   ├── repository
│   │   └── NewsRepository.kt
│   └── mapper
│       └── NewsMapper.kt
├── model
│   ├── NewsArticle.kt
│   └── NewsDetail.kt
└── ui
    └── detail
        ├── NewsDetailFragment.kt
        ├── NewsDetailViewModel.kt
        └── NewsDetailUiState.kt
```

说明：

- 详情页仍复用 `NewsRepository`
- 不建议单独再拆一个独立仓库，避免课程项目结构过重

## 6. 详情页 UI 方案

## 6.1 页面结构

详情页建议结构如下：

1. 顶部返回栏
2. 新闻标题
3. 来源、发布时间、分类信息
4. 封面图
5. 摘要
6. 正文内容
7. 操作区：收藏、分享、查看原文
8. 相关推荐区
9. 加载态 / 错误态

建议布局组合：

- 根容器：`ConstraintLayout`
- 内容滚动：`NestedScrollView`
- 操作按钮：`MaterialButton` 或 `ImageButton`
- 相关推荐：首版可用纵向 `LinearLayout` 或简单 `RecyclerView`

## 6.2 页面展示建议

首版详情页建议包含以下文本结构：

- 标题：醒目展示
- 元信息：来源 + 发布时间
- 摘要：强调重点内容
- 正文：使用多段文本模拟完整阅读体验
- 推荐阅读：展示 2~3 条相关新闻标题

## 6.3 交互建议

- 点击返回：回到首页
- 点击收藏：先做 UI 占位提示，目标 5 再接持久化
- 点击分享：首版可先预留 Toast / Snackbar
- 点击原文：首版可先提示“目标 8/扩展功能中接入”
- 点击相关推荐：首版可先预留二次跳转能力，但不强制实现

## 7. 数据模型定义

## 7.1 业务模型：NewsDetail

用途：

- 作为详情页完整展示的数据模型

建议定义：

```kotlin
data class NewsDetail(
    val id: String,
    val title: String,
    val summary: String,
    val coverImageUrl: String?,
    val source: String,
    val author: String?,
    val category: NewsCategory,
    val publishTime: String,
    val content: List<String>,
    val contentUrl: String?,
    val isCollected: Boolean = false,
    val relatedArticles: List<NewsArticle> = emptyList()
)
```

字段说明：

| 字段 | 类型 | 说明 |
|------|------|------|
| `id` | `String` | 新闻唯一标识 |
| `title` | `String` | 标题 |
| `summary` | `String` | 摘要 |
| `coverImageUrl` | `String?` | 封面图 |
| `source` | `String` | 来源 |
| `author` | `String?` | 作者 |
| `category` | `NewsCategory` | 分类 |
| `publishTime` | `String` | 发布时间 |
| `content` | `List<String>` | 正文段落集合 |
| `contentUrl` | `String?` | 原文地址 |
| `isCollected` | `Boolean` | 是否已收藏，先作为 UI 状态占位 |
| `relatedArticles` | `List<NewsArticle>` | 相关推荐 |

## 7.2 UI 状态模型：NewsDetailUiState

用途：

- 统一描述详情页当前状态

建议定义：

```kotlin
data class NewsDetailUiState(
    val isLoading: Boolean = false,
    val detail: NewsDetail? = null,
    val errorMessage: String? = null
)
```

字段说明：

| 字段 | 类型 | 说明 |
|------|------|------|
| `isLoading` | `Boolean` | 是否正在加载 |
| `detail` | `NewsDetail?` | 详情数据 |
| `errorMessage` | `String?` | 错误提示 |

## 7.3 DTO：NewsDetailDto

用途：

- 承接远端或 Mock 返回的详情数据

建议定义：

```kotlin
data class NewsDetailDto(
    val id: String?,
    val title: String?,
    val summary: String?,
    val coverImageUrl: String?,
    val source: String?,
    val author: String?,
    val category: String?,
    val publishTime: String?,
    val content: List<String>?,
    val contentUrl: String?,
    val relatedArticles: List<NewsArticleDto>?
)
```

## 8. 参数传递设计

## 8.1 推荐传递方式

建议首页进入详情页时只传递：

- `newsId`
- `newsTitle`（可选）

原因：

- `newsId` 是后续真实接口最稳定的获取详情依据
- 即使后续详情字段变多，也不会影响导航参数结构
- 避免直接传 `Parcelable` 业务对象导致额外实现成本

## 8.2 Navigation 设计建议

在 `nav_graph.xml` 中新增：

- `newsDetailFragment`
- `homeFragment -> newsDetailFragment` 的 action
- 参数：`newsId: string`

如需更好维护性，建议启用 `Safe Args`，但对于课程项目也可先使用 `bundleOf` 传值。

首选建议：

- 使用 `Safe Args`

原因：

- 类型安全
- 跳转代码更清晰
- 后续参数扩展更方便

## 9. 数据获取策略

## 9.1 推荐方案

详情页同样采用“两阶段策略”：

### 阶段 A：Mock 详情先跑通

- 根据 `newsId` 返回对应详情内容
- 保证页面结构和阅读体验先成立

### 阶段 B：切换真实接口

- 在 `RemoteNewsDataSource` 中补充 `getNewsDetail(newsId)` 方法
- `NewsRepository` 增加 `getNewsDetail(newsId)` 方法
- Fragment / ViewModel 不改结构

## 9.2 Repository 扩展建议

建议在 `NewsRepository` 中新增：

```kotlin
suspend fun getNewsDetail(newsId: String): ResultWrapper<NewsDetail>
```

同时在 `NewsDataSource` 中新增：

```kotlin
suspend fun getNewsDetail(newsId: String): ResultWrapper<NewsDetailDto>
```

说明：

- 详情页和列表页继续共用同一套数据源模式
- Mock 和 Remote 都必须实现该方法

## 10. 核心流程

### 10.1 用户从首页进入详情页

1. 用户在首页点击某条新闻
2. `HomeFragment` 通过 Navigation 跳转到 `NewsDetailFragment`
3. 跳转时传入 `newsId`
4. `NewsDetailFragment` 从参数中读取 `newsId`
5. `NewsDetailViewModel` 调用 `NewsRepository.getNewsDetail(newsId)`
6. Repository 从当前数据源获取详情 DTO
7. DTO 通过 Mapper 转换为 `NewsDetail`
8. Fragment 渲染标题、摘要、正文、相关推荐和操作区域

### 10.2 详情页加载失败

1. 请求失败
2. `NewsDetailViewModel` 更新 `errorMessage`
3. 页面展示错误态和重试按钮
4. 用户点击重试后再次请求

### 10.3 用户点击收藏 / 分享 / 原文

1. 用户点击操作按钮
2. 首版通过 Snackbar / Toast 给出反馈
3. 同时保留对应方法入口，供后续目标接入真实功能

## 11. 详细任务步骤

## 11.1 第一步：补充导航配置

任务目标：

- 在导航图中加入详情页 destination
- 从首页建立跳转 action

任务完成标准：

- 首页点击新闻时可跳转到详情页
- 参数可稳定传递到详情页

## 11.2 第二步：定义详情模型与状态

任务目标：

- 增加 `NewsDetail`、`NewsDetailUiState`、`NewsDetailDto`

任务完成标准：

- 详情页 UI 不依赖原始 DTO
- 列表模型与详情模型边界清晰

## 11.3 第三步：扩展数据源与仓库

任务目标：

- 在 `MockNewsDataSource` 中补充详情 Mock 数据
- 在 `RemoteNewsDataSource` 中预留详情方法
- 在 `NewsRepository` 中新增详情查询方法

任务完成标准：

- 详情数据获取同样支持 Mock / Remote 切换
- 切换到真实接口时不需要修改 Fragment 结构

## 11.4 第四步：实现详情页布局

任务目标：

- 搭建完整详情页展示结构

建议包含：

- 返回按钮
- 标题
- 来源 + 时间
- 封面图
- 摘要
- 正文
- 操作按钮区
- 相关推荐区
- 错误态 / 加载态

任务完成标准：

- 页面具备完整阅读体验
- 长内容可滚动

## 11.5 第五步：实现 NewsDetailViewModel

任务目标：

- 管理详情页加载、成功、失败状态

建议提供的方法：

```kotlin
fun loadDetail(newsId: String)
fun retry()
fun toggleCollect()
```

说明：

- `toggleCollect()` 首版可只改 UI 状态，不落库

任务完成标准：

- 详情页状态统一由 ViewModel 提供
- 加载与错误流程清晰

## 11.6 第六步：实现 Fragment 与数据绑定

任务目标：

- 将详情页布局与 ViewModel 状态绑定起来

任务完成标准：

- 页面能正常显示详情数据
- 错误态可重试
- 操作按钮有反馈

## 11.7 第七步：从首页接入真实导航

任务目标：

- 将首页点击从 Snackbar 提示改成真正跳转

任务完成标准：

- 首页点击任意新闻可进入对应详情页
- 不同新闻详情内容正确变化

## 11.8 第八步：自测与联调

建议检查项：

1. 首页点击新闻能否正常跳转
2. 不同新闻是否展示不同详情
3. 返回首页是否正常
4. 长正文是否可滚动
5. 错误态是否可见
6. 重试是否可生效
7. 操作按钮是否有反馈

## 12. 验收标准

### 12.1 功能验收

- 首页点击新闻后可进入详情页
- 详情页展示标题、来源、时间、封面图、摘要和正文
- 详情页支持返回上一页
- 详情页存在基础操作区：收藏、分享、原文入口
- 加载失败时有错误提示并可重试

### 12.2 结构验收

- 已形成 `HomeFragment -> Navigation -> NewsDetailFragment` 路径
- 详情页状态统一由 `NewsDetailUiState` 管理
- 详情模型与列表模型职责清晰区分
- 详情数据同样支持 Mock / Remote 切换

### 12.3 代码质量验收

- 详情页 Fragment 不直接获取数据
- 导航参数清晰且最小化
- 后续收藏、历史、原文跳转均有明确扩展入口

### 12.4 可演示性验收

- 页面具备明显的“阅读详情页”体验
- 与首页形成完整闭环：首页 -> 详情 -> 返回首页

## 13. 风险与缓解措施

| 风险 | 概率 | 影响 | 缓解措施 |
|------|------|------|---------|
| 详情参数传递过重 | 中 | 中 | 只传 `newsId`，其余通过 repository 获取 |
| 真实详情接口尚未确定 | 高 | 中 | 先在 Mock 数据源中按 `newsId` 返回详情 |
| 页面内容过长导致布局混乱 | 中 | 中 | 使用 `NestedScrollView` 并拆分段落展示 |
| 首页与详情模型混用导致职责模糊 | 中 | 中 | 单独定义 `NewsDetail` 模型 |
| 相关推荐逻辑过早复杂化 | 低 | 中 | 首版只展示静态 2~3 条推荐文章 |

## 14. 预估排期

以单人课程项目为前提，目标 3 预计耗时如下：

| 子任务 | 预估耗时 |
|------|------|
| 导航配置与参数传递 | 0.5 天 |
| 详情模型、DTO、Mapper、Repository 扩展 | 0.5 天 |
| 详情页布局开发 | 0.5 天 |
| ViewModel 与 Fragment 绑定 | 0.5 天 |
| 首页点击接入与联调 | 0.5 天 |
| 自测与调整 | 0.5 天 |
| 合计 | 3 天左右 |

## 15. 建议执行顺序

建议严格按下面顺序实施：

1. 补导航图和参数
2. 定义详情模型与 UI 状态
3. 扩展 Mock / Remote 数据源
4. 扩展 `NewsRepository`
5. 实现详情页布局
6. 实现 `NewsDetailViewModel`
7. 接首页点击跳转
8. 完成错误态与重试逻辑
9. 自测与准备进入目标 4 / 目标 5

## 16. 进入执行前待确认项

以下问题不影响你 review 方案，但会影响实现细节：

- 详情页参数是否采用 `Safe Args`
- 相关推荐首版是否直接展示 2~3 条静态推荐
- 收藏按钮首版是否只做 UI 状态切换，不做持久化
- 原文按钮首版是提示占位，还是直接外跳浏览器

## 17. 下一阶段衔接

目标 3 完成后，可以直接衔接：

- `目标 4：搜索功能`
- `目标 5：收藏与历史记录`

届时可以直接复用：

- `NewsArticle`
- `NewsDetail`
- 已建立的导航能力
- 统一的数据源切换结构
