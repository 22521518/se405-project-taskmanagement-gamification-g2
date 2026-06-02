@file:OptIn(ExperimentalUuidApi::class)
@file:Suppress("AssignedValueIsNeverRead")

package com.example.se405.android.features.workspaces_management.presentation.components

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.se405.android.core.presentation.components.ButtonApp
import com.example.se405.android.core.presentation.components.ButtonType
import com.example.se405.android.core.presentation.components.HabitLabel
import com.example.se405.android.core.presentation.components.PopUpLayout
import com.example.se405.android.core.presentation.popup.LocalPopupController
import com.example.se405.android.core.presentation.popup.PopupController
import com.example.se405.android.core.presentation.theme.AppText
import com.example.se405.android.core.presentation.theme.*
import com.example.se405.android.features.tasks_management.__test_data__.preview.PreviewDomainEntityData.projects
import com.example.se405.android.features.tasks_management.__test_data__.preview.PreviewDomainEntityData.tags
import com.example.se405.android.features.tasks_management.domain.entity.Tag
import com.example.se405.android.features.tasks_management.domain.entity.TagOwnershipType
import com.example.se405.android.graphql.type.TaskPriority
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

const val DEFAULT_MAX_TAGS = 3
sealed interface CreateTaskUiState {
    data class Editing(
        val title: String = "",
        val description: String = "",
        val priority: TaskPriority = TaskPriority.MEDIUM,
        val startDate: LocalDate? = null,
        val dueDate: LocalDate? = null,
        val selectedTagIds: List<Uuid> = emptyList(),
        val selectedAssigneeId: Uuid? = null,
        // field errors
        val isTitleError: Boolean = false,
        val isDateRangeError: Boolean = false,
        val isProjectError: Boolean = false,
    ) : CreateTaskUiState

    object Loading : CreateTaskUiState
}

sealed interface CreateTagSheetState {
    object Hidden : CreateTagSheetState
    data class Editing(
        val name: String = "",
        val color: Color = Blue40,
        val selectedLabel: HabitLabel? = null,
        val isNameError: Boolean = false,
    ) : CreateTagSheetState
    object Loading : CreateTagSheetState
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectDetailCreateProjectTaskPopUp(
    projectName: String,
    projectMembers: List<AddableMember> = emptyList(),
    availableTags: List<Tag> = emptyList(),
    onCreate: (
        title: String,
        description: String,
        priority: TaskPriority,
        startDate: LocalDate?,
        dueDate: LocalDate?,
        tagIds: List<Uuid>,
        assigneeId: Uuid?,
    ) -> Unit = { _, _, _, _, _, _, _ -> },
    onCreateTag: (name: String, color: Int, label: HabitLabel, ownershipType: TagOwnershipType) -> Unit =
        { _, _, _, _ -> },
    onCancel: () -> Unit = {},
) {

    CreateTaskProjectPopUpContent(
        availableTags = availableTags,
        projectMembers = projectMembers,
        onCreate = onCreate,
        onCreateTag = onCreateTag,
        onCancel = onCancel,
        projectSelector = {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(text = "Project: ", style = AppText.BodyBold)
                Text(
                    text = projectName,
                    style = AppText.BodySemiBold.copy(fontStyle = FontStyle.Italic)
                )
            }
        }
    )
}

@Preview(backgroundColor = 0xfff, showBackground = true, name = "SubScreenHeader")
@Composable
fun ProjectDetailCreateProjectTaskPopUpPreview() {
    CompositionLocalProvider(LocalPopupController provides PopupController()){
        Android_Theme {
            ProjectDetailCreateProjectTaskPopUp(
                projectName = projects.first().name,
                projectMembers = emptyList(),
                availableTags = tags,
                onCreate = { _, _, _, _, _, _, _ -> },
                onCreateTag = { _, _, _, _ -> },
                onCancel = {}
            )
        }
    }
}
