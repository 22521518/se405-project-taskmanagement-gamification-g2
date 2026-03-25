package com.example.se405.android_native_frontend.features.tasks_management.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.se405.android_native_frontend.R
import com.example.se405.android_native_frontend.core.presentation.components.BuiltinLabelIcon
import com.example.se405.android_native_frontend.core.presentation.components.BuiltinLabels
import com.example.se405.android_native_frontend.core.presentation.components.ButtonCTAText
import com.example.se405.android_native_frontend.core.presentation.components.HabitLabel
import com.example.se405.android_native_frontend.core.presentation.components.LabelColumnContent
import com.example.se405.android_native_frontend.core.presentation.components.TextFieldApp
import com.example.se405.android_native_frontend.core.presentation.theme.Android_native_frontendTheme
import com.example.se405.android_native_frontend.core.presentation.theme.AppText

data class CreateTagUiState(
    var name: String,
    var color: Int,
    var label: HabitLabel,
)

/**
 * Contract-only composable for create-tag popup.
 *
 * This function defines the API surface used by Screen/ViewModel.
 * Rendering can be implemented later without changing call sites.
 */
@Composable
fun CreateTagPopUp(
    onCreate: (tag: CreateTagUiState) -> Unit,
    onCancel: () -> Unit,
) {
    var tagUiState by remember { mutableStateOf(CreateTagUiState("", 0, BuiltinLabels[0])) }

//    This popup need to be refactored for using same logic for the size
    BoxWithConstraints {
        val targetWidth = maxWidth * 0.80f
        val targetMaxHeight = maxHeight * 0.90f
        Column(
            modifier = Modifier
                .widthIn(min = 300.dp, max = targetWidth)
                .heightIn(max = targetMaxHeight)
                .background(
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(16.dp),
                )
                .padding(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            )
            {
                Row(
                    modifier = Modifier.clickable(onClick = onCancel),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = "Create New Tags",
                        style = AppText.HeadBold,
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                }

                ButtonCTAText(
                    onClick = onCancel,
                    contentPadding = PaddingValues(2.dp),
                ) {
                    Icon(
                        painter = painterResource(R.drawable.icon_cross_thin),
                        contentDescription = "Close",
                        tint = Color.Black,
                        modifier = Modifier.size(16.dp),
                    )
                }
            }

            TextFieldApp(
                labelTitle = "Tag name: ",
                placeholder = {
                    Text("Enter your tag name",
                        style = AppText.Body2Regular.copy(fontStyle = FontStyle.Italic,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)))
                },
                value = tagUiState.name,
                onValueChange = { tagUiState.name = it}, maxTextLen = 20)

            LabelColumnContent("Select a label:", modifier = Modifier.padding(horizontal = 20.dp)) {
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    maxItemsInEachRow = 6
                ) {
                    BuiltinLabels.forEach { label ->
                        LabelSelectItem(label) {
                            tagUiState.label = label
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LabelSelectItem(label: HabitLabel, isSelected: Boolean = false, onClick: () -> Unit) {
    if (isSelected)
        BuiltinLabelIcon(label, Modifier.size(32.dp).border(width = 1.dp, color = MaterialTheme.colorScheme.onPrimaryContainer), tint = MaterialTheme.colorScheme.onPrimaryContainer)
    else
        BuiltinLabelIcon(label, Modifier.size(32.dp).clickable(onClick = onClick))
}

@Preview(showBackground = true, backgroundColor = 0xFFECF7FB)
@Composable
fun CreateTagPopUpPreview() {

    Android_native_frontendTheme {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            CreateTagPopUp({}, {})
        }
    }
}

