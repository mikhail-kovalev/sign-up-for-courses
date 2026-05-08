package ru.oborg.courses.core

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import ru.oborg.courses.R

class LocalNotificationService(
    private val context: Context
) {
    init {
        createChannel()
    }

    fun canShowNotifications(): Boolean {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED
    }

    fun showEnrollmentNotification(courseTitle: String) {
        if (!canShowNotifications()) return

        show(
            id = ENROLLMENT_NOTIFICATION_ID + courseTitle.hashCode().absoluteNotificationId() % 10_000,
            title = "Запись оформлена",
            text = "Вы записаны на курс \"$courseTitle\""
        )
    }

    fun showTestNotification() {
        if (!canShowNotifications()) return

        show(
            id = TEST_NOTIFICATION_ID,
            title = "Уведомления включены",
            text = "Obrorg напомнит о старте ближайших занятий."
        )
    }

    private fun show(id: Int, title: String, text: String) {
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_lucide_bell)
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(id, notification)
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val channel = NotificationChannel(
            CHANNEL_ID,
            "Напоминания Obrorg",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Уведомления о записях и старте курсов"
        }

        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }

    private fun Int.absoluteNotificationId(): Int = if (this == Int.MIN_VALUE) 0 else kotlin.math.abs(this)

    private companion object {
        const val CHANNEL_ID = "oborg_course_reminders"
        const val TEST_NOTIFICATION_ID = 1001
        const val ENROLLMENT_NOTIFICATION_ID = 2000
    }
}
