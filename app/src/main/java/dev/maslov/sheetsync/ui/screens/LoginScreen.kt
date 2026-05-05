package dev.maslov.sheetsync.ui.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import dev.maslov.sheetsync.ui.viewmodel.AuthViewModel

@Composable
fun LoginScreen(onLoginSuccess: () -> Unit, viewModel: AuthViewModel = hiltViewModel()) {
    val authState by viewModel.authState.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Sheet Sync",
                style = MaterialTheme.typography.displaySmall,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Text(
                text = "Sign in with your Google account to continue",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            if (authState.isLoading) {
                CircularProgressIndicator()
            } else {
                Button(
                    onClick = {
                        viewModel.signInWithGoogle()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text("Sign in with Google")
                }
            }

            if (authState.error != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = authState.error ?: "",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center
                )
            }

            if (authState.isLoggedIn) {
                Spacer(modifier = Modifier.height(16.dp))
                onLoginSuccess()
            }
        }
    }
}
