package com.example.homework.data.repository

import android.content.Context
import com.example.homework.data.auth.PasswordHasher
import com.example.homework.data.auth.SessionManager
import com.example.homework.data.local.HomeworkDatabase
import com.example.homework.data.local.dao.UserDao
import com.example.homework.data.local.entity.UserEntity
import com.example.homework.data.remote.network.ResultWrapper
import com.example.homework.model.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * 账号注册、登录与登录态管理。
 *
 * 账号数据通过 Room 持久化，密码以 PBKDF2 加盐哈希存储；
 * 登录态通过 [SessionManager]（DataStore）保存，重启后可自动恢复。
 */
class AuthRepository(
    private val userDao: UserDao,
    private val sessionManager: SessionManager
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
        if (userDao.exists(trimmedUsername)) {
            return ResultWrapper.Error("该用户名已被注册，请更换后再试。")
        }

        val salt = PasswordHasher.generateSalt()
        val entity = UserEntity(
            username = trimmedUsername,
            nickname = trimmedNickname.ifBlank { trimmedUsername },
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

    suspend fun login(username: String, password: String): ResultWrapper<User> {
        val trimmedUsername = username.trim()
        if (trimmedUsername.isBlank() || password.isBlank()) {
            return ResultWrapper.Error("请输入用户名和密码。")
        }

        val user = userDao.findByUsername(trimmedUsername)
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

    suspend fun logout() {
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
