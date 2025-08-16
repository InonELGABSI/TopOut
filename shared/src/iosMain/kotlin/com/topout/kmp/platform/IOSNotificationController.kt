package com.topout.kmp.platform

import com.topout.kmp.models.AlertType
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.UserNotifications.*
import platform.Foundation.*
import platform.darwin.dispatch_get_main_queue
import platform.darwin.dispatch_async
import kotlin.coroutines.resume
import platform.UIKit.UIApplication
import platform.UIKit.UIApplicationWillEnterForegroundNotification

actual class NotificationController() {

    private val notificationCenter = UNUserNotificationCenter.currentNotificationCenter()
    private var authorizationGranted: Boolean = false
    private var statusInitialized: Boolean = false

    private val lastSentByThread = mutableMapOf<String, Long>()
    private val minIntervalMsPerThread = 2000L

    private val pendingUntilInit = mutableListOf<() -> Unit>()

    init {
        refreshAuthorizationStatus()
        NSNotificationCenter.defaultCenter.addObserverForName(
            UIApplicationWillEnterForegroundNotification,
            null,
            null
        ) { _ ->
            refreshAuthorizationStatus()
        }
    }

    private fun refreshAuthorizationStatus() {
        notificationCenter.getNotificationSettingsWithCompletionHandler { settings ->
            val previous = authorizationGranted
            authorizationGranted = settings?.authorizationStatus == UNAuthorizationStatusAuthorized ||
                settings?.authorizationStatus == UNAuthorizationStatusProvisional ||
                settings?.authorizationStatus == UNAuthorizationStatusEphemeral

            val justInitialized = !statusInitialized
            statusInitialized = true

            if ((justInitialized || !previous) && authorizationGranted) {
                dispatch_async(dispatch_get_main_queue()) {
                    val toSend = pendingUntilInit.toList()
                    pendingUntilInit.clear()
                    toSend.forEach { it.invoke() }
                }
            }
            println("[Notif] Settings refreshed. granted=${authorizationGranted} status=${settings?.authorizationStatus}")
        }
    }


    actual fun sendAlertNotification(alertType: AlertType, title: String, message: String): Boolean =
        sendNotificationInternal(
            title = title,
            message = message,
            identifier = "alert_${alertType.name}_${getCurrentTimestamp()}",
            threadId = "topout_alert_${alertType.name.lowercase()}",
            category = "alert_${alertType.name.lowercase()}",
            incrementBadge = true
        )

    actual fun sendNotification(title: String, message: String): Boolean =
        sendNotificationInternal(
            title = title,
            message = message,
            identifier = "general_${getCurrentTimestamp()}",
            threadId = "topout_general",
            category = null,
            incrementBadge = false
        )

    actual fun areNotificationsEnabled(): Boolean = authorizationGranted


    actual suspend fun requestNotificationPermission(): Boolean = suspendCancellableCoroutine { continuation ->
        val options = UNAuthorizationOptionAlert or UNAuthorizationOptionSound or UNAuthorizationOptionBadge
        notificationCenter.requestAuthorizationWithOptions(options) { granted, error ->
            refreshAuthorizationStatus()
            dispatch_async(dispatch_get_main_queue()) {
                if (continuation.isActive) {
                    if (error != null) println("[Notif] Permission request error: ${error.localizedDescription}")
                    continuation.resume(granted && error == null)
                }
            }
        }
    }

    private fun getCurrentTimestamp(): Long = (NSDate().timeIntervalSince1970 * 1000.0).toLong()

    private fun sendNotificationInternal(
        title: String,
        message: String,
        identifier: String,
        threadId: String,
        category: String? = null,
        incrementBadge: Boolean = false,
        allowWhileNotAuthorizedIfPending: Boolean = true
    ): Boolean {
        if (!statusInitialized && allowWhileNotAuthorizedIfPending) {
            pendingUntilInit.add { sendNotificationInternal(title, message, identifier, threadId, category, incrementBadge, false) }
            println("[Notif] Queued (auth pending) id=$identifier")
            return true
        }

        if (statusInitialized && !authorizationGranted) {
            println("[Notif] Skip scheduling (denied) id=$identifier")
            return false
        }

        val now = getCurrentTimestamp()
        lastSentByThread[threadId]?.let { last ->
            if (now - last < minIntervalMsPerThread) {
                println("[Notif] Throttled thread=$threadId id=$identifier (delta=${now - last} ms)")
                return false
            }
        }
        lastSentByThread[threadId] = now

        return try {
            val content = UNMutableNotificationContent().apply {
                setTitle(title)
                setBody(message)
                setSound(UNNotificationSound.defaultSound())
                setThreadIdentifier(threadId)

                if (category != null) {
                    setCategoryIdentifier(category)
                }

                if (incrementBadge) {
                    dispatch_async(dispatch_get_main_queue()) {
                        val currentBadge = UIApplication.sharedApplication.applicationIconBadgeNumber
                        setBadge(NSNumber(long = currentBadge + 1))

                        UIApplication.sharedApplication.setApplicationIconBadgeNumber(currentBadge + 1)
                    }
                } else {
                    setBadge(NSNumber(long = 0))
                }

                setUserInfo(mapOf(
                    "sourceApp" to "TopOut",
                    "notificationType" to (category ?: "general"),
                    "timestamp" to getCurrentTimestamp()
                ))
            }

            val request = UNNotificationRequest.requestWithIdentifier(identifier, content, null)
            notificationCenter.addNotificationRequest(request) { error ->
                if (error != null) {
                    println("[Notif] Failed id=$identifier error=${error.localizedDescription}")
                } else {
                    println("[Notif] Scheduled id=$identifier thread=$threadId title=$title")
                }
            }
            true
        } catch (e: Exception) {
            println("[Notif] Exception scheduling id=$identifier msg=${e.message ?: "Unknown error"}")
            false
        }
    }
}
