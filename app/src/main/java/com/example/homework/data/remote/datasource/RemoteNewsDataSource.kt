package com.example.homework.data.remote.datasource

import com.example.homework.data.remote.dto.NewsDetailDto
import com.example.homework.data.remote.dto.NewsListResponseDto
import com.example.homework.data.remote.network.ResultWrapper
import com.example.homework.model.NewsCategory

class RemoteNewsDataSource : NewsDataSource {

    override suspend fun getNews(category: NewsCategory): ResultWrapper<NewsListResponseDto> {
        return ResultWrapper.Error(
            "当前仍在使用 Mock 阶段，后续只需在 RemoteNewsDataSource 中接入真实接口即可。"
        )
    }

    override suspend fun getNewsDetail(newsId: String): ResultWrapper<NewsDetailDto> {
        return ResultWrapper.Error(
            "当前仍在使用 Mock 阶段，后续只需在 RemoteNewsDataSource 中接入真实详情接口即可。"
        )
    }
}
