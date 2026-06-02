package com.example.se405.android.features.tasks_management.presentation.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.example.se405.android.core.presentation.theme.AppText

/**
 * Reusable label text used near form inputs in task-related popups.
 */
@Composable
fun LabelTextInput(text: String) {
    Text(text=text, style = AppText.Body2Bold, color = MaterialTheme.colorScheme.tertiary)
}