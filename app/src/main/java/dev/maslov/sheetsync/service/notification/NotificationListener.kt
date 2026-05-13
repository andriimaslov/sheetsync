package dev.maslov.sheetsync.service.notification

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import dagger.hilt.android.AndroidEntryPoint
import dev.maslov.sheetsync.model.Rule
import dev.maslov.sheetsync.service.googleapis.SheetService
import dev.maslov.sheetsync.service.rules.RuleRepository
import dev.maslov.sheetsync.service.token.AuthorizationManager
import jakarta.inject.Inject
import java.time.LocalDate
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
            val bankTransaction = parseNotificationText(notificationText)
            if (bankTransaction != null) {
                Log.d(
                    TAG,
                    "✓ Parsed notification: amount: ${bankTransaction.amount}, description: ${bankTransaction.description}"
                )

                val account = if (packageName.contains("business")) "fop" else "fiz"
                val category = if (bankTransaction.description.contains(
                        "Переказ"
                    )
                ) {
                    "transfer from card"
                } else {
                    "online purchase"
                }
                val values =
                    listOf(account, LocalDate.now().toString(), category, bankTransaction.description, bankTransaction.amount)

                scope.launch {
                    val accessToken = authorizationManager.validateAndRefreshToken()
                    sheetService.appendRow(accessToken!!, matchedRule.sheetId, "Sheet1", values)
                }
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
