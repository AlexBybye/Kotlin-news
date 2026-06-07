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
| 创意加分（最多 25） | 自建后端（Ktor+JWT）、真实 NewsAPI 接入、离线缓存兜底、点赞、设置中心、深色模式、字号、定时刷新 + 通知、内置 WebView、分享 |
| 报告内容（最高 20） | 本报告 |

---

## 二、功能介绍

### 2.1 账号体系（课程必做项）

- **注册**：用户名（3-20 位，字母/数字/下划线）、昵称、密码、确认密码；前端做完整校验，用户名查重。
- **登录**：用户名 + 密码校验，登录态通过 DataStore 持久化，重启应用自动恢复。
- **退出登录**：清除会话并返回登录页。
- **安全**：密码不明文存储，使用 PBKDF2WithHmacSHA256 加随机盐哈希，校验采用定长比较防时序攻击。

### 2.2 新闻浏览与详情

- 首页分类 Tab（推荐 / 科技 / 体育 / 校园 / 国际），支持下拉刷新、分类切换、加载/空/错误/缓存四态。
- 详情页展示标题、来源、时间、分类、封面、正文段落与相关推荐，支持相关推荐继续下钻。

### 2.3 增删改查能力

- **收藏**（增/删/查）：详情页一键收藏，收藏页列表展示，数据持久化。
- **点赞**（增/删/查）：详情页点赞，状态本地保存。
- **浏览历史**（增/查，自动裁剪）：阅读详情后自动记录，仅保留最近 20 条。
- **搜索历史**（增/删/查，自动裁剪）：搜索后保存关键词，仅保留最近 10 条，可一键清空。
- **账号**（增/改/查）：注册新增、修改昵称、登录查询。

### 2.4 搜索与发现

- 搜索页支持关键词输入、热门词与历史词快捷检索、结果列表。
- 发现页聚合各分类热点新闻并提供热门搜索词，点击可跳转搜索或详情。

### 2.5 设置中心

- 深色模式（跟随系统 / 浅色 / 深色），即时生效并持久化。
- 正文字号（小 / 标准 / 大 / 特大），通过 Configuration.fontScale 生效。
- 仅 Wi-Fi 加载大图：移动网络下不加载封面以省流量。
- 定时自动刷新热点：开启后 WorkManager 周期拉取并发通知。
- 清除缓存：清空新闻缓存（不影响收藏/历史）。

### 2.6 加分能力

- 真实新闻接口（聚合数据 · 头条新闻）接入，Mock 兜底可一键切换。
- 离线缓存：网络异常时自动回退本地缓存内容。
- 定时刷新 + 通知提醒（WorkManager + NotificationChannel）。
- 内置 WebView 查看新闻原文、系统分享（ACTION_SEND）。

<!-- REPORT_CONTINUE -->

---

## 三、技术架构

### 3.1 整体架构

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

