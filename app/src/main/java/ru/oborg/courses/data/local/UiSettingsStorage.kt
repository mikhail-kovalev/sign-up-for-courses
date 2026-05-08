package ru.oborg.courses.data.local

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class UiSettings(
    val darkTheme: Boolean,
    val notificationsEnabled: Boolean
)

class UiSettingsStorage(context: Context) {
    private val preferences = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val state = MutableStateFlow(readSettings())

    val settings: StateFlow<UiSettings> = state

    fun setDarkTheme(enabled: Boolean) {
        preferences.edit().putBoolean(KEY_DARK_THEME, enabled).apply()
        state.value = state.value.copy(darkTheme = enabled)
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        preferences.edit().putBoolean(KEY_NOTIFICATIONS, enabled).apply()
        state.value = state.value.copy(notificationsEnabled = enabled)
    }

    private fun readSettings(): UiSettings {
        return UiSettings(
            darkTheme = preferences.getBoolean(KEY_DARK_THEME, false),
            notificationsEnabled = preferences.getBoolean(KEY_NOTIFICATIONS, false)
        )
    }

    private companion object {
        const val PREFS_NAME = "oborg_ui_settings"
        const val KEY_DARK_THEME = "dark_theme"
        const val KEY_NOTIFICATIONS = "notifications"
    }
}
