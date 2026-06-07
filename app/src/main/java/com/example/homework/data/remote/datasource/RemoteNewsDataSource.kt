package com.example.homework.data.remote.datasource

import com.example.homework.data.mapper.JuheNewsMapper
import com.example.homework.data.remote.api.JuheNewsApi
import com.example.homework.data.remote.dto.JuheNewsItemDto
import com.example.homework.data.remote.dto.NewsDetailDto
import com.example.homework.data.remote.dto.NewsListResponseDto
import com.example.homework.data.remote.network.NetworkConfig
import com.example.homework.data.remote.network.ResultWrapper
import com.example.homework.data.remote.network.RetrofitClient
import com.example.homework.model.NewsCategory

/**
 * 真实新闻数据源（聚合数据 · 头条新闻）。
 *
 * 通过 Retrofit + OkHttp 发起请求，并把返回结果映射为应用内部统一的 DTO。
 * 由于头条接口只提供列表、没有独立详情接口，这里缓存最近拉取的条目用于详情展示。
 * 当未配置 APP_KEY 或网络异常时返回 [ResultWrapper.Error]，由上层 Repository 回退到本地缓存。
 */
class RemoteNewsDataSource(
    private val api: JuheNewsApi = RetrofitClient.create()
) : NewsDataSource {

    override suspend fun getNews(category: NewsCategory): ResultWrapper<NewsListResponseDto> {
        if (NetworkConfig.JUHE_APP_KEY.isBlank()) {
            return ResultWrapper.Error("尚未配置新闻接口 AppKey，已切换到本地缓存数据。")
        }

        return runCatching {
            val response = api.getNews(
                type = category.juheType,
                key = NetworkConfig.JUHE_APP_KEY
            )
            if (response.errorCode != 0 || response.result?.data == null) {
                ResultWrapper.Error(response.reason ?: "新闻接口返回异常。")
            } else {
                val items = response.result.data
                cacheItems(items)
                val articles = items.map { JuheNewsMapper.toArticleDto(it, category) }
                ResultWrapper.Success(
                    NewsListResponseDto(code = 0, message = "success", data = articles)
                )
            }
        }.getOrElse { throwable ->
            ResultWrapper.Error(throwable.message ?: "网络请求失败，请检查网络后重试。")
        }
    }

    override suspend fun getNewsDetail(newsId: String): ResultWrapper<NewsDetailDto> {
        val item = recentItems[newsId]
            ?: return ResultWrapper.Error("未找到对应新闻详情，请返回列表重试。")
        val category = NewsCategory.RECOMMEND
        return ResultWrapper.Success(JuheNewsMapper.toDetailDto(item, category))
    }

    private fun cacheItems(items: List<JuheNewsItemDto>) {
        items.forEach { item ->
            val key = item.uniqueKey ?: item.url ?: item.title.orEmpty()
            if (key.isNotBlank()) {
                recentItems[key] = item
            }
        }
    }

    companion object {
        // 简单的进程内缓存，保存最近拉取的新闻条目供详情页查询。
        private val recentItems = mutableMapOf<String, JuheNewsItemDto>()
    }
}
