package com.example.se405.android_native_frontend

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.example.se405.android_native_frontend.core.navigations.MainNavHost
import com.example.se405.android_native_frontend.core.presentation.theme.Android_native_frontendTheme

    class MainActivity : ComponentActivity() {
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            enableEdgeToEdge()
            setContent {
                Android_native_frontendTheme {
                    Scaffold { padding ->
                        Box(
                            modifier = Modifier
                                .padding(padding)
                        ) {
                            val navController = rememberNavController()
                            MainNavHost(navController)
                        }
                    }
                }
            }
        }
    }