package dev.maslov.sheetsync.ui.components

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.maslov.sheetsync.ui.viewmodel.SettingsViewModel

@Composable
fun NotificationsReadStatusBar(
    isNotificationListenerEnabled: Boolean,
    settingsViewModel: SettingsViewModel,
    context: Context
) {
    Text(
        text = "Notifications",
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.padding(bottom = 12.dp)
    )

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
            .padding(16.dp)
    ) {
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
                style = MaterialTheme.typography.titleSmall
            )
        }

        Text(
            text = if (isNotificationListenerEnabled) {
                "SheetSync can monitor notifications from other apps."
            } else {
                "SheetSync needs notification access to monitor incoming notifications from selected apps. Tap the button below to enable it."
            },
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(top = 12.dp)
        )

        if (!isNotificationListenerEnabled) {
            Button(
                onClick = {
                    val intent = settingsViewModel.getNotificationSettingsIntent()
                    context.startActivity(intent)
                    settingsViewModel.refreshNotificationPermissionStatus()
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
