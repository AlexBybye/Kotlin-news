# 校园新闻 APP 大作业设计报告

> 课程：移动应用开发（Android）  
> 选题类型：选题一 —— 基于课程 NewsApp 项目，从零实现并进行功能与界面改造  
> 说明：本文档为报告底稿（Markdown），请按课程提供的 doc 模板格式誊抄/排版后导出 PDF，文件命名遵循 `AppName_leaderStudentID.pdf`。

---

## 一、项目概述

本项目是一款面向校园场景的 Android 新闻客户端「校园新闻」。应用以「校园热点 + 综合新闻」为定位，覆盖新闻浏览、分类切换、详情阅读、搜索、收藏、点赞、浏览历史、离线缓存、个性化设置等完整功能链路，并实现了课程要求的**注册 / 登录**与多处**数据增删改查**能力。

项目从零搭建，采用 Kotlin + MVVM + Repository 架构，使用 Room 操作本地数据库、Retrofit/OkHttp 进行网络请求、Navigation 管理页面跳转，并通过 WorkManager 实现定时刷新与通知提醒等加分能力。项目同时提供**自建后端**（Ktor + H2 + JWT），形成「Android 前端 + Web 后端」的完整全栈方案：App → 自建后端 → NewsAPI。

### 1.1 功能与评分对应

| 评分项 | 对应实现 |
|------|------|
| APP 能正常运行（45） | 默认本地模式，无网络 / 无后端依赖即可完整演示；后端不可用时自动回退 |
| 使用 ROOM 和 Repository 层（10） | 7 张数据表 + 多个 DAO，统一经 Repository 协调本地与远程数据 |
| 创意加分（最多 25） | 自建后端（Ktor+JWT）、真实 NewsAPI 接入、和风天气 API 集成、离线缓存兜底、点赞、设置中心、深色模式、字号、定时刷新 + 通知、内置 WebView、分享 |
| 报告内容（最高 20） | 本报告 |

---

## 二、需求分析

### 2.1 选题与要求拆解（选题一）

本项目选择课程「选题一」：以 NewsApp 为基础从零实现，并对功能/界面进行改造。命题对功能与技术的硬性要求拆解如下：

| 命题要求 | 需求点 | 优先级 |
|----|----|----|
| 基本注册功能 | 用户名/密码注册、查重、密码安全存储 | 必做 |
| 基本登录功能 | 凭证校验、登录态保持、退出登录 | 必做 |
| 某些数据的增删改查 | 至少一类业务数据的完整 CRUD | 必做 |
| 适当的人机交互界面 | 多页面导航、加载/空/错误状态、交互反馈 | 必做 |
| 使用 ROOM 操作数据库 | 实体/DAO/数据库定义与读写 | 必做（10 分） |
| 使用 Repository 层 | 数据来源协调层 | 加分（10 分内） |
| 功能/界面改造与创意 | 在基础项目上扩展亮点功能 | 加分（≤25 分） |

### 2.2 功能性需求

1. **账号**：注册（含校验与查重）、登录、退出、登录态恢复。
2. **新闻浏览**：分类新闻列表、下拉刷新、详情阅读、相关推荐下钻。
3. **数据管理（CRUD）**：收藏、点赞、浏览历史、搜索历史的增删改查。
4. **搜索与发现**：关键词搜索、历史/热门词、热点聚合。
5. **个性化设置**：深色模式、字号、流量控制、缓存管理。
6. **扩展能力**：实时天气、真实新闻源、后台定时刷新与通知。

### 2.3 非功能性需求

- **鲁棒性**：弱网/无网/后端未启动时仍可运行（直接对应命题备注「往年常因环境无法运行」的痛点）。
- **安全性**：密码不可明文存储；第三方/后端密钥不硬编码进 APK。
- **可维护性**：分层清晰、职责单一、便于扩展与测试。
- **兼容性**：minSdk 24 起，适配深色模式与多档字号。

### 2.4 用户用例（简）

```
游客 ──注册/登录──▶ 已登录用户
已登录用户 ──▶ 浏览分类新闻 ──▶ 阅读详情 ──▶ 收藏/点赞/分享/看原文
           ──▶ 搜索新闻 ──▶ 查看结果
           ──▶ 发现页热点
           ──▶ 我的 ──▶ 设置（主题/字号/后端开关）/ 退出登录
系统（后台）──▶ WorkManager 定时刷新新闻与天气 ──▶ 通知提醒
```

