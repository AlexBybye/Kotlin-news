package com.example.newsbackend.routes

import com.example.newsbackend.model.ApiResponse
import com.example.newsbackend.model.AuthData
import com.example.newsbackend.model.LoginRequest
import com.example.newsbackend.model.RegisterRequest
import com.example.newsbackend.service.AuthException
import com.example.newsbackend.service.AuthService
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route

fun Route.authRoutes(authService: AuthService) {
    route("/auth") {
        post("/register") {
            val request = call.receive<RegisterRequest>()
            try {
                val data = authService.register(request.username, request.nickname, request.password)
                call.respond(ApiResponse.success(data, "注册成功"))
            } catch (e: AuthException) {
                call.respond(HttpStatusCode.BadRequest, ApiResponse.error<AuthData>(e.message ?: "注册失败"))
            }
        }

        post("/login") {
            val request = call.receive<LoginRequest>()
            try {
                val data = authService.login(request.username, request.password)
                call.respond(ApiResponse.success(data, "登录成功"))
            } catch (e: AuthException) {
                call.respond(HttpStatusCode.Unauthorized, ApiResponse.error<AuthData>(e.message ?: "登录失败"))
            }
        }

        // 校验 token 有效性并返回当前用户信息
        authenticate("auth-jwt") {
            get("/me") {
                val principal = call.principal<JWTPrincipal>()
                val username = principal?.payload?.getClaim("username")?.asString()
                val user = username?.let { authService.findUser(it) }
                if (user == null) {
                    call.respond(HttpStatusCode.Unauthorized, ApiResponse.error<Any>("登录态已失效，请重新登录。"))
                } else {
                    call.respond(ApiResponse.success(user))
                }
            }
        }
    }
}
