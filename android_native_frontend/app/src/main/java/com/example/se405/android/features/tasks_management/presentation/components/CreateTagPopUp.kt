package com.example.se405.android.features.tasks_management.presentation.components

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.se405.android.core.presentation.components.BuiltinLabelIcon
import com.example.se405.android.core.presentation.components.BuiltinLabels
import com.example.se405.android.core.presentation.components.ButtonApp
import com.example.se405.android.core.presentation.components.ButtonType
import com.example.se405.android.core.presentation.components.HabitLabel
import com.example.se405.android.core.presentation.components.LabelColumnContent
import com.example.se405.android.core.presentation.components.TextFieldApp
import com.example.se405.android.core.presentation.theme.Android_Theme
import com.example.se405.android.core.presentation.theme.AppText
import com.example.se405.android.core.presentation.components.PopUpLayout
import com.example.se405.android.core.presentation.components.ColorHueSlider
import com.example.se405.android.features.tasks_management.domain.entity.Tag
import kotlin.uuid.ExperimentalUuidApi

data class CreateTagUiState(
    var name: String,
    var color: Color,
    var label: HabitLabel,
)

@OptIn(ExperimentalUuidApi::class)
@Composable
fun CreateTagPopUp(
    onCreate: (tag: CreateTagUiState) -> Unit,
    onCancel: () -> Unit,
    initialTag: Tag? = null,
    onUpdate: ((Tag, CreateTagUiState) -> Unit)? = null,
    isLoading: Boolean = false,
) {
    val isEdit = initialTag != null && onUpdate != null
    var tagUiState by remember(initialTag) {
        val initialState = if (initialTag == null) {
            CreateTagUiState("", Color.Red, BuiltinLabels[0])
        } else {
            CreateTagUiState(initialTag.name, Color(initialTag.color), initialTag.label)
        }
        mutableStateOf(initialState)
    }

    PopUpLayout(
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                modifier = Modifier.clickable(onClick = onCancel),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = if (isEdit) "Edit Tag" else "Create Tag",
                    style = AppText.HeadBold,
                )
            }
        }

        TextFieldApp(
            labelTitle = "Tag Labels: ",
            placeholder = {
                Text(
                    "Enter your tag name",
                    style = AppText.Body2Regular.copy(
                        fontStyle = FontStyle.Italic,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                )
            },
            value = tagUiState.name,
            onValueChange = { tagUiState = tagUiState.copy(name = it) },
            maxTextLen = 20
        )

        LabelColumnContent("Select a label:", modifier = Modifier.padding(horizontal = 20.dp)) {
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                maxItemsInEachRow = 6
            ) {
                BuiltinLabels.forEach { label ->
                    LabelSelectItem(
                        label = label,
                        color = tagUiState.color,
                        isSelected = tagUiState.label == label
                    ) {
                        tagUiState = tagUiState.copy(label = label)
                    }
                }
            }

            ColorHueSlider(
                onColorSelected = { newColor -> tagUiState = tagUiState.copy(color = newColor) },
                initialColor = tagUiState.color
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ButtonApp(
                onClick = { if (!isLoading) onCancel() },
                type = ButtonType.OUTLINED
            ) {
                Text("Cancel")
            }
            ButtonApp(
                onClick = {
                    if (!isLoading) {
                        if (isEdit) {
                            onUpdate(initialTag, tagUiState)
                        } else {
                            onCreate(tagUiState)
                        }
                    }
                },
                type = ButtonType.FILLED
            ) {
                if (isLoading) {
                    androidx.compose.material3.CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = MaterialTheme.colorScheme.primary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Confirm")
                }
            }
        }
    }
}

@Composable
private fun LabelSelectItem(label: HabitLabel, color: Color = Color.Unspecified, isSelected: Boolean = false, onClick: () -> Unit) {
    if (isSelected)
        BuiltinLabelIcon(
            label = label,
            modifier = Modifier.size(32.dp).border(width = 1.dp, color = color),
            tint = color)
    else
        BuiltinLabelIcon(
            label = label,
            modifier = Modifier.size(32.dp).clickable(onClick = onClick),
            tint = color
        )
}

@Preview(showBackground = true, backgroundColor = 0xFFECF7FB)
@Composable
fun CreateTagPopUpPreview() {

    Android_Theme {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            CreateTagPopUp({}, {})
        }
    }
}