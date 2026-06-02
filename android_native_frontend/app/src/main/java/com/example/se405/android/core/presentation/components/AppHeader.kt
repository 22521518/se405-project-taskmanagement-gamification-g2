@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.se405.android.core.presentation.components

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.se405.android.core.presentation.theme.AppText

/**
 * Single standardized header for the whole app.
 *
 * Layout rules (see navigation refactor spec):
 *  - Main tab screens  -> [showBackButton] = false (NO back button).
 *  - Child/detail screens -> [showBackButton] = true (back button that pops the back stack).
 *
 * A leading [navigationIcon] slot is available for main screens that need a
 * non-back leading action (e.g. the Tasks screen settings shortcut). When
 * [showBackButton] is true the back arrow always takes precedence over
 * [navigationIcon].
 */
@Composable
fun AppHeader(
    title: String,
    modifier: Modifier = Modifier,
    showBackButton: Boolean = false,
    onBackClick: () -> Unit = {},
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
) {
    CenterAlignedTopAppBar(
        modifier = modifier,
        title = { Text(text = title, style = AppText.DisplayBold, color = Color.Black) },
        navigationIcon = {
            if (showBackButton) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = "Back"
                    )
                }
            } else {
                navigationIcon()
            }
        },
        actions = actions,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background
        ),
    )
}
