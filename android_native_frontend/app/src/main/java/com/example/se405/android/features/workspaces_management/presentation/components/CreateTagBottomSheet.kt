package com.example.se405.android.features.workspaces_management.presentation.components

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.tooling.preview.Preview
import com.example.se405.android.core.presentation.components.BuiltinLabelIcon
import com.example.se405.android.core.presentation.components.BuiltinLabels
import com.example.se405.android.core.presentation.components.ButtonApp
import com.example.se405.android.core.presentation.components.ButtonType
import com.example.se405.android.core.presentation.components.HabitLabel
import com.example.se405.android.core.presentation.popup.LocalPopupController
import com.example.se405.android.core.presentation.popup.PopupController
import com.example.se405.android.core.presentation.theme.*
import kotlin.uuid.ExperimentalUuidApi

private val tagColorOptions = listOf(
    Blue40, Blue60, Color(0xFF0EA5E9), Color(0xFF06B6D4),
    Color(0xFF10B981), Green40, Gold40, Orange40,
    Color(0xFFEC4899), Color(0xFF8B5CF6),
)

@ExperimentalUuidApi
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateTagBottomSheet(
    state: CreateTagSheetState,
    onStateChange: (CreateTagSheetState) -> Unit,
    onConfirm: (name: String, color: Int, label: HabitLabel) -> Unit,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        when (state) {
            is CreateTagSheetState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(48.dp),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator() }
            }

            is CreateTagSheetState.Editing -> {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .padding(bottom = 32.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(text = "Create new tag", style = AppText.DisplayBold)

                    // Name
                    Column {
                        Text(text = "Tag name*", style = AppText.BodyBold)
                        OutlinedTextField(
                            value = state.name,
                            onValueChange = {
                                onStateChange(state.copy(name = it, isNameError = false))
                            },
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                            placeholder = { Text("e.g. Backend, UI, Bug") },
                            isError = state.isNameError,
                            supportingText = {
                                if (state.isNameError) {
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
                                val isSelected = color == state.color
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(color)
                                        .clickable { onStateChange(state.copy(color = color)) }
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

                    // Label (optional)
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(text = "Label (optional)", style = AppText.BodyBold)
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(BuiltinLabels) { label ->
                                val isSelected = label == state.selectedLabel
                                Surface(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(20.dp))
                                        .clickable {
                                            onStateChange(
                                                state.copy(
                                                    selectedLabel = if (isSelected) null else label
                                                )
                                            )
                                        },
                                    shape = RoundedCornerShape(20.dp),
                                    color = if (isSelected) Blue90 else MaterialTheme.colorScheme.surface,
                                    border = BorderStroke(
                                        1.dp,
                                        if (isSelected) Blue40 else BlueGrey80
                                    ),
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        BuiltinLabelIcon(
                                            label = label,
                                            modifier = Modifier.size(16.dp),
                                            tint = if (isSelected) Blue40 else BlueGrey70,
                                        )
                                        Text(
                                            text = label.name,
                                            style = AppText.Body2Regular,
                                            color = if (isSelected) Blue40
                                            else MaterialTheme.colorScheme.onSurface,
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Confirm
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        ButtonApp(
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f),
                            type = ButtonType.OUTLINED
                        ) { Text("Cancel") }

                        ButtonApp(
                            type = ButtonType.FILLED,
                            modifier = Modifier.weight(1f),
                            onClick = {
                                if (state.name.trim().isBlank()) {
                                    onStateChange(state.copy(isNameError = true))
                                } else {
                                    onConfirm(
                                        state.name.trim(),
                                        state.color.toArgb(),
                                        state.selectedLabel ?: BuiltinLabels.first(),
                                    )
                                }
                            }
                        ) { Text("Create tag") }
                    }
                }
            }

            else -> Unit
        }
    }
}


@OptIn(ExperimentalUuidApi::class)
@Preview(backgroundColor = 0xfff, showBackground = true, name = "SubScreenHeader")
@Composable
fun CreateTagBottomSheetPreview() {
    var tagSheetState by remember { mutableStateOf<CreateTagSheetState>(CreateTagSheetState.Editing()) }
    CompositionLocalProvider(LocalPopupController provides PopupController()){
        Android_Theme {
            Column(modifier = Modifier.fillMaxSize()) {
                CreateTagBottomSheet(
                    state = CreateTagSheetState.Editing(),
                    onStateChange = { tagSheetState = it },
                    onConfirm = {_, _, _ ->},
                    onDismiss = {},
                )
            }
        }
    }
}
