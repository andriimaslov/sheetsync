package dev.maslov.sheetsync.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.maslov.sheetsync.service.parser.NotificationParser

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParserSelector(
    parserList: List<NotificationParser>,
    selectedParser: NotificationParser?,
    onSelect: (NotificationParser) -> Unit,
    modifier: Modifier = Modifier
) {
    var parserListExpanded by remember { mutableStateOf(false) }

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ExposedDropdownMenuBox(
            expanded = parserListExpanded,
            onExpandedChange = { parserListExpanded = !parserListExpanded },
            modifier = Modifier.weight(1f)
        ) {
            OutlinedTextField(
                value = selectedParser?.name ?: "Select a parser",
                onValueChange = {},
                readOnly = true,
                label = { Text("Parser type") },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(parserListExpanded)
                },
                modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
                enabled = parserList.isNotEmpty()
            )

            ExposedDropdownMenu(
                expanded = parserListExpanded,
                onDismissRequest = { parserListExpanded = false }
            ) {
                parserList.forEach { parser ->
                    DropdownMenuItem(
                        text = { Text(parser.name) },
                        onClick = {
                            onSelect(parser)
                            parserListExpanded = false
                        }
                    )
                }
            }
        }
    }
}
