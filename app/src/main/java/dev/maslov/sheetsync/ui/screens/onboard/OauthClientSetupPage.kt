package dev.maslov.sheetsync.ui.screens.onboard
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import dev.maslov.sheetsync.ui.components.ClientCredentialsForm
import dev.maslov.sheetsync.ui.viewmodel.ClientCredentialsViewModel

@Composable
fun OauthClientSetupPage(
    credentialsViewModel: ClientCredentialsViewModel = hiltViewModel(),
    onSetupCompleted: () -> Unit,
    onSetupReset: () -> Unit
) {
    val credentialsUiState by credentialsViewModel.uiState.collectAsState()

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri ->
            uri?.let { credentialsViewModel.loadServiceAccountFile(it) }
        }
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            "Setup your preferences",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(24.dp))

        ClientCredentialsForm(
            uiState = credentialsUiState,
            onSelectServiceAccountFile = {
                filePickerLauncher.launch(arrayOf("application/json", "text/plain", "*/*"))
            },
            onToggleShowSecret = { credentialsViewModel.toggleShowSecret() },
            onSave = {
                credentialsViewModel.saveCredentials()
                onSetupCompleted()
            },
            onClear = {
                credentialsViewModel.clearCredentials()
                onSetupReset()
            },
            modifier = Modifier.fillMaxWidth()
        )
    }
}
