package dev.maslov.sheetsync.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.maslov.sheetsync.model.Rule

@Composable
fun MetaInfoPanel(rule: Rule) {
    Column(modifier = Modifier.padding(top = 12.dp)) {

        Text("Rule ID: ${rule.id}")
        Text("Created: ${rule.createdAt}")
        Text("Sheet ID: ${rule.sheetId}")
        Text("Last Append: ${rule.lastRun}")
    }
}