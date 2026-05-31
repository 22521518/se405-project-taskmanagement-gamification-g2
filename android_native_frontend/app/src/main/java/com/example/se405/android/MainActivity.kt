package com.example.se405.android

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import com.example.se405.android.core.navigations.MainNavHost
import com.example.se405.android.core.presentation.popup.LocalPopupController
import com.example.se405.android.core.presentation.popup.PopupController
import com.example.se405.android.core.presentation.popup.PopupHost
import com.example.se405.android.core.presentation.theme.Android_Theme
import com.example.se405.android.features.users_management.domain.repository.UserRepository
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class MainActivity : FragmentActivity() {
    private val userRepository: UserRepository by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        enableEdgeToEdge()

        lifecycle.addObserver(LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> {
                    // Người dùng vào App -> Sáng đèn
                    lifecycleScope.launch {
                        userRepository.updateUserPresence(true)
                    }
                }

                Lifecycle.Event.ON_STOP -> {
                    // Người dùng ẩn App -> Tắt đèn
                    lifecycleScope.launch {
                        userRepository.updateUserPresence(false)
                    }
                }

                else -> {}
            }
        })

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
        Scaffold (contentWindowInsets = WindowInsets(0)){
            padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
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