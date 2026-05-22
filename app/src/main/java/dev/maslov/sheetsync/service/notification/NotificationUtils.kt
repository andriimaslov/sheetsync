package dev.maslov.sheetsync.service.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import dev.maslov.sheetsync.model.AppModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

suspend fun fetchAppsWithNotifications(context: Context): List<AppModel> = withContext(Dispatchers.IO) {
    val pm = context.packageManager

    // Use modern long-based flags
    val flags = PackageManager.ApplicationInfoFlags.of(PackageManager.GET_META_DATA.toLong())
    val allApps = pm.getInstalledApplications(flags)

    allApps
        .filter { appInfo ->
            val isSystem = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
            val isUpdatedSystem = (appInfo.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0
            !isSystem || isUpdatedSystem
        }
        .filter { appInfo ->
            isNotificationPermissionGranted(context, appInfo.packageName)
        }
        .map { appInfo ->
            AppModel(
                name = appInfo.loadLabel(pm).toString(),
                packageName = appInfo.packageName,
                icon = appInfo.loadIcon(pm)
            )
        }
        .sortedBy { it.name.lowercase() }
}

fun createNotificationChannel(context: Context) {
    val name = "Default Channel"
    val descriptionText = "Channel for general notifications"
    val importance = NotificationManager.IMPORTANCE_DEFAULT
    val channel = NotificationChannel("CHANNEL_ID", name, importance).apply {
        description = descriptionText
    }

    val notificationManager: NotificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    notificationManager.createNotificationChannel(channel)
}

private fun isNotificationPermissionGranted(context: Context, packageName: String): Boolean =
    context.packageManager.checkPermission(
        Manifest.permission.POST_NOTIFICATIONS,
        packageName
    ) == PackageManager.PERMISSION_GRANTED
