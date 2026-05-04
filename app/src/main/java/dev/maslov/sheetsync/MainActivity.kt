package dev.maslov.sheetsync

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import dagger.hilt.android.AndroidEntryPoint
import dev.maslov.sheetsync.navigation.AppNavGraph
import dev.maslov.sheetsync.service.createNotificationChannel
import dev.maslov.sheetsync.ui.theme.SheetSyncTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        createNotificationChannel(this)
        setContent {
            SheetSyncTheme {
                AppNavGraph()
            }
        }
    }
}
