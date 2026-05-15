package dev.maslov.sheetsync.model.uistate

data class OnboardingUiState(
    val isFirstLaunch: Boolean = true,
    val loginCompleted: Boolean = false,
    val setupCompleted: Boolean = false
) {
    val canFinish: Boolean
        get() = loginCompleted && setupCompleted
}
