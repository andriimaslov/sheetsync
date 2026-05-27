package dev.maslov.sheetsync.ui.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.maslov.sheetsync.model.Rule
import dev.maslov.sheetsync.ui.components.RuleCard
import dev.maslov.sheetsync.ui.components.TopBar
import dev.maslov.sheetsync.ui.viewmodel.AppListViewModel
import dev.maslov.sheetsync.ui.viewmodel.RuleViewModel
import java.util.UUID

@Composable
fun RuleListScreen(
    onOpenSettings: () -> Unit,
    onAddRule: () -> Unit,
    onEditRule: (UUID) -> Unit,
    ruleViewModel: RuleViewModel,
    appListViewModel: AppListViewModel
) {
    val rules by ruleViewModel.rules.collectAsState()
    val appList by appListViewModel.uiState.collectAsStateWithLifecycle()

    var ruleToDelete by remember { mutableStateOf<Rule?>(null) }

    ruleToDelete?.let { rule ->
        AlertDialog(
            onDismissRequest = { ruleToDelete = null },
            title = { Text(text = "Delete Rule") },
            text = { Text(text = "Are you sure you want to delete this rule?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        ruleViewModel.deleteRule(rule)
                        ruleToDelete = null
                    }
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { ruleToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopBar(
                title = "SheetSync",
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
                    onToggle = { ruleViewModel.toggleRule(rule) },
                    onDelete = { ruleToDelete = rule },
                    onEdit = { onEditRule(rule.id) }
                )
            }
        }
    }
}
