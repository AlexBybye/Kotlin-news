package com.example.homework.data.search

import com.example.homework.data.remote.dto.NewsArticleDto
import com.example.homework.data.remote.network.ResultWrapper

class RemoteSearchDataSource : SearchDataSource {
    override suspend fun searchNews(keyword: String): ResultWrapper<List<NewsArticleDto>> {
        return ResultWrapper.Error(
            "当前仍在使用 Mock 阶段，后续只需在 RemoteSearchDataSource 中接入真实搜索接口即可。"
        )
    }
}
