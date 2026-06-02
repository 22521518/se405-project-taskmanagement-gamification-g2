@file:OptIn(ExperimentalUuidApi::class)

package com.example.se405.android.features.workspaces_management.presentation.validation

import java.time.LocalDate
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Pure, UI-independent validation for the "create task" popups
 * ([com.example.se405.android.features.workspaces_management.presentation.components.CreateTaskProjectPopUpContent]
 * and its wrappers).
 *
 * The submit rules used to live inline inside the Composable's `onClick`, which made
 * them impossible to unit-test and let edge cases slip straight to the GraphQL
 * `createTask` mutation (e.g. a PROJECT task submitted with no project selected,
 * which silently stalled the popup on its loading spinner). Centralising them here
 * keeps the view and the API contract in sync and lets [CreateTaskInputValidatorTest]
 * cover every input combination.
 *
 * Rule order matters: the first failing rule wins, mirroring the order fields appear
 * in the form (title → project → date range → tags).
 */
object CreateTaskInputValidator {

    /** Maximum number of tags a task may carry; mirrors `DEFAULT_MAX_TAGS` in the UI. */
    const val MAX_TAGS = 3

    sealed interface Result {
        /** Input is valid; carries the normalized (trimmed) text ready for the API. */
        data class Valid(
            val title: String,
            val description: String,
        ) : Result

        data class Invalid(val error: CreateTaskInputError) : Result
    }

    /**
     * @param projectSelected whether a project is chosen. Always `true` for the
     *   project-scoped popup (project is fixed); driven by the dropdown selection for
     *   the workspace-scoped popup. A PROJECT task with no project is rejected by the
     *   backend, so we block it before flipping the popup into its loading state.
     */
    fun validate(
        title: String,
        description: String = "",
        projectSelected: Boolean = true,
        startDate: LocalDate? = null,
        dueDate: LocalDate? = null,
        tagIds: List<Uuid> = emptyList(),
    ): Result {
        val trimmedTitle = title.trim()
        return when {
            trimmedTitle.isBlank() -> Result.Invalid(CreateTaskInputError.BlankTitle)
            !projectSelected -> Result.Invalid(CreateTaskInputError.ProjectRequired)
            // A half-open range (only start or only due) is allowed: the backend stores
            // the two dates independently. Only an inverted full range is invalid.
            startDate != null && dueDate != null && dueDate.isBefore(startDate) ->
                Result.Invalid(CreateTaskInputError.InvalidDateRange)
            tagIds.size > MAX_TAGS -> Result.Invalid(CreateTaskInputError.TooManyTags)
            tagIds.size != tagIds.distinct().size -> Result.Invalid(CreateTaskInputError.DuplicateTags)
            else -> Result.Valid(title = trimmedTitle, description = description.trim())
        }
    }
}

enum class CreateTaskInputError {
    /** Title is empty or whitespace only. */
    BlankTitle,

    /** A PROJECT task was submitted without a project selected. */
    ProjectRequired,

    /** Both dates are set and the due date is before the start date. */
    InvalidDateRange,

    /** More than [CreateTaskInputValidator.MAX_TAGS] tags were selected. */
    TooManyTags,

    /** The same tag id appears more than once. */
    DuplicateTags,
}
