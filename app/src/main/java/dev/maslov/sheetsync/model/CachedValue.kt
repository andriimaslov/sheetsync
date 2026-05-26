package dev.maslov.sheetsync.model

class CachedValue<T>(val value: T, val timestamp: Long = System.currentTimeMillis()) {

    fun isSheetExpired(): Boolean = System.currentTimeMillis() - timestamp > SHEETS_TTL

    fun isTabExpired(): Boolean = System.currentTimeMillis() - timestamp > TABS_TTL

    companion object {
        private const val SHEETS_TTL = 5 * 60 * 1000L // 5 minutes
        private const val TABS_TTL = 10 * 60 * 1000L // 10 minutes
    }
}
