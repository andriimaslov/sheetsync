package dev.maslov.sheetsync.service.parser

import android.util.Log
import dev.maslov.sheetsync.model.BankTransaction
import dev.maslov.sheetsync.model.Currency
import java.util.Optional

abstract class AbstractPrivat24Parser : NotificationParser {
    protected abstract val tag: String
    protected abstract val accountType: String

    companion object {
        private val notificationTextRegex = """([+-][\d+(.|,)\s]+)(\S)\s+(.*?)(?=\s+\*|\n|\d{2}:\d{2})""".toRegex()
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
                Optional.of(
                    BankTransaction(
                        account = accountType,
                        description = rawDescription,
                        amount = rawAmount.toDouble(),
                        currency = currency
                    )
                )
            } catch (e: NumberFormatException) {
                Log.d(tag, "Failed to parse amount: ${matchResult.groupValues[1]}. Error: ${e.message}")
                Optional.empty()
            }
        } else {
            Log.d(tag, "Failed to match text with regex. Notification: $text")
            Optional.empty()
        }
    }
}
