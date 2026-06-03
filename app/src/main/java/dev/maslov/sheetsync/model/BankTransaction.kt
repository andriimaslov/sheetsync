package dev.maslov.sheetsync.model

data class BankTransaction(
    val account: String,
    val description: String,
    val amount: Double,
    val currency: Currency = Currency.UAH
)
