package dev.maslov.sheetsync.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import dev.maslov.sheetsync.model.AppModel
import dev.maslov.sheetsync.model.Rule

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RuleEditForm(rule: Rule, onSave: (Rule) -> Unit, appList: List<AppModel>, modifier: Modifier = Modifier) {
    var title by remember { mutableStateOf(rule.title) }
    var description by remember { mutableStateOf(rule.description) }
    var sheetId by remember { mutableStateOf(rule.sheetId) }
    var isActive by remember { mutableStateOf(rule.isActive) }

    // sheet dropdown state
    var sheetListExpanded by remember { mutableStateOf(false) }
    val sheetOptions = listOf("sheet_123", "sheet_456", "sheet_789")
    // app dropdown state
    var appListExpanded by remember { mutableStateOf(false) }
    var selectedApp by remember { mutableStateOf(appList.find { it.packageName == rule.appId }) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Title") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Description") },
            modifier = Modifier.fillMaxWidth()
        )

        ExposedDropdownMenuBox(
            expanded = sheetListExpanded,
            onExpandedChange = { sheetListExpanded = !sheetListExpanded }
        ) {
            OutlinedTextField(
                value = sheetId,
                onValueChange = {},
                readOnly = true,
                label = { Text("Sheet") },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(sheetListExpanded)
                },
                modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable).fillMaxWidth()
            )

            ExposedDropdownMenu(
                expanded = sheetListExpanded,
                onDismissRequest = { sheetListExpanded = false }
            ) {
                sheetOptions.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            sheetId = option
                            sheetListExpanded = false
                        }
                    )
                }
            }
        }

        ExposedDropdownMenuBox(
            expanded = appListExpanded,
            onExpandedChange = { appListExpanded = !appListExpanded }
        ) {
            // The TextField that acts as the trigger
            OutlinedTextField(
                value = selectedApp?.name ?: "Select an app",
                onValueChange = {},
                readOnly = true,
                label = { Text("Apps with Notifications On") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = appListExpanded) },
                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable).fillMaxWidth()
            )

            // The actual menu that pops up
            ExposedDropdownMenu(
                expanded = appListExpanded,
                onDismissRequest = { appListExpanded = false }
            ) {
                appList.forEach { app ->
                    DropdownMenuItem(
                        text = {
                            Text(text = app.name, style = MaterialTheme.typography.bodyLarge)
                        },
                        onClick = {
                            selectedApp = app
                            appListExpanded = false
                            // Do something with the selected app package here
                        },
                        leadingIcon = {
                            // Using Coil to display the app icon
                            AsyncImage(
                                model = app.icon,
                                contentDescription = null,
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                    )
                }
            }
        }

        Text("Active")
        Switch(
            checked = isActive,
            onCheckedChange = { isActive = it }
        )

        HorizontalDivider()

        Text("Last Run Status: ${rule.lastRunStatus}")
        Text("Last Run At: ${rule.createdAt}")

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = {
                val updatedRule = rule.copy(
                    title = title,
                    description = description,
                    sheetId = sheetId,
                    isActive = isActive
                )
                onSave(updatedRule)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Save")
        }
    }
}
