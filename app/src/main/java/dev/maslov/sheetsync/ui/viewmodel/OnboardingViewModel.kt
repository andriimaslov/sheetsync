package dev.maslov.sheetsync.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.maslov.sheetsync.AppPreferences
import dev.maslov.sheetsync.model.OnboardingUiState
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class OnboardingViewModel @Inject constructor(private val preferences: AppPreferences) : ViewModel() {

    val isFirstLaunch: StateFlow<Boolean?> =
        preferences.isFirstLaunchFlow
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                null
            )

    private val _uiState = MutableStateFlow(
        OnboardingUiState()
    )
    val uiState = _uiState.asStateFlow()

    fun onLoginSuccess() {
        _uiState.update {
            it.copy(loginCompleted = true)
        }
    }

    fun onLogoutSuccess() {
        _uiState.update {
            it.copy(loginCompleted = false)
        }
    }

    fun onSetupCompleted() {
        _uiState.update {
            it.copy(setupCompleted = true)
        }
    }

    fun onSetupReset() {
        _uiState.update {
            it.copy(setupCompleted = false)
        }
    }

    fun finishOnboarding() {
        viewModelScope.launch {
            preferences.completeOnboarding()
        }
    }
}
