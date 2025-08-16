package com.topout.kmp.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.topout.kmp.R

object NotificationHelper {
    private const val GENERAL_CHANNEL_ID = "topout_general_channel"
    private const val GENERAL_CHANNEL_NAME = "TopOut Alerts"

    fun createGeneralChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                GENERAL_CHANNEL_ID,
                GENERAL_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "General notifications from TopOut"
            }

            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }
}
