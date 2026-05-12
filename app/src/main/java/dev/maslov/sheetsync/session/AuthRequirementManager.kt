package dev.maslov.sheetsync.session

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRequirementManager @Inject constructor() {

    private val _authRequired = MutableStateFlow(false)
    val authRequired: StateFlow<Boolean> = _authRequired.asStateFlow()

    fun requestTokenAuthentication() {
        _authRequired.value = true
    }

    fun resetAuthRequirement() {
        _authRequired.value = false
    }
}
