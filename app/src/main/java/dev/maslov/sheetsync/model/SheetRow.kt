package dev.maslov.sheetsync.model

import java.time.LocalDate

data class SheetRow(
    private val account: String,
    private val date: LocalDate,
    private val category: String,
    private val description: String,
    private val amount: Double
)
