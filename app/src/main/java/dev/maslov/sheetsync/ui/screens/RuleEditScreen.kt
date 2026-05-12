package dev.maslov.sheetsync.ui.screens
import android.annotation.SuppressLint
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.maslov.sheetsync.model.DriveUiState
import dev.maslov.sheetsync.ui.components.RuleEditForm
import dev.maslov.sheetsync.ui.viewmodel.AppListViewModel
import dev.maslov.sheetsync.ui.viewmodel.AuthViewModel
import dev.maslov.sheetsync.ui.viewmodel.RuleViewModel
import dev.maslov.sheetsync.ui.viewmodel.SheetsViewModel
import java.util.UUID

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RuleEditScreen(
    ruleId: UUID,
    onBack: () -> Unit,
    viewModel: RuleViewModel = hiltViewModel(),
    appListViewModel: AppListViewModel = hiltViewModel(),
    sheetsViewModel: SheetsViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel(),
) {
    val rules by viewModel.rules.collectAsState()
    val apps by appListViewModel.uiState.collectAsStateWithLifecycle()
    val driveUiState by sheetsViewModel.driveUiState.collectAsState()
    val rule = rules.find { it.id == ruleId }

    // Extract sheet data from driveUiState
    val availableSheets = when (val state = driveUiState) {
        is DriveUiState.Success -> state.sheets
        else -> emptyList()
    }
    val isLoadingSheets = driveUiState is DriveUiState.Loading
    val loadingSheetsError = when (val state = driveUiState) {
        is DriveUiState.Error -> state.message
        else -> null
    }

    val oauthLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        authViewModel.onSheetsResolutionResult(result.data)
    }

    // Refresh sheets on mount
    LaunchedEffect(Unit) {
        sheetsViewModel.refreshSheetList()
        authViewModel.resolutionTrigger.collect { intentSender ->
            oauthLauncher.launch(
                IntentSenderRequest.Builder(intentSender).build()
            )
        }

    }


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Rule Detail") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                }
            )
        }
    ) { padding ->
        if (rule != null) {
            RuleEditForm(
                rule = rule,
                onSave = { updatedRule ->
                    viewModel.editRule(updatedRule)
                    onBack()
                },
                appList = apps,
                availableSheets = availableSheets,
                isLoadingSheets = isLoadingSheets,
                loadingSheetsError = loadingSheetsError,
                onSelectSheet = { /* No-op for edit form */ },
                onRefreshSheets = { sheetsViewModel.refreshSheetList() },
                modifier = Modifier.padding(padding)
            )
        } else {
            Text("ups no rule??")
        }
    }
}
