package com.example.newsbackend

import com.example.newsbackend.db.DatabaseFactory
import com.example.newsbackend.routes.authRoutes
import com.example.newsbackend.routes.newsRoutes
import com.example.newsbackend.security.JwtService
import com.example.newsbackend.service.AuthService
import com.example.newsbackend.service.NewsRepository
import com.example.newsbackend.service.NewsService
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation as ClientContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.jwt.jwt
import io.ktor.server.netty.EngineMain
import io.ktor.server.plugins.callloging.CallLogging
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.cors.routing.CORS
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import io.ktor.http.HttpStatusCode
import io.ktor.http.HttpMethod
import com.example.newsbackend.model.ApiResponse
import kotlinx.serialization.json.Json

/** 入口：由 Ktor EngineMain 读取 application.yaml 并加载 module。 */
fun main(args: Array<String>) = EngineMain.main(args)

fun Application.module() {
    DatabaseFactory.init()

    // ---- 读取配置（支持环境变量覆盖）----
    val newsApiKey = System.getenv("NEWS_API_KEY")
        ?: environment.config.propertyOrNull("app.newsApiKey")?.getString().orEmpty()
    val jwtSecret = System.getenv("JWT_SECRET")
        ?: environment.config.propertyOrNull("app.jwt.secret")?.getString()
        ?: "campus-news-dev-secret-change-me"
    val jwtIssuer = environment.config.propertyOrNull("app.jwt.issuer")?.getString() ?: "campus-news-backend"
    val jwtAudience = environment.config.propertyOrNull("app.jwt.audience")?.getString() ?: "campus-news-app"
    val jwtRealm = environment.config.propertyOrNull("app.jwt.realm")?.getString() ?: "campus-news"
    val jwtExpiresIn = environment.config.propertyOrNull("app.jwt.expiresInMs")?.getString()?.toLongOrNull()
        ?: 604_800_000L

    val jwtService = JwtService(jwtSecret, jwtIssuer, jwtAudience, jwtExpiresIn)
    val authService = AuthService(jwtService)

    val httpClient = HttpClient(CIO) {
        install(ClientContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }
    val newsService = NewsService(httpClient, newsApiKey, NewsRepository())

    // ---- 插件 ----
    install(ContentNegotiation) {
        json(Json { ignoreUnknownKeys = true; encodeDefaults = true })
    }
    install(CallLogging)
    install(CORS) {
        anyHost()
        allowMethod(HttpMethod.Get)
        allowMethod(HttpMethod.Post)
        allowHeader(io.ktor.http.HttpHeaders.Authorization)
        allowHeader(io.ktor.http.HttpHeaders.ContentType)
    }
    install(Authentication) {
        jwt("auth-jwt") {
            realm = jwtRealm
            verifier(jwtService.verifier)
            validate { credential ->
                if (credential.payload.getClaim(JwtService.CLAIM_USERNAME).asString() != null) {
                    JWTPrincipal(credential.payload)
                } else null
            }
        }
    }

    // ---- 路由 ----
    routing {
        get("/") {
            call.respond(ApiResponse.success("校园新闻后端服务运行中", "ok"))
        }
        get("/health") {
            call.respond(HttpStatusCode.OK, ApiResponse.success(mapOf("status" to "up")))
        }
        authRoutes(authService)
        newsRoutes(newsService)
    }
}
