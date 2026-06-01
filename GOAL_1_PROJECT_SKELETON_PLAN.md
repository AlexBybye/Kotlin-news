# 目标 1 详细开发计划：项目骨架搭建

## 1. 目标说明

本阶段目标是基于当前仅包含 `MainActivity` 和基础布局的 Android 工程，搭建出新闻 APP 的整体应用骨架，为后续首页列表、详情页、搜索、收藏、本地缓存等功能提供统一容器与导航基础。

本阶段重点覆盖以下能力：

- `Single Activity` 架构落地
- `BottomNavigationView` 底部导航
- `Navigation Component` 页面跳转管理
- 多 `Fragment` 页面组织
- 基础 `ViewModel` 页面结构预留
- 页面占位状态与包结构标准化

本阶段不包含：

- 新闻接口接入
- Room 数据库落地
- 新闻详情渲染
- 搜索与收藏业务逻辑

## 2. 当前现状分析

结合当前工程代码，现状如下：

- [`MainActivity.kt`](file:///Users/bytedance/AndroidStudioProjects/Homework/app/src/main/java/com/example/homework/MainActivity.kt) 仅保留 `onCreate()` 空实现，尚未加载界面。
- [`activity_main.xml`](file:///Users/bytedance/AndroidStudioProjects/Homework/app/src/main/res/layout/activity_main.xml) 仍是默认的 `Hello World` 页面。
- [`build.gradle.kts`](file:///Users/bytedance/AndroidStudioProjects/Homework/app/build.gradle.kts) 仅有基础依赖，尚未引入 `Fragment`、`Navigation`、`Lifecycle/ViewModel` 等骨架依赖。

因此，目标 1 的本质是先把“工程骨架 + 页面结构 + 导航机制”建立起来。

## 3. 本阶段产出物

完成后，项目应至少具备以下产出：

- 一个可运行的主界面容器 `MainActivity`
- 一个主布局：上层内容容器 + 下层底部导航
- 一个导航图 `nav_graph.xml`
- 4 个主页面 Fragment：
  - `HomeFragment`
  - `DiscoverFragment`
  - `FavoriteFragment`
  - `ProfileFragment`
- 每个页面各自的布局文件
- 每个页面对应的基础 `ViewModel`
- 一组统一的导航数据模型与页面占位数据模型
- 初步完成包结构划分

## 4. 推荐包结构

建议从目标 1 开始就按后续可扩展结构组织代码。

```text
com.example.homework
├── MainActivity.kt
├── ui
│   ├── home
│   │   ├── HomeFragment.kt
│   │   └── HomeViewModel.kt
│   ├── discover
│   │   ├── DiscoverFragment.kt
│   │   └── DiscoverViewModel.kt
│   ├── favorite
│   │   ├── FavoriteFragment.kt
│   │   └── FavoriteViewModel.kt
│   ├── profile
│   │   ├── ProfileFragment.kt
│   │   └── ProfileViewModel.kt
│   └── model
│       ├── MainTab.kt
│       ├── BottomNavItem.kt
│       └── PagePlaceholderUiState.kt
├── navigation
│   └── AppDestination.kt
└── common
    └── extensions / base（后续可继续扩展）
```

说明：

- `ui` 负责页面与页面级状态。
- `navigation` 用于封装路由常量或导航目标描述。
- `ui/model` 存放当前阶段需要的轻量 UI 模型。
- 暂不急于引入 `data`、`repository`、`domain`，但目录可以在目标 2 再补齐。

## 5. 详细任务步骤

## 5.1 第一步：补充基础依赖与构建配置

任务目标：

- 为多 Fragment 和 Navigation 做依赖准备。
- 开启更适合本项目开发的构建能力。

建议新增内容：

- `androidx.fragment:fragment-ktx`
- `androidx.navigation:navigation-fragment-ktx`
- `androidx.navigation:navigation-ui-ktx`
- `androidx.lifecycle:lifecycle-viewmodel-ktx`
- `androidx.lifecycle:lifecycle-livedata-ktx` 或后续改为 `StateFlow`
- `androidx.activity:activity-ktx`

建议同步配置：

- 开启 `viewBinding`
- 保持 `Java 11 / Kotlin JVM 11`

任务完成标准：

- Gradle 可同步成功
- 无依赖冲突
- 相关类可正常导入

## 5.2 第二步：重构 Activity 主布局

任务目标：

- 用一个真正的应用壳布局替换默认 `Hello World`。

布局组成建议：

- 根布局使用 `ConstraintLayout`
- 内容容器使用 `FragmentContainerView`
- 底部区域使用 `BottomNavigationView`

主布局职责：

- `FragmentContainerView` 承载导航图中的目标 Fragment
- `BottomNavigationView` 负责一级页面切换

建议资源文件：

- `res/layout/activity_main.xml`
- `res/menu/menu_main_bottom_nav.xml`
- `res/navigation/nav_graph.xml`
- `res/values/strings.xml` 中补充页面标题文案

任务完成标准：

- `MainActivity` 能加载新的主布局
- 页面底部显示 4 个 Tab
- 主内容区不再是静态 `TextView`

## 5.3 第三步：实现 MainActivity 的导航绑定逻辑

任务目标：

- 在 Activity 中初始化 `NavController`
- 将 `BottomNavigationView` 与导航图绑定

具体步骤：

1. 在 `MainActivity` 中调用 `setContentView()` 或使用 ViewBinding 绑定主布局。
2. 获取 `FragmentContainerView` 对应的 `NavHostFragment`。
3. 获取 `NavController`。
4. 通过 `NavigationUI.setupWithNavController()` 将底部导航与控制器绑定。
5. 处理系统边距、状态栏适配和必要的返回栈行为。

建议注意点：

- 首页 4 个 Tab 属于一级页面，切换时应保持行为简单稳定。
- 本阶段优先保证跳转清晰，不急于处理复杂返回栈优化。

任务完成标准：

- 点击底部不同 Tab 能切换不同 Fragment
- 切换不会崩溃
- 应用启动默认进入首页

## 5.4 第四步：创建 4 个一级页面 Fragment

任务目标：

- 搭建后续业务页面的宿主骨架

需要创建的类：

- `HomeFragment`
- `DiscoverFragment`
- `FavoriteFragment`
- `ProfileFragment`

每个 Fragment 至少包含：

- 对应布局文件
- 页面标题或占位说明
- 一个基础 ViewModel

当前阶段页面展示建议：

- 首页：显示“首页 / 推荐新闻入口”
- 发现：显示“发现 / 热点专题入口”
- 收藏：显示“收藏 / 历史记录入口”
- 我的：显示“我的 / 设置与个人信息入口”

任务完成标准：

- 4 个 Fragment 均可独立显示
- 每个页面视觉上能区分
- 页面类命名和资源命名统一

## 5.5 第五步：建立基础 ViewModel 层

任务目标：

- 为后续真实数据接入预留页面状态管理结构

建议每个页面对应一个 ViewModel：

- `HomeViewModel`
- `DiscoverViewModel`
- `FavoriteViewModel`
- `ProfileViewModel`

本阶段职责：

- 暂不处理网络请求
- 仅管理页面标题、提示文案、占位说明等基础状态

建议做法：

- 先使用 `LiveData` 或 `StateFlow` 暴露页面占位状态
- Fragment 负责观察状态并刷新 UI

任务完成标准：

- Fragment 不直接硬编码全部展示内容
- 基础展示信息由 ViewModel 提供

## 5.6 第六步：建立导航与占位 UI 数据模型

虽然目标 1 暂未进入真实新闻业务，但建议先建立几类轻量数据模型，统一管理页面定义与占位信息，避免后续结构混乱。

### 数据模型 1：一级页面枚举

用途：

- 统一定义 4 个底部主页面
- 便于后续做路由映射、统计、默认页配置

建议定义：

```kotlin
enum class MainTab {
    HOME,
    DISCOVER,
    FAVORITE,
    PROFILE
}
```

字段说明：

- `HOME`：首页
- `DISCOVER`：发现页
- `FAVORITE`：收藏页
- `PROFILE`：我的页

### 数据模型 2：底部导航项模型

用途：

- 统一维护底部导航的标题、图标、菜单 ID、目标页面

建议定义：

```kotlin
data class BottomNavItem(
    val menuId: Int,
    val title: String,
    val iconResId: Int,
    val tab: MainTab
)
```

字段说明：

| 字段 | 类型 | 说明 |
|------|------|------|
| `menuId` | `Int` | 底部菜单项 ID |
| `title` | `String` | 页面标题 |
| `iconResId` | `Int` | 图标资源 ID |
| `tab` | `MainTab` | 对应主页面类型 |

### 数据模型 3：页面占位状态模型

用途：

- 在功能尚未完成前，先用统一结构描述页面标题、说明文案和状态文本

建议定义：

```kotlin
data class PagePlaceholderUiState(
    val pageTitle: String,
    val pageSubtitle: String,
    val tips: String
)
```

字段说明：

| 字段 | 类型 | 说明 |
|------|------|------|
| `pageTitle` | `String` | 页面主标题 |
| `pageSubtitle` | `String` | 页面副标题或当前阶段说明 |
| `tips` | `String` | 引导文案，用于说明后续将接入哪些功能 |

### 数据模型 4：导航目标描述模型

用途：

- 为后续二级页面扩展做预留，例如详情页、搜索页、设置页

建议定义：

```kotlin
sealed class AppDestination(val route: String) {
    data object Home : AppDestination("home")
    data object Discover : AppDestination("discover")
    data object Favorite : AppDestination("favorite")
    data object Profile : AppDestination("profile")
    data object Search : AppDestination("search")
    data object Settings : AppDestination("settings")
}
```

字段说明：

- `route`：目标页面标识，用于统一管理导航目标名称

说明：

- 即使当前 XML Navigation 不直接使用字符串 `route`，该模型依然有价值，便于后续转向更统一的路由描述方式。

## 5.7 第七步：完成导航图配置

任务目标：

- 用 `nav_graph.xml` 定义应用的一级页面结构

推荐配置：

- `startDestination` 指向 `HomeFragment`
- 为 4 个一级页面分别声明 destination
- 本阶段先不强制加入复杂 action，但可为搜索页和设置页预留扩展位置

建议说明：

- 一级页面切换由底部导航驱动
- 二级页面跳转将在目标 2 或目标 3 再详细补全

任务完成标准：

- 导航图结构清晰
- Destination ID 命名规范
- 首页为默认启动页

## 5.8 第八步：统一页面样式与命名规范

任务目标：

- 从骨架阶段就规范命名与样式，降低后续返工概率

命名建议：

- Fragment：`HomeFragment`
- ViewModel：`HomeViewModel`
- 布局：`fragment_home.xml`
- 菜单：`menu_main_bottom_nav.xml`
- 导航图：`nav_graph.xml`

UI 建议：

- 4 个页面先采用简洁一致的占位样式
- 使用 Material 风格文本和间距
- 保持标题层级一致

任务完成标准：

- 页面视觉风格统一
- 文件命名统一
- 无明显重复代码

## 5.9 第九步：完成自测与联调检查

任务目标：

- 在进入目标 2 前，确保骨架稳定可扩展

建议检查项：

1. 应用启动是否正常
2. 4 个底部页面能否来回切换
3. 页面旋转或前后台切换后是否仍稳定
4. 是否存在空指针或导航崩溃
5. 是否已为后续业务页面预留清晰结构

任务完成标准：

- 通过基础手工测试
- 不存在影响继续开发的结构性问题

## 6. 具体任务清单

可以按以下顺序逐项执行：

| 序号 | 任务 | 输出 |
|------|------|------|
| 1 | 补充依赖与开启 ViewBinding | `build.gradle.kts` 可同步 |
| 2 | 重写主布局 | 新版 `activity_main.xml` |
| 3 | 新建底部菜单资源 | `menu_main_bottom_nav.xml` |
| 4 | 新建导航图 | `nav_graph.xml` |
| 5 | 实现 `MainActivity` 导航绑定 | Activity 可承载 Fragment |
| 6 | 创建 4 个 Fragment 与布局 | 页面骨架完成 |
| 7 | 创建 4 个 ViewModel | 页面状态结构完成 |
| 8 | 增加导航与 UI 占位模型 | 模型结构清晰 |
| 9 | 自测启动、切换、返回行为 | 骨架阶段验收通过 |

## 7. 验收标准

本阶段完成后，需要满足以下验收标准：

### 7.1 功能验收

- 应用启动后默认进入首页
- 底部导航展示首页、发现、收藏、我的 4 个入口
- 点击不同底部项时可进入对应 Fragment
- 每个页面都有明确的占位内容，不是空白页

### 7.2 结构验收

- 采用 `Single Activity + Multi Fragment`
- 已接入 `Navigation Component`
- 每个页面有独立 Fragment、布局和 ViewModel
- 包结构清晰，便于继续接入业务模块

### 7.3 代码质量验收

- 类名、资源名、ID 命名规范统一
- 无明显硬编码跳转逻辑散落在多个地方
- 导航关系清晰，后续可扩展详情、搜索、设置页面

### 7.4 可扩展性验收

- 目标 2 可以直接在 `HomeFragment` 基础上接入新闻列表
- 目标 3 可以直接扩展二级详情页
- 目标 4 与目标 7 可分别从 `HomeFragment` / `ProfileFragment` 扩展搜索与设置入口

## 8. 风险点与规避建议

### 风险 1：一开始就把目录拆得过重

影响：

- 对当前课程项目来说会增加理解成本

规避建议：

- 本阶段只保留必要目录
- 等进入目标 2 再引入 `data`、`repository` 等更完整层次

### 风险 2：导航结构和后续需求不兼容

影响：

- 详情页、搜索页接入时可能返工

规避建议：

- 一级页面只处理主导航
- 二级页面统一纳入同一 `NavHostFragment` 管理

### 风险 3：页面先写死展示文本，后续重构成本高

影响：

- Fragment 会变得臃肿

规避建议：

- 本阶段就引入基础 ViewModel 和占位状态模型

## 9. 预估工时

以单人课程项目为前提，目标 1 预计耗时如下：

| 子任务 | 预估耗时 |
|------|------|
| 依赖与构建配置 | 0.5 天 |
| 主布局与底部导航 | 0.5 天 |
| Navigation 接入 | 0.5 天 |
| 4 个 Fragment 与布局 | 0.5 天 |
| ViewModel 与 UI 模型 | 0.5 天 |
| 自测与修正 | 0.5 天 |
| 合计 | 3 天左右 |

## 10. 下一阶段衔接

完成本计划后，下一步即可进入 `目标 2：首页新闻列表`，重点开展以下内容：

- 首页 RecyclerView 结构设计
- 新闻列表数据模型定义
- Retrofit/OkHttp 接口接入
- 列表项 UI 设计
- 刷新与加载状态管理
