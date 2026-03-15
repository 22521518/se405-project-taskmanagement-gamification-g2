package com.example.se405.android_native_frontend.features.tasks_management.presentation.components

object PreviewTreeData {
    val trees = listOf(
        TreeNode(
            id = 1,
            name = "Project",
            data = "root",
            children = listOf(
                TreeNode(
                    id = 2,
                    name = "Frontend",
                    data = "branch",
                    children = listOf(
                        TreeNode(
                            id = 3,
                            name = "Login Screen",
                            data = "leaf"
                        ),
                        TreeNode(
                            id = 4,
                            name = "Dashboard",
                            data = "leaf"
                        )
                    )
                ),
                TreeNode(
                    id = 5,
                    name = "Backend",
                    data = "branch",
                    children = listOf(
                        TreeNode(
                            id = 6,
                            name = "Auth API",
                            data = "leaf"
                        ),
                        TreeNode(
                            id = 7,
                            name = "Task API",
                            data = "leaf"
                        )
                    )
                )
            )
        )
    )
}