package com.example.newsbackend.service

import com.example.newsbackend.model.NewsArticleDto
import com.example.newsbackend.model.NewsDetailDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.http.isSuccess
import java.security.MessageDigest

/** 新闻接口异常，message 可直接返回客户端。 */
class NewsException(message: String) : Exception(message)

/**
 * 新闻服务：
 * - 列表 / 详情统一从数据库读取（持久化，重启不丢）。
 * - syncFromNewsApi 负责在服务端调用 NewsAPI，将真实新闻批量落库；密钥仅存服务端。
 * - 数据库为空时由 DatabaseFactory 灌入中文校园新闻种子，保证离线可用。
 */
class NewsService(
    private val httpClient: HttpClient,
    private val newsApiKey: String,
    private val repository: NewsRepository
) {
    /** 列表：直接读库，按分类返回。 */
    suspend fun getNews(category: String): List<NewsArticleDto> =
        repository.listByCategory(category)

    /** 详情：直接读库。 */
    suspend fun getDetail(id: String): NewsDetailDto =
        repository.findDetail(id)
            ?: throw NewsException("未找到对应新闻详情，请返回列表刷新后重试。")

    /** 库内文章总数。 */
    suspend fun count(): Long = repository.count()

    /**
     * 从 NewsAPI 拉取全部分类的真实新闻并 upsert 入库。
     * @return 实际写入（新增或更新）的总条数。
     */
    suspend fun syncFromNewsApi(): Int {
        if (newsApiKey.isBlank()) {
            throw NewsException("服务端未配置 NewsAPI 密钥（NEWS_API_KEY）。")
        }
        var total = 0
        val errors = mutableListOf<String>()
        CategoryMapping.entries.forEach { mapping ->
            runCatching { fetchAndStore(mapping) }
                .onSuccess { total += it }
                .onFailure { errors += "${mapping.appValue}: ${it.message}" }
        }
        if (total == 0 && errors.isNotEmpty()) {
            throw NewsException("从 NewsAPI 拉取失败：${errors.joinToString("；")}")
        }
        return total
    }

    /** 拉取单个分类并落库，返回写入条数。 */
    private suspend fun fetchAndStore(mapping: CategoryMapping): Int {
        val response = fetchFromNewsApi(mapping)
        if (response.status != "ok") {
            throw NewsException(response.message ?: "新闻接口返回异常。")
        }
        val valid = response.articles.filter { !it.title.isNullOrBlank() && it.title != "[Removed]" }
        val dtos = valid.map { it.toArticleDto(mapping.appValue) }
        val contentMap = valid.associate { article ->
            val link = article.url ?: article.title.orEmpty()
            stableId(link) to buildDetailParagraphs(article)
        }
        return repository.upsertAll(dtos, contentMap)
    }

    /** 由 NewsAPI 文章拼装详情正文段落（NewsAPI 无独立详情接口）。 */
    private fun buildDetailParagraphs(article: NewsApiArticle): List<String> = listOfNotNull(
        article.description?.takeIf { it.isNotBlank() },
        article.content?.substringBefore(" [+")?.takeIf { it.isNotBlank() },
        "点击下方「原文」可在内置浏览器查看完整报道。"
    ).ifEmpty { listOf("暂无正文内容，请查看原文。") }

    private suspend fun fetchFromNewsApi(mapping: CategoryMapping): NewsApiResponse {
        val httpResponse = httpClient.get("https://newsapi.org/v2/${mapping.endpoint}") {
            parameter("apiKey", newsApiKey)
            parameter("pageSize", 30)
            mapping.queryParams.forEach { (k, v) -> parameter(k, v) }
        }
        if (!httpResponse.status.isSuccess()) {
            throw NewsException("新闻接口 HTTP ${httpResponse.status.value}")
        }
        return httpResponse.body()
    }

    private fun NewsApiArticle.toArticleDto(category: String): NewsArticleDto {
        val link = url ?: title.orEmpty()
        return NewsArticleDto(
            id = stableId(link),
            title = title ?: "未命名新闻",
            summary = description ?: source?.name ?: "暂无摘要",
            coverImageUrl = urlToImage,
            author = author,
            source = source?.name ?: "NewsAPI",
            category = category,
            publishTime = publishedAt?.take(10) ?: "",
            contentUrl = url,
            isTop = false
        )
    }

    private fun stableId(seed: String): String {
        val digest = MessageDigest.getInstance("MD5").digest(seed.toByteArray())
        return digest.joinToString("") { "%02x".format(it) }.take(16)
    }
}

/**
 * App 分类 → NewsAPI 查询参数映射。
 * top-headlines 支持 category；国际/校园等用 everything + 关键词更贴切。
 */
private enum class CategoryMapping(
    val appValue: String,
    val endpoint: String,
    val queryParams: Map<String, String>
) {
    RECOMMEND("recommend", "top-headlines", mapOf("language" to "en", "category" to "general")),
    TECHNOLOGY("technology", "top-headlines", mapOf("language" to "en", "category" to "technology")),
    SPORTS("sports", "top-headlines", mapOf("language" to "en", "category" to "sports")),
    CAMPUS("campus", "everything", mapOf("q" to "campus OR university OR education", "language" to "en", "sortBy" to "publishedAt")),
    INTERNATIONAL("international", "top-headlines", mapOf("language" to "en", "category" to "business"));

    companion object {
        fun of(appValue: String): CategoryMapping =
            entries.firstOrNull { it.appValue == appValue } ?: RECOMMEND
    }
}
