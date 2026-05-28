package dev.maslov.sheetsync.service.googleapis

import android.util.Log
import com.google.gson.JsonObject
import dev.maslov.sheetsync.model.SheetMetadata
import jakarta.inject.Inject
import jakarta.inject.Singleton
import retrofit2.http.GET
import retrofit2.http.Query

interface GoogleDriveApi {
    @GET("https://www.googleapis.com/drive/v3/files")
    suspend fun listFiles(
        @Query("q") query: String,
        @Query("spaces") spaces: String = "drive",
        @Query("fields") fields: String = "files(id,name,mimeType,modifiedTime)",
        @Query("pageSize") pageSize: Int = 1000
    ): JsonObject
}

@Singleton
class DriveService @Inject constructor(private val driveApi: GoogleDriveApi) {

    suspend fun getAllSheets(): Result<List<SheetMetadata>> = runCatching {
        val query = "mimeType='application/vnd.google-apps.spreadsheet' and trashed=false"
        val response = driveApi.listFiles(
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
