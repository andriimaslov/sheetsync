package dev.maslov.sheetsync.service.googleapis

import android.util.Log
import dev.maslov.sheetsync.model.AppendResponse
import dev.maslov.sheetsync.model.SheetsValueRange
import dev.maslov.sheetsync.model.Spreadsheet
import jakarta.inject.Inject
import jakarta.inject.Singleton
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface GoogleSheetsApi {
    @POST("v4/spreadsheets/{spreadsheetId}/values/{range}:append")
    suspend fun appendRow(
        @Path("spreadsheetId") spreadsheetId: String,
        @Path("range") range: String,
        @Body body: SheetsValueRange,
        @Query("valueInputOption") valueInputOption: String = "USER_ENTERED",
        @Query("insertDataOption") insertDataOption: String = "INSERT_ROWS"
    ): retrofit2.Response<AppendResponse>

    @GET("v4/spreadsheets/{spreadsheetId}")
    suspend fun getSpreadsheetInfo(
        @Path("spreadsheetId") spreadsheetId: String,
        @Query("includeGridData") includeGridData: Boolean = false
    ): retrofit2.Response<Spreadsheet>
}

@Singleton
class SheetService @Inject constructor(private val sheetsApi: GoogleSheetsApi) {

    /**
     * Appends a single row of data to the end of the specified sheet.
     * This function always appends to the bottom of the sheet, regardless of existing data.
     *
     * @param spreadsheetId The ID of the Google Sheets spreadsheet
     * @param range The name of the sheet (e.g., "Sheet1"). Defaults to "Sheet1"
     * @param rowData List of values to append as a single row
     * @return Result containing AppendResponse on success, or exception on failure
     */
    suspend fun appendRow(spreadsheetId: String, range: String, rowData: List<Any>): Result<AppendResponse> =
        runCatching {
            Log.d(TAG, "Appending row with ${rowData.size} columns to sheet '$range' in spreadsheet $spreadsheetId")

            require(rowData.isNotEmpty()) { "Row data cannot be empty" }
            require(spreadsheetId.isNotBlank()) { "Spreadsheet ID cannot be blank" }

            val valueRange = SheetsValueRange(
                values = listOf(rowData)
            )

            val response = sheetsApi.appendRow(
                spreadsheetId = spreadsheetId,
                range = range,
                body = valueRange
            )

            if (!response.isSuccessful) {
                val errorBody = response.errorBody()?.string() ?: "Unknown error"
                throw RuntimeException("API call failed: ${response.code()} ${response.message()}, body: $errorBody")
            }

            val appendResponse = response.body()
                ?: throw RuntimeException("Empty response body")

            if (appendResponse.spreadsheetId != spreadsheetId) {
                Log.w(
                    TAG,
                    "Response spreadsheet ID mismatch:" +
                        " expected $spreadsheetId, got ${appendResponse.spreadsheetId}"
                )
            }

            Log.d(TAG, "Successfully appended row: ${appendResponse.updates?.updatedRange}")
            appendResponse
        }.onFailure { exception ->
            Log.e(TAG, "Error appending row to sheet: ${exception.message}", exception)
            throw exception
        }

    /**
     * Retrieves information about a Google Sheets spreadsheet.
     *
     * @param spreadsheetId The ID of the Google Sheets spreadsheet
     * @param includeGridData Whether to include grid data in the response. Defaults to false
     * @return Result containing Spreadsheet information on success, or exception on failure
     */
    suspend fun getSpreadsheetInfo(spreadsheetId: String, includeGridData: Boolean = false): Result<Spreadsheet> =
        runCatching {
            Log.d(TAG, "Fetching spreadsheet info for $spreadsheetId")

            require(spreadsheetId.isNotBlank()) { "Spreadsheet ID cannot be blank" }

            val response = sheetsApi.getSpreadsheetInfo(
                spreadsheetId = spreadsheetId,
                includeGridData = includeGridData
            )

            if (!response.isSuccessful) {
                val errorBody = response.errorBody()?.string() ?: "Unknown error"
                throw RuntimeException("API call failed: ${response.code()} ${response.message()}, body: $errorBody")
            }

            val spreadsheet = response.body()
                ?: throw RuntimeException("Empty response body")

            Log.d(TAG, "Successfully retrieved spreadsheet info: ${spreadsheet.spreadsheetId}")
            spreadsheet
        }.onFailure { exception ->
            Log.e(TAG, "Error fetching spreadsheet info: ${exception.message}", exception)
            throw exception
        }

    companion object {
        private const val TAG = "SheetService"
    }
}
