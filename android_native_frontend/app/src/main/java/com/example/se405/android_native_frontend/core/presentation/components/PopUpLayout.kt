package com.example.se405.android_native_frontend.core.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Common layout for PopUps with responsive sizing.
 *
 * @param modifier Additional modifier to be applied AFTER the default styling.
 *                 Uses `default.then(modifier)` pattern as requested.
 * @param widthRatio Width as a fraction of screen width (0.0f - 1.0f).
 * @param heightRatio Optional max height as a fraction of screen height (0.0f - 1.0f).
 * @param verticalArrangement Spacing between elements in the Column.
 * @param horizontalAlignment Horizontal alignment of elements in the Column.
 * @param content The content to be displayed within the Column.
 */
@Composable
fun PopUpLayout(
    modifier: Modifier = Modifier,
    widthRatio: Float = 0.8f,
    heightRatio: Float? = null,
    verticalArrangement: Arrangement.Vertical = Arrangement.spacedBy(12.dp),
    horizontalAlignment: Alignment.Horizontal = Alignment.CenterHorizontally,
    content: @Composable ColumnScope.() -> Unit
) {
    BoxWithConstraints {
        val targetWidth = maxWidth * widthRatio
        val targetMaxHeight = heightRatio?.let { maxHeight * it }

        val defaultModifier = Modifier
            .widthIn(min = 300.dp, max = targetWidth)
            .let { 
                if (targetMaxHeight != null) it.heightIn(max = targetMaxHeight) else it 
            }
            .background(
                color = MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(16.dp),
            )
            .padding(vertical = 16.dp)

        Column(
            modifier = defaultModifier.then(modifier),
            verticalArrangement = verticalArrangement,
            horizontalAlignment = horizontalAlignment,
            content = content
        )
    }
}
