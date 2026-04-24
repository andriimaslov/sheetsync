package dev.maslov.sheetsync.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import dev.maslov.sheetsync.service.RuleRepository

class RuleViewModelFactory(
    private val repository: RuleRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return RuleViewModel(repository) as T
    }
}