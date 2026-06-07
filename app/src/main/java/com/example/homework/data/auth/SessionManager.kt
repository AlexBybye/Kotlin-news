package com.example.homework.data.auth

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.sessionDataStore by preferencesDataStore(name = "user_session")

/**
 * 登录态持久化。仅保存当前登录的用户名，重启应用后可恢复登录状态。
 */
class SessionManager(private val context: Context) {

    val loggedInUsername: Flow<String?> = context.sessionDataStore.data
        .map { preferences -> preferences[KEY_USERNAME] }

    suspend fun currentUsername(): String? = loggedInUsername.first()

    suspend fun saveSession(username: String) {
        context.sessionDataStore.edit { preferences ->
            preferences[KEY_USERNAME] = username
        }
    }

    suspend fun clearSession() {
        context.sessionDataStore.edit { preferences ->
            preferences.remove(KEY_USERNAME)
        }
    }

    companion object {
        private val KEY_USERNAME = stringPreferencesKey("logged_in_username")
    }
}
