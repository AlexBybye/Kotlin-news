package com.example.newsbackend.service

import com.example.newsbackend.db.DatabaseFactory.dbQuery
import com.example.newsbackend.db.Users
import com.example.newsbackend.model.AuthData
import com.example.newsbackend.model.UserDto
import com.example.newsbackend.security.JwtService
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.mindrot.jbcrypt.BCrypt
import java.time.Instant

/** 业务异常，携带可直接返回给客户端的中文提示。 */
class AuthException(message: String) : Exception(message)

/**
 * 账号业务：注册、登录。密码使用 BCrypt 加盐哈希存储。
 */
class AuthService(private val jwtService: JwtService) {

    suspend fun register(username: String, nickname: String?, password: String): AuthData {
        val cleanUsername = username.trim()
        validateUsername(cleanUsername)
        validatePassword(password)

        val exists = dbQuery {
            Users.selectAll().where { Users.username eq cleanUsername }.any()
        }
        if (exists) throw AuthException("该用户名已被注册，请更换后再试。")

        val finalNickname = nickname?.trim().takeUnless { it.isNullOrBlank() } ?: cleanUsername
        val hash = BCrypt.hashpw(password, BCrypt.gensalt())
        val createdAt = Instant.now()

        dbQuery {
            Users.insert {
                it[Users.username] = cleanUsername
                it[Users.nickname] = finalNickname
                it[passwordHash] = hash
                it[Users.createdAt] = createdAt
            }
        }

        val user = UserDto(cleanUsername, finalNickname, createdAt.toEpochMilli())
        return AuthData(token = jwtService.generateToken(cleanUsername), user = user)
    }

    suspend fun login(username: String, password: String): AuthData {
        val cleanUsername = username.trim()
        if (cleanUsername.isBlank() || password.isBlank()) {
            throw AuthException("请输入用户名和密码。")
        }

        val row = dbQuery {
            Users.selectAll().where { Users.username eq cleanUsername }.singleOrNull()
        } ?: throw AuthException("用户名不存在，请先注册。")

        if (!BCrypt.checkpw(password, row[Users.passwordHash])) {
            throw AuthException("密码错误，请重新输入。")
        }

        return AuthData(
            token = jwtService.generateToken(cleanUsername),
            user = row.toUserDto()
        )
    }

    suspend fun findUser(username: String): UserDto? = dbQuery {
        Users.selectAll().where { Users.username eq username }.singleOrNull()?.toUserDto()
    }

    private fun ResultRow.toUserDto() = UserDto(
        username = this[Users.username],
        nickname = this[Users.nickname],
        createdAt = this[Users.createdAt].toEpochMilli()
    )

    private fun validateUsername(username: String) {
        when {
            username.isBlank() -> throw AuthException("请输入用户名。")
            username.length < 3 -> throw AuthException("用户名至少需要 3 个字符。")
            username.length > 20 -> throw AuthException("用户名不能超过 20 个字符。")
            !username.matches(USERNAME_PATTERN) ->
                throw AuthException("用户名只能包含字母、数字和下划线。")
        }
    }

    private fun validatePassword(password: String) {
        when {
            password.isBlank() -> throw AuthException("请输入密码。")
            password.length < 6 -> throw AuthException("密码至少需要 6 位。")
            password.length > 32 -> throw AuthException("密码不能超过 32 位。")
        }
    }

    companion object {
        private val USERNAME_PATTERN = Regex("^[A-Za-z0-9_]+$")
    }
}
