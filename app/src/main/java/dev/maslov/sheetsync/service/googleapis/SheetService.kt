package dev.maslov.sheetsync.service.googleapis

import android.util.Log
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import jakarta.inject.Inject
import jakarta.inject.Singleton
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query

interface GoogleSheetsApi {
    @POST("https://sheets.googleapis.com/v4/spreadsheets/{spreadsheetId}/values/{range}:append")
    suspend fun appendValues(
        @Header("Authorization") authorization: String,
        @Query("valueInputOption") valueInputOption: String,
        @Body body: JsonObject
    ): JsonObject
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

    suspend fun appendToSheet(
        accessToken: String,
        spreadsheetId: String,
        range: String = "Sheet1!A1",
        values: List<String>
    ): Result<JsonObject> = runCatching {
        val body = JsonObject().apply {
            add(
                "values",
                JsonArray().apply {
                    add(
                        JsonArray().apply {
                            values.forEach { add(it) }
                        }
                    )
                }
            )
        }

        val response = api.appendValues(
            authorization = "Bearer $accessToken",
            valueInputOption = "RAW",
            body = body
        )

        Log.d(TAG, "Appended ${values.size} values to sheet $spreadsheetId at range $range")
        response
    }.onFailure { exception ->
        Log.e(TAG, "Error appending to sheet: ${exception.message}", exception)
    }

    suspend fun appendToSheetAsRow(
        accessToken: String,
        spreadsheetId: String,
        range: String = "Sheet1!A1",
        values: List<Any>
    ): Result<JsonObject> = runCatching {
        val body = JsonObject().apply {
            add(
                "values",
                JsonArray().apply {
                    add(
                        JsonArray().apply {
                            values.forEach { value ->
                                when (value) {
                                    is String -> add(value)
                                    is Number -> add(value)
                                    is Boolean -> add(value)
                                    else -> add(value.toString())
                                }
                            }
                        }
                    )
                }
            )
        }

        val response = api.appendValues(
            authorization = "Bearer $accessToken",
            valueInputOption = "RAW",
            body = body
        )

        Log.d(TAG, "Appended row with ${values.size} values to sheet $spreadsheetId at range $range")
        response
    }.onFailure { exception ->
        Log.e(TAG, "Error appending row to sheet: ${exception.message}", exception)
    }

    companion object {
        private const val TAG = "SheetService"
    }
}
