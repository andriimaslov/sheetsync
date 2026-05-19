package dev.maslov.sheetsync.service.parser

import dev.maslov.sheetsync.model.BankTransaction

interface NotificationParser {
    val name: String
    fun parse(text: String): BankTransaction
}
