package dev.maslov.sheetsync.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.maslov.sheetsync.ui.screens.onboard.FinalPage
import dev.maslov.sheetsync.ui.screens.onboard.OauthClientSetupPage
import dev.maslov.sheetsync.ui.screens.onboard.PageIndicator
import dev.maslov.sheetsync.ui.viewmodel.ClientCredentialsViewModel
import dev.maslov.sheetsync.ui.viewmodel.OnboardingViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(
    onboardingViewModel: OnboardingViewModel,
    credentialsViewModel: ClientCredentialsViewModel,
    onFinish: () -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { 2 })
    val scope = rememberCoroutineScope()
    val uiState by onboardingViewModel.uiState.collectAsState()

    val isLastPage = pagerState.currentPage == 1

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f)
        ) { page ->

            when (page) {
                0 -> OauthClientSetupPage(
                    credentialsViewModel = credentialsViewModel,
                    onSetupCompleted = {
                        onboardingViewModel.onSetupCompleted()
                    },
                    onSetupReset = {
                        onboardingViewModel.onSetupReset()
                    }
                )

                1 -> FinalPage()
            }
        }

        PageIndicator(
            currentPage = pagerState.currentPage,
            pageCount = 2
        )

        Button(
            enabled = !isLastPage || uiState.setupCompleted,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            onClick = {
                scope.launch {
                    if (!isLastPage) {
                        pagerState.animateScrollToPage(
                            pagerState.currentPage + 1
                        )
                    } else if (uiState.setupCompleted) {
                        onFinish()
                    }
                }
            }
        ) {
            Text(
                if (isLastPage) "Finish" else "Next"
            )
        }
    }
}
