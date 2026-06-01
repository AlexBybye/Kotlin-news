# 目标 2 详细开发计划：首页新闻列表

## 1. 目标说明

本阶段目标是在现有项目骨架基础上，完成新闻 APP 首页的核心内容区，实现“可展示、可刷新、可切换分类”的新闻列表页面，为后续详情页、搜索、收藏与缓存能力打下数据流基础。

本阶段重点覆盖以下能力：

- `RecyclerView` 新闻列表渲染
- 首页分类切换
- `Retrofit + OkHttp` 网络请求基础接入
- `ViewModel` 管理列表状态
- `SwipeRefreshLayout` 下拉刷新
- 加载态、空态、错误态展示
- `Repository` + `RemoteDataSource` 初步分层

本阶段不包含：

- 新闻详情页跳转落地
- Room 本地数据库缓存
- 分页加载 `Paging 3`
- 收藏、历史、搜索能力
- Banner 轮播的正式实现

## 2. 当前现状分析

当前目标 1 已完成骨架搭建，首页现状如下：

- [HomeFragment.kt](file:///Users/bytedance/AndroidStudioProjects/Homework/app/src/main/java/com/example/homework/ui/home/HomeFragment.kt) 仍是简单占位页，尚未包含列表组件。
- [HomeViewModel.kt](file:///Users/bytedance/AndroidStudioProjects/Homework/app/src/main/java/com/example/homework/ui/home/HomeViewModel.kt) 仅提供占位文案，尚未管理真实新闻数据。
- 项目尚未接入 `RecyclerView`、`SwipeRefreshLayout`、`Retrofit`、`OkHttp`。
- 项目尚未建立 `data/remote`、`repository`、`common/result` 等数据层结构。

因此，目标 2 的本质是先跑通“首页 UI -> ViewModel -> Repository -> RemoteDataSource”这条最核心的数据链路。

## 3. 设计原则

### 3.1 先保证可演示，再保证可扩展

- 第一优先级是首页能真实显示新闻列表。
- 第二优先级是结构清晰，便于目标 3 的详情页直接接入。

### 3.2 优先支持 Mock 与真实接口双模式

- [假设] 课程大作业阶段可能存在接口不稳定、配额限制或网络不可用情况。
- 因此建议支持 `Mock 数据优先可跑通 + Retrofit 接口可切换` 的方案。

### 3.3 列表状态必须统一管理

- 不在 Fragment 中散落处理加载、成功、失败、空数据逻辑。
- 所有首页展示状态统一交给 `HomeViewModel` 暴露。

## 4. 本阶段产出物

完成后，项目应新增以下产出：

- 一个首页新闻流布局 `fragment_home.xml` 的正式版本
- 一个新闻列表项布局 `item_news_article.xml`
- 一个首页分类区域
- 一个 `RecyclerView.Adapter`
- 一套首页 UI 状态模型
- 一套新闻文章领域模型 / 接口模型
- 一个首页数据仓库 `NewsRepository`
- 一个网络数据源 `NewsRemoteDataSource`
- 一个 `Retrofit` 服务定义 `NewsApiService`
- 基础网络配置类：`OkHttpClient`、日志拦截器、超时、统一返回处理

## 5. 推荐包结构

建议在目标 2 开始补齐数据层目录：

```text
com.example.homework
├── data
│   ├── remote
│   │   ├── api
│   │   │   └── NewsApiService.kt
│   │   ├── datasource
│   │   │   └── NewsRemoteDataSource.kt
│   │   ├── dto
│   │   │   ├── NewsArticleDto.kt
│   │   │   └── NewsListResponseDto.kt
│   │   └── network
│   │       ├── NetworkModule.kt
│   │       └── ResultWrapper.kt
│   ├── repository
│   │   └── NewsRepository.kt
│   └── mapper
│       └── NewsMapper.kt
├── ui
│   └── home
│       ├── HomeFragment.kt
│       ├── HomeViewModel.kt
│       ├── NewsAdapter.kt
│       ├── HomeUiState.kt
│       └── NewsCategory.kt
└── model
    └── NewsArticle.kt
```

说明：

- `dto` 负责承接接口结构。
- `model` 放页面可直接使用的业务模型。
- `mapper` 负责 DTO 到业务模型转换。
- `ResultWrapper` 用于统一成功、失败和异常状态。

## 6. 首页 UI 方案

## 6.1 页面结构

首页建议调整为以下布局结构：

1. 顶部标题栏
2. 搜索入口占位区
3. 分类横向区域
4. 下拉刷新容器
5. 新闻列表
6. 空态 / 错误态占位视图

建议布局组合：

- 根布局：`ConstraintLayout`
- 刷新：`SwipeRefreshLayout`
- 列表：`RecyclerView`
- 分类：`ChipGroup` 或简单横向 `LinearLayout`

## 6.2 首页交互

- 默认进入“推荐”分类
- 点击不同分类时刷新对应列表
- 下拉刷新时重新请求当前分类
- 请求失败时展示错误提示与重试入口
- 数据为空时展示空态提示
- 点击新闻卡片时先预留回调，目标 3 再接详情页

## 6.3 分类建议

首版建议先固定 5 个分类：

- 推荐
- 科技
- 体育
- 校园
- 国际

原因：

- 能满足老师对“分类浏览”的基本预期
- 数量适中，便于演示和维护

## 7. 数据模型定义

## 7.1 业务模型：新闻文章

用途：

- 作为首页列表展示的核心模型
- 后续详情页、收藏、历史都可复用

建议定义：

```kotlin
data class NewsArticle(
    val id: String,
    val title: String,
    val summary: String,
    val coverImageUrl: String?,
    val author: String?,
    val source: String,
    val category: NewsCategory,
    val publishTime: String,
    val contentUrl: String?,
    val isTop: Boolean = false
)
```

字段说明：

| 字段 | 类型 | 说明 |
|------|------|------|
| `id` | `String` | 新闻唯一标识 |
| `title` | `String` | 标题 |
| `summary` | `String` | 摘要 |
| `coverImageUrl` | `String?` | 封面图地址 |
| `author` | `String?` | 作者，可为空 |
| `source` | `String` | 来源 |
| `category` | `NewsCategory` | 新闻分类 |
| `publishTime` | `String` | 发布时间 |
| `contentUrl` | `String?` | 原文地址或详情地址 |
| `isTop` | `Boolean` | 是否置顶 |

## 7.2 分类模型：NewsCategory

用途：

- 用于分类切换与请求参数映射

建议定义：

```kotlin
enum class NewsCategory(val displayName: String, val apiValue: String) {
    RECOMMEND("推荐", "recommend"),
    TECHNOLOGY("科技", "technology"),
    SPORTS("体育", "sports"),
    CAMPUS("校园", "campus"),
    INTERNATIONAL("国际", "international")
}
```

字段说明：

- `displayName`：界面展示名称
- `apiValue`：接口请求参数值

## 7.3 首页 UI 状态模型：HomeUiState

用途：

- 统一管理首页加载态、列表态、错误态

建议定义：

```kotlin
data class HomeUiState(
    val selectedCategory: NewsCategory = NewsCategory.RECOMMEND,
    val categoryList: List<NewsCategory> = NewsCategory.entries,
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val articles: List<NewsArticle> = emptyList(),
    val errorMessage: String? = null,
    val isEmpty: Boolean = false
)
```

字段说明：

| 字段 | 类型 | 说明 |
|------|------|------|
| `selectedCategory` | `NewsCategory` | 当前选中分类 |
| `categoryList` | `List<NewsCategory>` | 分类集合 |
| `isLoading` | `Boolean` | 首次加载中 |
| `isRefreshing` | `Boolean` | 下拉刷新中 |
| `articles` | `List<NewsArticle>` | 当前列表数据 |
| `errorMessage` | `String?` | 错误提示 |
| `isEmpty` | `Boolean` | 是否为空数据 |

## 7.4 接口 DTO：NewsArticleDto

用途：

- 承接服务端返回的原始新闻数据

建议定义：

```kotlin
data class NewsArticleDto(
    val id: String?,
    val title: String?,
    val summary: String?,
    val coverImageUrl: String?,
    val author: String?,
    val source: String?,
    val category: String?,
    val publishTime: String?,
    val contentUrl: String?
)
```

说明：

- DTO 层字段可空，映射到业务模型时统一兜底。

## 7.5 列表响应 DTO：NewsListResponseDto

建议定义：

```kotlin
data class NewsListResponseDto(
    val code: Int,
    val message: String?,
    val data: List<NewsArticleDto>?
)
```

## 8. 接口设计

## 8.1 推荐方案

建议采用两阶段策略：

### 阶段 A：Mock 数据先跑通

- 优先确保 UI、状态管理、Adapter、刷新链路可运行
- 若真实接口延迟确定，不影响课堂进度

### 阶段 B：接入真实接口

- 用 `Retrofit` 替换 `MockRepository` 的数据来源
- UI 层不改动，尽量只修改 repository / datasource

## 8.2 接口抽象建议

如果使用真实接口，可按以下抽象设计：

- **接口名称**：获取新闻列表
- **请求方式**：`GET`
- **路径**：`/news/list`

请求参数建议：

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `category` | `String` | 是 | 分类值 |
| `page` | `Int` | 否 | 页码，首版可固定为 1 |
| `pageSize` | `Int` | 否 | 每页条数，首版可固定为 10 |

返回结构建议：

```json
{
  "code": 0,
  "message": "success",
  "data": [
    {
      "id": "1001",
      "title": "校园科技节正式启动",
      "summary": "本周校园科技节开幕，多项创新作品集中亮相。",
      "coverImageUrl": "https://example.com/news-1001.jpg",
      "author": "校新闻中心",
      "source": "校园新闻",
      "category": "campus",
      "publishTime": "2026-05-28 10:00",
      "contentUrl": "https://example.com/news/1001"
    }
  ]
}
```

错误码建议：

| 错误场景 | 表现 |
|------|------|
| 网络不可用 | 提示“网络连接失败，请稍后重试” |
| 服务器异常 | 提示“服务暂时不可用” |
| 空数据 | 展示空态视图 |

## 9. 核心流程

### 9.1 首次进入首页

1. 用户打开应用并进入 `HomeFragment`
2. `HomeFragment` 订阅 `HomeViewModel.uiState`
3. `HomeViewModel` 默认请求“推荐”分类数据
4. `NewsRepository` 调用 `NewsRemoteDataSource`
5. `NewsRemoteDataSource` 通过 `Retrofit` 获取数据，或先返回 Mock 数据
6. DTO 通过 `Mapper` 转换为 `NewsArticle`
7. `HomeViewModel` 更新 `HomeUiState`
8. Fragment 刷新分类选中态、新闻列表和加载状态

### 9.2 用户切换分类

1. 用户点击分类标签
2. Fragment 调用 `viewModel.onCategorySelected(category)`
3. ViewModel 更新选中分类并重新发起请求
4. 列表刷新为新分类数据
5. 若失败则展示错误态和重试入口

### 9.3 用户下拉刷新

1. 用户下拉触发刷新
2. Fragment 调用 `viewModel.refresh()`
3. ViewModel 以当前分类重新请求数据
4. 刷新成功则更新列表
5. 刷新失败则停止刷新动画并提示错误

## 10. 详细任务步骤

## 10.1 第一步：补充依赖

建议新增依赖：

- `androidx.recyclerview:recyclerview`
- `androidx.swiperefreshlayout:swiperefreshlayout`
- `com.squareup.retrofit2:retrofit`
- `com.squareup.retrofit2:converter-gson`
- `com.squareup.okhttp3:okhttp`
- `com.squareup.okhttp3:logging-interceptor`
- 图片库建议二选一：`Coil` 或 `Glide`

建议：

- 首版优先选 `Coil`，接入更轻量，适合 Kotlin 项目

任务完成标准：

- 依赖可同步成功
- 列表、刷新、网络、图片加载所需类可正常导入

## 10.2 第二步：重构首页布局

任务目标：

- 将首页从占位页升级为真实新闻流容器

建议输出：

- 顶部标题与搜索占位区域
- 分类选择区
- `SwipeRefreshLayout`
- `RecyclerView`
- 空态 / 错误态占位区域

任务完成标准：

- 首页结构完整
- 分类区和列表区清晰可见
- 不依赖真实数据也能预览页面框架

## 10.3 第三步：实现新闻列表项布局

任务目标：

- 定义新闻卡片视觉结构

建议包含：

- 标题
- 摘要
- 来源
- 发布时间
- 封面图
- 可选置顶标识

任务完成标准：

- 单个列表项信息层级清晰
- 有图和无图两种情况都能正常显示

## 10.4 第四步：定义模型与状态

任务目标：

- 补齐 `NewsArticle`、`NewsCategory`、`HomeUiState`、DTO、Mapper

任务完成标准：

- UI 层不直接依赖接口原始字段
- 状态模型能完整表达首页当前界面状态

## 10.5 第五步：接入网络层

任务目标：

- 建立 `Retrofit + OkHttp` 基础网络设施

建议内容：

- `baseUrl`
- 超时设置
- 日志拦截器
- Gson 转换器
- `NewsApiService`

说明：

- 若真实接口未最终确定，可先预留接口签名，并在 repository 中切 Mock 数据

任务完成标准：

- 网络层结构可运行
- repository 能从远端或 mock 获取数据

## 10.6 第六步：实现 Repository 与 DataSource

任务目标：

- 形成清晰的数据获取入口

建议职责：

- `NewsRemoteDataSource`：只负责请求
- `NewsRepository`：负责调用、映射、错误封装

任务完成标准：

- ViewModel 只依赖 repository
- Fragment 不直接请求网络

## 10.7 第七步：重构 HomeViewModel

任务目标：

- 从“占位文案 ViewModel”升级为“首页列表状态 ViewModel”

建议提供的方法：

```kotlin
fun loadNews(category: NewsCategory = NewsCategory.RECOMMEND)
fun onCategorySelected(category: NewsCategory)
fun refresh()
fun retry()
```

任务完成标准：

- 首页所有状态都从 `HomeUiState` 派生
- 不同操作下状态切换正确

## 10.8 第八步：实现 Adapter 与 UI 绑定

任务目标：

- 用 `RecyclerView.Adapter` 展示新闻数据

建议内容：

- `NewsAdapter`
- `DiffUtil`
- 点击事件回调接口

任务完成标准：

- 列表可展示多条新闻
- 数据更新时列表刷新稳定
- 点击事件可向上抛出

## 10.9 第九步：接入刷新、错误态与空态

任务目标：

- 完善用户体验与课堂演示效果

建议行为：

- 首次加载显示 loading
- 下拉时显示刷新动画
- 请求失败显示错误提示与重试
- 无数据时显示空态

任务完成标准：

- 用户能明确知道当前处于什么状态
- 所有状态切换都有可见反馈

## 10.10 第十步：自测与联调

建议检查项：

1. 默认分类是否正确
2. 分类切换是否正常
3. 刷新是否生效
4. 错误态是否可见
5. 空态是否可见
6. 旋转屏幕后列表状态是否稳定
7. 列表点击事件是否可扩展到详情页

## 11. 验收标准

### 11.1 功能验收

- 首页默认展示“推荐”分类新闻
- 用户可切换至少 5 个新闻分类
- 切换分类后列表内容会更新
- 支持下拉刷新当前分类
- 加载失败时可提示并支持重试

### 11.2 结构验收

- 已形成 `Fragment -> ViewModel -> Repository -> RemoteDataSource` 调用链
- 首页 UI 状态统一由 `HomeUiState` 描述
- DTO、业务模型、UI 状态模型边界清晰

### 11.3 代码质量验收

- Fragment 不直接发起网络请求
- Fragment 不手动散落维护多个布尔状态
- Adapter、ViewModel、Repository 职责分离清楚

### 11.4 可演示性验收

- 即使真实接口不稳定，也能通过 Mock 数据展示首页能力
- 界面上能明显看出分类切换、下拉刷新、加载态和错误态

## 12. 风险与缓解措施

| 风险 | 概率 | 影响 | 缓解措施 |
|------|------|------|---------|
| 真实新闻接口暂未确定 | 高 | 中 | 先用 MockRepository 保证 UI 和链路先跑通 |
| 接口字段不稳定 | 中 | 中 | 通过 DTO + Mapper 隔离接口波动 |
| 首页布局一次性做太复杂 | 中 | 中 | 先完成标题、分类、列表、状态四大块 |
| 刷新与分类切换状态混乱 | 中 | 高 | 用单一 `HomeUiState` 统一收口 |
| 图片加载导致列表抖动 | 低 | 中 | 使用稳定图片库，并预留占位图 |

## 13. 预估排期

以单人课程项目为前提，目标 2 预计耗时如下：

| 子任务 | 预估耗时 |
|------|------|
| 依赖补充与网络层基础设施 | 0.5 天 |
| 首页布局与列表项布局 | 0.5 天 |
| 数据模型、DTO、Mapper | 0.5 天 |
| Repository、DataSource、ViewModel | 0.5 天 |
| Adapter、分类切换、刷新与状态处理 | 1 天 |
| 自测与调整 | 0.5 天 |
| 合计 | 3.5 天左右 |

## 14. 建议执行顺序

建议严格按下面顺序实施：

1. 补依赖
2. 改首页布局
3. 实现列表项布局
4. 定义模型与状态
5. 接入 Mock 数据
6. 跑通 Adapter 与分类切换
7. 接入 Retrofit/OkHttp
8. 完善错误态与刷新逻辑
9. 自测并准备进入目标 3

## 15. 进入执行前待确认项

以下问题不影响先 review 方案，但会影响具体实现细节：

- 是否优先采用 `Mock 数据先跑通，再接真实接口`
- 首页分类区域使用 `ChipGroup` 还是普通横向按钮
- 图片加载库选 `Coil` 还是 `Glide`
- 当前阶段是否需要先预留“点击新闻进入详情”的导航 action

## 16. 下一阶段衔接

目标 2 完成后，可无缝进入 `目标 3：新闻详情页`，届时将直接复用：

- `NewsArticle` 业务模型
- 首页列表点击事件
- `Navigation Component` 跳转能力
- 已建立的网络层与 repository