---

## 三、功能介绍

### 3.1 账号体系（课程必做项）

- **注册**：用户名（3-20 位，字母/数字/下划线）、昵称、密码、确认密码；前端做完整校验，用户名查重。
- **登录**：用户名 + 密码校验，登录态通过 DataStore 持久化，重启应用自动恢复。
- **退出登录**：清除会话并返回登录页。
- **安全**：密码不明文存储，使用 PBKDF2WithHmacSHA256 加随机盐哈希，校验采用定长比较防时序攻击。

### 3.2 新闻浏览与详情

- 首页分类 Tab（推荐 / 科技 / 体育 / 校园 / 国际），支持下拉刷新、分类切换、加载/空/错误/缓存四态。
- 详情页展示标题、来源、时间、分类、封面、正文段落与相关推荐，支持相关推荐继续下钻。

### 3.3 增删改查能力

- **收藏**（增/删/查）：详情页一键收藏，收藏页列表展示，数据持久化。
- **点赞**（增/删/查）：详情页点赞，状态本地保存。
- **浏览历史**（增/查，自动裁剪）：阅读详情后自动记录，仅保留最近 20 条。
- **搜索历史**（增/删/查，自动裁剪）：搜索后保存关键词，仅保留最近 10 条，可一键清空。
- **账号**（增/改/查）：注册新增、修改昵称、登录查询。

### 3.4 搜索与发现

- 搜索页支持关键词输入、热门词与历史词快捷检索、结果列表。
- 发现页聚合各分类热点新闻并提供热门搜索词，点击可跳转搜索或详情。

### 3.5 设置中心

- 深色模式（跟随系统 / 浅色 / 深色），即时生效并持久化。
- 正文字号（小 / 标准 / 大 / 特大），通过 Configuration.fontScale 生效。
- 仅 Wi-Fi 加载大图：移动网络下不加载封面以省流量。
- 定时自动刷新热点：开启后 WorkManager 周期拉取并发通知。
- 天气城市：支持常用城市选择，城市 ID/城市名写入 DataStore；也可授权定位后自动解析当前位置对应的和风天气城市。
- 清除缓存：清空新闻缓存（不影响收藏/历史）。

### 3.6 加分能力

- 真实新闻接口（后端代理 NewsAPI）接入，本地 Mock 兜底可一键切换。
- 离线缓存：网络异常时自动回退本地缓存内容。
- 定时刷新 + 通知提醒（WorkManager + NotificationChannel）。
- 内置 WebView 查看新闻原文、系统分享（ACTION_SEND）。

### 3.7 天气模块（第三方 API 集成）

首页顶部展示当前城市实时天气（温度、天气状况、体感、湿度），集成 **和风天气 RESTful API**：

- 通过 Retrofit/OkHttp/Moshi 调用 `v7/weather/now`，天气状况以 emoji 图标呈现（离线安全、零缺图）。
- 天气城市默认广州（101280101），设置页可进入“天气城市”页面手动切换常用城市，城市 ID 和展示名持久化到 DataStore。
- 支持原生定位：授权后通过 Android `LocationManager` 获取经纬度，再调用和风天气 `geo/v2/city/lookup` 解析最近城市 ID，用于自动切换天气城市。
- 天气经 `WeatherRepository` 统一返回 `ResultWrapper`，成功写入本地缓存，失败时回退最近一次缓存，首页打开即可秒显。
- **后台同步**：`WeatherSyncWorker` + WorkManager 每小时联网时自动刷新天气并更新缓存（满足「使用 Service」要求，底层基于 JobScheduler/系统服务）。
- API Key 存于 `local.properties`，经 `BuildConfig.QWEATHER_API_KEY` 注入，不硬编码。

---

## 四、技术架构

### 4.1 整体架构

应用采用 **MVVM + Repository** 分层，单向数据流；并通过自建后端形成前后端分离架构：

```
[Android 前端]
UI (Activity / Fragment)
        ↓  观察 LiveData
ViewModel  （持有 UI 状态，调度协程）
        ↓  调用
Repository （协调远程与本地数据，统一返回 ResultWrapper，失败回退缓存）
     ↙           ↘
RemoteDataSource   LocalDataSource
(Retrofit/OkHttp)   (Room / DataStore)
        │
        ↓ HTTP (JWT)
[Web 后端 · Ktor]
Routes → Service → (AuthService: H2/Exposed/BCrypt/JWT)
                   (NewsService: Ktor Client)
                          ↓ HTTP
                     [NewsAPI 第三方新闻接口]
```

