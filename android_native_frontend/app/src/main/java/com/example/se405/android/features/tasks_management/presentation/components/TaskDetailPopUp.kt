package com.example.se405.android.features.tasks_management.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.se405.android.R
import com.example.se405.android.core.presentation.components.ButtonApp
import com.example.se405.android.core.presentation.components.ButtonCTAText
import com.example.se405.android.core.presentation.components.ButtonType
import com.example.se405.android.core.presentation.components.PopUpLayout
import com.example.se405.android.core.presentation.theme.Android_Theme
import com.example.se405.android.core.presentation.theme.AppText
import com.example.se405.android.features.tasks_management.__test_data__.preview.PreviewDomainEntityData
import com.example.se405.android.features.tasks_management.domain.entity.Task

/**
 * Read-only task detail popup.
 * Modernized layout with clear visual hierarchy.
 */
@Composable
fun TaskDetailPopUp(
    task: Task,
    onEditClick: (Task) -> Unit,
    onChatClick: (Task) -> Unit,
    onClose: () -> Unit,
    onDone: (Task) -> Unit,
    onWontDo: (Task) -> Unit,
    onDelete: (Task) -> Unit,
) {
    val taskStatusDescription = extractTaskStatusDescription(task)

    PopUpLayout(
        modifier = Modifier.padding(horizontal = 20.dp),
        widthRatio = 0.85f, // Mở rộng nhẹ để không gian thoáng hơn
        verticalArrangement = Arrangement.spacedBy(16.dp), // Tăng khoảng cách giữa các phần
    ) {
        // --- HEADER ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Cụm Tiêu đề và Edit
            Row(
                modifier = Modifier
                    .weight(1f)
                    .clickable(onClick = { onEditClick(task) }),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
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
                    maxLines = 1, // Tránh rớt dòng phá vỡ layout
                    overflow = TextOverflow.Ellipsis // Hiện ... nếu quá dài
                )
            }

            // Cụm Nút bấm góc phải
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(start = 8.dp)
            ) {
                ButtonCTAText(
                    onClick = { onChatClick(task) },
                    contentPadding = PaddingValues(4.dp),
                ) {
                    Icon(
                        painter = painterResource(R.drawable.icon_chat),
                        contentDescription = "Chat",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(20.dp),
                    )
                }

                ButtonCTAText(
                    onClick = onClose,
                    contentPadding = PaddingValues(4.dp),
                ) {
                    Icon(
                        painter = painterResource(R.drawable.icon_cross_thin),
                        contentDescription = "Close",
                        tint = Color.Black,
                        modifier = Modifier.size(16.dp),
                    )
                }
            }
        }

        // --- STATUS & PRIORITY ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                BulletTaskText(taskStatusDescription.statusScript, AppText.BodySemiBold)
                BulletTaskText(taskStatusDescription.taskPriority, AppText.BodySemiBold)
            }
            TaskStatus(task)
        }

        // --- TAGS (CHIP STYLE) ---
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            LabelTextInput(text = "Tags:")
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                task.tags.forEach { tag ->
                    // Khối Tag bo góc hiện đại
                    Row(
                        modifier = Modifier
                            .background(
                                color = Color(tag.color).copy(alpha = 0.15f), // Nền màu nhạt
                                shape = RoundedCornerShape(12.dp)
                            )
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            painter = painterResource(tag.label.icon),
                            tint = Color(tag.color),
                            contentDescription = tag.name,
                            modifier = Modifier.size(14.dp),
                        )
                        Text(
                            text = tag.name,
                            color = Color(tag.color),
                            style = AppText.CaptionRegular
                        )
                    }
                }
            }
        }

        // --- DESCRIPTION BOX ---
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            LabelTextInput(text = "Description:")
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 60.dp, max = 240.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.4f), // Khung nền phân biệt vùng text
                        shape = RoundedCornerShape(12.dp)
                    )
                    .padding(12.dp)
            ) {
                Text(
                    text = task.description.ifBlank { "No description provided." },
                    style = AppText.CaptionRegular.copy(lineHeight = 20.sp),
                    color = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.verticalScroll(rememberScrollState())
                )
            }
        }

        // --- CALL TO ACTION ---
        TaskDetailCTASection(
            onDone = { onDone(task) },
            onWontDo = { onWontDo(task) },
            onDelete = { onDelete(task) },
        )
    }
}

/**
 * Modern CTA row with hierarchical button types.
 */
@Composable
private fun TaskDetailCTASection(
    onDone: () -> Unit,
    onWontDo: () -> Unit,
    onDelete: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Nút Xóa (Kém quan trọng nhất, đẩy về góc trái)
        ButtonApp(onClick = onDelete, type = ButtonType.TEXT) {
            Icon(
                painter = painterResource(R.drawable.icon_garbage),
                contentDescription = "Delete",
                modifier = Modifier.size(22.dp),
                tint = MaterialTheme.colorScheme.onPrimary,
            )
        }

        // Khoảng trống đẩy 2 nút chính sang phải
        Spacer(modifier = Modifier.weight(1f))

        // Nút Won't Do (Ưu tiên số 2 - Dạng viền)
        ButtonApp(onClick = onWontDo, type = ButtonType.OUTLINED) {
            Text(
                text = "Won't",
                style = AppText.Body2SemiBold
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Nút Done (Hành động chính - Nổi bật nhất)
        ButtonApp(onClick = onDone, type = ButtonType.FILLED) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.icon_task),
                    contentDescription = "Done",
                    modifier = Modifier.size(16.dp),
                )
                Text(
                    text = "Done",
                    style = AppText.Body2SemiBold
                )
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0x00ECF7FB)
@Composable
fun TaskDetailPopUpPreview() {
    Android_Theme {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            TaskDetailPopUp(
                task = PreviewDomainEntityData.tasks[0],
                onEditClick = {},
                onChatClick = {},
                onClose = {},
                onDone = {},
                onWontDo = {},
                onDelete = {},
            )
        }
    }
}