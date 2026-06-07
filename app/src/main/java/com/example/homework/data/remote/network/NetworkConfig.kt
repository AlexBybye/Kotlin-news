package com.example.homework.data.remote.network

/**
 * 网络配置。
 *
 * 应用支持两种取数模式（见 NewsDataSourceFactory）：
 *  - MOCK（默认）：使用内置高质量校园新闻数据，无网络 / 无后端即可完整演示，保证可运行。
 *  - REMOTE：走「App → 自建后端 → NewsAPI」链路，新闻与账号均由后端提供，
 *            后端不可用时上层 Repository 自动回退本地缓存 / 本地账号。
 *
 * 后端地址说明：
 *  - Android 模拟器访问开发机宿主机请用 10.0.2.2；
 *  - 真机请改为后端所在主机的局域网 IP，例如 http://192.168.x.x:8080/。
 */
object NetworkConfig {
    const val BACKEND_BASE_URL = "http://10.0.2.2:8080/"

    const val CONNECT_TIMEOUT_SECONDS = 15L
    const val READ_TIMEOUT_SECONDS = 15L
}
