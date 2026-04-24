package dev.maslov.sheetsync.service

import android.content.Context
import androidx.room.Room

object DatabaseProvider {
    @Volatile
    private var INSTANCE: SheetSyncDB? = null


    fun getDatabase(context: Context): SheetSyncDB {
        return INSTANCE ?: synchronized(this) {
            val instance = Room.databaseBuilder(
                context.applicationContext,
                SheetSyncDB::class.java,
                "sheetsync_db"
            ).build()
            INSTANCE = instance
            instance
        }
    }


}