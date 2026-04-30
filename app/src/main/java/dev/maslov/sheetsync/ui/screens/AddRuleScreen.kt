package dev.maslov.sheetsync.ui.screens

import android.annotation.SuppressLint
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.maslov.sheetsync.model.Rule
import dev.maslov.sheetsync.ui.components.RuleAddForm
import dev.maslov.sheetsync.ui.components.RuleEditForm
import dev.maslov.sheetsync.ui.viewmodel.AppListViewModel
import dev.maslov.sheetsync.ui.viewmodel.RuleViewModel
import java.time.LocalDateTime
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun AddRuleScreen(
    onBack: () -> Unit,
    viewModel: RuleViewModel = hiltViewModel(),
    appListViewModel: AppListViewModel = hiltViewModel()
) {
    val apps by appListViewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Add Rule") }, navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, null)
                }
            })
        }
    ) { padding ->

        Column(
            modifier =
            Modifier
                .padding(padding)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            RuleAddForm(
                onSave = { newRule ->
                    viewModel.addRule(newRule)
                    onBack()
                },
                appList = apps,
                modifier = Modifier.padding(padding)
            )
        }
    }
}
