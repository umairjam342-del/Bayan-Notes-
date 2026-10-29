package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.example.bayan.data.auth.AuthenticationService
import com.example.bayan.data.repository.BayanRepository
import com.example.bayan.ui.MainAppContainer
import com.example.bayan.ui.auth.AuthScreen
import com.example.ui.theme.MyApplicationTheme
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                BayanAppContent()
            }
        }
    }
}

@Composable
fun BayanAppContent() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val authService = remember { AuthenticationService() }
    val currentUser: FirebaseUser? by authService.authState.collectAsState(initial = authService.currentUser)
    var isGuestSession by rememberSaveable { mutableStateOf(false) }

    Surface(modifier = Modifier.fillMaxSize()) {
        if (currentUser == null && !isGuestSession) {
            // Render interactive startup login UI with Google Sign-In and Continue as Guest buttons
            AuthScreen(
                authService = authService,
                onAuthSuccess = {
                    isGuestSession = false
                },
                onSignInAsGuest = {
                    isGuestSession = true
                }
            )
        } else {
            // User session (Authenticated Google User or Guest Session)
            val isGuest = isGuestSession && currentUser == null
            val repository = remember(currentUser?.uid, isGuest) {
                BayanRepository(context)
            }
            MainAppContainer(
                repository = repository,
                isGuestMode = isGuest,
                onSignOut = {
                    isGuestSession = false
                    coroutineScope.launch {
                        authService.signOut(context)
                    }
                }
            )
        }
    }
}
