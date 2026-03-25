package com.example.se405.android_native_frontend.core.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.se405.android_native_frontend.features.tasks_management.presentation.components.LabelTextInput

/**
 * Shared row for field label + field content in task detail popup.
 */
@Composable
fun LabelRowContent(labelName: String, modifier: Modifier = Modifier.Companion, iconId: Int? = null, content: @Composable () -> Unit) {
    Row(
        modifier = Modifier.Companion.fillMaxWidth().then(modifier),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Companion.CenterVertically,
    ) {
        iconId?.let {
            Icon(
                painter = painterResource(id = it),
                contentDescription = labelName,
                tint = MaterialTheme.colorScheme.tertiary
            )
        }
        LabelTextInput(text = labelName)
        content()
    }
}

@Composable
fun LabelColumnContent(labelName: String, modifier: Modifier = Modifier.Companion, iconId: Int? = null, content: @Composable () -> Unit) {
    Column(
        modifier = Modifier.Companion.fillMaxWidth().then(modifier),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.Companion.Start,
    ) {
        LabelRowContent(labelName = labelName, iconId = iconId) {}
        content()
    }
}