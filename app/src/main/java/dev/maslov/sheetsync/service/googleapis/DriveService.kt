package dev.maslov.sheetsync.service.googleapis

import android.util.Log
import com.google.gson.JsonObject
import dev.maslov.sheetsync.model.SheetMetadata
import jakarta.inject.Inject
import jakarta.inject.Singleton
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface GoogleDriveApi {
    @GET("https://www.googleapis.com/drive/v3/files")
    suspend fun listFiles(
        @Header("Authorization") authorization: String,
        @Query("q") query: String,
        @Query("spaces") spaces: String = "drive",
        @Query("fields") fields: String = "files(id,name,mimeType,modifiedTime)",
        @Query("pageSize") pageSize: Int = 1000
    ): JsonObject
}

@Singleton
class DriveService @Inject constructor() {
    private val retrofit: Retrofit by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }
        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()

        Retrofit.Builder()
            .baseUrl("https://www.googleapis.com/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    private val api: GoogleDriveApi by lazy {
        retrofit.create(GoogleDriveApi::class.java)
    }

    suspend fun getAllSheets(accessToken: String): Result<List<SheetMetadata>> = runCatching {
        val query = "mimeType='application/vnd.google-apps.spreadsheet' and trashed=false"
        val response = api.listFiles(
            authorization = "Bearer $accessToken",
            query = query
        )

        val sheets = response.getAsJsonArray("files")?.mapNotNull { fileElement ->
            val file = fileElement.asJsonObject
            try {
                SheetMetadata(
                    id = file.get("id").asString,
                    name = file.get("name").asString,
                    mimeType = file.get("mimeType").asString,
                    modifiedTime = file.get("modifiedTime")?.asString
                )
            } catch (e: Exception) {
                Log.w(TAG, "Error parsing file data: ${e.message}")
                null
            }
        } ?: emptyList()

        Log.d(TAG, "Retrieved ${sheets.size} spreadsheets from Drive")
        sheets
    }.onFailure { exception ->
        Log.e(TAG, "Error retrieving sheets from Drive: ${exception.message}", exception)
    }

    companion object {
        private const val TAG = "DriveService"
    }
}
