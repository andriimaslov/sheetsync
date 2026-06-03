package dev.maslov.sheetsync.model

enum class Currency(val symbol: String) {
    UAH("₴"),
    USD("$"),
    EUR("€"),
    PERCENT("%"), // From the example in settings
    UNKNOWN("");

    companion object {
        fun fromSymbol(symbol: String): Currency = entries.find { it.symbol == symbol } ?: UNKNOWN
    }
}
