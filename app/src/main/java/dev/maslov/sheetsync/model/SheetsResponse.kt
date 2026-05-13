package dev.maslov.sheetsync.model

// The request body. Sheets expects an array of rows, where each row is an array of columns.
data class SheetsValueRange(val values: List<List<Any>>)

// The response body (simplified to just what you need to verify success)
data class AppendResponse(val spreadsheetId: String?, val updates: AppendUpdates?)

data class AppendUpdates(val updatedRange: String?, val updatedRows: Int?, val updatedCells: Int?)
