package com.example.homework.data.repository

import android.content.Context
import com.example.homework.data.auth.PasswordHasher
import com.example.homework.data.auth.SessionManager
import com.example.homework.data.config.AppConfig
import com.example.homework.data.local.HomeworkDatabase
import com.example.homework.data.local.dao.UserDao
import com.example.homework.data.local.entity.UserEntity
import com.example.homework.data.remote.api.BackendApi
import com.example.homework.data.remote.dto.LoginRequestDto
import com.example.homework.data.remote.dto.RegisterRequestDto
import com.example.homework.data.remote.network.ResultWrapper
import com.example.homework.data.remote.network.RetrofitClient
import com.example.homework.model.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * 账号注册、登录与登录态管理。
 *
 * 支持两种模式（由 [AppConfig.useBackend] 决定）：
 *  - 本地模式：账号通过 Room 持久化，密码以 PBKDF2 加盐哈希存储。
 *  - 后端模式：注册 / 登录走自建后端（BCrypt + JWT），返回的用户信息同时缓存进 Room，
 *    令牌写入 [RetrofitClient.authToken] 供后续请求鉴权；后端不可用可回退本地账号校验。
 *
 * 登录态统一通过 [SessionManager]（DataStore）保存，重启后可自动恢复。
 */
class AuthRepository(
    private val userDao: UserDao,
    private val sessionManager: SessionManager,
    private val backendApi: BackendApi = RetrofitClient.create()
) {

    /** 当前登录用户名流，null 表示未登录。 */
    val loggedInUsername: Flow<String?> = sessionManager.loggedInUsername

    val isLoggedIn: Flow<Boolean> = loggedInUsername.map { it != null }

    suspend fun register(
        username: String,
        nickname: String,
        password: String,
        confirmPassword: String
    ): ResultWrapper<User> {
        val trimmedUsername = username.trim()
        val trimmedNickname = nickname.trim()

        validateUsername(trimmedUsername)?.let { return ResultWrapper.Error(it) }
        validatePassword(password)?.let { return ResultWrapper.Error(it) }
        if (password != confirmPassword) {
            return ResultWrapper.Error("两次输入的密码不一致，请重新确认。")
        }

        return if (AppConfig.useBackend) {
            registerViaBackend(trimmedUsername, trimmedNickname, password)
        } else {
            registerLocally(trimmedUsername, trimmedNickname, password)
        }
    }

    private suspend fun registerLocally(
        username: String,
        nickname: String,
        password: String
    ): ResultWrapper<User> {
        if (userDao.exists(username)) {
            return ResultWrapper.Error("该用户名已被注册，请更换后再试。")
        }

        val salt = PasswordHasher.generateSalt()
        val entity = UserEntity(
            username = username,
            nickname = nickname.ifBlank { username },
            passwordHash = PasswordHasher.hash(password, salt),
            passwordSalt = salt,
            createdAt = System.currentTimeMillis()
        )

        return runCatching {
            userDao.insert(entity)
            sessionManager.saveSession(entity.username)
            ResultWrapper.Success(entity.toUser())
        }.getOrElse {
            ResultWrapper.Error("注册失败，请稍后重试。")
        }
    }

    private suspend fun registerViaBackend(
        username: String,
        nickname: String,
        password: String
    ): ResultWrapper<User> {
        return runCatching {
            val response = backendApi.register(RegisterRequestDto(username, nickname, password))
            val data = response.data
            if (response.code != 0 || data == null) {
                ResultWrapper.Error(response.message ?: "注册失败，请稍后重试。")
            } else {
                onBackendAuthSuccess(data.token, data.user.username, data.user.nickname, password)
                ResultWrapper.Success(
                    User(data.user.username, data.user.nickname, data.user.createdAt)
                )
            }
        }.getOrElse { throwable ->
            ResultWrapper.Error(throwable.message ?: "无法连接后端，请检查服务后重试。")
        }
    }

    suspend fun login(username: String, password: String): ResultWrapper<User> {
        val trimmedUsername = username.trim()
        if (trimmedUsername.isBlank() || password.isBlank()) {
            return ResultWrapper.Error("请输入用户名和密码。")
        }

        return if (AppConfig.useBackend) {
            loginViaBackend(trimmedUsername, password)
        } else {
            loginLocally(trimmedUsername, password)
        }
    }

    private suspend fun loginLocally(username: String, password: String): ResultWrapper<User> {
        val user = userDao.findByUsername(username)
            ?: return ResultWrapper.Error("用户名不存在，请先注册。")

        val passwordMatches = PasswordHasher.verify(
            password = password,
            salt = user.passwordSalt,
            expectedHash = user.passwordHash
        )
        if (!passwordMatches) {
            return ResultWrapper.Error("密码错误，请重新输入。")
        }

        sessionManager.saveSession(user.username)
        return ResultWrapper.Success(user.toUser())
    }

    private suspend fun loginViaBackend(username: String, password: String): ResultWrapper<User> {
        return runCatching {
            val response = backendApi.login(LoginRequestDto(username, password))
            val data = response.data
            if (response.code != 0 || data == null) {
                ResultWrapper.Error(response.message ?: "登录失败，请稍后重试。")
            } else {
                onBackendAuthSuccess(data.token, data.user.username, data.user.nickname, password)
                ResultWrapper.Success(
                    User(data.user.username, data.user.nickname, data.user.createdAt)
                )
            }
        }.getOrElse { throwable ->
            ResultWrapper.Error(throwable.message ?: "无法连接后端，请检查服务后重试。")
        }
    }

    /**
     * 后端登录 / 注册成功后的统一处理：
     * 保存 JWT、写入会话，并把用户信息缓存进 Room（密码本地同样做哈希），
     * 便于离线时读取当前用户与回退校验。
     */
    private suspend fun onBackendAuthSuccess(
        token: String,
        username: String,
        nickname: String,
        password: String
    ) {
        RetrofitClient.authToken = token
        val salt = PasswordHasher.generateSalt()
        runCatching {
            userDao.insert(
                UserEntity(
                    username = username,
                    nickname = nickname,
                    passwordHash = PasswordHasher.hash(password, salt),
                    passwordSalt = salt,
                    createdAt = System.currentTimeMillis()
                )
            )
        }
        sessionManager.saveSession(username)
    }

    suspend fun logout() {
        RetrofitClient.authToken = null
        sessionManager.clearSession()
    }

    suspend fun currentUser(): User? {
        val username = sessionManager.currentUsername() ?: return null
        return userDao.findByUsername(username)?.toUser()
    }

    suspend fun updateNickname(nickname: String): ResultWrapper<User> {
        val username = sessionManager.currentUsername()
            ?: return ResultWrapper.Error("登录态已失效，请重新登录。")
        val trimmed = nickname.trim()
        if (trimmed.isBlank()) {
            return ResultWrapper.Error("昵称不能为空。")
        }
        userDao.updateNickname(username, trimmed)
        val updated = userDao.findByUsername(username)
            ?: return ResultWrapper.Error("更新失败，请稍后重试。")
        return ResultWrapper.Success(updated.toUser())
    }

    private fun validateUsername(username: String): String? {
        return when {
            username.isBlank() -> "请输入用户名。"
            username.length < 3 -> "用户名至少需要 3 个字符。"
            username.length > 20 -> "用户名不能超过 20 个字符。"
            !username.matches(USERNAME_PATTERN) -> "用户名只能包含字母、数字和下划线。"
            else -> null
        }
    }

    private fun validatePassword(password: String): String? {
        return when {
            password.isBlank() -> "请输入密码。"
            password.length < 6 -> "密码至少需要 6 位。"
            password.length > 32 -> "密码不能超过 32 位。"
            else -> null
        }
    }

    private fun UserEntity.toUser(): User {
        return User(
            username = username,
            nickname = nickname,
            createdAt = createdAt
        )
    }

    companion object {
        private val USERNAME_PATTERN = Regex("^[A-Za-z0-9_]+$")

        fun createDefault(context: Context): AuthRepository {
            return AuthRepository(
                userDao = HomeworkDatabase.getInstance(context).userDao(),
                sessionManager = SessionManager(context.applicationContext)
            )
        }
    }
}
