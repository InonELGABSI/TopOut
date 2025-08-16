package com.topout.kmp.platform

import com.topout.kmp.models.AlertType
expect class NotificationController {
    fun sendAlertNotification(alertType: AlertType, title: String, message: String): Boolean
    fun sendNotification(title: String, message: String): Boolean
    fun areNotificationsEnabled(): Boolean
    suspend fun requestNotificationPermission(): Boolean
}
