package com.topout.kmp.platform

import com.topout.kmp.models.AlertType

/**
 * Platform-specific expect class for handling notifications
 * Actual implementations will be provided for each platform (Android, iOS)
 */
expect class NotificationController {
    /**
     * Send a notification with the given alert type
     * @param alertType The type of alert that triggered the notification
     * @param title The notification title
     * @param message The notification message
     * @return true if notification was sent, false otherwise
     */
    fun sendAlertNotification(alertType: AlertType, title: String, message: String): Boolean

    /**
     * Send a simple notification (for settings toggle, etc.)
     * @param title The notification title
     * @param message The notification message
     * @return true if notification was sent, false otherwise
     */
    fun sendNotification(title: String, message: String): Boolean

    /**
     * Check if notifications are enabled in the system
     * @return true if notifications are enabled, false otherwise
     */
    fun areNotificationsEnabled(): Boolean

    /**
     * Request notification permissions (iOS specific, no-op on Android)
     * @return true if permissions were granted, false otherwise
     */
    suspend fun requestNotificationPermission(): Boolean
}
