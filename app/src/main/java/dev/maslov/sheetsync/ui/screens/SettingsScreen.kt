package dev.maslov.sheetsync.ui.screens

import android.annotation.SuppressLint
import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresPermission
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import coil.compose.AsyncImage
import dev.maslov.sheetsync.ui.components.ClientCredentialsForm
import dev.maslov.sheetsync.ui.viewmodel.AuthViewModel
import dev.maslov.sheetsync.ui.viewmodel.ClientCredentialsViewModel
import dev.maslov.sheetsync.ui.viewmodel.SettingsViewModel

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel(),
    credentialsViewModel: ClientCredentialsViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val isNotificationListenerEnabled by viewModel.isNotificationListenerEnabled.collectAsState()
    val authState by authViewModel.authState.collectAsState()
    val credentialsUiState by credentialsViewModel.uiState.collectAsState()

    // Launcher for OAuth resolution popup
    val oauthLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        authViewModel.onSheetsResolutionResult(result.data)
    }

    // Collect OAuth resolution trigger and launch the popup
    LaunchedEffect(Unit) {
        authViewModel.resolutionTrigger.collect { intentSender ->
            oauthLauncher.launch(
                IntentSenderRequest.Builder(intentSender).build()
            )
        }
    }

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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Account Section
            Text(
                text = "Account",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            if (authState.isLoggedIn && authState.user != null) {
                // User Info Card
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = MaterialTheme.shapes.medium
                        )
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Profile Picture
                        if (authState.user?.profilePicUrl != null) {
                            AsyncImage(
                                model = authState.user?.profilePicUrl,
                                contentDescription = "Profile Picture",
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = authState.user?.name?.firstOrNull()?.uppercase() ?: "?",
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                        }

                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(start = 12.dp)
                        ) {
                            Text(
                                text = authState.user?.name ?: "Unknown",
                                style = MaterialTheme.typography.titleSmall
                            )
                            Text(
                                text = authState.user?.email ?: "unknown@example.com",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }

                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Logged in",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Button(
                        onClick = { authViewModel.logout() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp)
                    ) {
                        Text("Logout")
                    }
                }
            } else {
                // Not logged in message
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = MaterialTheme.colorScheme.errorContainer,
                            shape = MaterialTheme.shapes.medium
                        )
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Not logged in",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Notification Permission Section
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
                            val intent = viewModel.getNotificationSettingsIntent()
                            context.startActivity(intent)
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

            Spacer(modifier = Modifier.height(24.dp))

            // Client Credentials Section
            ClientCredentialsForm(
                uiState = credentialsUiState,
                onClientIdChange = { credentialsViewModel.updateClientId(it) },
                onClientSecretChange = { credentialsViewModel.updateClientSecret(it) },
                onToggleShowSecret = { credentialsViewModel.toggleShowSecret() },
                onSave = { credentialsViewModel.saveCredentials() },
                onClear = { credentialsViewModel.clearCredentials() },
                modifier = Modifier.fillMaxWidth()
            )

            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Button(
                    onClick = {
                        authViewModel.beginSheetsAuthorization()
                    },
                    enabled = !authState.isLoading
                ) {
                    Text(if (authState.isLoading) "Authorizing..." else "Fire auth")
                }
            }
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