### 3.2 包结构

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
│   │   ├── api                Retrofit 接口（JuheNewsApi）
│   │   ├── datasource         Mock / Remote 新闻数据源 + 工厂
│   │   ├── dto                网络与响应数据模型
│   │   └── network            Retrofit/OkHttp 客户端、网络配置、ResultWrapper
│   ├── mapper                 DTO ↔ Entity ↔ Model 映射
│   ├── search                 搜索数据源
│   └── repository             各业务 Repository（News/Search/LocalNews/LocalCache/Auth）
├── ui
│   ├── auth                   登录 / 注册
│   ├── home / discover / favorite / profile / search / detail / settings / web
│   └── ...
├── util                       工具类（图片加载策略等）
└── work                       WorkManager 定时刷新与通知
```

### 3.3 数据库设计（Room，version 5）

| 表 | 用途 | 关键字段 |
|----|----|----|
| users | 账号 | username(PK), nickname, passwordHash, passwordSalt |
| favorite_news | 收藏 | newsId(PK), title, ..., favoritedAt |
| liked_news | 点赞 | newsId(PK), likedAt |
| browse_history | 浏览历史 | newsId(PK), ..., lastBrowseTime |
| search_history | 搜索历史 | keyword(PK), lastSearchTime |
| cached_news | 列表缓存 | category, displayOrder |
| cached_news_detail | 详情缓存 | newsId(PK) |

### 3.4 关键技术点

| 技术 | 用途 |
|----|----|
| Kotlin Coroutines + Flow | 异步加载、登录态/设置响应式读取 |
| ViewModel + LiveData | 页面状态管理与生命周期安全的 UI 更新 |
| Navigation Component | 双导航图（主图 + 认证图）与参数传递 |
| Room | 账号、收藏、点赞、历史、缓存的持久化 |
| Repository | 统一协调远程/本地，缓存兜底逻辑收敛于此 |
| Retrofit + OkHttp + Moshi | 真实新闻接口请求、日志拦截、JSON 解析 |
| DataStore (Preferences) | 登录态与应用设置持久化 |
| WorkManager | 周期性后台刷新 + 通知 |
| Coil | 新闻图片异步加载 |
| ViewBinding | 类型安全的视图绑定 |
| PBKDF2 | 密码加盐哈希 |

### 3.5 后端服务（Ktor）

为体现「前端 + 后端」的综合性，项目自建了一个轻量后端（代码位于仓库 `backend/` 目录）：

- **技术栈**：Ktor 2.3（Netty）+ Exposed ORM + H2 内嵌数据库 + JWT 鉴权 + BCrypt 密码哈希。
- **职责**：① 账号服务（注册/登录，签发 JWT）；② 新闻代理（服务端调用 NewsAPI 并归一化为 App 统一 DTO，**NewsAPI 密钥仅存于后端，不下发客户端**）。
- **接口**：`POST /auth/register`、`POST /auth/login`、`GET /auth/me`（需 Bearer Token）、`GET /news?category=`、`GET /news/detail/{id}`，统一响应 `{code,message,data}`。
- **安全**：密码 BCrypt 加盐哈希；JWT 无状态鉴权；OkHttp 拦截器自动在请求头附加令牌。

详见 `backend/README.md`。

### 3.6 设计亮点

1. **前后端分离 + 密钥后置**：新闻经「App → 后端 → NewsAPI」三段链路，NewsAPI 密钥只存在于后端，APK 中不含任何密钥，符合真实工程的安全实践。
2. **数据源开关 + 兜底策略**：`AppConfig.useBackend` 统一切换本地/后端；Repository 在远程失败时自动回退 Room 缓存与本地账号，保证弱网/无网/后端未启动时仍可演示——直接应对课程「往年常因环境无法运行」的痛点。
3. **统一结果封装**：`ResultWrapper<Success/Error>` 贯穿数据层到 UI，配合 `CacheAwareData` 标识数据来源（实时/缓存），UI 据此展示缓存提示。
4. **同步镜像设置**：字号与图片策略需要在 `attachBaseContext`/图片加载等无法挂起的场景同步读取，故在 DataStore 之外维护一份 SharedPreferences 镜像。
5. **安全实践**：前端 PBKDF2 / 后端 BCrypt 加盐哈希，定长比较，JWT 鉴权，清除缓存不误删用户数据。

<!-- REPORT_CONTINUE_2 -->

---

## 四、主要界面

> 以下为各界面说明，正式报告中请在每节插入对应运行截图。

1. **登录页**：应用标题、用户名/密码输入、登录按钮、跳转注册入口。截图占位：`[截图：登录页]`
2. **注册页**：用户名/昵称/密码/确认密码，含输入校验提示。`[截图：注册页]`
3. **首页**：搜索入口、分类 Tab、新闻列表、下拉刷新、缓存提示条。`[截图：首页]`
4. **详情页**：封面、正文、点赞/收藏/分享/原文操作、相关推荐。`[截图：详情页]`
5. **搜索页**：搜索框、热门词、历史词、结果列表。`[截图：搜索页]`
6. **发现页**：热门搜索词 + 今日热点聚合列表。`[截图：发现页]`
7. **收藏页**：收藏文章 + 最近浏览两个分区。`[截图：收藏页]`
8. **我的页**：头像/昵称/账号、收藏与浏览统计、设置入口、退出登录。`[截图：我的页]`
9. **设置中心**：深色模式、字号、仅 Wi-Fi 图片、定时刷新、清除缓存。`[截图：设置页 + 深色模式效果]`
10. **原文 WebView**：内置浏览器加载新闻原文。`[截图：原文页]`

---

## 五、运行与构建说明

### 5.1 环境要求

- Android Studio（建议 Ladybug 及以上）
- JDK 17
- compileSdk 35 / minSdk 24 / targetSdk 35
- Gradle 8.11.1，AGP 8.9.1，Kotlin 2.0.21
- 后端：JDK 17 + Gradle（仅在使用后端模式演示时需要）

### 5.2 前端运行步骤

1. Android Studio 打开 `Kotlin-news` 工程，等待 Gradle 同步。
2. 直接运行 `app`，**默认本地模式，无需网络与后端即可完整体验全部功能**。
3.（可选）后端模式：先启动后端（见 5.4），在 App「我的 → 设置 → 使用后端服务」中开启开关，重新登录即可走「App → 后端 → NewsAPI」链路。

### 5.3 数据来源切换说明

| 模式 | 账号 | 新闻 | 说明 |
|----|----|----|----|
| 本地（默认） | 本地 Room | 内置 Mock | 稳定可演示，无外部依赖 |
| 后端 | 自建后端(JWT) | 后端代理 NewsAPI | 真实链路；后端不可用自动回退本地 |

模拟器访问宿主机后端地址为 `http://10.0.2.2:8080/`（真机改为后端主机局域网 IP），配置见 `NetworkConfig.BACKEND_BASE_URL`。

