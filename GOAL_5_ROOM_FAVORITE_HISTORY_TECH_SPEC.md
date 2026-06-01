# 目标 5 技术方案：收藏与浏览历史的 Room 数据闭环 Tech Spec

## 1. 背景与目标

### 1.1 背景

当前新闻 APP 已经具备以下能力：

- 首页新闻流浏览
- 新闻详情页查看
- 搜索页与搜索历史
- 详情页“收藏”按钮 UI 占位
- `Room` 已经通过搜索历史表接入项目

但当前收藏与浏览历史仍未形成真正的数据闭环：

- 详情页点击收藏只切换按钮文案，没有落库
- 浏览详情页不会记录浏览历史
- 收藏页仍然是占位页面，无法展示真实数据
- 应用重启后，收藏状态和历史状态不会保留

对于课程大作业来说，目标 5 是“本地数据管理能力”的核心展示点。完成这一阶段后，可以清晰体现：

- `Room` 数据库设计能力
- 本地持久化能力
- Fragment / ViewModel / Repository / Room 的完整协作链路

### 1.2 现状分析

当前项目中与目标 5 直接相关的现状如下：

- 搜索历史数据库已存在，入口为 [HomeworkDatabase.kt](file:///Users/bytedance/AndroidStudioProjects/Homework/app/src/main/java/com/example/homework/data/local/HomeworkDatabase.kt)
- 详情页逻辑已完成，但收藏仅是 UI 切换，文件为 [NewsDetailFragment.kt](file:///Users/bytedance/AndroidStudioProjects/Homework/app/src/main/java/com/example/homework/ui/detail/NewsDetailFragment.kt)
- 收藏页仍为占位页面，文件为 [FavoriteFragment.kt](file:///Users/bytedance/AndroidStudioProjects/Homework/app/src/main/java/com/example/homework/ui/favorite/FavoriteFragment.kt)
- 收藏页 ViewModel 仍为文案占位，文件为 [FavoriteViewModel.kt](file:///Users/bytedance/AndroidStudioProjects/Homework/app/src/main/java/com/example/homework/ui/favorite/FavoriteViewModel.kt)

当前不足主要有五点：

1. `Room` 只有 `search_history` 表，没有收藏和浏览历史表
2. `NewsDetailViewModel` 没有本地收藏能力
3. 浏览详情页时没有写入浏览历史
4. 收藏页没有真实数据渲染
5. 首页、搜索结果、详情页之间没有共享收藏状态

### 1.3 目标

本期核心目标：

- 用 `Room` 保存收藏新闻
- 用 `Room` 保存浏览历史
- 进入详情页时自动记录浏览历史
- 点击收藏时真正落库并支持取消收藏
- 收藏页展示收藏列表和浏览历史列表
- 重启应用后数据仍然存在

本期非目标：

- 不实现账号云同步
- 不实现多设备同步
- 不做离线正文缓存
- 不做浏览历史分页
- 不做收藏分组与标签管理

### 1.4 术语表

| 术语 | 含义 |
|------|------|
| 收藏 | 用户主动标记希望保留的新闻 |
| 浏览历史 | 用户实际打开过的新闻详情记录 |
| 本地持久化 | 使用 Room 保存数据，应用重启后仍可恢复 |
| 闭环 | 从产生数据到查询展示都使用同一套本地数据链路 |

## 2. 技术方案

### 2.1 整体架构

目标 5 继续沿用当前分层架构：

`Fragment` -> `ViewModel` -> `Repository` -> `Room Dao`

本次新增两条核心链路：

#### 收藏链路

1. 用户在详情页点击“收藏”
2. `NewsDetailViewModel` 调用本地仓库切换收藏状态
3. `Room` 中的收藏表写入或删除记录
4. `NewsDetailViewModel` 刷新当前详情状态
5. 收藏页读取收藏表并展示真实列表

#### 浏览历史链路

1. 用户进入详情页
2. `NewsDetailViewModel` 在详情加载成功后写入浏览历史
3. `Room` 历史表记录最近浏览时间
4. 收藏页读取浏览历史表并展示最近浏览

### 2.2 模块拆分

| 模块 | 职责 | 输入 | 输出 | 依赖 |
|------|------|------|------|------|
| `FavoriteFragment` | 展示收藏列表与浏览历史列表 | UI 状态 | 页面展示 | `FavoriteViewModel` |
| `FavoriteViewModel` | 读取收藏与历史数据 | 无 / 刷新事件 | `FavoriteUiState` | `LocalNewsRepository` |
| `NewsDetailViewModel` | 管理详情页收藏状态与历史写入 | `newsId`、收藏点击 | `NewsDetailUiState` | `NewsRepository`、`LocalNewsRepository` |
| `LocalNewsRepository` | 统一本地收藏与历史操作 | 新闻对象 / `newsId` | 本地数据结果 | `FavoriteDao`、`HistoryDao` |
| `FavoriteNewsDao` | 收藏数据读写 | 实体对象 | 收藏列表 / 收藏状态 | Room |
| `BrowseHistoryDao` | 浏览历史读写 | 实体对象 | 历史列表 | Room |
| `HomeworkDatabase` | 挂载所有本地表 | 实体集合 | DAO 实例 | Room |

### 2.3 接口设计

本期主要新增本地仓库接口，不新增 HTTP API。

#### 接口 1：切换收藏状态

- **接口名称**：`toggleFavorite(detail: NewsDetail)`
- **类型**：Repository 内部接口

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `detail` | `NewsDetail` | 是 | 当前详情页新闻对象 |

返回结构：

```kotlin
Boolean
```

说明：

- `true` 表示当前已收藏
- `false` 表示当前已取消收藏

#### 接口 2：查询是否已收藏

- **接口名称**：`isFavorite(newsId: String)`
- **类型**：Repository 内部接口

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `newsId` | `String` | 是 | 新闻唯一标识 |

返回结构：

```kotlin
Boolean
```

#### 接口 3：保存浏览历史

- **接口名称**：`saveBrowseHistory(detail: NewsDetail)`
- **类型**：Repository 内部接口

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `detail` | `NewsDetail` | 是 | 当前详情新闻对象 |

说明：

- 相同 `newsId` 重复浏览时，更新最近浏览时间
- 历史记录建议保留最近 `20` 条

#### 接口 4：读取收藏列表

- **接口名称**：`getFavoriteNewsList()`
- **类型**：Repository 内部接口

返回结构：

```kotlin
List<NewsArticle>
```

#### 接口 5：读取浏览历史列表

- **接口名称**：`getBrowseHistoryList()`
- **类型**：Repository 内部接口

返回结构：

```kotlin
List<NewsArticle>
```

### 2.4 数据结构

#### 收藏表

建议新增：

```kotlin
@Entity(tableName = "favorite_news")
data class FavoriteNewsEntity(
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
    val favoritedAt: Long
)
```

约束：

- `newsId` 唯一
- 重复收藏采用覆盖更新时间

#### 浏览历史表

建议新增：

```kotlin
@Entity(tableName = "browse_history")
data class BrowseHistoryEntity(
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
    val lastBrowseTime: Long
)
```

约束：

- `newsId` 唯一
- 每次重新阅读时只更新时间
- 保留最近 `20` 条

#### 收藏页状态模型

建议新增：

```kotlin
data class FavoriteUiState(
    val favorites: List<NewsArticle> = emptyList(),
    val histories: List<NewsArticle> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)
```

#### 数据库升级

当前 [HomeworkDatabase.kt](file:///Users/bytedance/AndroidStudioProjects/Homework/app/src/main/java/com/example/homework/data/local/HomeworkDatabase.kt) 版本为 `1`，仅包含 `SearchHistoryEntity`。

本期建议升级为：

- `version = 2`
- 新增实体：
  - `FavoriteNewsEntity`
  - `BrowseHistoryEntity`

[假设]

- 当前项目仍处于课程开发阶段，可接受 `fallbackToDestructiveMigration()`，避免为早期实验数据编写复杂迁移脚本。
- 如果你更希望保留搜索历史数据，再单独补 migration 也可以，但课程项目通常优先开发效率。

### 2.5 核心流程

#### 流程 1：进入详情页写浏览历史

1. 用户从首页、搜索页或相关推荐进入详情页
2. `NewsDetailViewModel.loadDetail(newsId)` 拉取详情成功
3. `ViewModel` 调用 `LocalNewsRepository.saveBrowseHistory(detail)`
4. `BrowseHistoryDao` 执行插入或覆盖更新
5. `BrowseHistoryDao.trimToLatest20()` 清理超量历史

#### 流程 2：点击收藏

1. 用户点击详情页收藏按钮
2. `NewsDetailViewModel` 调用 `LocalNewsRepository.toggleFavorite(detail)`
3. 如果已收藏，则执行删除
4. 如果未收藏，则写入收藏表
5. `ViewModel` 刷新 `uiState.detail.isCollected`
6. 页面更新按钮文案

#### 流程 3：打开收藏页

1. 用户进入 `FavoriteFragment`
2. `FavoriteViewModel` 调用 `LocalNewsRepository.getFavoriteNewsList()`
3. 同时调用 `LocalNewsRepository.getBrowseHistoryList()`
4. 生成 `FavoriteUiState`
5. 页面展示“收藏文章”和“最近浏览”两块列表

#### 流程 4：点击收藏页条目

1. 用户点击收藏或历史列表中的任意文章
2. `FavoriteFragment` 跳转详情页
3. 传递 `newsId`
4. 详情页复用现有加载逻辑

## 3. 影响范围与风险评估

### 3.1 影响范围

| 影响模块/服务 | 影响程度 | 说明 |
|--------------|---------|------|
| [HomeworkDatabase.kt](file:///Users/bytedance/AndroidStudioProjects/Homework/app/src/main/java/com/example/homework/data/local/HomeworkDatabase.kt) | 高 | 需要新增实体、DAO 并升级数据库版本 |
| [NewsDetailViewModel.kt](file:///Users/bytedance/AndroidStudioProjects/Homework/app/src/main/java/com/example/homework/ui/detail/NewsDetailViewModel.kt) | 高 | 需要真正接入收藏与浏览历史 |
| [NewsDetailFragment.kt](file:///Users/bytedance/AndroidStudioProjects/Homework/app/src/main/java/com/example/homework/ui/detail/NewsDetailFragment.kt) | 中 | 收藏提示文案将从占位逻辑改为真实结果 |
| [FavoriteFragment.kt](file:///Users/bytedance/AndroidStudioProjects/Homework/app/src/main/java/com/example/homework/ui/favorite/FavoriteFragment.kt) | 高 | 从占位页改为真实数据页 |
| [FavoriteViewModel.kt](file:///Users/bytedance/AndroidStudioProjects/Homework/app/src/main/java/com/example/homework/ui/favorite/FavoriteViewModel.kt) | 高 | 从静态文案改为本地数据状态管理 |
| 搜索历史库 | 中 | 需要与新表共同维护在同一个数据库中 |

### 3.2 风险评估

| 风险 | 概率 | 影响 | 缓解措施 |
|------|------|------|---------|
| 详情模型与本地实体字段不一致 | 中 | 中 | 明确 DTO / 业务模型 / Entity 三层职责，使用单独 Mapper |
| 数据库升级影响已有搜索历史 | 中 | 中 | 课程阶段可接受 destructive migration，或额外补 migration |
| 收藏状态与详情页状态不同步 | 中 | 高 | 收藏切换统一放到 `NewsDetailViewModel` 中处理 |
| 收藏页一次展示两类列表导致 UI 复杂 | 中 | 中 | 采用“收藏文章 + 最近浏览”两个独立分区，避免混排 |
| 浏览历史过多导致列表膨胀 | 低 | 中 | DAO 层限制最多 `20` 条 |

### 3.3 兼容性

- 与当前详情页导航兼容，仍然只依赖 `newsId`
- 与当前 `NewsArticle` 模型兼容，收藏页和历史页可直接复用列表卡片
- 与当前搜索页兼容，搜索结果进入详情后也会自动写浏览历史

## 4. 排期估算

| 阶段 | 任务 | 预估人天 | 负责人 |
|------|------|---------|-------|
| 开发 | 新增收藏 / 浏览历史实体、DAO、数据库升级 | 0.8 | 待定 |
| 开发 | 新增本地仓库与实体映射 | 0.6 | 待定 |
| 开发 | 改造详情页收藏与浏览历史写入 | 0.7 | 待定 |
| 开发 | 改造收藏页与 ViewModel | 0.8 | 待定 |
| 联调 | 首页 / 搜索 / 详情 / 收藏联调 | 0.4 | 待定 |
| 测试 | 重启恢复、取消收藏、历史覆盖、自测回归 | 0.5 | 待定 |
| **合计** |  | **3.8 人天** |  |

说明：

- 如果本期先只做收藏，不做浏览历史，可减少约 `1.0` 人天
- 但从课程展示角度，建议一次性把收藏与历史都做成完整闭环

## 5. 测试策略

### 5.1 单元测试

建议优先覆盖：

- `toggleFavorite()` 的新增 / 取消逻辑
- 浏览历史去重更新逻辑
- 收藏与历史截断数量逻辑

### 5.2 集成测试

重点验证以下场景：

1. 从首页进入详情页后，收藏按钮默认状态正确
2. 点击收藏后，收藏页能看到该新闻
3. 退出应用重启后，收藏仍保留
4. 多次打开同一篇新闻，浏览历史不重复，只更新时间
5. 从搜索结果进入详情页后，同样会写入浏览历史
6. 点击收藏页条目可重新进入详情页

### 5.3 验收标准

| # | 验收项 | 预期结果 |
|---|--------|---------|
| 1 | 详情页点击收藏 | 能真实写入本地数据库 |
| 2 | 再次点击收藏 | 能取消收藏 |
| 3 | 收藏页展示 | 能看到真实收藏列表 |
| 4 | 浏览详情页 | 能写入浏览历史 |
| 5 | 浏览历史展示 | 收藏页能展示最近浏览 |
| 6 | 重启应用 | 收藏与历史数据仍存在 |
| 7 | 点击收藏 / 历史项 | 能重新进入详情页 |

### 5.4 灰度与发布策略

本项目为课程大作业，本期采用本地直接验证。

发布策略：

- 先在本地模拟器验证首页、搜索页、详情页、收藏页闭环
- 重点演示“收藏持久化”和“浏览历史自动记录”

回滚方案：

- 若收藏页联调出现问题，可先只保留详情页落库能力
- 不回滚已有搜索历史库结构
