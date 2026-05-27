package dev.maslov.sheetsync

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp
import de.brudaswen.android.logcat.Logcat
import java.util.concurrent.Executors
import javax.inject.Inject
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

@HiltAndroidApp
class SheetSyncApplication :
    Application(),
    Configuration.Provider {
    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    private val applicationScope = MainScope()

    override fun onCreate() {
        super.onCreate()

        applicationScope.launch {
            Logcat(this@SheetSyncApplication).service.start()
        }
    }

    override val workManagerConfiguration: Configuration
        get() {
            return Configuration.Builder()
                .setWorkerFactory(workerFactory)
                .setExecutor(Executors.newFixedThreadPool(2))
                .build()
        }
}
