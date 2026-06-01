# 目标 3 下一步实施方案：详情页 Fragment、ViewModel 与 Mock 数据绑定

## 1. 本次实施范围

本轮只实现详情页从“静态布局”升级到“可展示 Mock 详情数据的完整页面”，范围控制如下：

- 新增 `NewsDetailFragment`
- 新增 `NewsDetailViewModel`
- 新增 `NewsDetailUiState`
- 新增 `NewsDetail` 与 `NewsDetailDto`
- 扩展 `NewsDataSource`、`MockNewsDataSource`、`RemoteNewsDataSource`
- 扩展 `NewsRepository` 与 `NewsMapper`
- 导航图新增详情页 destination
- 首页点击新闻卡片后跳转到详情页

本轮明确不做：

- Room 收藏持久化
- 分享系统接入
- 原文跳浏览器 / WebView
- 相关推荐二次点击跳转
- 真实详情网络接口接入

## 2. 当前代码基础

当前项目已经具备以下基础：

- 首页新闻流已完成，点击新闻入口位于 [HomeFragment.kt](file:///Users/bytedance/AndroidStudioProjects/Homework/app/src/main/java/com/example/homework/ui/home/HomeFragment.kt)
- 详情页静态布局已完成，文件为 [fragment_news_detail.xml](file:///Users/bytedance/AndroidStudioProjects/Homework/app/src/main/res/layout/fragment_news_detail.xml)
- 列表业务模型已存在，文件为 [NewsArticle.kt](file:///Users/bytedance/AndroidStudioProjects/Homework/app/src/main/java/com/example/homework/model/NewsArticle.kt)
- 数据源结构已支持 `Mock / Remote` 可切换，入口为 [NewsDataSource.kt](file:///Users/bytedance/AndroidStudioProjects/Homework/app/src/main/java/com/example/homework/data/remote/datasource/NewsDataSource.kt) 和 [NewsRepository.kt](file:///Users/bytedance/AndroidStudioProjects/Homework/app/src/main/java/com/example/homework/data/repository/NewsRepository.kt)

因此本次最合适的推进方式，不是再重做结构，而是在现有架构上补齐详情页链路。

## 3. 实施目标

完成后应达到以下效果：

1. 首页点击任意新闻卡片后进入详情页
2. 详情页根据 `newsId` 加载对应的 Mock 详情数据
3. 页面展示标题、来源、时间、分类、封面、摘要、正文和相关推荐
4. 详情页有加载态、错误态、重试能力
5. 收藏、分享、原文按钮先做交互占位
6. 后续切换真实网络请求时，只需替换数据源实现

## 4. 技术方案

## 4.1 导航参数方案

本次建议采用“只传 `newsId`”方案。

### 传递参数

- `newsId: String`

### 原因

- 与真实接口设计一致，后续最稳定
- 避免把整个 `NewsArticle` 做 `Parcelable`
- 减少首页和详情页耦合

### 实现方式

- 在 `nav_graph.xml` 中新增 `newsDetailFragment`
- 在 `homeFragment` 下新增 action
- 用 `Bundle` 或 `bundleOf("newsId" to article.id)` 传递

本次实现建议：

- 先使用 `bundleOf`

原因：

- 实现轻量
- 当前项目尚未接入 `Safe Args`
- 更适合快速完成课程大作业阶段目标

## 4.2 数据模型方案

### 业务模型：NewsDetail

建议新增：

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

### UI 状态模型：NewsDetailUiState

建议新增：

```kotlin
data class NewsDetailUiState(
    val isLoading: Boolean = false,
    val detail: NewsDetail? = null,
    val errorMessage: String? = null
)
```

### DTO：NewsDetailDto

建议新增：

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

## 4.3 数据层扩展方案

### DataSource 接口扩展

在 `NewsDataSource` 中新增：

```kotlin
suspend fun getNewsDetail(newsId: String): ResultWrapper<NewsDetailDto>
```

### Mock 数据源扩展

在 `MockNewsDataSource` 中：

- 根据 `newsId` 返回对应详情
- 详情数据与首页现有新闻标题、摘要保持一致
- 正文使用 3 段文本模拟真实阅读内容
- 相关推荐返回 2 条 `NewsArticleDto`

### Remote 数据源扩展

在 `RemoteNewsDataSource` 中：

- 新增同名方法
- 先返回“尚未接入真实详情接口”的错误提示

### Repository 扩展

在 `NewsRepository` 中新增：

```kotlin
suspend fun getNewsDetail(newsId: String): ResultWrapper<NewsDetail>
```

并在内部：

- 调用数据源
- DTO -> `NewsDetail` 映射
- 统一处理错误

### Mapper 扩展

在 `NewsMapper` 中新增：

```kotlin
fun toNewsDetail(dto: NewsDetailDto): NewsDetail
```

## 4.4 ViewModel 方案

新增 `NewsDetailViewModel`

建议职责：

- 读取详情页状态
- 拉取详情数据
- 管理重试
- 管理收藏按钮的 UI 切换

建议方法：

```kotlin
fun loadDetail(newsId: String)
fun retry()
fun toggleCollect()
```

说明：

- `toggleCollect()` 本次只切换 `isCollected`，不落库
- `retry()` 使用上一次 `newsId` 再次请求

## 4.5 Fragment 方案

新增 `NewsDetailFragment`

建议职责：

- 从参数读取 `newsId`
- 初始化按钮事件
- 观察 `uiState`
- 将状态渲染到布局控件中

建议绑定内容：

- 标题
- 来源
- 发布时间
- 分类
- 封面图
- 摘要
- 3 段正文
- 相关推荐 2 条
- 加载态 / 错误态

建议按钮行为：

- 返回按钮：`findNavController().navigateUp()`
- 收藏按钮：切换按钮文案或状态，并提示“收藏功能将在目标 5 完成”
- 分享按钮：提示“分享功能后续接入”
- 原文按钮：提示“原文跳转后续接入”
- 重试按钮：调用 `viewModel.retry()`

## 4.6 首页接入方案

当前首页点击事件仍是 Snackbar 占位：

- [HomeFragment.kt](file:///Users/bytedance/AndroidStudioProjects/Homework/app/src/main/java/com/example/homework/ui/home/HomeFragment.kt)

本次需要改为：

```kotlin
findNavController().navigate(
    R.id.action_homeFragment_to_newsDetailFragment,
    bundleOf("newsId" to article.id)
)
```

这样首页和详情页将形成完整链路。

## 5. 详细任务步骤

## 5.1 第一步：补充详情数据模型

输出：

- `NewsDetail.kt`
- `NewsDetailUiState.kt`
- `NewsDetailDto.kt`

完成标准：

- 详情页有独立业务模型和状态模型
- 不复用首页 `HomeUiState`

## 5.2 第二步：扩展数据层接口

输出：

- `NewsDataSource` 新增 `getNewsDetail()`
- `NewsRepository` 新增 `getNewsDetail()`
- `NewsMapper` 新增 `toNewsDetail()`

完成标准：

- 详情数据查询链路与首页保持一致

## 5.3 第三步：补充 Mock 详情数据

输出：

- `MockNewsDataSource` 中为 `r1/r2/r3/t1...` 等新闻 ID 构造详情

完成标准：

- 不同新闻进入详情页时，内容不同
- 相关推荐可展示

## 5.4 第四步：创建 ViewModel

输出：

- `NewsDetailViewModel.kt`

完成标准：

- 有加载、成功、失败、重试、收藏 UI 状态管理

## 5.5 第五步：创建 Fragment 并绑定布局

输出：

- `NewsDetailFragment.kt`

完成标准：

- 可从参数读取 `newsId`
- 可渲染完整详情
- 可显示错误态和加载态

## 5.6 第六步：补导航图

输出：

- `nav_graph.xml` 新增详情页和首页跳转 action

完成标准：

- 首页点击新闻后可跳转到详情页

## 5.7 第七步：联调与自测

重点检查：

1. 首页点击是否跳转成功
2. 不同新闻详情是否不同
3. 返回键是否正常
4. 加载态是否出现
5. 错误态是否可以手动验证
6. 收藏按钮是否仅切换 UI 状态

## 6. 验收标准

### 功能验收

- 首页点击新闻后可正常进入详情页
- 详情页能展示标题、来源、时间、分类、封面、摘要、正文
- 详情页能展示 2 条相关推荐
- 重试按钮可用
- 返回按钮可用

### 结构验收

- 详情页仍然遵守 `Mock / Remote` 可切换原则
- 详情数据入口统一走 `Repository`
- Fragment 不直接拼装详情数据

### 可扩展性验收

- 后续切到真实详情接口时，只改 `RemoteNewsDataSource`
- 后续做收藏持久化时，只扩展 `toggleCollect()` 背后的实现
- 后续做原文跳转时，只补 `originButton` 的点击逻辑

## 7. 风险与规避

| 风险 | 概率 | 影响 | 缓解措施 |
|------|------|------|---------|
| 首页和详情页模型职责混乱 | 中 | 中 | 单独引入 `NewsDetail` |
| 详情参数传递过多 | 中 | 中 | 只传 `newsId` |
| Mock 详情与首页摘要不一致 | 中 | 中 | 基于现有新闻 ID 和标题构造对应详情 |
| 布局字段较多，绑定容易漏 | 中 | 中 | 先按区域分组渲染：头部、摘要、正文、相关推荐、状态区 |

## 8. 建议实现顺序

建议严格按下面顺序执行：

1. 新增模型
2. 扩展 `NewsDataSource`
3. 扩展 `MockNewsDataSource`
4. 扩展 `NewsRepository` 和 `NewsMapper`
5. 新建 `NewsDetailViewModel`
6. 新建 `NewsDetailFragment`
7. 补导航图
8. 修改首页点击逻辑
9. 构建验证与自测

## 9. 本次执行后的预期成果

本轮完成后，课程作业将形成第一条完整内容闭环：

- 首页加载新闻
- 点击新闻进入详情
- 查看完整内容
- 返回首页

这会让 APP 从“列表展示”升级为“基础内容阅读产品”，对课程答辩展示价值很高。