数据来源由设置项「使用后端服务」开关（`AppConfig.useBackend`）统一控制：
- **本地模式（默认）**：账号走本地 Room，新闻走内置 Mock，无网络/无后端即可完整演示。
- **后端模式**：账号与新闻均走「App → 自建后端 → NewsAPI」；后端不可用时各 Repository 自动回退本地缓存/本地账号。

### 4.2 包结构

```
com.example.homework
├── NewsApplication            应用入口，启动应用设置（深色模式 / 定时任务）
├── MainActivity               主界面（BottomNav + Navigation 宿主，注入字号）
├── model                      领域模型（NewsArticle / NewsDetail / NewsCategory / User）
├── navigation                 导航相关
├── data
│   ├── auth                   密码哈希、会话管理（DataStore）
│   ├── settings               应用设置（DataStore + 同步镜像）
│   ├── local                  Room 数据库、Entity、DAO
│   ├── remote
│   │   ├── api                Retrofit 接口（BackendApi 自建后端 / QWeatherApi 和风天气）
│   │   ├── datasource         Mock / Remote 新闻数据源 + 工厂
│   │   ├── dto                网络与响应数据模型
│   │   └── network            Retrofit/OkHttp 客户端、网络配置、ResultWrapper
│   ├── mapper                 DTO ↔ Entity ↔ Model 映射
│   ├── config                 数据来源总开关（AppConfig.useBackend）
│   ├── search                 搜索数据源
│   └── repository             各业务 Repository（News/Search/LocalNews/LocalCache/Auth/Weather）
├── ui
│   ├── auth                   登录 / 注册
│   ├── home / discover / favorite / profile / search / detail / settings / web
│   └── ...
├── util                       工具类（图片加载策略等）
└── work                       WorkManager 定时刷新与通知
```

### 4.3 数据库设计（Room，version 5）

| 表 | 用途 | 关键字段 |
|----|----|----|
| users | 账号 | username(PK), nickname, passwordHash, passwordSalt |
| favorite_news | 收藏 | newsId(PK), title, ..., favoritedAt |
| liked_news | 点赞 | newsId(PK), likedAt |
| browse_history | 浏览历史 | newsId(PK), ..., lastBrowseTime |
| search_history | 搜索历史 | keyword(PK), lastSearchTime |
| cached_news | 列表缓存 | category, displayOrder |
| cached_news_detail | 详情缓存 | newsId(PK) |

### 4.4 关键技术点

| 技术 | 用途 |
|----|----|
| Kotlin Coroutines + Flow | 异步加载、登录态/设置响应式读取 |
| ViewModel + LiveData | 页面状态管理与生命周期安全的 UI 更新 |
| Navigation Component | 双导航图（主图 + 认证图）与参数传递 |
| Room | 账号、收藏、点赞、历史、缓存的持久化 |
| Repository | 统一协调远程/本地，缓存兜底逻辑收敛于此 |
| Retrofit + OkHttp + Moshi | 真实新闻接口请求、日志拦截、JSON 解析 |
| DataStore (Preferences) | 登录态、应用设置、天气城市持久化 |
| WorkManager | 周期性后台刷新 + 通知 |
| Coil | 新闻图片异步加载 |
| ViewBinding | 类型安全的视图绑定 |
| PBKDF2 | 密码加盐哈希 |

### 4.5 后端服务（Ktor）

为体现「前端 + 后端」的综合性，项目自建了一个轻量后端（代码位于仓库 `backend/` 目录）：

- **技术栈**：Ktor 2.3（Netty）+ Exposed ORM + H2 内嵌数据库 + JWT 鉴权 + BCrypt 密码哈希。
- **职责**：① 账号服务（注册/登录，签发 JWT）；② 新闻代理（服务端调用 NewsAPI 并归一化为 App 统一 DTO，**NewsAPI 密钥仅存于后端，不下发客户端**）。
- **接口**：`POST /auth/register`、`POST /auth/login`、`GET /auth/me`（需 Bearer Token）、`GET /news?category=`、`GET /news/detail/{id}`，统一响应 `{code,message,data}`。
- **安全**：密码 BCrypt 加盐哈希；JWT 无状态鉴权；OkHttp 拦截器自动在请求头附加令牌。

