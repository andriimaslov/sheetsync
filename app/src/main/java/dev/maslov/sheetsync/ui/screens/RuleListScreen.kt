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
import androidx.hilt.navigation.compose.hiltViewModel
import dev.maslov.sheetsync.model.Rule
import dev.maslov.sheetsync.ui.components.RuleCard
import dev.maslov.sheetsync.ui.components.TopBar
import dev.maslov.sheetsync.ui.viewmodel.RuleViewModel
import java.time.LocalDateTime
import java.util.UUID

@Composable
fun RuleListScreen(
    onOpenSettings: () -> Unit,
    onSearch: () -> Unit,
    viewModel: RuleViewModel = hiltViewModel()
) {

    val rules by viewModel.rules.collectAsState()

    Scaffold(
        topBar = {
            TopBar(
                title = "SheetSync",
                onSearchClick = onSearch,
                onSettingsClick = onOpenSettings
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                viewModel.addRule(
                    Rule(
                        id = UUID.randomUUID(),
                        title = "New Rule",
                        description = "Demo rule",
                        isActive = true,
                        createdAt = LocalDateTime.now(),
                        sheetId = "sheet_new",
                        lastRunStatus = "Failed",
                        lastRunAt = LocalDateTime.now()
                    )
                )
            }) {
                Icon(Icons.Default.Add, contentDescription = "Add")
            }
        }
    ) { padding ->

        LazyColumn(modifier = Modifier.padding(padding)) {
            items(rules, key = { it.id }) { rule ->
                RuleCard(
                    rule = rule,
                    onToggle = { viewModel.toggleRule(rule) },
                    onDelete = { viewModel.deleteRule(rule) }
                )
            }
        }
    }
}