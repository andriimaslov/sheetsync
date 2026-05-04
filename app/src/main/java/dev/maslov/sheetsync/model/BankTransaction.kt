package dev.maslov.sheetsync.model

import java.time.LocalDateTime

data class BankTransaction(val date: LocalDateTime, val description: String, val amount: String)
