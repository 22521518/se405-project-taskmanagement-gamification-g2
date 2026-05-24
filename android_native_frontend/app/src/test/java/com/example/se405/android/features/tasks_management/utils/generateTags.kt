@file:OptIn(ExperimentalUuidApi::class)

package com.example.se405.android.features.tasks_management.utils

import com.example.se405.android.core.presentation.components.BuiltinLabels
import com.example.se405.android.features.tasks_management.domain.entity.Tag
import com.example.se405.android.features.tasks_management.domain.entity.TagOwnershipType
import java.time.LocalDateTime
import kotlin.random.Random
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

fun generateTags(count: Int, userId: Uuid, workspaceId: Uuid? = null): List<Tag> {
    val tagNames = listOf(
        "Urgent", "High Priority", "Low Priority", "In Progress", "Review",
        "Bug", "Feature", "Refactor", "Documentation", "Design",
        "Meeting", "Research", "Exercise", "Finance", "Chore"
    )

    val tagColors = listOf(
        0xFF4CAF50.toInt(), // Green
        0xFFF44336.toInt(), // Red
        0xFF2196F3.toInt(), // Blue
        0xFFFFEB3B.toInt(), // Yellow
        0xFF9C27B0.toInt(), // Purple
        0xFFFF9800.toInt(), // Orange
        0xFF00BCD4.toInt(), // Cyan
        0xFFE91E63.toInt(), // Pink
        0xFF795548.toInt(), // Brown
        0xFF607D8B.toInt()  // Blue Grey
    )

    return List(count) {
        val randomName = tagNames.random() // + " #${Random.nextInt(100, 999)}" // Thêm hậu tố số để tăng tính duy nhất
        val randomColor = tagColors.random()
        val randomLabel = BuiltinLabels.random()
        val ownershipType = if (workspaceId != null) TagOwnershipType.WORKSPACE else TagOwnershipType.PERSONAL

        Tag(
            createdBy = userId,
            ownershipType = ownershipType,
            workspaceId = workspaceId,
            taskIds = emptyList(),
            uuid = Uuid.random(),
            name = randomName,
            color = randomColor,
            label = randomLabel,
            createdAt = LocalDateTime.now().minusDays(Random.nextLong(1, 30)), // Random within 30 ngày qua
            updatedAt = LocalDateTime.now(),
            creator = null,
            tasks = emptyList()
        )
    }
}