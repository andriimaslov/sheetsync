package dev.maslov.sheetsync.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.maslov.sheetsync.model.uistate.ClientCredentialsUiState

@Composable
fun ClientCredentialsForm(
    uiState: ClientCredentialsUiState,
    onToggleShowSecret: () -> Unit,
    onSelectServiceAccountFile: () -> Unit,
    onSave: () -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = "API Credentials",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = if (uiState.areSaved) {
                        MaterialTheme.colorScheme.surfaceVariant
                    } else {
                        MaterialTheme.colorScheme.errorContainer
                    },
                    shape = MaterialTheme.shapes.medium
                )
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (uiState.areSaved) {
                    Icons.Default.CheckCircle
                } else {
                    Icons.Default.Warning
                },
                contentDescription = null,
                tint = if (uiState.areSaved) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.error
                },
                modifier = Modifier
                    .size(20.dp)
                    .padding(end = 8.dp)
            )
            Text(
                text = if (uiState.areSaved) {
                    "Credentials are saved"
                } else {
                    "Credentials not configured"
                },
                style = MaterialTheme.typography.bodySmall
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = onSelectServiceAccountFile,
                modifier = Modifier.weight(1f),
                shape = MaterialTheme.shapes.small
            ) {
                Text("Select JSON File")
            }
        }

        TextField(
            value = if (uiState.showSecret) {
                uiState.serviceAccountJson
            } else {
                uiState.serviceAccountJsonName
            },
            onValueChange = {},
            label = { Text("Service Account JSON") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            maxLines = 10,
            shape = MaterialTheme.shapes.small,
            trailingIcon = {
                IconButton(onClick = onToggleShowSecret, modifier = Modifier.size(48.dp)) {
                    Text(
                        text = if (uiState.showSecret) "Hide" else "Show",
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                }
            }
        )

        if (uiState.errorMessage != null) {
            Text(
                text = uiState.errorMessage,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            Button(
                onClick = onSave,
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp),
                enabled = !uiState.isSaving
            ) {
                Text(if (uiState.isSaving) "Saving..." else "Save")
            }

            Button(
                onClick = onClear,
                modifier = Modifier.weight(1f),
                enabled = !uiState.isSaving && uiState.areSaved
            ) {
                Text("Clear")
            }
        }
    }
}
