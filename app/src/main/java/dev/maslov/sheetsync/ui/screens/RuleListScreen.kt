package dev.maslov.sheetsync.ui.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.maslov.sheetsync.ui.components.RuleCard
import dev.maslov.sheetsync.ui.components.TopBar
import dev.maslov.sheetsync.ui.viewmodel.AppListViewModel
import dev.maslov.sheetsync.ui.viewmodel.RuleViewModel
import java.util.UUID

@Composable
fun RuleListScreen(
    onOpenSettings: () -> Unit,
    onAddRule: () -> Unit,
    onSearch: () -> Unit,
    onEditRule: (UUID) -> Unit,
    viewModel: RuleViewModel = hiltViewModel(),
    appListViewModel: AppListViewModel = hiltViewModel()
) {
    val rules by viewModel.rules.collectAsState()
    val appList by appListViewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopBar(
                title = "SheetSync",
                onSearchClick = onSearch,
                onSettingsClick = onOpenSettings
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddRule) {
                Icon(Icons.Default.Add, "add rule")
            }
        }
    ) { padding ->

        LazyColumn(modifier = Modifier.padding(padding)) {
            items(rules, key = { it.id }) { rule ->

                val app = appList.find { it.packageName == rule.appId }
                RuleCard(
                    rule = rule,
                    app = app,
                    onToggle = { viewModel.toggleRule(rule) },
                    onDelete = { viewModel.deleteRule(rule) },
                    onEdit = { onEditRule(rule.id) }
                )
            }
        }
    }
}
