package dev.maslov.sheetsync.ui.screens
import android.annotation.SuppressLint
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import dev.maslov.sheetsync.ui.components.MetaInfoPanel
import dev.maslov.sheetsync.ui.viewmodel.RuleViewModel
import java.util.UUID

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RuleEditScreen(
    ruleId: UUID,
    onBack: () -> Unit,
    viewModel: RuleViewModel = hiltViewModel()
) {
    val rules by viewModel.rules.collectAsState()

    val rule = rules.find { it.id == ruleId }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Rule Detail") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, null)
                    }
                }
            )
        }
    ) {
        if (rule != null) {
            MetaInfoPanel(rule)
        } else {
            Text("Rule not found")
        }
    }
}