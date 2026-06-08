# WEATHER_report：天气 SDK 与后台 Service 模块单独报告

> 项目：校园新闻 APP  
> 包名：`com.example.homework`  
> 模块主题：和风天气第三方接口接入 + 天气后台同步 Service 化任务  
> 说明：本文档为天气模块的单独报告，可作为总报告中“第三方 SDK/API 与 Service”部分的独立补充。

---

## 一、任务要求与模块目标

### 1.1 作业任务拆解

| 作业任务 | 要求说明 | 本项目对应实现 |
|----|----|----|
| 任务 1：使用 Service | 设计并实现一款使用 Service 的应用，熟悉 Start Service、Bind Service、Foreground Service 或其它后台服务能力 | 使用 `WeatherSyncWorker` + `WeatherSyncScheduler` 实现天气后台同步任务，由 WorkManager 按周期调度，底层依赖 Android 系统服务完成后台执行 |
| 任务 2：使用第三方 SDK/API | 设计并实现一款使用第三方 SDK 的应用，如地图、天气或其它功能 | 接入和风天气 QWeather 实时天气接口 `v7/weather/now` 与城市查询接口 `geo/v2/city/lookup`，在首页展示用户选择城市的实时天气 |

### 1.2 天气模块目标

天气模块服务于首页浏览体验：用户打开首页时，可以在新闻列表上方看到当前城市天气，包括城市、温度、天气状况、体感温度和湿度。模块同时支持后台定时同步、城市选择、定位自动选城与本地缓存兜底，即使短时间无网或接口失败，首页仍可以读取当前城市最近一次缓存数据。

本模块默认城市为广州，城市 ID 为 `101280101`，对应华南理工大学所在地。用户可在“设置 → 天气城市”中切换常用城市，城市 ID 与展示名持久化到 DataStore；也可授权定位后自动解析当前位置对应的和风天气城市 ID。

---

## 二、总体设计

### 2.1 架构分层

天气模块遵循项目整体 MVVM + Repository 分层，没有让 Activity 或 Fragment 直接访问网络接口。

```
HomeFragment
    ↓ 观察 LiveData
HomeViewModel
    ↓ 调用
WeatherRepository
    ↓ 读取 DataStore 城市 / 成功写缓存 / 失败读缓存
WeatherRetrofitClient + QWeatherApi
    ↓
devapi.qweather.com

城市选择：
SettingsFragment
    ↓
WeatherCityFragment
    ↓ 手动选择 / 原生定位
WeatherCityViewModel
    ↓ 保存城市 ID
SettingsManager(DataStore)

后台同步：
NewsApplication
    ↓ 注册周期任务
WeatherSyncScheduler
    ↓ WorkManager 调度
WeatherSyncWorker
    ↓
WeatherRepository
```

这样的设计使 UI、业务逻辑、网络请求和缓存职责清晰分离，也与项目其它新闻、搜索、收藏等模块的 Repository 风格保持一致。

### 2.2 核心原则

1. 包名统一使用 `com.example.homework`，不使用模板包名 `com.example.kotlin_news`。
2. JSON 解析复用项目已有 Moshi，不引入 Gson，避免两套 JSON 库并存。
3. Retrofit、OkHttp、WorkManager、`buildConfig = true` 在项目之前阶段已具备，天气模块不新增依赖。
4. 天气接口使用独立 `WeatherRetrofitClient`，不复用后端 `RetrofitClient`，防止自建后端 JWT 被发送给第三方域名。
5. 天气数据通过 `WeatherRepository` 统一返回 `ResultWrapper`，成功缓存，失败回退缓存。
6. 首页通过 `HomeViewModel.weather` 暴露天气 LiveData，Fragment 只负责渲染。
7. 天气城市通过 `SettingsManager` 写入 DataStore，后台 Worker 与首页读取同一份城市配置。
8. 定位能力使用 Android 原生 `LocationManager`，避免为了定位新增 SDK 依赖。

---

