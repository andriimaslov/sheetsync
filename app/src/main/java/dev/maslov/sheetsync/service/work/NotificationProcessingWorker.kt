package dev.maslov.sheetsync.service.work

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import dev.maslov.sheetsync.MainActivity
import dev.maslov.sheetsync.model.Rule
import dev.maslov.sheetsync.service.googleapis.SheetService
import dev.maslov.sheetsync.service.parser.NotificationParser
import dev.maslov.sheetsync.service.rules.RuleRepository
import dev.maslov.sheetsync.service.token.AuthorizationManager
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@HiltWorker
class NotificationProcessingWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val authorizationManager: AuthorizationManager,
    private val sheetService: SheetService,
    private val ruleRepository: RuleRepository,
    private val parsers: Map<String, @JvmSuppressWildcards NotificationParser>
) : CoroutineWorker(appContext, params) {

    companion object {
        private const val TAG = "NotificationWorker"
        private const val NOTIFICATION_ID = 9001
        private const val CHANNEL_ID = "NOTIFICATION_WORKER_CHANNEL"
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val ruleId = inputData.getString("ruleId") ?: ""
            val packageName = inputData.getString("pkg") ?: ""
            val notificationText = inputData.getString("text") ?: ""

            val rule = ruleRepository.getRuleById(UUID.fromString(ruleId))

            Log.d(TAG, "Processing notification for rule=${rule.id}, package=$packageName, sheet=${rule.sheetId}")

            val accessToken = authorizationManager.validateAndRefreshToken()
            if (accessToken.isNullOrBlank()) {
                Log.w(TAG, "No access token available; posting user notification to re-authenticate")
                postReAuthNotification()
                updateRule(rule, "Auth Required", null)
                return@withContext Result.failure()
            }

            if (notificationText.isCreditTransaction()) {
                Log.d(TAG, "Skipping credit limit transaction notification")
                return@withContext Result.success()
            }

            val parser = parsers[rule.parser]
            if (parser == null) {
                Log.e(TAG, "Unknown parser id=${rule.parser} for rule=${rule.id}")
                updateRule(rule, "Invalid Parser", null)
                return@withContext Result.failure()
            }

            val parsedTransaction = try {
                parser.parse(notificationText)
            } catch (e: Exception) {
                Log.e(TAG, "Parser ${rule.parser} failed for rule=${rule.id}: ${e.message}", e)
                updateRule(rule, "Parse Error", null)
                return@withContext Result.failure()
            }

            val values =
                listOf(
                    parsedTransaction.account,
                    LocalDate.now().toString(),
                    parsedTransaction.description,
                    parsedTransaction.amount
                )

            val result = sheetService.appendRow(accessToken, rule.sheetId, rule.tabName, values)
            return@withContext if (result.isSuccess) {
                Log.d(TAG, "Sheet append successful for ${rule.sheetId}")
                updateRule(rule, "Success", LocalDateTime.now())
                Result.success()
            } else {
                Log.w(TAG, "Sheet append failed, scheduling retry")
                updateRule(rule, "Failed", null)
                Result.retry()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Worker failed: ${e.message}", e)
            return@withContext Result.retry()
        }
    }

    private suspend fun updateRule(rule: Rule, status: String, lastRunAt: LocalDateTime?) {
        try {
            Log.d(TAG, "Updating rule ${rule.id} status to $status, lastRunAt=$lastRunAt")
            val updatedRule = rule.copy(lastRunStatus = status, lastRunAt = lastRunAt ?: rule.lastRunAt)
            ruleRepository.updateRule(updatedRule)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update rule: ${e.message}")
        }
    }

    private fun postReAuthNotification() {
        val notificationManager = applicationContext.getSystemService(
            Context.NOTIFICATION_SERVICE
        ) as NotificationManager

        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("SheetSync Re-authentication Required")
            .setContentText("Your Google account has expired. Open the app to sign in again.")
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    private fun String.isCreditTransaction(): Boolean = contains("Кред. ліміт")
}