详见 `backend/README.md`。

---

## 五、项目模块设计

### 5.1 分层职责

| 层 | 组成 | 职责 |
|----|----|----|
| 表现层 UI | Activity / Fragment + ViewBinding | 渲染界面、收集交互、观察 LiveData |
| 状态层 ViewModel | `*ViewModel` + `*UiState` | 持有并暴露不可变 UI 状态，调度协程 |
| 仓库层 Repository | `NewsRepository` 等 6 个 | 协调远程/本地，统一返回 `ResultWrapper`，缓存兜底 |
| 数据源层 DataSource | Mock/Remote 新闻源、搜索源、Retrofit API | 具体取数实现，可切换 |
| 持久层 Local | Room（Entity/DAO/DB）、DataStore | 结构化数据与键值配置持久化 |
| 后端 Backend | Ktor Routes/Service/DB | 账号鉴权与新闻代理 |

### 5.2 功能模块划分

| 模块 | 关键类 | 说明 |
|----|----|----|
| 认证模块 | `AuthRepository`、`AuthViewModel`、`Login/RegisterFragment`、`AuthActivity`、`SessionManager`、`PasswordHasher` | 注册/登录/登录态/门控 |
| 新闻模块 | `NewsRepository`、`HomeViewModel`、`NewsAdapter`、`NewsDataSourceFactory` | 分类列表、刷新、缓存兜底 |
| 详情模块 | `NewsDetailViewModel`、`NewsDetailFragment`、`LocalNewsRepository` | 正文、相关推荐、收藏/点赞/分享/原文 |
| 搜索/发现 | `SearchRepository`、`SearchViewModel`、`DiscoverViewModel` | 关键词搜索、历史、热点聚合 |
| 收藏/历史 | `LocalNewsRepository`、`FavoriteViewModel` | 收藏与浏览历史 CRUD |
| 设置模块 | `SettingsManager`、`SettingsViewModel`、`AppSettings` | 主题/字号/流量/后端开关/天气城市/清缓存 |
| 天气模块 | `WeatherRepository`、`QWeatherApi`、`WeatherIconMapper`、`WeatherCityFragment`、`WeatherSyncWorker` | 实时天气 + 城市选择/定位 + 后台同步 |
| 后台任务 | `NewsRefreshWorker`、`WeatherSyncWorker` + 各 Scheduler | WorkManager 周期任务 |
| 后端模块 | `AuthService`、`NewsService`、`JwtService`、`*Routes` | Ktor 账号与新闻代理 |

### 5.3 模块依赖关系

```
ui.* ──▶ data.repository.* ──▶ data.remote.* / data.local.* / data.settings.*
  │                                   │
  └──▶ data.config.AppConfig ◀────────┘   （数据来源总开关）
work.* ──▶ data.repository.*            （后台任务复用仓库）
data.remote.api.BackendApi ──HTTP──▶ [backend] routes ──▶ service ──▶ NewsAPI
```

---

## 六、基础业务逻辑设计

> 本章选取若干核心业务，配合关键代码说明实现思路。

### 6.1 数据库与 Room 持久化

数据库集中定义 7 张表，单例构建，DAO 以挂起函数暴露：

```kotlin
@Database(
    entities = [
        SearchHistoryEntity::class, FavoriteNewsEntity::class,
        BrowseHistoryEntity::class, CachedNewsEntity::class,
        CachedNewsDetailEntity::class, UserEntity::class, LikedNewsEntity::class
    ],
    version = 5, exportSchema = false
)
abstract class HomeworkDatabase : RoomDatabase() {
    abstract fun favoriteNewsDao(): FavoriteNewsDao
    abstract fun likedNewsDao(): LikedNewsDao
    // ... 其余 DAO
    companion object {
        @Volatile private var INSTANCE: HomeworkDatabase? = null
        fun getInstance(context: Context): HomeworkDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext, HomeworkDatabase::class.java, "homework.db"
                ).fallbackToDestructiveMigration(dropAllTables = true).build()
                    .also { INSTANCE = it }
            }
    }
}
```

DAO 直接用 SQL 表达增删改查，例如收藏表：