## 三、任务 1：后台 Service 化天气同步

### 3.1 设计方案

天气数据不需要长时间占用前台，也不需要用户立即感知后台执行过程，因此选择 WorkManager 实现周期后台任务。WorkManager 会根据系统版本自动适配 JobScheduler、AlarmManager 等系统调度能力，适合“每隔一段时间联网同步一次天气”的场景。

本项目的后台同步由两个文件完成：

| 文件 | 职责 |
|----|----|
| `WeatherSyncWorker.kt` | 后台执行单元，调用 `WeatherRepository.getCurrentWeather()` 拉取天气并写入缓存 |
| `WeatherSyncScheduler.kt` | 任务注册入口，设置联网约束、周期时间和唯一任务名 |

应用启动时，`NewsApplication.onCreate()` 调用 `WeatherSyncScheduler.schedule(this)` 注册周期任务。任务采用唯一名称 `weather_sync_work`，避免重复注册多个相同天气同步任务。

### 3.2 执行流程

1. App 启动，进入 `NewsApplication.onCreate()`。
2. 调用 `WeatherSyncScheduler.schedule(this)`。
3. `WeatherSyncScheduler` 构建 `PeriodicWorkRequestBuilder<WeatherSyncWorker>(1, TimeUnit.HOURS)`。
4. 设置网络约束 `NetworkType.CONNECTED`，保证只有联网时才执行。
5. 使用 `enqueueUniquePeriodicWork()` 注册唯一周期任务。
6. 系统在合适时机调度 `WeatherSyncWorker.doWork()`。
7. Worker 内部创建 `WeatherRepository`，调用和风天气接口。
8. 请求成功时写入 `WeatherCache`；请求失败时返回 `Result.retry()`，等待 WorkManager 后续重试。

### 3.3 Service 使用点说明

虽然本模块没有手写传统 `Service` 子类，但它实现的是 Android 后台服务化任务：天气同步不依赖 Activity 生命周期，退出首页后仍可由系统调度。WorkManager 是 Android 官方推荐的可延迟、需保证执行的后台任务方案，底层通过系统服务完成调度，适合本项目“周期、联网、轻量同步”的天气场景。

相比直接使用启动服务或前台服务，本方案有以下优点：

| 方案 | 适用场景 | 天气模块取舍 |
|----|----|----|
| Start Service | 立即执行、短期后台操作 | 天气无需每次手动启动服务 |
| Bind Service | Activity 与 Service 双向通信 | 天气同步不需要持续绑定 UI |
| Foreground Service | 长时间运行且用户必须感知，如音乐播放、定位 | 天气同步很轻量，不适合常驻通知 |
| WorkManager | 可延迟、周期性、需满足网络约束的后台任务 | 本项目采用，稳定且省电 |

---

## 四、任务 2：第三方天气 SDK/API 接入

### 4.1 第三方服务选择

本模块接入和风天气 QWeather 实时天气接口：

- 第三方服务：和风天气 QWeather
- 接口域名：`https://devapi.qweather.com/`
- 实时天气接口：`v7/weather/now`
- 城市反查接口：`geo/v2/city/lookup`
- 请求参数：`location`、`key`、`lang`
- 默认语言：`zh`
- 默认城市：广州 `101280101`

项目没有额外引入和风天气官方 SDK 包，而是复用已有 Retrofit/OkHttp/Moshi 直接接入 RESTful API。这样可以满足第三方天气能力接入要求，同时保持依赖零新增。

### 4.2 接口定义

`QWeatherApi` 使用 Retrofit 注解声明接口：

```kotlin
@GET("v7/weather/now")
suspend fun getCurrentWeather(
    @Query("location") location: String,
    @Query("key") apiKey: String,
    @Query("lang") lang: String = "zh"
): WeatherResponseDto
```

天气响应由 `WeatherResponseDto` 承接，只声明本项目实际使用的字段：接口状态码、更新时间、温度、体感温度、天气文字、天气图标代码、风向、风力和湿度。

