package dev.maslov.sheetsync.service.parser

import dev.maslov.sheetsync.model.BankTransaction
import java.util.Optional

interface NotificationParser {
    val name: String
    fun parse(text: String): Optional<BankTransaction>
}
