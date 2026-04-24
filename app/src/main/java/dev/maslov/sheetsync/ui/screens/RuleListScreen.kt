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
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.maslov.sheetsync.model.Rule
import dev.maslov.sheetsync.service.DatabaseProvider
import dev.maslov.sheetsync.service.RuleRepository
import dev.maslov.sheetsync.ui.components.RuleCard
import dev.maslov.sheetsync.ui.components.TopBar
import dev.maslov.sheetsync.ui.viewmodel.RuleViewModel
import dev.maslov.sheetsync.ui.viewmodel.RuleViewModelFactory

@Composable
fun RuleListScreen(
    onOpenSettings: () -> Unit,
    onSearch: () -> Unit
) {

    val context = LocalContext.current

    val viewModel: RuleViewModel = viewModel(
        factory = RuleViewModelFactory(
            RuleRepository(
                DatabaseProvider.getDatabase(context).ruleDao()
            )
        )
    )

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
                    onToggle = { viewModel.toggleRule(rule) },
                    onDelete = { viewModel.deleteRule(rule) }
                )
            }
        }
    }
}