```kotlin
@Dao
interface FavoriteNewsDao {
    @Query("SELECT * FROM favorite_news ORDER BY favoritedAt DESC")
    suspend fun getAllFavorites(): List<FavoriteNewsEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: FavoriteNewsEntity)

    @Query("DELETE FROM favorite_news WHERE newsId = :newsId")
    suspend fun deleteById(newsId: String)

    @Query("SELECT EXISTS(SELECT 1 FROM favorite_news WHERE newsId = :newsId)")
    suspend fun isFavorite(newsId: String): Boolean
}
```

### 6.2 注册 / 登录（必做项）

`AuthRepository` 做完整校验、查重，再用 PBKDF2 加盐哈希写入 Room，并保存登录态：

```kotlin
private suspend fun registerLocally(
    username: String, nickname: String, password: String
): ResultWrapper<User> {
    if (userDao.exists(username)) {
        return ResultWrapper.Error("该用户名已被注册，请更换后再试。")
    }
    val salt = PasswordHasher.generateSalt()
    val entity = UserEntity(
        username = username,
        nickname = nickname.ifBlank { username },
        passwordHash = PasswordHasher.hash(password, salt),
        passwordSalt = salt,
        createdAt = System.currentTimeMillis()
    )
    return runCatching {
        userDao.insert(entity)
        sessionManager.saveSession(entity.username)   // 登录态写入 DataStore
        ResultWrapper.Success(entity.toUser())
    }.getOrElse { ResultWrapper.Error("注册失败，请稍后重试。") }
}
```

登录校验使用定长比较，避免时序侧信道：

```kotlin
private fun constantTimeEquals(a: String, b: String): Boolean {
    if (a.length != b.length) return false
    var result = 0
    for (i in a.indices) result = result or (a[i].code xor b[i].code)
    return result == 0
}
```

### 6.3 新闻加载与缓存兜底

`NewsRepository` 先取远程，成功则写入缓存并标记数据来源；失败回退本地缓存：

```kotlin
suspend fun getNews(category: NewsCategory): ResultWrapper<CacheAwareData<List<NewsArticle>>> {
    return when (val result = dataSource.getNews(category)) {
        is ResultWrapper.Success -> {
            val articles = result.data.data.orEmpty().map { NewsMapper.toNewsArticle(it, category) }
            runCatching { localCacheRepository.saveCategoryNews(category, articles) }
            ResultWrapper.Success(CacheAwareData(value = articles, isFromCache = false))
        }
        is ResultWrapper.Error -> getCachedNews(category, result.message)  // 回退缓存
    }
}
```

ViewModel 将结果转为 UI 状态，Fragment 据此渲染加载/空/错误/缓存四态：

```kotlin
when (val result = newsRepository.getNews(category)) {
    is ResultWrapper.Success -> _uiState.value = currentState().copy(
        isLoading = false, articles = result.data.value,
        isEmpty = result.data.value.isEmpty(), isFromCache = result.data.isFromCache
    )
    is ResultWrapper.Error -> _uiState.value = currentState().copy(
        isLoading = false, errorMessage = result.message
    )
}
```

### 6.4 收藏与点赞（CRUD）

详情页切换收藏，返回切换后的状态供 UI 即时更新：

```kotlin
suspend fun toggleFavorite(detail: NewsDetail): Boolean {
    return if (favoriteNewsDao.isFavorite(detail.id)) {
        favoriteNewsDao.deleteById(detail.id); false
    } else {
        favoriteNewsDao.insert(LocalNewsMapper.toFavoriteEntity(detail)); true
    }
}
```

---

## 七、亮点设计与加分项设计

> 以下为在基础项目上扩展的创意/加分功能，配合关键代码说明。

### 7.1 前后端分离 + 密钥后置（自建 Ktor 后端）

新闻经「App → 自建后端 → NewsAPI」三段链路，**NewsAPI 密钥仅存于后端，APK 不含任何密钥**。后端用 Ktor Client 在服务端调第三方接口并归一化：

```kotlin
// backend: NewsService.kt
suspend fun getNews(category: String): List<NewsArticleDto> {
    if (newsApiKey.isBlank()) throw NewsException("服务端未配置 NewsAPI 密钥。")
    val response = fetchFromNewsApi(CategoryMapping.of(category))
    if (response.status != "ok") throw NewsException(response.message ?: "新闻接口异常。")
    return response.articles
        .filter { !it.title.isNullOrBlank() && it.title != "[Removed]" }
        .map { it.toArticleDto(category) }
}
```

