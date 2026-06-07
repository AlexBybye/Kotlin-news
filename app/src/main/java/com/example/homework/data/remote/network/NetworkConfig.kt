package com.example.homework.data.remote.network

/**
 * 网络配置。
 *
 * 默认数据源为本地 Mock（见 NewsDataSourceFactory），保证无网络 / 无 API Key 时应用仍可完整演示。
 * 如需接入真实新闻接口（聚合数据 · 头条新闻），在此填入申请到的 APP_KEY，
 * 并将 NewsDataSourceFactory.currentMode 切换为 REMOTE 即可。
 */
object NetworkConfig {
    const val BASE_URL = "http://v.juhe.cn/toutiao/"

    /** 聚合数据头条新闻接口 AppKey，演示时如使用真实接口请替换为有效值。 */
    const val JUHE_APP_KEY = ""

    const val CONNECT_TIMEOUT_SECONDS = 15L
    const val READ_TIMEOUT_SECONDS = 15L
}
