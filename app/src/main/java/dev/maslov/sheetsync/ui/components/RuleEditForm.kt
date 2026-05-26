package dev.maslov.sheetsync.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import dev.maslov.sheetsync.model.AppModel
import dev.maslov.sheetsync.model.Rule
import dev.maslov.sheetsync.model.Sheet
import dev.maslov.sheetsync.model.uistate.SheetSelectorState
import dev.maslov.sheetsync.model.uistate.TabSelectorUiState
import dev.maslov.sheetsync.service.parser.NotificationParser
import dev.maslov.sheetsync.ui.validation.FormFieldErrors
import dev.maslov.sheetsync.ui.validation.ValidationHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RuleEditForm(
    rule: Rule,
    onSave: (Rule) -> Unit,
    appList: List<AppModel>,
    sheetSelectorState: SheetSelectorState,
    tabSelectorUiState: TabSelectorUiState,
    parserList: List<NotificationParser>,
    modifier: Modifier = Modifier
) {
    var title by remember { mutableStateOf(rule.title) }
    var selectedSheet by remember { mutableStateOf(sheetSelectorState.sheets.find { it.id == rule.sheetId }) }
    var selectedTab: Sheet? by remember {
        mutableStateOf(
            tabSelectorUiState.tabs.find {
                it.properties.title ==
                    rule.tabName
            }
        )
    }
    var isActive by remember { mutableStateOf(rule.isActive) }

    var appListExpanded by remember { mutableStateOf(false) }
    var selectedApp by remember { mutableStateOf(appList.find { it.packageName == rule.appId }) }
    var selectedParser by remember { mutableStateOf(parserList.find { it.name == rule.parser }) }

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

    LaunchedEffect(sheetSelectorState.sheets, rule.sheetId) {
        if (selectedSheet == null) {
            sheetSelectorState.sheets.find { it.id == rule.sheetId }?.let {
                selectedSheet = it
                sheetSelectorState.onSelect(it)
                if (errors.sheetError != null) {
                    errors = errors.copy(sheetError = null)
                }
            }
        }
    }

    LaunchedEffect(tabSelectorUiState.tabs, rule.tabName) {
        if (selectedTab == null) {
            tabSelectorUiState.tabs.find { it.properties.title == rule.tabName }?.let {
                selectedTab = it
                tabSelectorUiState.onSelect(it)
                if (errors.tabError != null) {
                    errors = errors.copy(tabError = null)
                }
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
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
            enabled = selectedSheet != null && tabSelectorUiState.tabs.isNotEmpty(),
            isError = errors.tabError != null,
            validationError = errors.tabError
        )

        ExposedDropdownMenuBox(
            expanded = appListExpanded,
            onExpandedChange = { appListExpanded = !appListExpanded }
        ) {
            OutlinedTextField(
                value = selectedApp?.name ?: "Select an app",
                onValueChange = {},
                readOnly = true,
                label = { Text("Apps with Notifications On") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = appListExpanded) },
                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                modifier = Modifier
                    .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                    .fillMaxWidth(),
                isError = errors.appError != null,
                supportingText = {
                    if (errors.appError != null) {
                        Text(errors.appError!!)
                    }
                }
            )

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

                            if (errors.appError != null) {
                                errors = errors.copy(appError = null)
                            }
                        },
                        leadingIcon = {
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
        Text("Last Run At: ${rule.lastRunAt?.toLocalDate()}")

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = {
                if (validate()) {
                    val updatedRule = rule.copy(
                        title = title,
                        sheetId = selectedSheet?.id ?: rule.sheetId,
                        sheetName = selectedSheet?.name ?: rule.sheetName,
                        tabName = selectedTab?.properties?.title ?: rule.tabName,
                        isActive = isActive
                    )
                    onSave(updatedRule)
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Save")
        }
    }
}
