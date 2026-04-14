package com.example.se405.android_native_frontend.features.tasks_management.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.se405.android_native_frontend.R
import com.example.se405.android_native_frontend.core.presentation.components.ButtonApp
import com.example.se405.android_native_frontend.core.presentation.components.ButtonCTAText
import com.example.se405.android_native_frontend.core.presentation.components.ButtonType
import com.example.se405.android_native_frontend.core.presentation.components.PopUpLayout
import com.example.se405.android_native_frontend.core.presentation.theme.Android_native_frontendTheme
import com.example.se405.android_native_frontend.core.presentation.theme.AppText
import com.example.se405.android_native_frontend.features.tasks_management.__test_data__.preview.PreviewDomainEntityData
import com.example.se405.android_native_frontend.features.tasks_management.domain.entity.Task

/**
 * Read-only task detail popup.
 *
 * This composable does not manage popup transitions itself.
 * It emits UI events so Screen/ViewModel layers can decide what to do next.
 */
@Composable
fun TaskDetailPopUp(
    task: Task,
    onEditClick: (Task) -> Unit,
    onClose: () -> Unit,
    onDone: (Task) -> Unit,
    onWontDo: (Task) -> Unit,
    onDelete: (Task) -> Unit,
) {
    val taskStatusDescription = extractTaskStatusDescription(task)

    PopUpLayout(
        modifier = Modifier.padding(horizontal = 20.dp),
        widthRatio = 0.8f,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                modifier = Modifier.clickable(onClick = { onEditClick(task) }),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.icon_edit_pencil),
                    contentDescription = "Edit",
                    tint = MaterialTheme.colorScheme.onPrimary,
                )
                Text(
                    text = task.title,
                    style = AppText.HeadBold,
                    color = MaterialTheme.colorScheme.onPrimary,
                )
            }

            ButtonCTAText(
                onClick = onClose,
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

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            BulletTaskText(taskStatusDescription.statusScript, AppText.BodySemiBold)
            TaskStatus(task)
        }
        BulletTaskText(
            taskStatusDescription.taskPriority,
            AppText.BodySemiBold,
            modifier = Modifier.fillMaxWidth(),
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            LabelTextInput(text = "Tags:")
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                task.tags.forEach { tag ->
                    Icon(
                        painter = painterResource(tag.label.icon),
                        tint = Color(tag.color),
                        contentDescription = tag.name,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
        }

        Text(
            text = task.description,
            style = AppText.CaptionRegular.copy(lineHeight = 20.sp),
            color = MaterialTheme.colorScheme.tertiary,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 320.dp)
                .verticalScroll(rememberScrollState()),
        )
        TaskDetailCTASection(
            onDone = { onDone(task) },
            onWontDo = { onWontDo(task) },
            onDelete = { onDelete(task) },
        )
    }
}

private data class CTAButton(
    val text: String,
    val icon: Int,
    val onClick: () -> Unit,
)

/**
 * CTA row for task detail popup actions.
 */
@Composable
private fun TaskDetailCTASection(
    onDone: () -> Unit,
    onWontDo: () -> Unit,
    onDelete: () -> Unit,
) {
    val ctaButtons = listOf(
        CTAButton("Done", R.drawable.icon_task, onDone),
        CTAButton("Won't", R.drawable.icon_cross_box, onWontDo),
        CTAButton("Delete", R.drawable.icon_garbage, onDelete),
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ctaButtons.forEach { button ->
            ButtonApp(onClick = button.onClick, type = ButtonType.TEXT) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Icon(
                        painter = painterResource(button.icon),
                        contentDescription = button.text,
                        modifier = Modifier.size(24.dp),
                        tint = MaterialTheme.colorScheme.onPrimary,
                    )
                    Text(
                        text = button.text,
                        style = AppText.Body2SemiBold,
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                }
            }
        }
    }
}

/**
 * Preview for TaskDetailPopUp.
 *
 * Usage guide:
 * - In production, pass real callbacks from Screen container.
 * - onEditClick should trigger Screen-level popup/navigation change.
 * - onDone/onWontDo/onDelete should dispatch events to ViewModel.
 */
@Preview(showBackground = true, backgroundColor = 0x00ECF7FB)
@Composable
fun TaskDetailPopUpPreview() {
    Android_native_frontendTheme {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            TaskDetailPopUp(
                task = PreviewDomainEntityData.tasks[0],
                onEditClick = {},
                onClose = {},
                onDone = {},
                onWontDo = {},
                onDelete = {},
            )
        }
    }
}
