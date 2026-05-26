package dev.maslov.sheetsync.ui.validation

/**
 * Reusable validation helper functions for forms
 */
object ValidationHelper {
    const val TITLE_REQUIRED = "Title is required"
    const val PARSER_REQUIRED = "Please select a parser type"
    const val SHEET_REQUIRED = "Please select a sheet"
    const val TAB_REQUIRED = "Please select a tab"
    const val APP_REQUIRED = "Please select an app"

    /**
     * Validates if a string field is not empty
     */
    fun isStringValid(value: String?): Boolean = !value.isNullOrBlank()

    /**
     * Validates if an object (selection) is not null
     */
    fun <T> isSelected(value: T?): Boolean = value != null

    /**
     * Validates if a list has items
     */
    fun isListNotEmpty(list: List<*>?): Boolean = !list.isNullOrEmpty()
}
