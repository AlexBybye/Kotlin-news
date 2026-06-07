package com.example.homework.data.config

import com.example.homework.data.remote.datasource.NewsDataSourceFactory
import com.example.homework.data.remote.datasource.NewsDataSourceMode

/**
 * 应用级数据来源开关。
 *
 * [useBackend] 为唯一总开关：
 *  - false（默认）：账号走本地 Room，新闻走本地 Mock —— 无后端 / 无网络也能完整演示，保证可运行。
 *  - true：账号与新闻均走「App → 自建后端 → NewsAPI」链路；后端不可用时各 Repository 自动回退本地。
 *
 * 切换入口集中在此，避免散落多处。
 */
object AppConfig {

    @Volatile
    var useBackend: Boolean = false
        set(value) {
            field = value
            NewsDataSourceFactory.currentMode =
                if (value) NewsDataSourceMode.REMOTE else NewsDataSourceMode.MOCK
        }
}
