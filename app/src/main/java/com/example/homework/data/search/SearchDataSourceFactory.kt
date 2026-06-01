package com.example.homework.data.search

enum class SearchDataSourceMode {
    MOCK,
    REMOTE
}

object SearchDataSourceFactory {
    // 后续切换到真实搜索接口时，只需要把这里改成 REMOTE。
    var currentMode: SearchDataSourceMode = SearchDataSourceMode.MOCK

    fun createDefault(): SearchDataSource {
        return when (currentMode) {
            SearchDataSourceMode.MOCK -> MockSearchDataSource()
            SearchDataSourceMode.REMOTE -> RemoteSearchDataSource()
        }
    }
}
