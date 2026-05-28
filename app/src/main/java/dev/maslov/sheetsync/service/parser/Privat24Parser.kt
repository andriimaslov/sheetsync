package dev.maslov.sheetsync.service.parser

import android.util.Log
import dev.maslov.sheetsync.model.BankTransaction
import java.util.Optional
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
    override fun parse(text: String): Optional<BankTransaction> {
        val matchResult = notificationTextRegex.find(text)
        return if (matchResult != null) {
            val rawAmount = matchResult.groupValues[1].trim()
            val rawDescription = matchResult.groupValues[2]
                .replace(Regex("\\s+"), " ")
                .trim()
            Optional.of<BankTransaction>(BankTransaction(ACCOUNT_TYPE, rawDescription, rawAmount))
        } else {
            Log.d(TAG, "Failed to match text with regex")
            Optional.empty<BankTransaction>()
        }
    }
}
