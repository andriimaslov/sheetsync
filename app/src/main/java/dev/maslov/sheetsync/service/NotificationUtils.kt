package dev.maslov.sheetsync.service

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import dev.maslov.sheetsync.model.AppModel
import dev.maslov.sheetsync.model.BankTransaction
import java.time.LocalDateTime
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

val notificationTextRegex = """([+-][\d\s]+)\S\s+(.*?)(?=\s+\*|\n|\d{2}:\d{2})""".toRegex()

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

fun parseNotificationText(text: String): BankTransaction? {
    // We use [\s\S] to match across newlines without needing extra flags
    val matchResult = notificationTextRegex.find(text)

    return if (matchResult != null) {
        // groupValues[0] is the entire match, [1] and [2] are our captures
        val rawAmount = matchResult.groupValues[1].trim()

        // Clean up the description: replace newlines and multiple spaces with a single space
        val rawDescription = matchResult.groupValues[2]
            .replace(Regex("\\s+"), " ")
            .trim()

        BankTransaction(LocalDateTime.now(), rawDescription, rawAmount)
    } else {
        null
    }
}

private fun isNotificationPermissionGranted(context: Context, packageName: String): Boolean =
    context.packageManager.checkPermission(
        Manifest.permission.POST_NOTIFICATIONS,
        packageName
    ) == PackageManager.PERMISSION_GRANTED
