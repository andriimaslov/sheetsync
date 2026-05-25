package dev.maslov.sheetsync.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.maslov.sheetsync.model.Rule
import java.time.format.DateTimeFormatter

@Composable
fun RuleMetaInfoPanel(rule: Rule) {
    val formatter = DateTimeFormatter.ofPattern("HH:mm dd-MM-yyyy ")
    Column(modifier = Modifier.padding(top = 12.dp)) {
        Text("Sheet name: ${rule.sheetName}")
        Text("Tab name: ${rule.tabName}")
        Text("Last Append Status: ${rule.lastRunStatus}")
        Text("Last Append At: ${rule.lastRunAt?.format(formatter)}")
    }
}