后端用 JWT 做无状态鉴权，BCrypt 哈希密码：

```kotlin
// backend: AuthService.kt（登录核心）
val row = dbQuery { Users.selectAll().where { Users.username eq cleanUsername }.singleOrNull() }
    ?: throw AuthException("用户名不存在，请先注册。")
if (!BCrypt.checkpw(password, row[Users.passwordHash])) throw AuthException("密码错误。")
return AuthData(token = jwtService.generateToken(cleanUsername), user = row.toUserDto())
```

### 7.2 数据来源总开关 + 兜底策略

`AppConfig.useBackend` 一处切换本地/后端，并联动新闻数据源工厂：

```kotlin
object AppConfig {
    @Volatile var useBackend: Boolean = false
        set(value) {
            field = value
            NewsDataSourceFactory.currentMode =
                if (value) NewsDataSourceMode.REMOTE else NewsDataSourceMode.MOCK
        }
}
```

配合各 Repository 的「远程失败回退本地」，保证弱网/无网/后端未启动时仍可完整演示——直接应对命题备注「往年常因环境无法运行」的痛点。

### 7.3 第三方 API 集成：和风天气

天气使用**独立的 Retrofit 实例**，刻意不复用后端客户端，避免把后端 JWT 头泄露给第三方域名：

```kotlin
object WeatherRetrofitClient {
    private const val BASE_URL = "https://devapi.qweather.com/"
    // 单独 OkHttp（不带后端鉴权拦截器）+ Moshi
    val api: QWeatherApi by lazy {
        Retrofit.Builder().baseUrl(BASE_URL).client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build().create(QWeatherApi::class.java)
    }
}
```

`WeatherRepository` 统一返回 `ResultWrapper` 并做缓存兜底，密钥经 `BuildConfig` 注入：

```kotlin
val response = api.getCurrentWeather(location = location, apiKey = apiKey)
if (response.code != "200" || response.now == null) cachedOrError("天气接口返回异常。")
else { WeatherCache.save(context, weather); ResultWrapper.Success(weather) }
```

天气图标用 emoji 映射，离线安全、零缺图：

```kotlin
fun toEmoji(iconCode: String): String = when (iconCode) {
    "100", "150" -> "☀️"
    "101", "102", "103", "153" -> "⛅"
    in "300".."399" -> "🌧️"
    in "400".."499" -> "❄️"
    else -> "🌡️"
}
```

天气城市不再写死在代码中：`SettingsManager` 将 `weatherLocationId` 与 `weatherCityName` 保存到 DataStore。常用城市直接保存和风天气城市 ID；“使用当前位置”则先通过原生 `LocationManager` 获取经纬度，再调用 `geo/v2/city/lookup` 解析最近的和风天气城市 ID，最后仍按城市 ID 请求实时天气。

### 7.4 使用 Service：WorkManager 后台任务

新闻定时刷新与天气定时同步均用 `CoroutineWorker`（底层基于系统 JobScheduler，满足「使用 Service」要求）：

```kotlin
class WeatherSyncWorker(ctx: Context, p: WorkerParameters) : CoroutineWorker(ctx, p) {
    override suspend fun doWork(): Result {
        return when (WeatherRepository.createDefault(applicationContext).getCurrentWeather()) {
            is ResultWrapper.Success -> Result.success()
            is ResultWrapper.Error -> Result.retry()
        }
    }
}
```

```kotlin
val request = PeriodicWorkRequestBuilder<WeatherSyncWorker>(1, TimeUnit.HOURS)
    .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
    .build()
WorkManager.getInstance(context).enqueueUniquePeriodicWork(
    UNIQUE_WORK_NAME, ExistingPeriodicWorkPolicy.KEEP, request
)
```

### 7.5 其它工程化亮点

- **统一结果封装**：`ResultWrapper<Success/Error>` + `CacheAwareData` 贯穿数据层到 UI，UI 据 `isFromCache` 展示缓存提示。
- **同步镜像设置**：字号/图片策略需在 `attachBaseContext`、图片加载等无法挂起场景同步读取，故在 DataStore 外另存一份 SharedPreferences 镜像。
- **个性化体验**：深色模式即时切换、四档字号（`Configuration.fontScale`）、仅 Wi-Fi 加载大图、内置 WebView 看原文、系统分享。
- **安全实践**：前端 PBKDF2、后端 BCrypt 加盐哈希，定长比较，JWT 鉴权，清缓存不误删用户数据。

