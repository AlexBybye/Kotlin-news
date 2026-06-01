# 目标 4 技术方案：搜索页与搜索历史 Tech Spec

## 1. 背景与目标

### 1.1 背景

当前新闻 APP 已经具备以下基础能力：

- 首页新闻流列表与分类切换
- 新闻详情页与相关推荐跳转
- `Mock / Remote` 可切换的数据层结构
- 基础 Navigation、多 Fragment、ViewModel 架构

但目前仍缺少用户主动找内容的入口：

- 首页搜索卡片仍是占位提示
- 没有独立搜索页
- 没有关键词搜索结果列表
- 没有搜索历史和热门关键词展示

对于课程大作业来说，搜索页不仅是常见新闻产品能力，也能覆盖输入交互、页面跳转、列表复用、本地持久化等多个技术点。

### 1.2 现状分析

当前项目已经具备实现搜索页的几个前提：

- 首页已有搜索入口，位置在 [HomeFragment.kt](file:///Users/bytedance/AndroidStudioProjects/Homework/app/src/main/java/com/example/homework/ui/home/HomeFragment.kt)
- 现有新闻业务模型 `NewsArticle` 可直接复用为搜索结果项
- 现有 `NewsRepository -> NewsDataSource` 链路已支持新闻列表与详情查询
- 现有 `NewsAdapter` 已可复用到搜索结果列表

当前不足主要有四点：

1. 缺少搜索页 `Fragment`
2. 缺少搜索结果状态模型
3. 缺少搜索历史持久化结构
4. 缺少搜索 API / Mock 搜索实现

### 1.3 目标

本期核心目标：

- 新增独立搜索页
- 支持输入关键词并执行搜索
- 支持展示搜索结果列表
- 支持展示搜索历史
- 支持热门关键词展示
- 继续遵守 `Mock / Remote` 可切换原则

本期非目标：

- 不做全文索引优化
- 不做搜索联想下拉建议
- 不做复杂排序与筛选
- 不做账号级云同步历史

### 1.4 术语表

| 术语 | 含义 |
|------|------|
| 搜索关键词 | 用户输入的查询字符串 |
| 搜索历史 | 本地保存的最近搜索记录 |
| 热门关键词 | 用于展示和快捷点击的预设热词 |
| 搜索结果页 | 输入后展示匹配新闻列表的页面 |
| `Mock / Remote` | 可切换的数据源模式，当前默认 `Mock` |

## 2. 技术方案

### 2.1 整体架构

搜索页整体仍沿用当前项目的 MVVM + Repository 分层：

1. 用户在首页点击搜索入口进入 `SearchFragment`
2. `SearchFragment` 负责输入框、历史词、热门词和结果列表渲染
3. `SearchViewModel` 管理输入、提交搜索、清空历史、恢复界面状态
4. `SearchRepository` 统一协调搜索接口和搜索历史数据
5. 搜索远程结果仍通过 `SearchDataSource` 抽象
6. 当前默认由 `MockSearchDataSource` 返回模拟结果
7. 搜索历史本期建议用 `Room` 落地，以便提前为目标 5 复用本地数据库能力

### 2.2 模块拆分

| 模块 | 职责 | 输入 | 输出 | 依赖 |
|------|------|------|------|------|
| `SearchFragment` | 搜索页 UI、交互绑定、导航跳转 | 用户输入、热词点击、历史点击 | 页面渲染、详情跳转 | `SearchViewModel` |
| `SearchViewModel` | 管理搜索状态与动作 | 关键词、点击事件 | `SearchUiState` | `SearchRepository` |
| `SearchRepository` | 统一搜索入口与历史管理 | 关键词 | 搜索结果、历史记录 | `SearchDataSource`、`SearchHistoryDao` |
| `SearchDataSource` | 抽象搜索数据源 | 关键词 | 搜索 DTO 列表 | Mock / Remote |
| `MockSearchDataSource` | 返回匹配的模拟新闻结果 | 关键词 | `NewsArticleDto` 列表 | 本地模拟数据 |
| `SearchHistoryDao` | 搜索历史读写 | 关键词 | 历史记录列表 | Room |

### 2.3 接口设计

#### 接口 1：搜索页导航入口

- **接口名称**：`action_homeFragment_to_searchFragment`
- **类型**：Navigation Action

当前搜索入口来源：

- 首页顶部搜索卡片

本期跳转不需要传参数，搜索页独立维护自身状态。

#### 接口 2：搜索仓库接口

- **接口名称**：`searchNews(keyword: String)`
- **类型**：Repository 内部接口

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `keyword` | `String` | 是 | 用户输入关键词，去空格后使用 |

返回结构：

```kotlin
ResultWrapper.Success(List<NewsArticle>)
ResultWrapper.Error(message)
```

#### 接口 3：搜索历史接口

- **接口名称**：`saveSearchHistory(keyword: String)`
- **类型**：Repository / DAO 内部接口

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `keyword` | `String` | 是 | 用户完成一次有效搜索后保存 |

补充接口：

- `getRecentSearchHistory(): List<SearchHistory>`
- `clearSearchHistory()`
- `deleteSearchHistory(keyword: String)` [可选，本期可不做]

#### 未来 Remote HTTP 接口建议

- **接口名称**：`GET /news/search`

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `keyword` | `string` | 是 | 搜索关键词 |
| `page` | `int` | 否 | 页码，默认 1 |
| `pageSize` | `int` | 否 | 每页数量，默认 20 |

返回结构示例：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "keyword": "科技",
    "list": [
      {
        "id": "t1",
        "title": "国产大模型应用持续落地，教育场景成重点方向",
        "summary": "AI 工具在教学辅助、内容生产和校园服务中的应用正在不断扩展。"
      }
    ]
  }
}
```

### 2.4 数据结构

#### 搜索页状态模型

```kotlin
data class SearchUiState(
    val keyword: String = "",
    val isLoading: Boolean = false,
    val results: List<NewsArticle> = emptyList(),
    val recentHistory: List<SearchHistory> = emptyList(),
    val hotKeywords: List<String> = emptyList(),
    val errorMessage: String? = null,
    val hasSearched: Boolean = false
)
```

关键说明：

- `keyword`：用于回显当前搜索词
- `hasSearched`：区分“初始态”和“空结果态”
- `recentHistory`：用于展示最近搜索
- `hotKeywords`：用于首屏快捷入口

#### 搜索历史模型

```kotlin
@Entity(tableName = "search_history")
data class SearchHistoryEntity(
    @PrimaryKey
    val keyword: String,
    val lastSearchTime: Long
)
```

建议约束：

- 关键词唯一，重复搜索只更新时间
- 历史记录最多保留最近 `10` 条

#### 搜索数据源接口

```kotlin
interface SearchDataSource {
    suspend fun searchNews(keyword: String): ResultWrapper<List<NewsArticleDto>>
}
```

#### Mock 搜索策略

- 在现有新闻池中按标题、摘要、来源做包含匹配
- 忽略大小写
- 去除首尾空格
- 空关键词不执行真实搜索，直接返回空列表或由 ViewModel 拦截

### 2.5 核心流程

#### 正常搜索流程

1. 用户从首页点击搜索卡片进入 `SearchFragment`
2. `SearchFragment` 展示输入框、热词、历史记录
3. 用户输入关键词并点击搜索
4. `SearchViewModel` 先做关键词合法性校验
5. 若关键词合法，则调用 `SearchRepository.searchNews(keyword)`
6. `SearchRepository` 调用当前启用的 `SearchDataSource`
7. 当前默认由 `MockSearchDataSource` 返回搜索结果
8. `SearchRepository` 同步保存搜索历史
9. `SearchViewModel` 更新 `SearchUiState`
10. `SearchFragment` 渲染结果列表
11. 用户点击某条结果，跳转到详情页并传递 `newsId`

#### 热词搜索流程

1. 用户点击热门关键词
2. `SearchFragment` 将关键词回填到输入框
3. 直接触发搜索动作
4. 后续链路与正常搜索一致

#### 搜索历史回填流程

1. 用户点击历史关键词
2. `SearchFragment` 回填输入框
3. 直接触发搜索
4. 历史时间更新到最新

#### 清空历史流程

1. 用户点击“清空历史”
2. `ViewModel` 调用 `SearchRepository.clearSearchHistory()`
3. `Fragment` 刷新首屏列表

#### 异常流程

1. 用户输入空字符串
2. `ViewModel` 直接拦截
3. 不请求数据源
4. 页面给出轻提示或保持当前状态

如果搜索失败：

1. `Repository` 返回 `ResultWrapper.Error`
2. `ViewModel` 写入错误信息
3. `Fragment` 展示错误态和重试按钮

## 3. 影响范围与风险评估

### 3.1 影响范围

| 影响模块/服务 | 影响程度 | 说明 |
|--------------|---------|------|
| [HomeFragment.kt](file:///Users/bytedance/AndroidStudioProjects/Homework/app/src/main/java/com/example/homework/ui/home/HomeFragment.kt) | 高 | 搜索入口将从占位提示改为真实导航 |
| `nav_graph.xml` | 高 | 需要新增 `searchFragment` 节点和导航 action |
| `SearchFragment` / `SearchViewModel` | 高 | 新增完整搜索页逻辑 |
| `NewsAdapter` | 中 | 预计可复用为搜索结果列表适配器 |
| `Room` 数据库层 | 中 | 需要新增搜索历史表和 DAO |
| `Mock / Remote` 数据源层 | 中 | 需补搜索接口抽象 |

### 3.2 风险评估

| 风险 | 概率 | 影响 | 缓解措施 |
|------|------|------|---------|
| 过早接入 Room 导致目标 4 工作量扩大 | 中 | 中 | 先只实现最小历史表与 DAO，不提前做收藏表 |
| 搜索结果与首页列表模型职责不清 | 低 | 中 | 直接复用 `NewsArticle`，不新建重复结果模型 |
| Mock 搜索与未来 Remote 接口不一致 | 中 | 中 | 统一走 `SearchDataSource` 和 `SearchRepository` |
| 空状态过多导致 UI 复杂 | 中 | 低 | 用 `hasSearched` 区分初始态与空结果态 |

### 3.3 兼容性

- 与当前 `NewsArticle` 模型兼容，详情页导航可直接复用 `newsId`
- 与当前 `Mock / Remote` 结构兼容，不需要改首页和详情页数据层
- Room 仅新增搜索历史表，不影响后续收藏与浏览历史扩展

## 4. 排期估算

| 阶段 | 任务 | 预估人天 | 负责人 |
|------|------|---------|-------|
| 开发 | 新建 `SearchFragment`、布局与导航 | 0.5 | 待定 |
| 开发 | 新建 `SearchViewModel`、`SearchUiState`、结果列表复用 | 0.6 | 待定 |
| 开发 | 新建 `SearchDataSource`、`SearchRepository`、Mock 搜索实现 | 0.6 | 待定 |
| 开发 | 新增 `Room` 搜索历史表、DAO、数据库接入 | 0.7 | 待定 |
| 联调 | 搜索、热词、历史、详情跳转联调 | 0.4 | 待定 |
| 测试 | 空输入、无结果、错误态、自测回归 | 0.4 | 待定 |
| **合计** |  | **3.2 人天** |  |

说明：

- 若本期决定“不接 Room，只做内存历史”，可减少约 `0.7` 人天
- 但考虑课程技术覆盖，建议保留 Room 搜索历史

## 5. 测试策略

### 5.1 单元测试

建议优先覆盖以下逻辑：

- 空关键词拦截
- 关键词去空格处理
- 搜索历史去重与更新时间刷新
- 历史列表截断为最近 `10` 条

### 5.2 集成测试

重点验证以下链路：

- 首页进入搜索页
- 输入关键词搜索并展示结果
- 点击热词触发搜索
- 点击历史词触发搜索
- 点击搜索结果进入详情页
- 清空历史后列表立即更新

Mock 策略：

- 搜索结果全部先走 `MockSearchDataSource`
- 热词使用固定预设，如 `科技`、`校园`、`体育`
- Room 只保存本地历史，不依赖账号

### 5.3 验收标准

| # | 验收项 | 预期结果 |
|---|--------|---------|
| 1 | 首页点击搜索入口 | 能进入搜索页 |
| 2 | 输入关键词搜索 | 能展示匹配新闻结果 |
| 3 | 点击热词 | 能触发搜索并展示结果 |
| 4 | 搜索历史 | 能保存并展示最近搜索记录 |
| 5 | 点击结果项 | 能进入新闻详情页 |
| 6 | 空关键词 | 不触发无效请求 |
| 7 | 无结果态 | 页面能正常显示空结果提示 |

### 5.4 灰度与发布策略

本项目为课程大作业，搜索页采用本地直接验证。

发布策略：

- 先用 `Mock` 搜索实现跑通 UI 和交互
- 再按同一接口替换为 Remote 搜索

回滚方案：

- 若搜索页联调出现问题，可临时保留页面入口但只展示热词和历史
- 不回滚现有首页与详情页链路
