package com.example.newsbackend.routes

import com.example.newsbackend.model.ApiResponse
import com.example.newsbackend.model.NewsArticleDto
import com.example.newsbackend.model.NewsDetailDto
import com.example.newsbackend.service.NewsException
import com.example.newsbackend.service.NewsService
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route

fun Route.newsRoutes(newsService: NewsService) {
    route("/news") {
        // GET /news?category=technology
        get {
            val category = call.request.queryParameters["category"] ?: "recommend"
            try {
                val articles = newsService.getNews(category)
                call.respond(ApiResponse.success(articles))
            } catch (e: NewsException) {
                call.respond(
                    HttpStatusCode.BadGateway,
                    ApiResponse.error<List<NewsArticleDto>>(e.message ?: "获取新闻失败")
                )
            }
        }

        // POST /news/sync —— 从 NewsAPI 拉取真实新闻并入库，返回写入条数与库内总数。
        post("/sync") {
            try {
                val written = newsService.syncFromNewsApi()
                val total = newsService.count()
                call.respond(
                    ApiResponse.success(
                        mapOf("written" to written.toLong(), "total" to total),
                        "已从 NewsAPI 同步 $written 条新闻入库"
                    )
                )
            } catch (e: NewsException) {
                call.respond(
                    HttpStatusCode.BadGateway,
                    ApiResponse.error<Map<String, Long>>(e.message ?: "同步新闻失败")
                )
            }
        }

        // GET /news/detail/{id}
        get("/detail/{id}") {
            val id = call.parameters["id"].orEmpty()
            try {
                val detail = newsService.getDetail(id)
                call.respond(ApiResponse.success(detail))
            } catch (e: NewsException) {
                call.respond(
                    HttpStatusCode.NotFound,
                    ApiResponse.error<NewsDetailDto>(e.message ?: "获取详情失败")
                )
            }
        }
    }
}
