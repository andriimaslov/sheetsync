package dev.maslov.sheetsync.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import de.brudaswen.android.logcat.database.LogcatItemDto
import de.brudaswen.android.logcat.ui.details.LogcatDetailsScreen
import de.brudaswen.android.logcat.ui.list.LogcatListScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogsListScreen(onBack: () -> Unit, onLogClick: (LogcatItemDto) -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Logs") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            LogcatListScreen(
                onItemClick = onLogClick
            )
        }
    }
}

@Composable
fun LogsDetailsScreen(uuid: String, onBack: () -> Unit) {
    LogcatDetailsScreen(
        uuid = uuid,
        onUpClick = onBack
    )
}
