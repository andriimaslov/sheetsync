package dev.maslov.sheetsync.ui.screens.onboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import dev.maslov.sheetsync.ui.viewmodel.AuthViewModel

@Composable
fun SignInWithGooglePage(
    authViewModel: AuthViewModel,
    onLoginSuccess: () -> Unit,
    onLogoutSuccess: () -> Unit
) {

    val authState by authViewModel.authState.collectAsState()


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Welcome to My App",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text("Some intro text here")

        Spacer(modifier = Modifier.height(32.dp))


        if (authState.isLoggedIn && authState.user != null) {
            onLoginSuccess()
            // User Info Card
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = MaterialTheme.shapes.medium
                    )
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Profile Picture
                    if (authState.user?.profilePicUrl != null) {
                        AsyncImage(
                            model = authState.user?.profilePicUrl,
                            contentDescription = "Profile Picture",
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = authState.user?.name?.firstOrNull()?.uppercase() ?: "?",
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 12.dp)
                    ) {
                        Text(
                            text = authState.user?.name ?: "Unknown",
                            style = MaterialTheme.typography.titleSmall
                        )
                        Text(
                            text = authState.user?.email ?: "unknown@example.com",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Logged in",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Button(
                    onClick = { authViewModel.logout()
                              onLogoutSuccess() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                ) {
                    Text("Logout")
                }
            }
        } else {
            Button(
                onClick = {
                    authViewModel.signInWithGoogle()

                }
            ) {
                Text("Login with Google")
            }
            // Not logged in message
//            Box(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .background(
//                        color = MaterialTheme.colorScheme.errorContainer,
//                        shape = MaterialTheme.shapes.medium
//                    )
//                    .padding(16.dp),
//                contentAlignment = Alignment.Center
//            ) {
//                Text(
//                    text = "Not logged in",
//                    style = MaterialTheme.typography.bodySmall,
//                    color = MaterialTheme.colorScheme.error
//                )
//            }
        }


    }
}
