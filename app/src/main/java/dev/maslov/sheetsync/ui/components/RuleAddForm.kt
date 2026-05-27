package dev.maslov.sheetsync.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
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
import dev.maslov.sheetsync.model.AppModel
import dev.maslov.sheetsync.model.Rule
import dev.maslov.sheetsync.model.Sheet
import dev.maslov.sheetsync.model.SheetMetadata
import dev.maslov.sheetsync.model.uistate.SheetSelectorState
import dev.maslov.sheetsync.model.uistate.TabSelectorUiState
import dev.maslov.sheetsync.service.parser.NotificationParser
import dev.maslov.sheetsync.ui.validation.FormFieldErrors
import dev.maslov.sheetsync.ui.validation.ValidationHelper
import java.time.LocalDateTime
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RuleAddForm(
    onSave: (Rule) -> Unit,
    appList: List<AppModel>,
    onRefreshAppList: () -> Unit,
    sheetSelectorState: SheetSelectorState,
    tabSelectorUiState: TabSelectorUiState,
    parserList: List<NotificationParser>,
    modifier: Modifier = Modifier
) {
    var title by remember { mutableStateOf("") }
    var selectedSheet by remember { mutableStateOf<SheetMetadata?>(null) }
    var selectedTab: Sheet? by remember { mutableStateOf(null) }
    var isActive by remember { mutableStateOf(false) }

    var selectedApp by remember { mutableStateOf<AppModel?>(null) }
    var selectedParser by remember { mutableStateOf<NotificationParser?>(null) }

    var errors by remember { mutableStateOf(FormFieldErrors()) }

    fun validate(): Boolean {
        val newErrors = FormFieldErrors(
            titleError = if (!ValidationHelper.isStringValid(title)) ValidationHelper.TITLE_REQUIRED else null,
            parserError = if (!ValidationHelper.isSelected(selectedParser)) ValidationHelper.PARSER_REQUIRED else null,
            sheetError = if (!ValidationHelper.isSelected(selectedSheet)) ValidationHelper.SHEET_REQUIRED else null,
            tabError = if (!ValidationHelper.isSelected(selectedTab)) ValidationHelper.TAB_REQUIRED else null,
            appError = if (!ValidationHelper.isSelected(selectedApp)) ValidationHelper.APP_REQUIRED else null
        )
        errors = newErrors
        return newErrors.isEmpty()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (sheetSelectorState.isLoading || tabSelectorUiState.isLoading) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }
        OutlinedTextField(
            value = title,
            onValueChange = {
                title = it

                if (errors.titleError != null && title.isNotBlank()) {
                    errors = errors.copy(titleError = null)
                }
            },
            label = { Text("Title") },
            modifier = Modifier.fillMaxWidth(),
            isError = errors.titleError != null,
            supportingText = {
                if (errors.titleError != null) {
                    Text(errors.titleError!!)
                }
            }
        )

        ParserSelector(
            parserList = parserList,
            selectedParser = selectedParser,
            onSelect = { parser ->
                selectedParser = parser

                if (errors.parserError != null) {
                    errors = errors.copy(parserError = null)
                }
            },
            isError = errors.parserError != null,
            errorMessage = errors.parserError
        )

        SheetSelector(
            sheetList = sheetSelectorState.sheets,
            selectedSheet = selectedSheet,
            isLoading = sheetSelectorState.isLoading,
            errorMessage = sheetSelectorState.error,
            onSelect = { sheet ->
                selectedSheet = sheet
                sheetSelectorState.onSelect(sheet)

                if (errors.sheetError != null) {
                    errors = errors.copy(sheetError = null)
                }
            },
            onRefresh = sheetSelectorState.onRefresh,
            isError = errors.sheetError != null,
            validationError = errors.sheetError
        )

        TabSelector(
            tabList = tabSelectorUiState.tabs,
            selectedTab = selectedTab,
            isLoading = tabSelectorUiState.isLoading,
            errorMessage = tabSelectorUiState.error,
            onSelect = { tab ->
                selectedTab = tab
                tabSelectorUiState.onSelect(tab)

                if (errors.tabError != null) {
                    errors = errors.copy(tabError = null)
                }
            },
            onRefresh = { selectedSheet?.let { tabSelectorUiState.onRefresh(it) } },
            enabled = selectedSheet != null,
            isError = errors.tabError != null,
            validationError = errors.tabError
        )

        AppSelector(
            appList = appList,
            selectedApp = selectedApp,
            onSelect = { app ->
                selectedApp = app
                if (errors.appError != null) {
                    errors = errors.copy(appError = null)
                }
            },
            onRefresh = onRefreshAppList,
            isError = errors.appError != null,
            errorMessage = errors.appError
        )

        Text("Active")
        Switch(
            checked = isActive,
            onCheckedChange = { isActive = it }
        )

        HorizontalDivider()

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = {
                if (validate()) {
                    val rule = Rule(
                        id = UUID.randomUUID(),
                        title = title,
                        isActive = isActive,
                        createdAt = LocalDateTime.now(),
                        sheetId = selectedSheet?.id ?: "Unknown",
                        sheetName = selectedSheet?.name ?: "Unknown",
                        tabName = selectedTab?.properties?.title ?: "Unknown",
                        lastRunStatus = "Init",
                        lastRunAt = null,
                        appId = selectedApp?.packageName ?: "Unknown",
                        parser = selectedParser?.name ?: "Unknown"
                    )
                    onSave(rule)
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Save")
        }
    }
}
