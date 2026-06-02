package com.example.se405.android.features.workspaces_management.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.se405.android.core.presentation.components.ButtonApp
import com.example.se405.android.core.presentation.components.ButtonType
import com.example.se405.android.core.presentation.components.PopUpLayout
import com.example.se405.android.core.presentation.popup.LocalPopupController
import com.example.se405.android.core.presentation.popup.PopupController
import com.example.se405.android.core.presentation.theme.Android_Theme
import com.example.se405.android.core.presentation.theme.AppText
import com.example.se405.android.features.tasks_management.__test_data__.preview.PreviewDomainEntityData


sealed interface CreateProjectUiState {
    data class Editing(val projectName: String = "", val isError: Boolean = false) : CreateProjectUiState
    object Loading : CreateProjectUiState
}

@Composable
fun WorkspaceDetailCreateProjectPopUp(
    workspaceName: String,
    onCreate: (String) -> Unit = {},
    onCancel: () -> Unit = {},
) {
    var uiState by remember { mutableStateOf<CreateProjectUiState>(CreateProjectUiState.Editing()) }

    PopUpLayout {
        if (uiState is CreateProjectUiState.Loading) {
            Box(
                modifier = Modifier.fillMaxWidth().padding(48.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            val currentState = uiState as? CreateProjectUiState.Editing ?: CreateProjectUiState.Editing()

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(text = "Create new project", style = AppText.DisplayBold)
                    Text(
                        text = "Projects help you track tasks, milestones, and collaborate with your team.",
                        style = AppText.Body2Regular,
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(text = "Workspace name: ", style = AppText.BodyBold)
                    Text(text = workspaceName, style = AppText.BodySemiBold.copy(fontStyle = FontStyle.Italic))
                }
                Column {
                    Text(text = "Project name*", style = AppText.BodyBold)
                    OutlinedTextField(
                        value = currentState.projectName,
                        onValueChange = { newValue ->
                            uiState = CreateProjectUiState.Editing(
                                projectName = newValue,
                                isError = false
                            )
                        },
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        isError = currentState.isError,
                        supportingText = {
                            if (currentState.isError) {
                                Text(
                                    text = "Project name cannot be blank",
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ButtonApp(
                        onClick = onCancel,
                        modifier = Modifier.weight(1f),
                        type = ButtonType.OUTLINED
                    ) {
                        Text("Cancel")
                    }

                    Spacer(modifier = Modifier.width(24.dp))

                    ButtonApp(
                        type = ButtonType.FILLED,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            if (currentState.projectName.trim().isBlank()) {
                                uiState = currentState.copy(isError = true)
                            } else {
                                uiState = CreateProjectUiState.Loading
                                onCreate(currentState.projectName.trim())
                            }
                        }
                    ) {
                        Text("Create")
                    }
                }
            }
        }
    }
}

@Preview(backgroundColor = 0xfff, showBackground = true, name = "SubScreenHeader")
@Composable
fun WorkspaceDetailCreateProjectPopUpPreview() {
    CompositionLocalProvider(LocalPopupController provides PopupController()) {
        Android_Theme {
            WorkspaceDetailCreateProjectPopUp(workspaceName = PreviewDomainEntityData.workspaces[0].name, onCreate = {}, onCancel = {})
        }
    }
}