定位选城通过同一个 `QWeatherApi` 调用 GeoAPI：

```kotlin
@GET("geo/v2/city/lookup")
suspend fun lookupCity(
    @Query("location") location: String,
    @Query("key") apiKey: String,
    @Query("lang") lang: String = "zh",
    @Query("number") number: Int = 1
): WeatherCityLookupResponseDto
```

其中 `location` 使用原生定位得到的 `longitude,latitude` 格式。接口返回后，`WeatherRepository.resolveCityByCoordinates()` 取最近城市的 `id/name/adm1/adm2`，再交给 `SettingsManager.setWeatherLocation()` 保存。

### 4.3 密钥管理

天气 API Key 不硬编码在 Kotlin 源码中，而是写入 `local.properties`：

```properties
QWEATHER_API_KEY=你的和风天气密钥
```

Gradle 在构建时读取该值并注入：

```kotlin
buildConfigField("String", "QWEATHER_API_KEY", "\"$qweatherApiKey\"")
```

业务层通过 `BuildConfig.QWEATHER_API_KEY` 获取密钥。`local.properties` 已被 `.gitignore` 忽略，避免把个人密钥提交到仓库。

---

## 五、城市选择与定位能力

### 5.1 城市 ID 持久化

`AppSettings` 新增天气城市字段：

| 字段 | 默认值 | 说明 |
|----|----|----|
| `weatherLocationId` | `101280101` | 和风天气城市 ID，默认广州 |
| `weatherCityName` | `广州` | UI 展示城市名 |

`SettingsManager` 使用 DataStore 保存这两个字段，并额外写入 SharedPreferences 同步镜像，供首页首屏读取缓存时快速判断缓存是否属于当前城市。

### 5.2 城市选择页面

新增 `WeatherCityFragment` 作为独立页面，入口位于“设置 → 天气城市”。页面能力包括：

- 展示当前城市名与城市 ID；
- 提供广州、北京、上海、深圳、杭州、南京、成都、武汉、西安、重庆等常用城市；
- 点击常用城市后立即保存该城市 ID 到 DataStore；
- 如果当前城市来自定位且不在常用城市列表中，则只显示当前城市，不强行选中某个 chip。

### 5.3 定位自动选城

定位能力使用 Android 原生 `LocationManager`，并在 `AndroidManifest.xml` 中声明：

```xml
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
```

执行流程：

1. 用户在天气城市页点击“使用当前位置”。
2. Fragment 通过 `ActivityResultContracts.RequestMultiplePermissions` 申请定位权限。
3. `DeviceLocationProvider` 优先读取最近一次定位；若没有可用位置，则发起单次定位请求。
4. 得到经纬度后，`WeatherRepository.resolveCityByCoordinates()` 调用和风天气 GeoAPI。
5. 取最近城市的城市 ID 与展示名，写入 DataStore。
6. 返回首页时 `HomeFragment.onResume()` 重新加载天气，天气卡片切换为新城市。

---

## 六、独立 WeatherRetrofitClient 的必要性

这是天气模块最关键的安全设计。

项目原有 `RetrofitClient` 面向自建后端，内部 OkHttp 拦截器会自动给请求添加登录后的 JWT：

```kotlin
authToken?.let { token ->
    builder.header("Authorization", "Bearer $token")
}
```

如果天气请求复用这个客户端，请求 `devapi.qweather.com` 时也可能携带后端登录令牌，造成后端 token 泄露给第三方服务。因此天气模块单独创建 `WeatherRetrofitClient`：

- base URL 固定为 `https://devapi.qweather.com/`；
- 使用独立 OkHttpClient；
- 不添加 Authorization 拦截器；
- JSON 解析仍使用 Moshi；
- 日志级别为 BASIC，仅用于调试请求状态。

这一点保证了“后端接口认证”和“第三方天气接口调用”边界清晰，符合移动端网络安全实践。

---

## 七、数据与缓存设计

