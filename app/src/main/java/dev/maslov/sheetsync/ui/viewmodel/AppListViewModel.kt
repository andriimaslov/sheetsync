package dev.maslov.sheetsync.ui.viewmodel

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.maslov.sheetsync.model.AppModel
import dev.maslov.sheetsync.service.fetchAppsWithNotifications
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@HiltViewModel
class AppListViewModel @Inject constructor(@ApplicationContext private val context: Context) : ViewModel() {

    private val _uiState = MutableStateFlow<List<AppModel>>(emptyList())
    val uiState: StateFlow<List<AppModel>> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            _uiState.value = fetchAppsWithNotifications(context)
        }
    }
}
