package dev.maslov.sheetsync.service.token

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import dev.maslov.sheetsync.model.Token
import kotlinx.coroutines.flow.Flow

@Dao
interface TokenDao {
    @Query("SELECT * FROM tokens LIMIT 1")
    fun getToken(): Flow<Token?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveToken(token: Token)

    @Query("DELETE FROM tokens")
    suspend fun cleanToken()
}
