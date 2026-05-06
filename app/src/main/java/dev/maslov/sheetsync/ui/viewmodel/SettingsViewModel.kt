package dev.maslov.sheetsync.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.maslov.sheetsync.service.notification.NotificationPermissionHelper
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class SettingsViewModel
@Inject
constructor(@param:ApplicationContext private val context: Context) : ViewModel() {

    private val _isNotificationListenerEnabled = MutableStateFlow(
        NotificationPermissionHelper.isNotificationListenerEnabled(context)
    )
    val isNotificationListenerEnabled: StateFlow<Boolean> = _isNotificationListenerEnabled.asStateFlow()

    /**
     * Refresh the notification listener permission status
     */
    fun refreshNotificationPermissionStatus() {
        viewModelScope.launch {
            val status = NotificationPermissionHelper.isNotificationListenerEnabled(context)
            _isNotificationListenerEnabled.value = status
        }
    }

    /**
     * Get the intent to open notification access settings
     */
    fun getNotificationSettingsIntent() = NotificationPermissionHelper.getNotificationSettingsIntent()
}
