package dev.maslov.sheetsync.model

data class SheetsValueRange(val values: List<List<Any>>)
data class AppendResponse(val spreadsheetId: String?, val updates: AppendUpdates?)
data class AppendUpdates(val updatedRange: String?, val updatedRows: Int?, val updatedCells: Int?)
