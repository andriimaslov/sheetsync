package dev.maslov.sheetsync.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import java.util.UUID

@Composable
fun RuleCard(rule: Rule, app: AppModel?, onToggle: () -> Unit, onDelete: () -> Unit, onEdit: (UUID) -> Unit) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier =
        Modifier
            .padding(8.dp)
            .fillMaxWidth()
            .clickable { expanded = !expanded }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (app != null) {
                    AsyncImage(
                        model = app.icon,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp)
                    )
                }
                Column {
                    Text(rule.title)
                    Text(
                        text =
                        if (rule.isActive) {
                            rule.description
                        } else {
                            "${rule.description} (Paused)"
                        }
                    )
                }

                Row {
                    Switch(
                        checked = rule.isActive,
                        onCheckedChange = { onToggle() }
                    )

                    IconButton(onClick = { onEdit(rule.id) }) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit"
                        )
                    }

                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete rule")
                    }
                }
            }

            if (expanded) {
                RuleMetaInfoPanel(rule)
            }
        }
    }
}
