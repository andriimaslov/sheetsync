package dev.maslov.sheetsync.service.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import dev.maslov.sheetsync.model.Rule
import kotlinx.coroutines.flow.Flow

@Dao
interface RuleDao {
    @Query("SELECT * FROM rules")
    fun getAllRules(): Flow<List<Rule>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRule(rule: Rule)

    @Update
    suspend fun updateRule(rule: Rule)

    @Delete
    suspend fun deleteRule(rule: Rule)
}