---

## 八、主要界面

> 以下为各界面说明，正式报告中请在每节插入对应运行截图。

1. **登录页**：应用标题、用户名/密码输入、登录按钮、跳转注册入口。截图占位：`[截图：登录页]`
2. **注册页**：用户名/昵称/密码/确认密码，含输入校验提示。`[截图：注册页]`
3. **首页**：顶部实时天气卡片、搜索入口、分类 Tab、新闻列表、下拉刷新、缓存提示条。`[截图：首页（含天气卡片）]`
4. **详情页**：封面、正文、点赞/收藏/分享/原文操作、相关推荐。`[截图：详情页]`
5. **搜索页**：搜索框、热门词、历史词、结果列表。`[截图：搜索页]`
6. **发现页**：热门搜索词 + 今日热点聚合列表。`[截图：发现页]`
7. **收藏页**：收藏文章 + 最近浏览两个分区。`[截图：收藏页]`
8. **我的页**：头像/昵称/账号、收藏与浏览统计、设置入口、退出登录。`[截图：我的页]`
9. **设置中心**：深色模式、字号、天气城市、仅 Wi-Fi 图片、定时刷新、清除缓存。`[截图：设置页 + 深色模式效果]`
10. **天气城市页**：展示当前城市 ID，支持常用城市切换和授权定位自动选择。`[截图：天气城市页 + 定位授权]`
11. **原文 WebView**：内置浏览器加载新闻原文。`[截图：原文页]`

---

## 九、运行与构建说明

### 9.1 环境要求

- Android Studio（建议 Ladybug 及以上）
- JDK 17
- compileSdk 35 / minSdk 24 / targetSdk 35
- Gradle 8.11.1，AGP 8.9.1，Kotlin 2.0.21
- 后端：JDK 17 + Gradle（仅在使用后端模式演示时需要）

### 9.2 前端运行步骤

1. Android Studio 打开 `Kotlin-news` 工程，等待 Gradle 同步。
2. 直接运行 `app`，**默认本地模式，无需网络与后端即可完整体验全部功能**。
3.（可选）后端模式：先启动后端（见 5.4），在 App「我的 → 设置 → 使用后端服务」中开启开关，重新登录即可走「App → 后端 → NewsAPI」链路。

### 9.3 数据来源切换说明

| 模式 | 账号 | 新闻 | 说明 |
|----|----|----|----|
| 本地（默认） | 本地 Room | 内置 Mock | 稳定可演示，无外部依赖 |
| 后端 | 自建后端(JWT) | 后端代理 NewsAPI | 真实链路；后端不可用自动回退本地 |

模拟器访问宿主机后端地址为 `http://10.0.2.2:8080/`（真机改为后端主机局域网 IP），配置见 `NetworkConfig.BACKEND_BASE_URL`。

### 9.4 后端运行步骤

```bash
cd backend
export NEWS_API_KEY=<你的 NewsAPI 密钥>   # 不设则用 application.yaml 中的默认值
./gradlew run
```

服务监听 `http://0.0.0.0:8080`，接口与示例见 `backend/README.md`。

---

## 十、开源代码使用说明

- **NewsAPI**（https://newsapi.org）：真实新闻数据来源。由**自建后端**在服务端调用，密钥仅存于后端，客户端不接触；仅在后端模式启用，默认不依赖。
- **和风天气 API**（https://dev.qweather.com）：首页实时天气与定位选城数据来源。App 端通过 Retrofit 直连其 `devapi.qweather.com` 的实时天气接口与 GeoAPI，密钥经 `local.properties` + `BuildConfig` 注入，不硬编码。
- **后端框架**：Ktor、Exposed、H2、java-jwt、jBCrypt 等开源库，用于搭建 Web 后端服务。
- **前端依赖**：AndroidX / Material / Retrofit / OkHttp / Moshi / Coil / Room / WorkManager 等业界标准开源库，用于网络、数据库、图片加载、后台任务等基础能力。
- 本项目未直接下载任何完整开源 App 作为提交内容，所有业务代码（含前端与后端）均为本组实现。

---

## 十一、AI 编程使用说明

本项目在开发过程中合理使用了 AI 辅助编程，范围与程度如下：

