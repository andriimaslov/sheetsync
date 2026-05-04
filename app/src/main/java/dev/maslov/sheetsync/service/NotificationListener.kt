package dev.maslov.sheetsync.service

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import dagger.hilt.android.AndroidEntryPoint
import dev.maslov.sheetsync.model.Rule
import dev.maslov.sheetsync.ui.viewmodel.RuleViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

@AndroidEntryPoint
class NotificationListener : NotificationListenerService() {
    @Inject
    lateinit var ruleViewModel: RuleViewModel

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
            ruleViewModel.rules.collect { rules ->
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
            val bankTransaction = parseNotificationText(notificationText)
            if (bankTransaction != null) {
                Log.d(
                    TAG,
                    "✓ Parsed notification: amount: ${bankTransaction.amount}, description: ${bankTransaction.description}"
                )
            } else {
                Log.d(TAG, "✗ Failed to parse bank transaction from notification text")
            }
        } else {
            Log.d(TAG, "✗ No matching rule for package: $packageName")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }
}
