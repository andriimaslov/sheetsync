package dev.maslov.sheetsync.model

data class NotificationText(
    val value: String
) {

    fun isCredit(): Boolean {
        return value.contains("Кред. ліміт")
    }

    fun isRejection(): Boolean {
        return value.contains("Відмова");
    }
}
