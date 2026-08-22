package com.homebax.axionis

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.homebax.axionis.data.ModelManager
import com.homebax.axionis.data.SettingsRepository
import com.homebax.axionis.ui.chat.ChatScreen
import com.homebax.axionis.ui.chat.SpeechScreen
import com.homebax.axionis.ui.navigation.AxionisRoute
import com.homebax.axionis.ui.settings.SettingsScreen
import com.homebax.axionis.ui.setup.SetupScreen
import com.homebax.axionis.ui.theme.AxionisAITheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        setContent {
            AxionisAITheme {
                ImmersiveModeHandler()
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AxionisApp()
                }
            }
        }
    }
}

@Composable
fun ImmersiveModeHandler() {
    val view = LocalView.current
    if (!view.isInEditMode) {
        DisposableEffect(view) {
            val window = (view.context as ComponentActivity).window
            val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
            
            windowInsetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
            
            onDispose {
                // We want it to be always immersive, but if we wanted to show them again:
                // windowInsetsController.show(WindowInsetsCompat.Type.systemBars())
            }
        }
    }
}

@Composable
fun AxionisApp() {
    val context = LocalContext.current
    val repository = remember { SettingsRepository(context) }
    val modelManager = remember { ModelManager(context) }
    
    val setupCompleted by repository.setupCompleted.collectAsState(initial = null)
    
    // We only decide initial route once we know if setup is completed
    if (setupCompleted == null) return

    val initialRoute = if (setupCompleted == true && modelManager.getDownloadedModels().isNotEmpty()) {
        AxionisRoute.Chat
    } else {
        AxionisRoute.Setup
    }

    val backStack = rememberNavBackStack(initialRoute)
    
    NavDisplay(
        backStack = backStack
    ) { key ->
        when (key) {
            is AxionisRoute.Setup -> NavEntry(key) {
                SetupScreen(
                    onComplete = {
                        backStack.clear()
                        backStack.add(AxionisRoute.Chat)
                    }
                )
            }
            is AxionisRoute.Chat -> NavEntry(key) {
                ChatScreen(
                    onSettingsClick = {
                        backStack.add(AxionisRoute.Settings)
                    },
                    onMicClick = {
                        backStack.add(AxionisRoute.Speech)
                    }
                )
            }
            is AxionisRoute.Settings -> NavEntry(key) {
                SettingsScreen(onBack = { backStack.removeLastOrNull() })
            }
            is AxionisRoute.Speech -> NavEntry(key) {
                SpeechScreen(onBackToChat = { backStack.removeLastOrNull() })
            }
            else -> NavEntry(key) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {}
            }
        }
    }
}