- **使用范围**：架构设计建议、样板代码（Entity/DAO/Repository/ViewModel/Fragment）生成、布局编写、单元测试编写、代码审查与本报告初稿撰写。
- **使用程度**：AI 负责在既有架构约定下生成与重构代码，组员负责需求确定、方案选型、逐处审阅、在 Android Studio 中编译运行、真机/模拟器联调与问题修复。
- **人工把关**：所有 AI 生成代码均经过人工评审与编译验证，关键安全逻辑（密码哈希、登录态）与数据库迁移策略由组员确认。

---

## 十二、优缺点分析与改进展望

### 12.1 优点

- 架构清晰、分层规范，组件覆盖面广，且包含自建后端，综合性强，便于答辩讲解。
- 兜底策略完善（后端不可用自动回退本地），演示环境鲁棒性高。
- 功能链路完整，包含必做项与多个加分项。
- 天气模块支持城市持久化与定位解析，第三方 API 接入从“展示数据”扩展到“用户可配置”的完整闭环。
- 安全实践到位，前后端密码均加盐哈希、后端 JWT 鉴权。

### 12.2 不足

- 后端使用 H2 内嵌库，未接入生产级数据库（MySQL/PG），且未做容器化部署。
- NewsAPI 为英文新闻源，详情正文受第三方接口限制（仅摘要 + 原文跳转）。
- 未引入依赖注入框架（Hilt），依赖通过工厂方法手动装配。
- UI 测试与端到端测试覆盖有限，目前以核心逻辑单元测试为主。

### 12.3 改进展望

- 后端接入 MySQL/PostgreSQL 与 Docker 部署，并扩展点赞/收藏的云端同步。
- 接入 Paging 3 实现真正的分页加载。
- 引入 Hilt 完成依赖注入，进一步解耦。
- 补充 Espresso UI 测试与更完整的 Repository 测试（含 Robolectric）。
- 接入中文新闻源或抓取详情正文并做富文本渲染。

---

## 十三、组内分工

> 请按实际情况填写。

| 成员 | 学号 | 主要工作 |
|----|----|----|
| （组长）xxx | 2022xxxxxxxx | 架构设计、数据层与认证模块、报告统稿 |
| xxx | 2022xxxxxxxx | 首页/详情/搜索界面与 ViewModel |
| xxx | 2022xxxxxxxx | 设置中心、发现页、WorkManager 与测试 |

---

## 十四、其它说明

- 数据库版本升级采用 `fallbackToDestructiveMigration`，升级时会重建本地数据（演示无影响）。
- 应用所有用户可见文案均为简体中文，已适配深色模式与多档字号。

---

## 附：作业要求完成度对照表

| command.md 要求 | 完成情况 | 对应实现 |
| :--- | :---: | :--- |
| 注册功能（必做） | ✅ | `AuthRepository.register` + `RegisterFragment`，校验/查重/加盐哈希 |
| 登录功能（必做） | ✅ | `AuthRepository.login` + `LoginFragment` + `SessionManager` 登录态恢复 |
| 数据增删改查（必做） | ✅ | 收藏/点赞/浏览历史/搜索历史/账号 的 CRUD（Room） |
| 适当的人机交互界面（必做） | ✅ | 单 Activity + 双导航图，8+ 页面，加载/空/错误/缓存四态 |
| 使用 ROOM 操作数据库（10 分） | ✅ | 7 张表 + 7 个 DAO + `HomeworkDatabase` |
| 使用 Repository 层（加分） | ✅ | 6 个 Repository 统一协调远程/本地 |
| 界面/功能改造与创意（≤25 分） | ✅ | 见第七章：自建后端、天气 API、天气城市/定位、设置中心、深色模式、点赞、分享、WebView 等 |
| 使用 Service（实验要求） | ✅ | WorkManager 新闻刷新 + 天气同步（底层 JobScheduler） |
| 使用第三方 SDK / API（实验要求） | ✅ | 和风天气实时天气 + GeoAPI 定位城市解析、NewsAPI（经后端代理） |
| 前端 + 后端综合 | ✅（超额） | Android 客户端 + Ktor 后端（H2 + JWT） |
| 合理使用 AI 编程并说明（加分） | ✅ | 见第十一章 |

> 结论：command.md 中选题一的全部**必做项**均已实现，ROOM + Repository 满分项达成，并完成多项创意加分与「Service / 第三方 API」实验要求。
