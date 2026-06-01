package com.example.homework.data.remote.datasource

enum class NewsDataSourceMode {
    MOCK,
    REMOTE
}

object NewsDataSourceFactory {
    // 后续切换到真实网络请求时，只需要把这里改成 REMOTE。
    var currentMode: NewsDataSourceMode = NewsDataSourceMode.MOCK

    fun createDefault(): NewsDataSource {
        return when (currentMode) {
            NewsDataSourceMode.MOCK -> MockNewsDataSource()
            NewsDataSourceMode.REMOTE -> RemoteNewsDataSource()
        }
    }
}
