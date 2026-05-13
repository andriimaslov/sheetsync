package dev.maslov.sheetsync.service.googleapis

import android.util.Log
import dev.maslov.sheetsync.model.AppendResponse
import dev.maslov.sheetsync.model.SheetsValueRange
import jakarta.inject.Inject
import jakarta.inject.Singleton
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface GoogleSheetsApi {
    @POST("v4/spreadsheets/{spreadsheetId}/values/{range}:append")
    suspend fun appendRow(
        @Header("Authorization") authorization: String,
        @Path("spreadsheetId") spreadsheetId: String,
        @Path("range") range: String,
        @Body body: SheetsValueRange,
        @Query("valueInputOption") valueInputOption: String = "USER_ENTERED",
        @Query("insertDataOption") insertDataOption: String = "INSERT_ROWS"
    ): retrofit2.Response<AppendResponse>
}

@Singleton
class SheetService @Inject constructor() {
    private val retrofit: Retrofit by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }
        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()

        Retrofit.Builder()
            .baseUrl("https://sheets.googleapis.com/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    private val api: GoogleSheetsApi by lazy {
        retrofit.create(GoogleSheetsApi::class.java)
    }

    /**
     * Appends a single row of data to the end of the specified sheet.
     * This function always appends to the bottom of the sheet, regardless of existing data.
     *
     * @param accessToken OAuth 2.0 access token for authentication
     * @param spreadsheetId The ID of the Google Sheets spreadsheet
     * @param range The name of the sheet (e.g., "Sheet1"). Defaults to "Sheet1"
     * @param rowData List of values to append as a single row
     * @return Result containing AppendResponse on success, or exception on failure
     */
    suspend fun appendRow(
        accessToken: String,
        spreadsheetId: String,
        range: String,
        rowData: List<Any>
    ): Result<AppendResponse> = runCatching {
        Log.d(TAG, "Appending row with ${rowData.size} columns to sheet '$range' in spreadsheet $spreadsheetId")

        // Validate input
        require(rowData.isNotEmpty()) { "Row data cannot be empty" }
        require(accessToken.isNotBlank()) { "Access token cannot be blank" }
        require(spreadsheetId.isNotBlank()) { "Spreadsheet ID cannot be blank" }

        val valueRange = SheetsValueRange(
            values = listOf(rowData)
        )

        val response = api.appendRow(
            authorization = "Bearer $accessToken",
            spreadsheetId = spreadsheetId,
            range = range,
            body = valueRange
        )

        if (!response.isSuccessful) {
            val errorBody = response.errorBody()?.string() ?: "Unknown error"
            throw RuntimeException("API call failed: ${response.code()} ${response.message()}, body: $errorBody")
        }

        // Get the parsed response body (Retrofit handles JSON parsing automatically)
        val appendResponse = response.body()
            ?: throw RuntimeException("Empty response body")

        // Validate the response has the expected spreadsheet ID
        if (appendResponse.spreadsheetId != spreadsheetId) {
            Log.w(TAG, "Response spreadsheet ID mismatch: expected $spreadsheetId, got ${appendResponse.spreadsheetId}")
        }

        Log.d(TAG, "Successfully appended row: ${appendResponse.updates?.updatedRange}")
        appendResponse
    }.onFailure { exception ->
        Log.e(TAG, "Error appending row to sheet: ${exception.message}", exception)
        throw exception // Re-throw to maintain Result.failure behavior
    }

    companion object {
        private const val TAG = "SheetService"
    }
}
