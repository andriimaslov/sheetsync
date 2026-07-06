package dev.maslov.sheetsync.service.parser

import android.util.Log
import dev.maslov.sheetsync.model.BankTransaction
import dev.maslov.sheetsync.model.Currency
import java.util.Optional
import javax.inject.Inject

class Privat24Parser @Inject constructor() : NotificationParser {
    override val name: String
        get() = NAME

    companion object {
        private const val NAME = "PRIVAT24"
        private const val TAG = "Privat24Parser"
        private const val ACCOUNT_TYPE = "fiz"
        val notificationTextRegex = """([+-][\d+(.|,)\s]+)(\S)\s+(.*?)(?=\s+\*|\n|\d{2}:\d{2})""".toRegex()
    }
    override fun parse(text: String): Optional<BankTransaction> {
        val matchResult = notificationTextRegex.find(text)
        return if (matchResult != null) {
            val rawAmount = matchResult.groupValues[1]
                .replace(Regex("[\\s ]"), "")
                .replace(",", ".")
                .trim()
            val rawSymbol = matchResult.groupValues[2]
            val currency = Currency.fromSymbol(rawSymbol)

            val rawDescription = matchResult.groupValues[3]
                .replace(Regex("\\s+"), " ")
                .trim()
            try {
                Optional.of<BankTransaction>(
                    BankTransaction(
                        account = ACCOUNT_TYPE,
                        description = rawDescription,
                        amount = rawAmount.toDouble(),
                        currency = currency
                    )
                )
            } catch (e: NumberFormatException) {
                Log.d(TAG, "Failed to parse amount: ${matchResult.groupValues[1]}. Error: ${e.message}")
                Optional.empty<BankTransaction>()
            }
        } else {
            Log.d(TAG, "Failed to match text with regex. Notification: $text")
            Optional.empty<BankTransaction>()
        }
    }
}
