package dev.maslov.sheetsync.ui.screens

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresPermission
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import dev.maslov.sheetsync.ui.viewmodel.SettingsViewModel

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onBack: () -> Unit, viewModel: SettingsViewModel = hiltViewModel()) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val isNotificationListenerEnabled by viewModel.isNotificationListenerEnabled.collectAsState()

    // Refresh permission status when screen resumes/becomes visible
    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.lifecycle.addObserver(object : androidx.lifecycle.LifecycleEventObserver {
            override fun onStateChanged(source: androidx.lifecycle.LifecycleOwner, event: Lifecycle.Event) {
                if (event == Lifecycle.Event.ON_RESUME) {
                    viewModel.refreshNotificationPermissionStatus()
                }
            }
        })
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, null)
                    }
                }
            )
        }
    ) { paddingValues ->
        // Notification Permission Status Card
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = if (isNotificationListenerEnabled) {
                        MaterialTheme.colorScheme.surfaceVariant
                    } else {
                        MaterialTheme.colorScheme.errorContainer
                    },
                    shape = MaterialTheme.shapes.medium
                )
                .padding(paddingValues)
        ) {
            // Status header with icon
            val icon = if (isNotificationListenerEnabled) {
                Icons.Default.CheckCircle
            } else {
                Icons.Default.Warning
            }
            val statusColor = if (isNotificationListenerEnabled) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.error
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = statusColor,
                    modifier = Modifier.padding(end = 12.dp)
                )
                Text(
                    text = if (isNotificationListenerEnabled) {
                        "Notification Access: Enabled"
                    } else {
                        "Notification Access: Disabled"
                    },
                    style = MaterialTheme.typography.titleMedium
                )
            }

            // Description text
            Text(
                text = if (isNotificationListenerEnabled) {
                    "SheetSync can monitor notifications from other apps."
                } else {
                    "SheetSync needs notification access to monitor incoming notifications from selected apps. Tap the button below to enable it."
                },
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 12.dp, start = 10.dp)
            )

            // Enable button (only show if disabled)
            if (!isNotificationListenerEnabled) {
                Button(
                    onClick = {
                        val intent = viewModel.getNotificationSettingsIntent()
                        context.startActivity(intent)
                        // Refresh status after returning from settings
                        viewModel.refreshNotificationPermissionStatus()
                    },
                    modifier = Modifier
                        .padding(top = 12.dp)
                        .fillMaxWidth()
                ) {
                    Text("Open Notification Access Settings")
                }
            }
        }
    }

    val channelId = "CHANNEL_ID"

    // 2. Permission Launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            showSimpleNotification(context, channelId)
        }
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Button(onClick = {
            // 3. Check and request permission
            when (PackageManager.PERMISSION_GRANTED) {
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) -> {
                    showSimpleNotification(context, channelId)
                }
                else -> {
                    permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        }) {
            Text("Fire Notification")
        }
    }
}

@RequiresPermission("android.permission.POST_NOTIFICATIONS")
private fun showSimpleNotification(context: Context, channelId: String) {
    val builder = NotificationCompat.Builder(context, channelId)
        .setSmallIcon(android.R.drawable.ic_dialog_info) // Use your own icon here
        .setContentTitle("New Message")
        .setContentText(
            """
            ПриватБанк: -99% Цифрові товари. Youtube premium
            1234 22:10
            Бал. 1111.00 грн
            """.trimIndent()
        )
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        .setAutoCancel(true)

    with(NotificationManagerCompat.from(context)) {
        // NotificationId is a unique int for each notification
        notify(101, builder.build())
    }
}
