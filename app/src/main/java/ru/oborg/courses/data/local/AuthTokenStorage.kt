package ru.oborg.courses.data.local

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import ru.oborg.courses.domain.model.AuthSession
import ru.oborg.courses.domain.model.User

class AuthTokenStorage(context: Context) {
    private val preferences = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val state = MutableStateFlow(readSession())

    val authState: StateFlow<AuthSession?> = state

    fun getToken(): String? = state.value?.token

    fun saveSession(session: AuthSession) {
        val expiresAt = System.currentTimeMillis() + session.expiresInMinutes * 60_000L
        preferences.edit()
            .putString(KEY_TOKEN, session.token)
            .putInt(KEY_EXPIRES, session.expiresInMinutes)
            .putLong(KEY_EXPIRES_AT, expiresAt)
            .putInt(KEY_USER_ID, session.user.id)
            .putString(KEY_FULL_NAME, session.user.fullName)
            .putString(KEY_EMAIL, session.user.email)
            .putString(KEY_ROLE, session.user.role)
            .apply()
        state.value = session
    }

    fun clear() {
        preferences.edit().clear().apply()
        state.value = null
    }

    private fun readSession(): AuthSession? {
        val expiresAt = preferences.getLong(KEY_EXPIRES_AT, 0L)
        if (expiresAt <= System.currentTimeMillis()) {
            preferences.edit().clear().apply()
            return null
        }

        val token = preferences.getString(KEY_TOKEN, null) ?: return null
        val email = preferences.getString(KEY_EMAIL, null) ?: return null
        val fullName = preferences.getString(KEY_FULL_NAME, null) ?: return null
        val role = preferences.getString(KEY_ROLE, "student") ?: "student"
        val userId = preferences.getInt(KEY_USER_ID, -1)
        if (userId <= 0) return null

        return AuthSession(
            token = token,
            expiresInMinutes = preferences.getInt(KEY_EXPIRES, 0),
            user = User(
                id = userId,
                fullName = fullName,
                email = email,
                role = role
            )
        )
    }

    private companion object {
        const val PREFS_NAME = "oborg_auth"
        const val KEY_TOKEN = "token"
        const val KEY_EXPIRES = "expires"
        const val KEY_EXPIRES_AT = "expires_at"
        const val KEY_USER_ID = "user_id"
        const val KEY_FULL_NAME = "full_name"
        const val KEY_EMAIL = "email"
        const val KEY_ROLE = "role"
    }
}
