package dev.maslov.sheetsync.service

import android.content.Context
import android.content.Intent
import android.provider.Settings

object NotificationPermissionHelper {
    /**
     * Check if the NotificationListenerService is enabled for this app
     */
    fun isNotificationListenerEnabled(context: Context): Boolean {
        val enabledListeners = Settings.Secure.getString(
            context.contentResolver,
            "enabled_notification_listeners"
        ) ?: return false

        val packageName = context.packageName
        return enabledListeners.contains(packageName)
    }

    /**
     * Get an intent to launch the notification access settings screen
     */
    fun getNotificationSettingsIntent(): Intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
}
