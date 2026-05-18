package dev.maslov.sheetsync.model

import kotlinx.serialization.Serializable

@Serializable
data class Spreadsheet(
    val spreadsheetId: String = "",
    val properties: SpreadsheetProperties = SpreadsheetProperties(title = ""),
    val sheets: List<Sheet> = emptyList(),
    val namedRanges: List<NamedRange> = emptyList(),
    val spreadsheetUrl: String? = null,
    val developerMetadata: List<DeveloperMetadata> = emptyList(),
    val dataSources: List<DataSource> = emptyList(),
    val dataSourceSchedules: List<DataSourceRefreshSchedule> = emptyList()
)

@Serializable
data class SpreadsheetProperties(
    val title: String,
    val locale: String? = null,
    val autoRecalc: String? = null,
    val timeZone: String? = null
)

@Serializable
data class Sheet(
    val properties: SheetProperties,
    val data: List<GridData> = emptyList(),
    val protectedRanges: List<ProtectedRange> = emptyList(),
    val filterViews: List<FilterView> = emptyList()
)

@Serializable
data class SheetProperties(
    val sheetId: Int,
    val title: String,
    val index: Int,
    val sheetType: String = "GRID",
    val gridProperties: GridProperties? = null
)

@Serializable
data class NamedRange(val namedRangeId: String, val name: String, val range: GridRange)

@Serializable
data class GridRange(
    val sheetId: Int,
    val startRowIndex: Int? = null,
    val endRowIndex: Int? = null,
    val startColumnIndex: Int? = null,
    val endColumnIndex: Int? = null
)

// Placeholder classes for the more complex nested objects
@Serializable data class DeveloperMetadata(val metadataId: Int, val metadataKey: String)

@Serializable data class DataSource(val dataSourceId: String)

@Serializable data class DataSourceRefreshSchedule(val enabled: Boolean)

@Serializable data class GridData(val startRow: Int, val startColumn: Int)

@Serializable data class ProtectedRange(val range: GridRange)

@Serializable data class FilterView(val filterViewId: Int, val title: String)

@Serializable data class GridProperties(val rowCount: Int, val columnCount: Int)

@Serializable data class CellFormat(val backgroundColor: String? = null)
