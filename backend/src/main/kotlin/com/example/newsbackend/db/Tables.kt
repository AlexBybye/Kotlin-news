package com.example.newsbackend.db

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.timestamp

/**
 * 用户表。密码以 BCrypt 哈希存储，不保存明文。
 */
object Users : IntIdTable("users") {
    val username = varchar("username", 32).uniqueIndex()
    val nickname = varchar("nickname", 64)
    val passwordHash = varchar("password_hash", 100)
    val createdAt = timestamp("created_at")
}

/**
 * 新闻文章表。新闻数据持久化到此表，重启不丢失。
 * articleId 为业务主键（与 NewsArticleDto.id 一致，由 url/标题哈希生成），全表唯一。
 * content 保存详情正文，按段落用换行符拼接存储。
 */
object NewsArticles : IntIdTable("news_articles") {
    val articleId = varchar("article_id", 64).uniqueIndex()
    val title = varchar("title", 512)
    val summary = text("summary")
    val coverImageUrl = varchar("cover_image_url", 1024).nullable()
    val author = varchar("author", 256).nullable()
    val source = varchar("source", 256)
    val category = varchar("category", 32).index()
    val publishTime = varchar("publish_time", 32)
    val contentUrl = varchar("content_url", 1024).nullable()
    val content = text("content").nullable()
    val isTop = bool("is_top").default(false)
    val createdAt = timestamp("created_at")
}
