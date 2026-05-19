package dev.maslov.sheetsync.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import dev.maslov.sheetsync.model.Sheet
import dev.maslov.sheetsync.model.SheetMetadata

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TabSelector(
    tabList: List<Sheet>,
    selectedTab: Sheet?,
    isLoading: Boolean,
    errorMessage: String?,
    onSelect: (Sheet) -> Unit,
    onRefresh: (SheetMetadata) -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
    validationError: String? = null
) {
    var sheetListExpanded by remember { mutableStateOf(enabled) }

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ExposedDropdownMenuBox(
            expanded = sheetListExpanded,
            onExpandedChange = { sheetListExpanded = !sheetListExpanded },
            modifier = Modifier.weight(1f)
        ) {
            OutlinedTextField(
                value = selectedTab?.properties?.title ?: "Select a tab",
                onValueChange = {},
                readOnly = true,
                label = { Text("Sheet") },
                trailingIcon = {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    } else {
                        ExposedDropdownMenuDefaults.TrailingIcon(sheetListExpanded)
                    }
                },
                modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
                enabled = enabled && !isLoading && tabList.isNotEmpty(),
                isError = isError,
                supportingText = {
                    if (validationError != null) {
                        Text(validationError)
                    }
                }
            )

            ExposedDropdownMenu(
                expanded = sheetListExpanded,
                onDismissRequest = { sheetListExpanded = false }
            ) {
                tabList.forEach { sheet ->
                    DropdownMenuItem(
                        text = { Text(sheet.properties.title) },
                        onClick = {
                            onSelect(sheet)
                            sheetListExpanded = false
                        }
                    )
                }
            }
        }

        IconButton(
            onClick = { onRefresh },
            enabled = enabled && !isLoading
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = "Refresh sheets"
            )
        }
    }

    // Error message
    if (errorMessage != null) {
        Text(
            text = errorMessage,
            color = Color.Red,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(start = 16.dp, top = 4.dp)
        )
    }
}
