package dev.maslov.sheetsync.service.notification

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import dagger.hilt.android.AndroidEntryPoint
import dev.maslov.sheetsync.model.Rule
import dev.maslov.sheetsync.service.googleapis.SheetService
import dev.maslov.sheetsync.service.rules.RuleRepository
import dev.maslov.sheetsync.service.token.AuthorizationManager
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
    lateinit var sheetService: SheetService

    @Inject
    lateinit var authorizationManager: AuthorizationManager

    @Inject
    lateinit var workManager: WorkManager

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var activeRules = listOf<Rule>()

    private companion object {
        const val TAG = "NotificationListener"
    }

    override fun onCreate() {
        super.onCreate()
        Log.i(TAG, "NotificationListener service created")
        // Collect active rules in the background
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
        Log.d(TAG, "Notification posted from package: $packageName")
        Log.d(TAG, "Currently tracking ${activeRules.size} active rules")

        // Check if notification is from an app in active rules
        val matchedRule = activeRules.find { it.appId == packageName }
        val extras = sbn.notification.extras
        val notificationText = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: "No text content"

        if (matchedRule != null) {
            Log.d(TAG, "✓ Notification matched rule: ${matchedRule.title} (appId: ${matchedRule.appId})")
            // Enqueue background work to process and append the row. WorkManager will handle retries.
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
