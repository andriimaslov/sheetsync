package dev.maslov.sheetsync.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.maslov.sheetsync.model.Rule
import dev.maslov.sheetsync.service.RuleRepository
import jakarta.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
@HiltViewModel
class RuleViewModel @Inject constructor(
    private val repository: RuleRepository
) : ViewModel() {

    val rules: StateFlow<List<Rule>> =
        repository.rules
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

    fun toggleRule(rule: Rule) {
        viewModelScope.launch {
            repository.updateRule(
                rule.copy(isActive = !rule.isActive)
            )
        }
    }

    fun addRule(rule: Rule) {
        viewModelScope.launch {
            repository.addRule(rule)
        }
    }

    fun deleteRule(rule: Rule) {
        viewModelScope.launch {
            repository.deleteRule(rule)
        }
    }
}