### 7.1 领域模型

`WeatherNow` 是 UI 直接消费的领域模型：

| 字段 | 含义 |
|----|----|
| `cityName` | 展示城市名 |
| `temperature` | 实时温度 |
| `feelsLike` | 体感温度 |
| `text` | 天气状况文字 |
| `iconCode` | 和风天气图标代码 |
| `windDir` | 风向 |
| `windScale` | 风力等级 |
| `humidity` | 湿度 |
| `updateTime` | 接口更新时间 |

### 7.2 Repository 逻辑

`WeatherRepository.getCurrentWeather()` 负责完整业务流程：

1. 从 DataStore 读取当前天气城市 ID 与展示城市名。
2. 检查 `BuildConfig.QWEATHER_API_KEY` 是否为空。
3. 调用 `QWeatherApi.getCurrentWeather()`。
4. 校验接口 `code == "200"` 且 `now != null`。
5. 将 DTO 转换为 `WeatherNow`。
6. 成功后调用 `WeatherCache.save()` 写入缓存。
7. 返回 `ResultWrapper.Success(weather)`。
8. 如果密钥缺失、接口异常或网络失败，则读取当前城市最近一次缓存。
9. 有同城市缓存时仍返回 `Success(cached)`；无缓存时返回 `ResultWrapper.Error(message)`。

这种缓存兜底策略使首页天气具备离线可用能力，不会因为天气接口失败影响新闻主流程；同时避免切换城市后错误展示旧城市缓存。

### 7.3 缓存实现

`WeatherCache` 使用 `SharedPreferences + Moshi` 保存天气 JSON。由于项目已有 Moshi，缓存序列化也复用 Moshi：

- 不引入 Gson；
- 不额外增加数据库表；
- Worker 与 UI 可以共享同一份缓存；
- 首页首屏可以先读缓存，再等待网络刷新。

---

## 八、首页 UI 集成

### 8.1 天气卡片

首页 `fragment_home.xml` 在标题下方、搜索卡片上方新增 `weatherCard`。卡片展示内容包括：

- 天气图标；
- 城市名；
- 当前温度；
- 天气状况；
- 体感温度；
- 湿度。

布局约束链为：

```
subtitleText
    ↓
weatherCard
    ↓
searchCard
    ↓
categoryScrollView
```

天气卡片默认 `visibility="gone"`，当 `HomeFragment.renderWeather()` 收到有效天气数据时再显示，避免无缓存、无网络时出现空白卡片。

### 8.2 图标策略

天气图标没有引入图片资源，而是通过 `WeatherIconMapper` 将和风天气 icon code 映射为 emoji。这样可以避免图片缺失、资源体积增加和离线加载问题。

示例映射：

| icon code | 天气含义 | 展示 |
|----|----|----|
| `100` / `150` | 晴 | 太阳图标 |
| `101` / `102` / `103` | 多云 | 多云图标 |
| `300` - `399` | 雨 | 雨图标 |
| `400` - `499` | 雪 | 雪图标 |
| 其它 | 默认 | 温度计图标 |

### 8.3 下拉刷新联动

首页下拉刷新时同时刷新新闻和天气：

```kotlin
binding.swipeRefreshLayout.setOnRefreshListener {
    viewModel.refresh()
    viewModel.loadWeather()
}
```

这样用户不需要进入额外页面，就能同步更新首页新闻列表与天气数据。

### 8.4 天气城市页

天气城市页位于设置页下一级，独立承担天气位置配置：

- 当前城市区显示城市名和城市 ID，便于答辩时说明“城市 ID 已持久化”；
- 常用城市用单选 chip 呈现，点击后写入 DataStore；
- “使用当前位置”入口负责申请定位权限、获取经纬度、调用 GeoAPI 解析城市 ID；
- 选择完成后返回首页，`HomeFragment.onResume()` 会重新加载天气，保证首页卡片跟随城市配置变化。

---

## 九、落地文件清单

