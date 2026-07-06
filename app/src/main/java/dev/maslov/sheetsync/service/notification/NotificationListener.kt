package dev.maslov.sheetsync.service.notification

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import android.util.LruCache
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import dagger.hilt.android.AndroidEntryPoint
import dev.maslov.sheetsync.model.Rule
import dev.maslov.sheetsync.service.rules.RuleRepository
import dev.maslov.sheetsync.service.work.NotificationProcessingWorker
import jakarta.inject.Inject
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

@AndroidEntryPoint
class NotificationListener : NotificationListenerService() {
    @Inject
    lateinit var repository: RuleRepository

    @Inject
    lateinit var workManager: WorkManager

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var activeRules = listOf<Rule>()
    private val processedNotifications = LruCache<String, String>(100)

    private companion object {
        const val TAG = "NotificationListener"
    }

    override fun onCreate() {
        super.onCreate()
        Log.i(TAG, "NotificationListener service created")
        scope.launch {
            repository.rules.collect { rules ->
                activeRules = rules.filter { it.isActive }
                Log.d(TAG, "Active rules updated: ${activeRules.size} rules loaded")
            }
        }
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        if (sbn == null) return

        val packageName = sbn.packageName
        val matchedRule = activeRules.find { it.appId == packageName }

        if (matchedRule != null) {
            if ((sbn.notification.flags and Notification.FLAG_GROUP_SUMMARY) != 0) {
                Log.d(TAG, "Ignoring group summary notification from ${sbn.packageName}")
                return
            }

            val notificationKey = sbn.key
            val extras = sbn.notification.extras
            val notificationText = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: "No text content"

            Log.d(TAG, "notification key - $notificationKey")
            Log.d(TAG, "text - $notificationText")
            Log.d(TAG, "cache size before - ${processedNotifications.size()}")
            val lastSeenText = processedNotifications[notificationKey]
            if (lastSeenText == notificationText) {
                Log.d(TAG, "Ignoring duplicate notification update for key: $notificationKey")
                return
            }

            Log.d(TAG, "Notification posted from package: $packageName")
            Log.d(TAG, "Currently tracking ${activeRules.size} active rules")

            Log.d(TAG, "✓ Notification matched rule: ${matchedRule.title} (appId: ${matchedRule.appId})")

            processedNotifications.put(notificationKey, notificationText)
            Log.d(TAG, "cache size after - ${processedNotifications.size()}")

            val workData = workDataOf(
                "ruleId" to matchedRule.id.toString(),
                "pkg" to packageName,
                "text" to notificationText
            )

            val request = OneTimeWorkRequestBuilder<NotificationProcessingWorker>()
                .setInputData(workData)
                .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
                .build()

            workManager.enqueue(request)
        } else {
            Log.d(TAG, "✗ No matching rule for package: $packageName")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }
}
