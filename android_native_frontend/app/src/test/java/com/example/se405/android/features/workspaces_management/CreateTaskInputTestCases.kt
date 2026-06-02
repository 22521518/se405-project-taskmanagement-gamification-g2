@file:OptIn(ExperimentalUuidApi::class)

package com.example.se405.android.features.workspaces_management

import com.example.se405.android.features.workspaces_management.presentation.validation.CreateTaskInputError
import com.example.se405.android.features.workspaces_management.presentation.validation.CreateTaskInputValidator
import java.time.LocalDate
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Declarative input cases for [CreateTaskInputValidatorTest].
 *
 * Each case is one possible combination of form input the create-task popup can
 * produce. Kept in its own file so the test class only contains the running logic
 * and new cases can be added here without touching it.
 */
data class CreateTaskInputCase(
    val name: String,
    val title: String,
    val description: String = "",
    val projectSelected: Boolean = true,
    val startDate: LocalDate? = null,
    val dueDate: LocalDate? = null,
    val tagIds: List<Uuid> = emptyList(),
    /** `null` means the input is expected to be valid. */
    val expectedError: CreateTaskInputError? = null,
    /** Expected normalized (trimmed) title for valid cases. */
    val expectedTitle: String? = null,
    /** Expected normalized (trimmed) description for valid cases. */
    val expectedDescription: String? = null,
)

/** Stable, distinct tag ids reused across cases. */
object TagIds {
    val a: Uuid = Uuid.parse("11111111-1111-1111-1111-111111111111")
    val b: Uuid = Uuid.parse("22222222-2222-2222-2222-222222222222")
    val c: Uuid = Uuid.parse("33333333-3333-3333-3333-333333333333")
    val d: Uuid = Uuid.parse("44444444-4444-4444-4444-444444444444")
}

private val START: LocalDate = LocalDate.of(2026, 6, 15)
private val DUE: LocalDate = LocalDate.of(2026, 6, 26)

val createTaskInputCases: List<CreateTaskInputCase> = listOf(
    // ── Valid ────────────────────────────────────────────────────────────────
    CreateTaskInputCase(
        name = "minimal valid: title only",
        title = "Write report",
        expectedError = null,
        expectedTitle = "Write report",
        expectedDescription = "",
    ),
    CreateTaskInputCase(
        name = "valid: title is trimmed",
        title = "   Padded title   ",
        description = "  some notes  ",
        expectedError = null,
        expectedTitle = "Padded title",
        expectedDescription = "some notes",
    ),
    CreateTaskInputCase(
        name = "valid: full date range start < due",
        title = "Sprint task",
        startDate = START,
        dueDate = DUE,
        expectedError = null,
        expectedTitle = "Sprint task",
    ),
    CreateTaskInputCase(
        name = "valid: same start and due date",
        title = "One-day task",
        startDate = START,
        dueDate = START,
        expectedError = null,
        expectedTitle = "One-day task",
    ),
    CreateTaskInputCase(
        name = "valid: only start date (half-open range)",
        title = "Open-ended task",
        startDate = START,
        dueDate = null,
        expectedError = null,
        expectedTitle = "Open-ended task",
    ),
    CreateTaskInputCase(
        name = "valid: only due date (half-open range)",
        title = "Deadline-only task",
        startDate = null,
        dueDate = DUE,
        expectedError = null,
        expectedTitle = "Deadline-only task",
    ),
    CreateTaskInputCase(
        name = "valid: exactly MAX_TAGS distinct tags",
        title = "Tagged task",
        tagIds = listOf(TagIds.a, TagIds.b, TagIds.c),
        expectedError = null,
        expectedTitle = "Tagged task",
    ),

    // ── Blank title ──────────────────────────────────────────────────────────
    CreateTaskInputCase(
        name = "invalid: empty title",
        title = "",
        expectedError = CreateTaskInputError.BlankTitle,
    ),
    CreateTaskInputCase(
        name = "invalid: whitespace-only title",
        title = "    ",
        expectedError = CreateTaskInputError.BlankTitle,
    ),
    CreateTaskInputCase(
        name = "blank title takes priority over other errors",
        title = "",
        projectSelected = false,
        dueDate = START.minusDays(1),
        startDate = START,
        expectedError = CreateTaskInputError.BlankTitle,
    ),

    // ── Project required ─────────────────────────────────────────────────────
    CreateTaskInputCase(
        name = "invalid: no project selected",
        title = "Needs a project",
        projectSelected = false,
        expectedError = CreateTaskInputError.ProjectRequired,
    ),
    CreateTaskInputCase(
        name = "project error takes priority over date range error",
        title = "Needs a project",
        projectSelected = false,
        startDate = DUE,
        dueDate = START, // inverted, but project check fires first
        expectedError = CreateTaskInputError.ProjectRequired,
    ),

    // ── Invalid date range ───────────────────────────────────────────────────
    CreateTaskInputCase(
        name = "invalid: due before start",
        title = "Time-travel task",
        startDate = DUE,
        dueDate = START,
        expectedError = CreateTaskInputError.InvalidDateRange,
    ),
    CreateTaskInputCase(
        name = "invalid: due one day before start",
        title = "Off-by-one task",
        startDate = START,
        dueDate = START.minusDays(1),
        expectedError = CreateTaskInputError.InvalidDateRange,
    ),

    // ── Too many tags ────────────────────────────────────────────────────────
    CreateTaskInputCase(
        name = "invalid: more than MAX_TAGS",
        title = "Over-tagged task",
        tagIds = listOf(TagIds.a, TagIds.b, TagIds.c, TagIds.d),
        expectedError = CreateTaskInputError.TooManyTags,
    ),

    // ── Duplicate tags ───────────────────────────────────────────────────────
    CreateTaskInputCase(
        name = "invalid: duplicate tag ids",
        title = "Dup-tag task",
        tagIds = listOf(TagIds.a, TagIds.a),
        expectedError = CreateTaskInputError.DuplicateTags,
    ),
)

/** Convenience runner so both the test and any future caller share the same call. */
fun CreateTaskInputCase.run(): CreateTaskInputValidator.Result =
    CreateTaskInputValidator.validate(
        title = title,
        description = description,
        projectSelected = projectSelected,
        startDate = startDate,
        dueDate = dueDate,
        tagIds = tagIds,
    )
