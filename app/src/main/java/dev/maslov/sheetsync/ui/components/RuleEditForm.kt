package dev.maslov.sheetsync.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
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
import dev.maslov.sheetsync.model.Rule

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RuleEditForm(rule: Rule, onSave: (Rule) -> Unit, modifier: Modifier = Modifier) {
    var title by remember { mutableStateOf(rule.title) }
    var description by remember { mutableStateOf(rule.description) }
    var sheetId by remember { mutableStateOf(rule.sheetId) }
    var isActive by remember { mutableStateOf(rule.isActive) }

    // Dropdown state
    var expanded by remember { mutableStateOf(false) }

    val sheetOptions = listOf("sheet_123", "sheet_456", "sheet_789")

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
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = sheetId,
                onValueChange = {},
                readOnly = true,
                label = { Text("Sheet") },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded)
                },
                modifier = Modifier.menuAnchor().fillMaxWidth()
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                sheetOptions.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            sheetId = option
                            expanded = false
                        }
                    )
                }
            }
        }
        Text("Active")
        Switch(
            checked = isActive,
            onCheckedChange = { isActive = it }
        )

        Divider()

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
