package com.example.newsbackend.db

import com.example.newsbackend.service.NewsRepository
import com.example.newsbackend.service.NewsSeed
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction

/**
 * 数据库初始化。使用 H2 内嵌文件库，零安装、随服务启动自动建表。
 */
object DatabaseFactory {

    fun init() {
        Database.connect(
            url = "jdbc:h2:file:./data/campus_news;DB_CLOSE_DELAY=-1;AUTO_SERVER=TRUE",
            driver = "org.h2.Driver",
            user = "sa",
            password = ""
        )
        transaction {
            SchemaUtils.create(Users, NewsArticles)
        }
        seedNewsIfEmpty()
    }

    /** 新闻表为空时灌入内置中文校园新闻种子，保证后端离线也有可展示数据。 */
    private fun seedNewsIfEmpty() {
        val repository = NewsRepository()
        runBlocking {
            if (repository.count() == 0L) {
                val (dtos, contentMap) = NewsSeed.asUpsertPayload()
                val written = repository.upsertAll(dtos, contentMap)
                println("[DatabaseFactory] 新闻库为空，已灌入 $written 条种子新闻。")
            }
        }
    }

    /** 在 IO 调度器上执行挂起式数据库事务。 */
    suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }
}
