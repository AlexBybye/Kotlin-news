package com.example.newsbackend.service

import com.example.newsbackend.db.DatabaseFactory.dbQuery
import com.example.newsbackend.db.NewsArticles
import com.example.newsbackend.model.NewsArticleDto
import com.example.newsbackend.model.NewsDetailDto
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update
import java.time.Instant

/**
 * 新闻持久化层：把新闻文章读写到 H2 数据库的 news_articles 表。
 * 与 NewsService 解耦——Service 负责拉取与映射，Repository 只管落库与查询。
 */
class NewsRepository {

    /** 详情正文段落之间的分隔符（存库时拼接，读取时拆分）。 */
    private val paragraphSeparator = "\n\n"

    /**
     * 按分类查询新闻列表，置顶优先、发布时间倒序。
     * category 为空或 "recommend" 时返回全部分类的最新文章。
     */
    suspend fun listByCategory(category: String, limit: Int = 60): List<NewsArticleDto> = dbQuery {
        val query = if (category.isBlank() || category == "recommend") {
            NewsArticles.selectAll()
        } else {
            NewsArticles.selectAll().where { NewsArticles.category eq category }
        }
        query
            .orderBy(
                NewsArticles.isTop to SortOrder.DESC,
                NewsArticles.publishTime to SortOrder.DESC,
                NewsArticles.createdAt to SortOrder.DESC
            )
            .limit(limit)
            .map { it.toArticleDto() }
    }

    /** 按业务 id 查询详情；不存在返回 null。 */
    suspend fun findDetail(articleId: String): NewsDetailDto? = dbQuery {
        NewsArticles.selectAll().where { NewsArticles.articleId eq articleId }
            .singleOrNull()
            ?.toDetailDto()
    }

    /** 当前库内文章总数。 */
    suspend fun count(): Long = dbQuery {
        NewsArticles.selectAll().count()
    }

    /**
     * 批量 upsert：已存在（按 articleId）则更新，否则插入。
     * @param contentByArticleId 可选的详情正文（按段落列表），键为 articleId。
     * @return 实际写入（新增或更新）的条数。
     */
    suspend fun upsertAll(
        articles: List<NewsArticleDto>,
        contentByArticleId: Map<String, List<String>> = emptyMap()
    ): Int = dbQuery {
        var affected = 0
        val now = Instant.now()
        articles.forEach { dto ->
            val joinedContent = contentByArticleId[dto.id]
                ?.filter { it.isNotBlank() }
                ?.joinToString(paragraphSeparator)
            val exists = NewsArticles.selectAll()
                .where { NewsArticles.articleId eq dto.id }
                .any()
            if (exists) {
                NewsArticles.update({ NewsArticles.articleId eq dto.id }) {
                    it[title] = dto.title
                    it[summary] = dto.summary
                    it[coverImageUrl] = dto.coverImageUrl
                    it[author] = dto.author
                    it[source] = dto.source
                    it[category] = dto.category
                    it[publishTime] = dto.publishTime
                    it[contentUrl] = dto.contentUrl
                    it[isTop] = dto.isTop
                    if (joinedContent != null) it[content] = joinedContent
                }
            } else {
                NewsArticles.insert {
                    it[articleId] = dto.id
                    it[title] = dto.title
                    it[summary] = dto.summary
                    it[coverImageUrl] = dto.coverImageUrl
                    it[author] = dto.author
                    it[source] = dto.source
                    it[category] = dto.category
                    it[publishTime] = dto.publishTime
                    it[contentUrl] = dto.contentUrl
                    it[content] = joinedContent
                    it[isTop] = dto.isTop
                    it[createdAt] = now
                }
            }
            affected++
        }
        affected
    }

    private fun ResultRow.toArticleDto() = NewsArticleDto(
        id = this[NewsArticles.articleId],
        title = this[NewsArticles.title],
        summary = this[NewsArticles.summary],
        coverImageUrl = this[NewsArticles.coverImageUrl],
        author = this[NewsArticles.author],
        source = this[NewsArticles.source],
        category = this[NewsArticles.category],
        publishTime = this[NewsArticles.publishTime],
        contentUrl = this[NewsArticles.contentUrl],
        isTop = this[NewsArticles.isTop]
    )

    private fun ResultRow.toDetailDto(): NewsDetailDto {
        val storedContent = this[NewsArticles.content]
        val paragraphs = storedContent
            ?.split(paragraphSeparator)
            ?.map { it.trim() }
            ?.filter { it.isNotBlank() }
            ?.takeIf { it.isNotEmpty() }
            ?: listOf(this[NewsArticles.summary], "点击下方「原文」可在内置浏览器查看完整报道。")
        return NewsDetailDto(
            id = this[NewsArticles.articleId],
            title = this[NewsArticles.title],
            summary = this[NewsArticles.summary],
            coverImageUrl = this[NewsArticles.coverImageUrl],
            source = this[NewsArticles.source],
            author = this[NewsArticles.author],
            category = this[NewsArticles.category],
            publishTime = this[NewsArticles.publishTime],
            content = paragraphs,
            contentUrl = this[NewsArticles.contentUrl],
            relatedArticles = emptyList()
        )
    }
}
