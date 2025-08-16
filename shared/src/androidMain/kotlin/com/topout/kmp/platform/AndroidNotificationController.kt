package com.topout.kmp.platform

import android.annotation.SuppressLint
import android.content.Context
import com.topout.kmp.models.AlertType
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
actual class NotificationController(private val context: Context) {

    companion object {
        private const val GENERAL_CHANNEL_ID = "topout_general_channel"
    }

    actual fun sendAlertNotification(alertType: AlertType, title: String, message: String): Boolean {
        if (!areNotificationsEnabled()) {
            return false
        }

        return try {
            sendPushNotification(
                context = context,
                title = title,
                message = message,
                notificationId = alertType.hashCode()
            )
            true
        } catch (e: Exception) {
            false
        }
    }

    actual fun sendNotification(title: String, message: String): Boolean {
        if (!areNotificationsEnabled()) {
            return false
        }

        return try {
            sendPushNotification(
                context = context,
                title = title,
                message = message
            )
            true
        } catch (e: Exception) {
            false
        }
    }

    actual fun areNotificationsEnabled(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ActivityCompat.checkSelfPermission(
                context,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        }

        return NotificationManagerCompat.from(context).areNotificationsEnabled()
    }

    actual suspend fun requestNotificationPermission(): Boolean {
        return areNotificationsEnabled()
    }

    @SuppressLint("MissingPermission")
    private fun sendPushNotification(
        context: Context,
        title: String,
        message: String,
        notificationId: Int = (System.currentTimeMillis() % Int.MAX_VALUE).toInt()
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permissionGranted = ActivityCompat.checkSelfPermission(
                context,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED

            if (!permissionGranted) {
                return
            }
        }

        val builder = NotificationCompat.Builder(context, GENERAL_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        NotificationManagerCompat.from(context).notify(notificationId, builder.build())
    }
}