### 9.1 新增天气模块文件

| 文件 | 类型 | 说明 |
|----|----|----|
| `model/WeatherNow.kt` | 领域模型 | 首页直接展示的天气数据结构 |
| `model/WeatherCity.kt` | 领域模型 | 城市 ID、城市名与行政区信息 |
| `data/remote/dto/WeatherResponseDto.kt` | DTO | 和风天气接口响应模型，使用 Moshi `@Json` 注解 |
| `data/remote/dto/WeatherCityLookupDto.kt` | DTO | 和风天气 GeoAPI 城市查询响应模型 |
| `data/remote/api/QWeatherApi.kt` | API 接口 | Retrofit 声明 `v7/weather/now` 与 `geo/v2/city/lookup` 请求 |
| `data/remote/network/WeatherRetrofitClient.kt` | 网络客户端 | 独立天气 Retrofit/OkHttp 实例，避免 JWT 泄露 |
| `data/repository/WeatherRepository.kt` | 仓库层 | 请求天气、DTO 转 Model、缓存兜底、统一 `ResultWrapper` |
| `data/repository/WeatherCache.kt` | 缓存 | SharedPreferences + Moshi 保存最近一次天气 |
| `data/location/DeviceLocationProvider.kt` | 定位 | 原生 LocationManager 获取当前位置 |
| `util/WeatherIconMapper.kt` | 工具 | 和风天气 icon code 到本地安全图标的映射 |
| `ui/weather/WeatherCityViewModel.kt` | ViewModel | 城市选择、定位解析、DataStore 写入 |
| `ui/weather/WeatherCityFragment.kt` | UI | 天气城市选择页面 |
| `work/WeatherSyncWorker.kt` | 后台任务 | 周期同步天气并写入缓存 |
| `work/WeatherSyncScheduler.kt` | 调度器 | 注册每小时联网执行的唯一周期任务 |
| `res/layout/fragment_weather_city.xml` | UI 布局 | 天气城市选择页布局 |

### 9.2 修改的既有文件

| 文件 | 修改点 |
|----|----|
| `app/build.gradle.kts` | 从 `local.properties` 读取 `QWEATHER_API_KEY`，注入 `BuildConfig` |
| `NewsApplication.kt` | App 启动时注册天气后台同步任务 |
| `AppSettings.kt` / `SettingsManager.kt` | 新增天气城市 ID/城市名 DataStore 持久化 |
| `HomeViewModel.kt` | 新增 `weather` LiveData 与 `loadWeather()`，按当前城市请求天气 |
| `HomeFragment.kt` | 观察天气数据并渲染天气卡片，下拉刷新/返回首页时同步刷新天气 |
| `SettingsFragment.kt` / `fragment_settings.xml` | 设置页新增“天气城市”入口和当前城市展示 |
| `nav_graph.xml` | 新增天气城市页导航节点与设置页跳转 action |
| `fragment_home.xml` | 首页顶部新增天气卡片，保持约束链完整 |
| `strings.xml` | 新增天气温度、城市选择、定位等字符串 |
| `AndroidManifest.xml` | 新增定位权限，已具备网络权限与联网约束判断 |

---

## 十、作业要求对照表

| 要求 | 落地实现 | 说明 |
|----|----|----|
| 使用 Service | `WeatherSyncWorker` + `WeatherSyncScheduler` | 后台周期同步天气，脱离首页生命周期运行 |
| 可采用任一种 Service 实现 | 采用 WorkManager 后台服务化任务 | 适合周期、联网、轻量同步 |
| 使用第三方 SDK/API | 和风天气 QWeather | 请求实时天气接口并展示 |
| 包名正确 | `com.example.homework` | 与项目 namespace/applicationId 一致 |
| JSON 库复用 | Moshi | DTO 与缓存均使用 Moshi，无 Gson |
| 依赖零新增 | 复用已有 Retrofit/OkHttp/WorkManager | 没有为了天气模块增加新依赖 |
| 独立 Retrofit | `WeatherRetrofitClient` | 不复用带 JWT 拦截器的后端 `RetrofitClient` |
| MVVM 分层 | Repository -> ViewModel -> Fragment | Activity/Fragment 不直接发请求 |
| 缓存兜底 | `WeatherCache` | 网络失败时展示最近一次天气 |
| 密钥不硬编码 | `local.properties` -> `BuildConfig.QWEATHER_API_KEY` | 避免密钥写入源码 |
| 城市 ID 持久化 | `SettingsManager` + DataStore | 保存 `weatherLocationId` / `weatherCityName` |
| 定位自动选城 | `DeviceLocationProvider` + `geo/v2/city/lookup` | 原生定位获取经纬度，再解析和风天气城市 ID |

