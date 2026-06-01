# 目标 6 技术方案：缓存与离线能力 Tech Spec

## 1. 背景与目标

### 1.1 背景

当前新闻 APP 已经完成：

- 首页新闻流浏览
- 新闻详情页展示
- 搜索页与搜索历史
- 收藏与浏览历史的 `Room` 数据闭环

但当前内容展示仍然主要依赖运行时数据获取：

- 首页列表目前走 Mock 数据链路，后续切到 Remote 后将面临网络依赖问题
- 详情页当前通过 `NewsRepository` 拉取详情，但没有独立缓存表
- 虽然收藏与浏览历史已落库，但收藏数据只保存“列表摘要信息”，不能保证离线情况下完整展示正文
- 一旦未来切换到真实网络接口，在弱网或无网场景下，首页和详情页会直接失去可读性

目标 6 的意义是把 App 从“有本地记录”升级为“具备真实离线能力”的内容应用，这也是课程答辩中非常有展示价值的一部分。

### 1.2 现状分析

当前项目已具备适合扩展离线能力的几个基础：

- 本地数据库已存在：[HomeworkDatabase.kt](file:///Users/bytedance/AndroidStudioProjects/Homework/app/src/main/java/com/example/homework/data/local/HomeworkDatabase.kt)
- 当前数据库已经承载：
  - 搜索历史
  - 收藏记录
  - 浏览历史
- 新闻远程与本地逻辑已经分层：
  - [NewsRepository.kt](file:///Users/bytedance/AndroidStudioProjects/Homework/app/src/main/java/com/example/homework/data/repository/NewsRepository.kt)
  - [LocalNewsRepository.kt](file:///Users/bytedance/AndroidStudioProjects/Homework/app/src/main/java/com/example/homework/data/repository/LocalNewsRepository.kt)
- 当前详情页和首页的 `ViewModel` 已经适合接入“先查缓存 / 失败回退缓存”的策略

当前不足主要有四点：

1. 没有“新闻列表缓存表”
2. 没有“新闻详情缓存表”
3. Repository 没有本地缓存兜底逻辑
4. 收藏虽然能离线看到标题摘要，但不能保证正文完整离线可读

### 1.3 目标

本期核心目标：

- 缓存首页分类新闻列表
- 缓存新闻详情正文
- 无网或请求失败时优先回退本地缓存
- 收藏文章支持离线阅读完整内容
- Repository 层形成 `Remote + Cache` 的统一读取策略

本期非目标：

- 不做大规模图片离线缓存
- 不做复杂缓存失效策略中心
- 不做分布式同步
- 不做分页缓存
- 不做全文搜索缓存索引

### 1.4 术语表

| 术语 | 含义 |
|------|------|
| 列表缓存 | 缓存首页各分类新闻列表的摘要信息 |
| 详情缓存 | 缓存单篇新闻的正文、摘要、相关推荐等详情信息 |
| 离线阅读 | 在无网时仍可打开并阅读本地已缓存内容 |
| 回退缓存 | 远程请求失败时改为读取本地缓存 |
| 缓存时间戳 | 用于标记缓存生成时间，辅助后续扩展失效策略 |

## 2. 技术方案

### 2.1 整体架构

目标 6 推荐采用“远程优先，失败回退缓存”的 Repository 策略：

#### 首页列表读取策略

1. 优先请求远程列表
2. 请求成功后写入本地缓存表
3. 如果远程失败，则读取分类对应的本地缓存列表
4. 若本地缓存存在，页面继续展示缓存内容，并标记为缓存来源
5. 若本地缓存不存在，再展示错误态

#### 详情页读取策略

1. 优先请求远程详情
2. 请求成功后写入详情缓存表
3. 同步更新收藏文章的正文缓存
4. 若远程失败，则尝试读取本地详情缓存
5. 若详情缓存存在，则允许离线打开正文

#### 收藏离线阅读策略

1. 当文章被收藏时，如果当前详情已加载成功，则把完整详情同步写入缓存表
2. 收藏页点击进入详情时：
   - 优先走现有详情加载逻辑
   - 若网络失败，仍可从详情缓存表中读取正文
3. 这样收藏文章不仅有“收藏记录”，还具备“离线可读正文”

整体分层关系：

`Fragment` -> `ViewModel` -> `NewsRepository / CacheRepository` -> `RemoteDataSource + Room Dao`

### 2.2 模块拆分

| 模块 | 职责 | 输入 | 输出 | 依赖 |
|------|------|------|------|------|
| `NewsRepository` | 统一首页和详情的数据读取策略 | 分类 / `newsId` | 新闻列表 / 详情 | Remote + Cache |
| `LocalCacheRepository` | 封装新闻缓存的读写 | 新闻列表 / 详情 | 缓存结果 | Cache DAO |
| `CachedNewsDao` | 读写分类新闻列表缓存 | 分类 | `CachedNewsEntity` 列表 | Room |
| `CachedNewsDetailDao` | 读写新闻详情缓存 | `newsId` | `CachedNewsDetailEntity` | Room |
| `HomeViewModel` | 读取列表并感知缓存回退 | 分类 | `HomeUiState` | `NewsRepository` |
| `NewsDetailViewModel` | 读取详情并感知离线可读 | `newsId` | `NewsDetailUiState` | `NewsRepository` |

### 2.3 接口设计

本期主要新增 Repository 与本地缓存接口，不新增新的 HTTP API。

#### 接口 1：缓存分类新闻列表

- **接口名称**：`saveCategoryNews(category: NewsCategory, articles: List<NewsArticle>)`
- **类型**：本地缓存仓库接口

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `category` | `NewsCategory` | 是 | 新闻分类 |
| `articles` | `List<NewsArticle>` | 是 | 首页新闻列表数据 |

#### 接口 2：读取分类新闻缓存

- **接口名称**：`getCategoryNews(category: NewsCategory)`
- **类型**：本地缓存仓库接口

返回结构：

```kotlin
List<NewsArticle>
```

#### 接口 3：缓存新闻详情

- **接口名称**：`saveNewsDetail(detail: NewsDetail)`
- **类型**：本地缓存仓库接口

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `detail` | `NewsDetail` | 是 | 已成功获取的详情对象 |

#### 接口 4：读取新闻详情缓存

- **接口名称**：`getNewsDetail(newsId: String)`
- **类型**：本地缓存仓库接口

返回结构：

```kotlin
NewsDetail?
```

#### 接口 5：首页读取统一策略

- **接口名称**：`getNews(category: NewsCategory)`
- **类型**：主仓库接口

返回建议扩展为：

```kotlin
sealed class CachedResult<T> {
    data class Remote<T>(val data: T) : CachedResult<T>()
    data class Cache<T>(val data: T) : CachedResult<T>()
    data class Error(val message: String) : CachedResult<Nothing>()
}
```

也可保持当前 `ResultWrapper`，并在 `HomeUiState` 中增加 `isFromCache` 字段。

本期更推荐：

- 保留 `ResultWrapper`
- 在 UI 状态中单独记录缓存来源

原因：

- 改动更小
- 与当前项目结构更兼容

### 2.4 数据结构

#### 分类新闻缓存表

建议新增：

```kotlin
@Entity(tableName = "cached_news")
data class CachedNewsEntity(
    @PrimaryKey(autoGenerate = true)
    val cacheId: Long = 0,
    val newsId: String,
    val category: String,
    val title: String,
    val summary: String,
    val coverImageUrl: String?,
    val author: String?,
    val source: String,
    val publishTime: String,
    val contentUrl: String?,
    val isTop: Boolean,
    val cachedAt: Long
)
```

说明：

- 同一分类可缓存多条记录
- 刷新分类缓存时，建议先删除该分类旧数据，再整体插入新数据

#### 新闻详情缓存表

建议新增：

```kotlin
@Entity(tableName = "cached_news_detail")
data class CachedNewsDetailEntity(
    @PrimaryKey
    val newsId: String,
    val title: String,
    val summary: String,
    val coverImageUrl: String?,
    val author: String?,
    val source: String,
    val category: String,
    val publishTime: String,
    val contentUrl: String?,
    val content: String,
    val relatedNewsIds: String,
    val cachedAt: Long
)
```

说明：

- `content` 使用 JSON 字符串保存正文段落
- `relatedNewsIds` 使用 JSON 或逗号分隔字符串保存相关推荐 ID
- 如需更强规范性，后续也可拆表，但课程阶段不建议过度设计

#### UI 状态扩展

建议扩展：

```kotlin
data class HomeUiState(
    ...,
    val isFromCache: Boolean = false
)
```

```kotlin
data class NewsDetailUiState(
    ...,
    val isFromCache: Boolean = false
)
```

用途：

- 页面可在必要时展示“当前为离线内容”提示
- 答辩时也更容易说明离线能力已真正生效

#### 数据库升级

当前数据库版本已是 `2`，目标 6 建议升级为：

- `version = 3`

新增实体：

- `CachedNewsEntity`
- `CachedNewsDetailEntity`

课程阶段继续建议：

- 使用 `fallbackToDestructiveMigration(dropAllTables = true)`

原因：

- 当前项目仍在快速迭代
- 缓存类数据本身就是可重建数据
- 课程环境下优先交付功能闭环

### 2.5 核心流程

#### 流程 1：首页加载成功并写缓存

1. `HomeViewModel` 请求 `NewsRepository.getNews(category)`
2. `NewsRepository` 优先调用远程数据源
3. 远程成功后，将结果映射为 `NewsArticle`
4. 调用缓存仓库保存该分类新闻列表
5. `HomeViewModel` 更新页面并标记 `isFromCache = false`

#### 流程 2：首页请求失败并回退缓存

1. 远程请求失败
2. `NewsRepository` 调用缓存仓库读取该分类缓存
3. 若缓存存在，则返回缓存结果
4. `HomeViewModel` 更新列表并标记 `isFromCache = true`
5. 页面可提示“当前展示离线缓存内容”

#### 流程 3：详情页请求成功并写缓存

1. `NewsDetailViewModel` 请求详情
2. `NewsRepository` 调用远程详情接口
3. 成功后写入详情缓存表
4. 如果当前文章已收藏，也同步保证其正文缓存已更新
5. `ViewModel` 更新页面，`isFromCache = false`

#### 流程 4：详情页请求失败并读取缓存

1. 远程详情请求失败
2. `NewsRepository` 读取本地详情缓存
3. 若缓存存在，则返回本地详情
4. `ViewModel` 更新页面，`isFromCache = true`
5. 页面允许用户继续阅读正文

#### 流程 5：收藏文章离线可读

1. 用户打开一篇新闻详情
2. 详情成功加载后，本地已有完整详情缓存
3. 用户点击收藏
4. 收藏记录写入 `favorite_news`
5. 即使后续无网，点击收藏页中的该文章仍能进入详情并读取缓存正文

## 3. 影响范围与风险评估

### 3.1 影响范围

| 影响模块/服务 | 影响程度 | 说明 |
|--------------|---------|------|
| [HomeworkDatabase.kt](file:///Users/bytedance/AndroidStudioProjects/Homework/app/src/main/java/com/example/homework/data/local/HomeworkDatabase.kt) | 高 | 需要新增缓存表并升级数据库版本 |
| [NewsRepository.kt](file:///Users/bytedance/AndroidStudioProjects/Homework/app/src/main/java/com/example/homework/data/repository/NewsRepository.kt) | 高 | 需要改造成 Remote + Cache 双数据源策略 |
| [LocalNewsRepository.kt](file:///Users/bytedance/AndroidStudioProjects/Homework/app/src/main/java/com/example/homework/data/repository/LocalNewsRepository.kt) | 中 | 需明确与缓存仓库职责边界 |
| `HomeViewModel` / `HomeUiState` | 中 | 需要支持缓存来源状态 |
| `NewsDetailViewModel` / `NewsDetailUiState` | 中 | 需要支持缓存来源状态和离线提示 |
| 收藏页 | 中 | 需要验证收藏新闻在离线下仍可读正文 |

### 3.2 风险评估

| 风险 | 概率 | 影响 | 缓解措施 |
|------|------|------|---------|
| 缓存表与收藏表职责混淆 | 中 | 中 | 收藏表只保存收藏记录，正文离线能力统一走缓存表 |
| 详情缓存字段较多，序列化复杂 | 中 | 中 | 先采用 JSON 字符串保存 `content` 和相关推荐 ID |
| Repository 改动过大影响现有逻辑 | 中 | 高 | 保持对外接口不变，优先在内部增加缓存回退 |
| 缓存数据陈旧 | 高 | 低 | 本期先接受“最近成功拉取即为可读缓存”，后续再加过期策略 |
| 数据库升级清空旧数据 | 中 | 低 | 课程阶段可接受 destructive migration，缓存可重建 |

### 3.3 兼容性

- 与当前收藏 / 浏览历史能力兼容，不需要推翻已有本地表结构
- 与当前搜索页兼容，搜索结果进入详情页后也可自动生成详情缓存
- 与后续真实网络接口兼容，目标 6 反而是切 Remote 后的关键保障层

## 4. 排期估算

| 阶段 | 任务 | 预估人天 | 负责人 |
|------|------|---------|-------|
| 开发 | 新增缓存表、DAO、数据库升级 | 0.8 | 待定 |
| 开发 | 新增缓存仓库与实体映射 | 0.7 | 待定 |
| 开发 | 改造 `NewsRepository` 接入回退缓存策略 | 0.9 | 待定 |
| 开发 | 扩展首页与详情页状态，支持离线提示 | 0.5 | 待定 |
| 联调 | 首页、详情页、收藏页离线联调 | 0.5 | 待定 |
| 测试 | 模拟断网、重启应用、缓存回退验证 | 0.6 | 待定 |
| **合计** |  | **4.0 人天** |  |

说明：

- 若本期只做详情缓存，不做列表缓存，可减少约 `1.0` 人天
- 但从答辩展示角度，建议列表和详情缓存一起做，效果更完整

## 5. 测试策略

### 5.1 单元测试

建议覆盖：

- 分类缓存的保存与读取
- 详情缓存的保存与读取
- 缓存回退策略
- 分类覆盖更新逻辑

### 5.2 集成测试

重点验证以下场景：

1. 首页联网加载一次后断网，再次进入首页仍能看到分类新闻
2. 首页点进已读新闻，断网后仍能打开详情
3. 收藏新闻后断网，从收藏页仍能进入完整详情
4. 搜索进入详情后断网，若已读过则仍可打开
5. 缓存不存在且无网时，页面才展示错误态

### 5.3 验收标准

| # | 验收项 | 预期结果 |
|---|--------|---------|
| 1 | 首页远程成功后 | 能写入分类缓存 |
| 2 | 首页远程失败时 | 能回退展示本地缓存 |
| 3 | 详情远程成功后 | 能写入详情缓存 |
| 4 | 详情远程失败时 | 能读取本地缓存并展示正文 |
| 5 | 收藏文章离线查看 | 能从收藏页进入并阅读缓存正文 |
| 6 | 页面状态提示 | 能区分远程内容与缓存内容 |

### 5.4 灰度与发布策略

本项目为课程大作业，本期采用本地直接验证。

发布策略：

- 先在模拟器中联网访问首页和详情页，生成缓存
- 再手动关闭网络，验证首页、详情页和收藏页的离线能力

回滚方案：

- 若列表缓存联调成本过高，可先保留详情缓存能力
- 若缓存页状态标记影响现有 UI，可先不展示 `isFromCache` 提示，只保留能力本身
