package dev.maslov.sheetsync.service.credentials

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import dev.maslov.sheetsync.model.ClientCredentials
import kotlinx.coroutines.flow.Flow

@Dao
interface ClientCredentialsDao {
    @Query("SELECT * FROM client_credentials LIMIT 1")
    fun getCredentials(): Flow<ClientCredentials?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveCredentials(credentials: ClientCredentials)

    @Query("DELETE FROM client_credentials")
    suspend fun clearCredentials()
}
