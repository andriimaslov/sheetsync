package dev.maslov.sheetsync.service

import androidx.collection.SieveCache
import dev.maslov.sheetsync.model.CachedValue
import dev.maslov.sheetsync.model.Sheet
import dev.maslov.sheetsync.model.SheetMetadata
import jakarta.inject.Inject

class SheetRepository @Inject constructor() {

    private val sheetCache = SieveCache<String, CachedValue<List<SheetMetadata>>>(SHEET_SIZE)
    private val tabCache = SieveCache<String, CachedValue<List<Sheet>>>(TAB_SIZE)

    fun getSheetCache(): SieveCache<String, CachedValue<List<SheetMetadata>>> = sheetCache

    fun getTabCache(): SieveCache<String, CachedValue<List<Sheet>>> = tabCache

    companion object {
        private const val SHEET_SIZE = 1
        private const val TAB_SIZE = 50
    }
}
