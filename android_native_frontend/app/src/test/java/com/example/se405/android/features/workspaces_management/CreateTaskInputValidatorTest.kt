@file:OptIn(ExperimentalUuidApi::class)

package com.example.se405.android.features.workspaces_management

import com.example.se405.android.features.workspaces_management.presentation.validation.CreateTaskInputError
import com.example.se405.android.features.workspaces_management.presentation.validation.CreateTaskInputValidator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.uuid.ExperimentalUuidApi

/**
 * Unit tests for [CreateTaskInputValidator] — the create-task popup's submit rules.
 *
 * Pure JUnit (no Robolectric/Koin) because the validator has no Android or network
 * dependency. Input combinations live in [createTaskInputCases]; this class only
 * drives them and asserts the expected outcome.
 */
class CreateTaskInputValidatorTest {

    @Test
    fun `all declared input cases produce the expected result`() {
        val failures = mutableListOf<String>()

        for (case in createTaskInputCases) {
            when (val result = case.run()) {
                is CreateTaskInputValidator.Result.Valid -> {
                    if (case.expectedError != null) {
                        failures += "[${case.name}] expected error ${case.expectedError} but was Valid"
                        continue
                    }
                    case.expectedTitle?.let { expected ->
                        if (result.title != expected) {
                            failures += "[${case.name}] expected title='$expected' but was '${result.title}'"
                        }
                    }
                    case.expectedDescription?.let { expected ->
                        if (result.description != expected) {
                            failures += "[${case.name}] expected description='$expected' but was '${result.description}'"
                        }
                    }
                }

                is CreateTaskInputValidator.Result.Invalid -> {
                    if (case.expectedError == null) {
                        failures += "[${case.name}] expected Valid but was Invalid(${result.error})"
                    } else if (result.error != case.expectedError) {
                        failures += "[${case.name}] expected error ${case.expectedError} but was ${result.error}"
                    }
                }
            }
        }

        assertTrue(
            "Validation mismatches:\n" + failures.joinToString("\n"),
            failures.isEmpty()
        )
    }

    @Test
    fun `blank title is rejected before project check`() {
        val result = CreateTaskInputValidator.validate(title = "  ", projectSelected = false)
        assertEquals(
            CreateTaskInputValidator.Result.Invalid(CreateTaskInputError.BlankTitle),
            result
        )
    }

    @Test
    fun `project required fires before date range check`() {
        val result = CreateTaskInputValidator.validate(
            title = "Task",
            projectSelected = false,
            startDate = java.time.LocalDate.of(2026, 6, 20),
            dueDate = java.time.LocalDate.of(2026, 6, 1),
        )
        assertEquals(
            CreateTaskInputValidator.Result.Invalid(CreateTaskInputError.ProjectRequired),
            result
        )
    }

    @Test
    fun `valid input trims title and description`() {
        val result = CreateTaskInputValidator.validate(
            title = "  Build feature  ",
            description = "  details  ",
        )
        assertEquals(
            CreateTaskInputValidator.Result.Valid(title = "Build feature", description = "details"),
            result
        )
    }

    @Test
    fun `MAX_TAGS boundary is inclusive`() {
        val atLimit = CreateTaskInputValidator.validate(
            title = "Task",
            tagIds = listOf(TagIds.a, TagIds.b, TagIds.c),
        )
        assertTrue(atLimit is CreateTaskInputValidator.Result.Valid)
        assertEquals(3, CreateTaskInputValidator.MAX_TAGS)
    }
}
