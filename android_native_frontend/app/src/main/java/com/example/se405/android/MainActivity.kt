package com.example.se405.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.example.se405.android.core.navigations.MainNavHost
import com.example.se405.android.core.presentation.popup.LocalPopupController
import com.example.se405.android.core.presentation.popup.PopupController
import com.example.se405.android.core.presentation.popup.PopupHost
import com.example.se405.android.core.presentation.theme.Android_Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Android_Theme {
                App()
            }
        }
    }
}

/**
 * Root composable of the application.
 *
 * Sets up the global [PopupController] and provides it to the entire
 * composable tree via [LocalPopupController]. Any composable in the tree
 * can access it with `LocalPopupController.current` to push/dismiss popups.
 *
 * [PopupHost] is placed **outside** the [Scaffold] so that popups render
 * above all navigation content and persist across navigation transitions.
 */
@Composable
fun App() {
    val popupController = remember { PopupController() }
    CompositionLocalProvider(LocalPopupController provides popupController) {
        Scaffold { padding ->
            Box(
                modifier = Modifier
                    .padding(padding)
            ) {
                val navController = rememberNavController()
                MainNavHost(navController)
            }
        }
        // Render popups above everything — survives navigation transitions
        PopupHost(controller = popupController)
    }
}