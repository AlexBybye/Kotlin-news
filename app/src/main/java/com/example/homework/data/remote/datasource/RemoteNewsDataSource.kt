package com.example.homework.data.remote.datasource

import com.example.homework.data.remote.api.BackendApi
import com.example.homework.data.remote.dto.NewsDetailDto
import com.example.homework.data.remote.dto.NewsListResponseDto
import com.example.homework.data.remote.network.ResultWrapper
import com.example.homework.data.remote.network.RetrofitClient
import com.example.homework.model.NewsCategory

/**
 * 真实新闻数据源：调用自建后端（后端再代理 NewsAPI）。
 *
 * 网络异常或后端返回失败时返回 [ResultWrapper.Error]，
 * 由上层 [com.example.homework.data.repository.NewsRepository] 回退到本地缓存。
 */
class RemoteNewsDataSource(
    private val api: BackendApi = RetrofitClient.create()
) : NewsDataSource {

    override suspend fun getNews(category: NewsCategory): ResultWrapper<NewsListResponseDto> {
        return runCatching {
            val response = api.getNews(category.apiValue)
            if (response.code != 0 || response.data == null) {
                ResultWrapper.Error(response.message ?: "新闻接口返回异常。")
            } else {
                ResultWrapper.Success(
                    NewsListResponseDto(code = 0, message = "success", data = response.data)
                )
            }
        }.getOrElse { throwable ->
            ResultWrapper.Error(throwable.message ?: "网络请求失败，请检查后端服务与网络后重试。")
        }
    }

    override suspend fun getNewsDetail(newsId: String): ResultWrapper<NewsDetailDto> {
        return runCatching {
            val response = api.getNewsDetail(newsId)
            if (response.code != 0 || response.data == null) {
                ResultWrapper.Error(response.message ?: "未找到对应新闻详情。")
            } else {
                ResultWrapper.Success(response.data)
            }
        }.getOrElse { throwable ->
            ResultWrapper.Error(throwable.message ?: "网络请求失败，请稍后重试。")
        }
    }
}
