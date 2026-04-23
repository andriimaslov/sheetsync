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
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.maslov.sheetsync.model.Rule
import dev.maslov.sheetsync.ui.components.RuleCard
import dev.maslov.sheetsync.ui.components.TopBar
import dev.maslov.sheetsync.ui.viewmodel.RuleViewModel

@Composable
fun RuleListScreen(
    onOpenSettings: () -> Unit,
    onSearch: () -> Unit,
    viewModel: RuleViewModel = viewModel()
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
                        id = System.currentTimeMillis().toString(),
                        title = "New Rule",
                        description = "Demo rule",
                        isActive = true,
                        createdAt = "Now",
                        sheetId = "sheet_new",
                        lastRun = "Never"
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
                    onToggle = { viewModel.toggleRule(rule.id) },
                    onDelete = { viewModel.deleteRule(rule.id) }
                )
            }
        }
    }
}

val sampleRules = listOf(
    Rule(
        id = "a1b2c3",
        title = "Gmail: Important Emails",
        description = "Append emails to sheet",
        isActive = true,
        createdAt = "May 10, 2025",
        sheetId = "sheet_123",
        lastRun = "2m ago • Success"
    ),
    Rule(
        id = "d4e5f6",
        title = "Telegram: New Orders",
        description = "Append new orders to sheet",
        isActive = true,
        createdAt = "May 12, 2025",
        sheetId = "sheet_456",
        lastRun = "4m ago • Success"
    ),
    Rule(
        id = "g7h8i9",
        title = "Slack: Alerts",
        description = "Append alerts to sheet",
        isActive = true,
        createdAt = "May 14, 2025",
        sheetId = "sheet_789",
        lastRun = "10m ago • Success"
    ),
    Rule(
        id = "j1k2l3",
        title = "Gmail: Promotional",
        description = "Append promo emails to sheet",
        isActive = false,
        createdAt = "May 8, 2025",
        sheetId = "sheet_999",
        lastRun = "— (Paused)"
    )
)