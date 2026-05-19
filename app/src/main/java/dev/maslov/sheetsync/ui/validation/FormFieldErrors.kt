package dev.maslov.sheetsync.ui.validation

/**
 * Represents field-level validation errors in forms
 * Maps field names to their error messages
 */
data class FormFieldErrors(
    val titleError: String? = null,
    val parserError: String? = null,
    val sheetError: String? = null,
    val tabError: String? = null,
    val appError: String? = null
) {
    fun isEmpty(): Boolean =
        titleError == null && parserError == null && sheetError == null && tabError == null && appError == null
}
