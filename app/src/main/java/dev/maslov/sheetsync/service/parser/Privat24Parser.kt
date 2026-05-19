package dev.maslov.sheetsync.service.parser

import android.util.Log
import dev.maslov.sheetsync.model.BankTransaction
import javax.inject.Inject

class Privat24Parser @Inject constructor() : NotificationParser {
    override val name: String
        get() = NAME

    companion object {
        private const val NAME = "PRIVAT24"
        private const val TAG = "Privat24Parser"
        private const val ACCOUNT_TYPE = "fiz"
        val notificationTextRegex = """([+-][\d\s]+)\S\s+(.*?)(?=\s+\*|\n|\d{2}:\d{2})""".toRegex()
    }
    override fun parse(text: String): BankTransaction {
        // We use [\s\S] to match across newlines without needing extra flags
        val matchResult = notificationTextRegex.find(text)

        return if (matchResult != null) {
            // groupValues[0] is the entire match, [1] and [2] are our captures
            val rawAmount = matchResult.groupValues[1].trim()

            // Clean up the description: replace newlines and multiple spaces with a single space
            val rawDescription = matchResult.groupValues[2]
                .replace(Regex("\\s+"), " ")
                .trim()

            BankTransaction(ACCOUNT_TYPE, rawDescription, rawAmount)
        } else {
            Log.d(TAG, "Failed to match text with regex")
            throw RuntimeException()
        }
    }
}
