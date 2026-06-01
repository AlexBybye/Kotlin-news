# 目标 3 下一步技术方案：首页到详情页导航联调 Tech Spec

## 1. 背景与目标

### 1.1 背景

当前新闻 APP 已经完成以下能力：

- 首页新闻流列表已可展示 Mock 数据
- 详情页布局已完成
- 详情页 `Fragment`、`ViewModel`、`Repository`、`Mock / Remote` 数据链路已完成

但当前仍缺少关键的页面闭环：

- 首页点击新闻卡片后，仍然只是 Snackbar 提示
- `nav_graph.xml` 中尚未注册详情页 destination
- 首页与详情页尚未通过 `newsId` 正式串联

这会导致 APP 目前仍停留在“列表展示”阶段，不能形成“浏览新闻 -> 查看详情 -> 返回列表”的完整阅读链路。

### 1.2 现状分析

当前项目架构如下：

- 首页入口在 [HomeFragment.kt](file:///Users/bytedance/AndroidStudioProjects/Homework/app/src/main/java/com/example/homework/ui/home/HomeFragment.kt)
- 详情页页面逻辑在 [NewsDetailFragment.kt](file:///Users/bytedance/AndroidStudioProjects/Homework/app/src/main/java/com/example/homework/ui/detail/NewsDetailFragment.kt)
- 导航图在 [nav_graph.xml](file:///Users/bytedance/AndroidStudioProjects/Homework/app/src/main/res/navigation/nav_graph.xml)
- 详情数据获取入口在 [NewsRepository.kt](file:///Users/bytedance/AndroidStudioProjects/Homework/app/src/main/java/com/example/homework/data/repository/NewsRepository.kt)

当前缺口主要有三类：

1. 路由缺口：缺少首页到详情页的 `action`
2. 参数缺口：点击新闻后还没有把 `newsId` 传入详情页
3. 联调缺口：未验证 `HomeFragment -> NavController -> NewsDetailFragment -> ViewModel -> Repository` 的完整链路

### 1.3 目标

本次核心目标：

- 打通首页点击新闻进入详情页
- 使用 `newsId` 作为唯一参数完成页面跳转
- 验证详情页能够根据不同新闻展示不同内容
- 验证返回栈、加载态、错误态在真实导航场景下工作正常

本次非目标：

- 不引入 `Safe Args`
- 不实现收藏持久化
- 不实现相关推荐二次跳转
- 不接入真实详情网络接口

### 1.4 术语表

| 术语 | 含义 |
|------|------|
| `newsId` | 新闻唯一标识，用于首页到详情页的数据定位 |
| Destination | Navigation 中的页面节点 |
| Action | Navigation 中页面之间的跳转关系 |
| Mock 数据源 | 当前用于演示的本地模拟数据实现 |
| Remote 数据源 | 后续接入真实网络接口的数据源实现 |

## 2. 技术方案

### 2.1 整体架构

本次链路维持现有分层，不新增复杂抽象。

完整流程如下：

1. 用户在 `HomeFragment` 点击新闻卡片
2. `HomeFragment` 调用 `findNavController().navigate(...)`
3. `NavController` 打开 `NewsDetailFragment`
4. `NewsDetailFragment` 从 `arguments` 中读取 `newsId`
5. `NewsDetailViewModel` 根据 `newsId` 调用 `NewsRepository.getNewsDetail(newsId)`
6. `NewsRepository` 调用当前启用的数据源
7. 当前默认走 `MockNewsDataSource`
8. `MockNewsDataSource` 返回 `NewsDetailDto`
9. `NewsMapper` 将 DTO 转为 `NewsDetail`
10. `NewsDetailFragment` 渲染 UI

该方案的关键点是：导航层只传 `newsId`，业务数据统一仍由 `Repository` 拉取，确保后续切网络时不影响 UI 层。

### 2.2 模块拆分

| 模块 | 职责 | 输入 | 输出 | 依赖 |
|------|------|------|------|------|
| `HomeFragment` | 处理新闻点击与页面跳转 | `NewsArticle.id` | 导航动作 | `NavController` |
| `nav_graph.xml` | 注册详情页与跳转关系 | destination 配置 | 页面路由 | Navigation Component |
| `NewsDetailFragment` | 读取参数并渲染详情页 | `newsId` | UI 展示 | `NewsDetailViewModel` |
| `NewsDetailViewModel` | 管理详情状态与重试 | `newsId` | `NewsDetailUiState` | `NewsRepository` |
| `NewsRepository` | 统一详情查询入口 | `newsId` | `ResultWrapper<NewsDetail>` | `NewsDataSource` |
| `MockNewsDataSource` | 返回 Mock 详情数据 | `newsId` | `NewsDetailDto` | 本地模拟数据 |

### 2.3 接口设计

本次不新增 HTTP API，主要新增的是页面间导航接口和内部仓库接口。

#### 接口 1：首页到详情页导航参数

- **接口名称**：`action_homeFragment_to_newsDetailFragment`
- **类型**：Navigation Action

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `newsId` | `String` | 是 | 目标新闻 ID |

建议调用方式：

```kotlin
findNavController().navigate(
    R.id.action_homeFragment_to_newsDetailFragment,
    bundleOf(NewsDetailFragment.ARG_NEWS_ID to article.id)
)
```

#### 接口 2：详情查询仓库接口

- **接口名称**：`getNewsDetail(newsId: String)`
- **类型**：Repository 内部接口

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `newsId` | `String` | 是 | 新闻唯一标识 |

返回结构：

```kotlin
ResultWrapper.Success(NewsDetail)
ResultWrapper.Error(message)
```

#### 错误场景

| 场景 | 表现 | 处理方式 |
|------|------|---------|
| 首页点击时 `id` 为空 | 不应跳转 | 弹出提示并中止 |
| 详情页未收到参数 | 展示错误态 | 显示“未获取到新闻标识” |
| 数据源无此新闻 | 展示错误态 | 支持点击重试 |
| 后续切到 Remote 未实现接口 | 展示错误态 | 由 `RemoteNewsDataSource` 返回占位错误 |

### 2.4 数据结构

本次不新增核心模型，但会明确导航与联调中依赖的结构。

#### 页面参数

```kotlin
const val ARG_NEWS_ID = "newsId"
```

#### 首页列表模型

```kotlin
data class NewsArticle(
    val id: String,
    val title: String,
    ...
)
```

约束：

- `id` 必须非空
- 后续所有详情查询都依赖此字段

#### 详情状态模型

```kotlin
data class NewsDetailUiState(
    val isLoading: Boolean = false,
    val detail: NewsDetail? = null,
    val errorMessage: String? = null
)
```

状态约束：

- `isLoading = true` 时，优先展示加载态
- `detail != null` 时，展示正文内容
- `detail == null && errorMessage != null` 时，展示错误态

### 2.5 核心流程

#### 正常流程

1. 用户在首页点击新闻卡片，触发 `NewsAdapter` 回调
2. `HomeFragment` 读取 `article.id`
3. `HomeFragment` 调用 `navigate(action, bundleOf(newsId))`
4. `NavController` 创建并打开 `NewsDetailFragment`
5. `NewsDetailFragment` 在 `onViewCreated()` 中读取 `arguments`
6. `NewsDetailFragment` 调用 `viewModel.loadDetail(newsId)`
7. `NewsDetailViewModel` 更新状态为加载中
8. `NewsRepository` 调用 `NewsDataSource.getNewsDetail(newsId)`
9. `MockNewsDataSource` 返回详情 DTO
10. `NewsMapper` 转换为 `NewsDetail`
11. `ViewModel` 发布成功状态
12. `NewsDetailFragment` 渲染标题、摘要、正文、相关推荐

#### 异常流程 1：缺少参数

1. `NewsDetailFragment` 未读到 `newsId`
2. 直接构造错误状态
3. 展示错误态和提示文案
4. 用户可通过返回按钮返回首页

#### 异常流程 2：查询失败

1. `Repository` 返回 `ResultWrapper.Error`
2. `ViewModel` 生成失败状态
3. `Fragment` 展示错误态
4. 用户点击“重新加载”
5. `ViewModel.retry()` 使用上一次 `newsId` 重试

## 3. 影响范围与风险评估

### 3.1 影响范围

| 影响模块/服务 | 影响程度 | 说明 |
|--------------|---------|------|
| [HomeFragment.kt](file:///Users/bytedance/AndroidStudioProjects/Homework/app/src/main/java/com/example/homework/ui/home/HomeFragment.kt) | 高 | 点击事件将从 Snackbar 改为真实导航 |
| [nav_graph.xml](file:///Users/bytedance/AndroidStudioProjects/Homework/app/src/main/res/navigation/nav_graph.xml) | 高 | 需要新增详情页节点和首页 action |
| [NewsDetailFragment.kt](file:///Users/bytedance/AndroidStudioProjects/Homework/app/src/main/java/com/example/homework/ui/detail/NewsDetailFragment.kt) | 中 | 需要验证参数读取和导航进入场景 |
| `NewsAdapter` 点击回调链路 | 中 | 需要确保 `article.id` 可用 |
| 返回栈体验 | 中 | 需要验证返回首页是否自然 |

### 3.2 风险评估

| 风险 | 概率 | 影响 | 缓解措施 |
|------|------|------|---------|
| 首页卡片存在空 `id` | 低 | 中 | 导航前做空值判断并拦截 |
| 导航图配置错误导致跳转失败 | 中 | 高 | 构建后手动验证首页所有分类点击 |
| 详情页重复加载导致闪烁 | 中 | 中 | 仅在拿到参数后调用一次 `loadDetail()` |
| 未来接入 `Safe Args` 需要调整调用方式 | 低 | 低 | 当前统一封装参数 key，降低替换成本 |

### 3.3 兼容性

- 与当前首页列表结构兼容，不需要改 `NewsArticle` 主体设计
- 与当前 `Mock / Remote` 数据源结构兼容，不需要调整 `Repository` 分层
- 后续若切换 `Safe Args`，只会影响导航参数传递层，不影响数据层和 UI 状态层

## 4. 排期估算

| 阶段 | 任务 | 预估人天 | 负责人 |
|------|------|---------|-------|
| 开发 | 补 `nav_graph.xml` 详情页 destination 和首页 action | 0.2 | 待定 |
| 开发 | 修改 `HomeFragment` 点击事件接入导航 | 0.2 | 待定 |
| 联调 | 验证详情页参数读取、成功态、错误态、返回栈 | 0.4 | 待定 |
| 测试 | 多分类点击、自测不同 `newsId` 详情内容 | 0.3 | 待定 |
| 发布 | 本地构建和演示验证 | 0.1 | 待定 |
| **合计** |  | **1.2 人天** |  |

说明：

- 排期基于当前详情页 `Fragment` 和 `ViewModel` 已完成的前提
- 若执行中决定同步接入相关推荐二次跳转，需额外增加约 `0.5` 人天

## 5. 测试策略

### 5.1 单元测试

本轮不强制新增单元测试，原因是：

- 当前课程项目以 UI 链路联调为主
- 本次改动主要集中在导航配置和现有逻辑接线

如需补最小单测，建议覆盖：

- `newsId` 为空时的拦截逻辑
- `ViewModel.retry()` 对上一次 `newsId` 的复用逻辑

### 5.2 集成测试

建议重点做手动集成验证：

- 首页默认分类点击第一条新闻
- 切换分类后点击新闻
- 返回首页后再次点击其他新闻
- 手动构造一个无效 `newsId` 进入详情页验证错误态

Mock 策略：

- 继续使用现有 `MockNewsDataSource`
- 不切换 Remote
- 确保不同分类下至少各验证 1 条新闻

### 5.3 验收标准

| # | 验收项 | 预期结果 |
|---|--------|---------|
| 1 | 首页点击新闻卡片 | 可正常进入详情页 |
| 2 | 详情页参数读取 | 能根据 `newsId` 显示对应内容 |
| 3 | 不同新闻点击 | 展示不同标题、摘要和正文 |
| 4 | 返回按钮 | 可正常返回首页 |
| 5 | 缺少参数场景 | 展示错误态而不是崩溃 |
| 6 | 重试按钮 | 失败后可重新触发请求 |

### 5.4 灰度与发布策略

本项目为课程大作业，本轮采用本地直接验证，不做灰度。

发布策略：

- 本地运行验证通过后提交代码
- 答辩演示时优先展示“首页点击 -> 详情页 -> 返回首页”的完整闭环

回滚方案：

- 若导航联调失败，可临时回退为 Snackbar 占位逻辑
- 不回退详情页数据层，因为其已可独立复用
