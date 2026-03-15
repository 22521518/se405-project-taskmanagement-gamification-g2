@file:OptIn(ExperimentalUuidApi::class)

package com.example.se405.android_native_frontend.features.tasks_management.presentation.components

import com.example.se405.android_native_frontend.core.utils.PreviewTaskData
import kotlin.collections.listOf
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

object PreviewWPData {
    // Projects
    val projects = listOf(
        Project(
            id = Uuid.random(),
            name = "Android App",
            tasks = listOf(
                PreviewTaskData.tasks[0]
            )
        ),
        Project(
            id = Uuid.random(),
            name = "Personal Habits",
            tasks = listOf(
                PreviewTaskData.tasks[1],
                PreviewTaskData.tasks[2]
            )
        )
    )

    // Workspaces
    val workspaces = listOf(
        Workspace(
            id = Uuid.random(),
            name = "Work",
            projects = listOf(
                projects[0]
            )
        ),
        Workspace(
            id = Uuid.random(),
            name = "Life",
            projects = listOf(
                projects[1]
            )
        )
    )
}