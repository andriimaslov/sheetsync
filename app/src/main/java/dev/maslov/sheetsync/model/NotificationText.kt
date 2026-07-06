package dev.maslov.sheetsync.model

data class NotificationText(val value: String) {

    fun isCredit(): Boolean = value.contains("Кред. ліміт")

    fun isRejection(): Boolean = value.contains("Відмова")
}
