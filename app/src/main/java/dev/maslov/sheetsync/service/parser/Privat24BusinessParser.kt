package dev.maslov.sheetsync.service.parser

import android.util.Log
import dev.maslov.sheetsync.model.BankTransaction
import javax.inject.Inject

class Privat24BusinessParser @Inject constructor() : NotificationParser {
    override val name: String
        get() = NAME

    companion object {
        private const val NAME = "PRIVAT24BUSINESS"
        private const val TAG = "Privat24BusinessParser"
        private const val ACCOUNT_TYPE = "fop"

        // TODO update with actual regex for P24B
        private val notificationTextRegex = """([+-][\d\s]+)\S\s+(.*?)(?=\s+\*|\n|\d{2}:\d{2})""".toRegex()
    }
    override fun parse(text: String): BankTransaction {
        val matchResult = notificationTextRegex.find(text)
        return if (matchResult != null) {
            val rawAmount = matchResult.groupValues[1].trim()
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
