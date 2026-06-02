@file:OptIn(ExperimentalUuidApi::class)

package com.example.se405.android.features.tasks_management.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.se405.android.core.presentation.components.BuiltinLabelIcon
import com.example.se405.android.core.presentation.components.BuiltinLabels
import com.example.se405.android.core.presentation.components.ButtonApp
import com.example.se405.android.core.presentation.components.ButtonType
import com.example.se405.android.core.presentation.components.HabitLabel
import com.example.se405.android.core.presentation.components.PopUpLayout
import com.example.se405.android.core.presentation.theme.Android_Theme
import com.example.se405.android.core.presentation.theme.AppText
import com.example.se405.android.core.presentation.theme.Blue40
import com.example.se405.android.core.presentation.theme.Blue60
import com.example.se405.android.core.presentation.theme.Blue90
import com.example.se405.android.core.presentation.theme.BlueGrey70
import com.example.se405.android.core.presentation.theme.BlueGrey80
import com.example.se405.android.core.presentation.theme.Gold40
import com.example.se405.android.core.presentation.theme.Green40
import com.example.se405.android.core.presentation.theme.Orange40
import com.example.se405.android.core.presentation.theme.White
import com.example.se405.android.features.tasks_management.domain.entity.Tag
import kotlin.uuid.ExperimentalUuidApi

data class CreateTagUiState(
    var name: String,
    var color: Color,
    var label: HabitLabel,
)

/**
 * Curated swatch palette, mirroring the design-system palette used by
 * `CreateTagBottomSheet` so tag creation feels consistent across entry points.
 */
private val tagColorOptions = listOf(
    Blue40, Blue60, Color(0xFF0EA5E9), Color(0xFF06B6D4),
    Color(0xFF10B981), Green40, Gold40, Orange40,
    Color(0xFFEC4899), Color(0xFF8B5CF6),
)

/**
 * Create / edit a tag.
 *
 * Restyled to share the design language of `CreateTagBottomSheet`
 * (DisplayBold header, BodyBold field labels, `OutlinedTextField`, swatch color
 * picker, label chip row, weighted action buttons) while keeping the popup's
 * public contract intact.
 */
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
            CreateTagUiState("", tagColorOptions.first(), BuiltinLabels[0])
        } else {
            CreateTagUiState(initialTag.name, Color(initialTag.color), initialTag.label)
        }
        mutableStateOf(initialState)
    }
    var isNameError by remember(initialTag) { mutableStateOf(false) }

    PopUpLayout {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = if (isEdit) "Edit tag" else "Create new tag",
                    style = AppText.DisplayBold,
                )
                Text(
                    text = "Group related tasks with a colored label.",
                    style = AppText.Body2Regular,
                )
            }

            // Name
            Column {
                Text(text = "Tag name*", style = AppText.BodyBold)
                OutlinedTextField(
                    value = tagUiState.name,
                    onValueChange = {
                        if (it.length <= 20) {
                            tagUiState = tagUiState.copy(name = it)
                            isNameError = false
                        }
                    },
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    placeholder = { Text("e.g. Backend, UI, Bug") },
                    isError = isNameError,
                    supportingText = {
                        if (isNameError) {
                            Text(
                                text = "Tag name cannot be blank",
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    },
                    singleLine = true,
                )
            }

            // Color picker
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(text = "Color", style = AppText.BodyBold)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(tagColorOptions) { color ->
                        val isSelected = color == tagUiState.color
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(color)
                                .clickable { tagUiState = tagUiState.copy(color = color) }
                                .then(
                                    if (isSelected) Modifier.border(2.dp, White, CircleShape)
                                    else Modifier
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Label
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(text = "Label", style = AppText.BodyBold)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(BuiltinLabels) { label ->
                        val isSelected = tagUiState.label == label
                        LabelChip(
                            isSelected = isSelected,
                            onClick = { tagUiState = tagUiState.copy(label = label) },
                        ) {
                            BuiltinLabelIcon(
                                label = label,
                                modifier = Modifier.size(16.dp),
                                tint = if (isSelected) Blue40 else BlueGrey70,
                            )
                            Text(
                                text = label.name,
                                style = AppText.Body2Regular,
                                color = if (isSelected) Blue40 else MaterialTheme.colorScheme.onSurface,
                            )
                        }
                    }
                }
            }

            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ButtonApp(
                    onClick = { if (!isLoading) onCancel() },
                    modifier = Modifier.weight(1f),
                    type = ButtonType.OUTLINED,
                ) { Text("Cancel") }

                ButtonApp(
                    onClick = {
                        if (isLoading) return@ButtonApp
                        if (tagUiState.name.trim().isBlank()) {
                            isNameError = true
                        } else {
                            val sanitized = tagUiState.copy(name = tagUiState.name.trim())
                            if (isEdit) onUpdate!!(initialTag!!, sanitized) else onCreate(sanitized)
                        }
                    },
                    modifier = Modifier.weight(1f),
                    type = ButtonType.FILLED,
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(if (isEdit) "Save tag" else "Create tag")
                    }
                }
            }
        }
    }
}

/**
 * Selectable label chip, matching the chip styling used by `CreateTagBottomSheet`.
 */
@Composable
private fun LabelChip(
    isSelected: Boolean,
    onClick: () -> Unit,
    content: @Composable () -> Unit,
) {
    androidx.compose.material3.Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        color = if (isSelected) Blue90 else MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, if (isSelected) Blue40 else BlueGrey80),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) { content() }
    }
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
