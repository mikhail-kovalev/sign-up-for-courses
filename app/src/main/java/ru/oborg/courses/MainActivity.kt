package ru.oborg.courses

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import ru.oborg.courses.core.AppContainer
import ru.oborg.courses.core.LocalNotificationService
import ru.oborg.courses.presentation.navigation.CourseApp
import ru.oborg.courses.presentation.theme.ObOrgCoursesTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val container = remember { AppContainer(applicationContext) }
            val notificationService = remember { LocalNotificationService(applicationContext) }
            val settings by container.uiSettingsStorage.settings.collectAsState()
            var pendingNotification by remember { mutableStateOf<NotificationAction?>(null) }
            val notificationPermissionLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestPermission()
            ) { granted ->
                container.uiSettingsStorage.setNotificationsEnabled(granted)
                if (granted) {
                    when (pendingNotification) {
                        NotificationAction.Test -> notificationService.showTestNotification()
                        is NotificationAction.Enrollment -> {
                            val courseTitle = (pendingNotification as NotificationAction.Enrollment).courseTitle
                            notificationService.showEnrollmentNotification(courseTitle)
                        }
                        null -> Unit
                    }
                }
                pendingNotification = null
            }

            fun requestNotifications(action: NotificationAction) {
                if (notificationService.canShowNotifications()) {
                    container.uiSettingsStorage.setNotificationsEnabled(true)
                    when (action) {
                        NotificationAction.Test -> notificationService.showTestNotification()
                        is NotificationAction.Enrollment -> notificationService.showEnrollmentNotification(action.courseTitle)
                    }
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    pendingNotification = action
                    notificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                }
            }

            ObOrgCoursesTheme(darkTheme = settings.darkTheme) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    CourseApp(
                        container = container,
                        uiSettings = settings,
                        onDarkThemeChanged = container.uiSettingsStorage::setDarkTheme,
                        onNotificationsChanged = { enabled ->
                            if (enabled) {
                                requestNotifications(NotificationAction.Test)
                            } else {
                                container.uiSettingsStorage.setNotificationsEnabled(false)
                            }
                        },
                        onEnrollmentNotification = { courseTitle ->
                            if (settings.notificationsEnabled) {
                                requestNotifications(NotificationAction.Enrollment(courseTitle))
                            }
                        }
                    )
                }
            }
        }
    }
}

private sealed interface NotificationAction {
    data object Test : NotificationAction
    data class Enrollment(val courseTitle: String) : NotificationAction
}