### 5.4 后端运行步骤

```bash
cd backend
export NEWS_API_KEY=<你的 NewsAPI 密钥>   # 不设则用 application.yaml 中的默认值
./gradlew run
```

服务监听 `http://0.0.0.0:8080`，接口与示例见 `backend/README.md`。

---

## 六、开源代码使用说明

- **NewsAPI**（https://newsapi.org）：真实新闻数据来源。由**自建后端**在服务端调用，密钥仅存于后端，客户端不接触；仅在后端模式启用，默认不依赖。
- **后端框架**：Ktor、Exposed、H2、java-jwt、jBCrypt 等开源库，用于搭建 Web 后端服务。
- **前端依赖**：AndroidX / Material / Retrofit / OkHttp / Moshi / Coil / Room / WorkManager 等业界标准开源库，用于网络、数据库、图片加载、后台任务等基础能力。
- 本项目未直接下载任何完整开源 App 作为提交内容，所有业务代码（含前端与后端）均为本组实现。

---

## 七、AI 编程使用说明

本项目在开发过程中合理使用了 AI 辅助编程，范围与程度如下：

- **使用范围**：架构设计建议、样板代码（Entity/DAO/Repository/ViewModel/Fragment）生成、布局编写、单元测试编写、代码审查与本报告初稿撰写。
- **使用程度**：AI 负责在既有架构约定下生成与重构代码，组员负责需求确定、方案选型、逐处审阅、在 Android Studio 中编译运行、真机/模拟器联调与问题修复。
- **人工把关**：所有 AI 生成代码均经过人工评审与编译验证，关键安全逻辑（密码哈希、登录态）与数据库迁移策略由组员确认。

---

## 八、优缺点分析与改进展望

### 8.1 优点

- 架构清晰、分层规范，组件覆盖面广，且包含自建后端，综合性强，便于答辩讲解。
- 兜底策略完善（后端不可用自动回退本地），演示环境鲁棒性高。
- 功能链路完整，包含必做项与多个加分项。
- 安全实践到位，前后端密码均加盐哈希、后端 JWT 鉴权。

### 8.2 不足

- 后端使用 H2 内嵌库，未接入生产级数据库（MySQL/PG），且未做容器化部署。
- NewsAPI 为英文新闻源，详情正文受第三方接口限制（仅摘要 + 原文跳转）。
- 未引入依赖注入框架（Hilt），依赖通过工厂方法手动装配。
- UI 测试与端到端测试覆盖有限，目前以核心逻辑单元测试为主。

### 8.3 改进展望

- 后端接入 MySQL/PostgreSQL 与 Docker 部署，并扩展点赞/收藏的云端同步。
- 接入 Paging 3 实现真正的分页加载。
- 引入 Hilt 完成依赖注入，进一步解耦。
- 补充 Espresso UI 测试与更完整的 Repository 测试（含 Robolectric）。
- 接入中文新闻源或抓取详情正文并做富文本渲染。

---

## 九、组内分工

> 请按实际情况填写。

| 成员 | 学号 | 主要工作 |
|----|----|----|
| （组长）xxx | 2022xxxxxxxx | 架构设计、数据层与认证模块、报告统稿 |
| xxx | 2022xxxxxxxx | 首页/详情/搜索界面与 ViewModel |
| xxx | 2022xxxxxxxx | 设置中心、发现页、WorkManager 与测试 |

---

## 十、其它说明

- 数据库版本升级采用 `fallbackToDestructiveMigration`，升级时会重建本地数据（演示无影响）。
- 应用所有用户可见文案均为简体中文，已适配深色模式与多档字号。


