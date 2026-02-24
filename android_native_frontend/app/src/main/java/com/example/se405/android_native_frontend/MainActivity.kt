package com.example.se405.android_native_frontend

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import com.example.se405.android_native_frontend.core.navigations.MainNavHost
import com.example.se405.android_native_frontend.core.presentation.theme.Android_native_frontendTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Android_native_frontendTheme {
                val navController = rememberNavController()
                MainNavHost(navController)
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun ScreenAPreview() {
    Android_native_frontendTheme {
        






    }
}