---

## 十一、验证状态

天气模块已完成静态校验：

1. 首页 5 个天气控件与布局 ID 一一对应：`weatherCard`、`weatherIconText`、`weatherCityText`、`weatherTempText`、`weatherDescText`。
2. 首页约束链完整：`weatherCard -> searchCard -> categoryScrollView`。
3. 天气城市页控件与绑定 ID 一一对应：`currentCityText`、`useCurrentLocationRow`、`cityChipGroup` 等。
4. 天气相关字符串已在 `strings.xml` 定义，无未定义字符串引用。
5. 天气 DTO 与缓存均使用 Moshi，项目中无新增 Gson 依赖。
6. 天气模块新增/修改文件的 import 均可解析，包名均为 `com.example.homework`。
7. `local.properties` 已被 `.gitignore` 忽略，天气 API Key 不会进入版本库。
8. 天气请求使用 `WeatherRetrofitClient`，不会携带后端 JWT Authorization 头。
9. 城市 ID/城市名由 DataStore 持久化，后台 Worker 与首页读取同一份配置。
10. 定位权限已在 Manifest 声明，Fragment 使用运行时权限申请。

---

## 十二、优点与不足

### 12.1 优点

- 模块职责清晰，网络、缓存、后台同步和 UI 渲染分层明确。
- 使用独立天气 Retrofit 实例，避免后端 JWT 泄露到第三方域名，安全边界清楚。
- 不新增依赖，复用项目已有 Retrofit、OkHttp、Moshi、WorkManager，工程复杂度低。
- 缓存兜底完善，弱网或接口失败时首页仍可展示最近一次天气。
- 支持常用城市选择与定位自动选城，城市 ID 持久化后可被首页与后台任务共同使用。
- 后台周期同步让首页首屏更容易展示可用天气数据。

### 12.2 不足

- 天气同步只缓存当前天气，没有扩展未来逐小时或逐日预报。
- WorkManager 周期任务受系统调度策略影响，不能保证精确每小时立即执行。
- 原生定位依赖设备定位服务与权限授权；模拟器如果没有注入定位，可能需要手动选择城市演示。

### 12.3 改进方向

- 扩展 24 小时天气、7 天天气预报和灾害预警。
- 对 `WeatherRepository` 增加单元测试，覆盖密钥缺失、接口异常、缓存命中和缓存缺失等场景。
- 支持更多城市搜索，而不仅是常用城市列表。

---

## 十三、总结

天气模块围绕课程“Service 使用”和“第三方 SDK/API 使用”两个任务进行设计：一方面通过 WorkManager 后台服务化任务实现每小时联网同步天气，另一方面通过和风天气接口展示实时天气，并进一步支持城市选择、城市 ID DataStore 持久化和定位自动选城。实现过程中坚持项目原有 MVVM + Repository 架构，复用 Moshi 与已有网络依赖，并单独创建 `WeatherRetrofitClient` 防止后端 JWT 泄露。

最终效果是：首页能够展示当前配置城市的实时天气，下拉刷新可同步更新天气，后台任务可周期刷新缓存；在无网或接口失败时，也能回退当前城市最近一次天气缓存，保证模块可演示、可维护且符合课程报告要求。
