package com.topout.kmp.platform

import com.topout.kmp.models.AlertType
import kotlinx.cinterop.*
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.UserNotifications.*
import platform.Foundation.*
import platform.darwin.dispatch_get_main_queue
import platform.darwin.dispatch_async
import kotlin.coroutines.resume

/**
 * iOS-specific actual implementation of NotificationController
 * Uses UNUserNotificationCenter for local notifications
 */
actual class NotificationController() {

    private val notificationCenter = UNUserNotificationCenter.currentNotificationCenter()

    actual fun sendAlertNotification(alertType: AlertType, title: String, message: String): Boolean {
        return sendNotificationInternal(
            title = title,
            message = message,
            identifier = "alert_${alertType.name}_${getCurrentTimestamp()}"
        )
    }

    actual fun sendNotification(title: String, message: String): Boolean {
        return sendNotificationInternal(
            title = title,
            message = message,
            identifier = "general_${getCurrentTimestamp()}"
        )
    }

    actual fun areNotificationsEnabled(): Boolean {
        // This is a synchronous check - on iOS we need to be more careful
        // For now, return true and let the actual notification request handle permission
        return true
    }

    actual suspend fun requestNotificationPermission(): Boolean = suspendCancellableCoroutine { continuation ->
        val options = UNAuthorizationOptionAlert or UNAuthorizationOptionSound or UNAuthorizationOptionBadge

        notificationCenter.requestAuthorizationWithOptions(
            options = options.toULong()
        ) { granted, error ->
            dispatch_async(dispatch_get_main_queue()) {
                if (continuation.isActive) {
                    continuation.resume(granted && error == null)
                }
            }
        }
    }

    private fun getCurrentTimestamp(): Long {
        return NSDate().timeIntervalSince1970.toLong()
    }

    private fun sendNotificationInternal(
        title: String,
        message: String,
        identifier: String
    ): Boolean {
        return try {
            // Create notification content
            val content = UNMutableNotificationContent().apply {
                setTitle(title)
                setBody(message)
                setSound(UNNotificationSound.defaultSound())
            }

            // Create trigger (immediate notification)
            val trigger = UNTimeIntervalNotificationTrigger.triggerWithTimeInterval(
                timeInterval = 0.1, // Almost immediate
                repeats = false
            )

            // Create request
            val request = UNNotificationRequest.requestWithIdentifier(
                identifier = identifier,
                content = content,
                trigger = trigger
            )

            // Add to notification center
            notificationCenter.addNotificationRequest(request) { error ->
                if (error != null) {
                    println("Failed to schedule notification: ${error.localizedDescription}")
                }
            }

            true
        } catch (e: Exception) {
            println("Error sending iOS notification: ${e.message}")
            false
        }
    }
}
