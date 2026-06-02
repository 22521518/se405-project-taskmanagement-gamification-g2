@file:OptIn(ExperimentalUuidApi::class)

package com.example.se405.android.features.tasks_management

import com.example.se405.android.core.authentication.data.AuthPreferences
import com.example.se405.android.core.onLogin
import com.example.se405.android.features.tasks_management.domain.entity.Task
import com.example.se405.android.features.tasks_management.domain.entity.TaskPriority
import com.example.se405.android.features.tasks_management.domain.entity.TaskStatus
import com.example.se405.android.features.tasks_management.domain.entity.TaskType
import com.example.se405.android.features.tasks_management.domain.use_case.PersonalTaskUsecase
import com.example.se405.android.features.tasks_management.domain.use_case.TaskUseCases
import com.example.se405.android.features.tasks_management.domain.use_case.crud.MarkTaskDone
import kotlinx.coroutines.flow.firstOrNull
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.Koin
import org.robolectric.RobolectricTestRunner
import java.time.LocalDate
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

private fun String?.toUuidOrNull(): Uuid? {
    if (this.isNullOrBlank()) return null
    return runCatching { Uuid.parse(this) }.getOrNull()
}

@RunWith(RobolectricTestRunner::class)
class PersonalTaskUseCaseTest {

    private fun baseTestLayout(name: String, block: suspend (Koin, Uuid) -> Unit) {
        println("=== test_$name ===")
        onLogin(
            runWithAuthentication = { koin, _ ->
                val userId = koin.get<AuthPreferences>().userId.firstOrNull()?.toUuidOrNull()
                if (userId != null) block(koin, userId)
                else println("Loi: Khong tim thay UserId hop le hoac UserId khong dung dinh dang UUID")
            },
            onFailed = { println("Info FAILURE: ${it.message}") }
        )
        println()
    }

    @Test
    fun test_getAllPersonalTasks() = baseTestLayout("getAllPersonalTasks") { koin, userId ->
        val personalTaskUsecase = koin.get<PersonalTaskUsecase>()
        val tasks = personalTaskUsecase.getAllTasks(userId)
        println("So luong personal tasks: ${tasks.size}")
        tasks.forEach { println("Task: $it") }
        assertNotNull(tasks)
    }

    @Test
    fun test_getPersonalTasksByDateRange() = baseTestLayout("getPersonalTasksByDateRange") { koin, userId ->
        val personalTaskUsecase = koin.get<PersonalTaskUsecase>()
        val today = LocalDate.now()
        val tasks = personalTaskUsecase.getTasksByDateRange(userId, today.minusDays(7), today)
        println("So luong personal tasks trong khoang ngay: ${tasks.size}")
        tasks.forEach { println("Task: $it") }
        assertNotNull(tasks)
    }

    @Test
    fun test_statusCalculation_and_repetition() = baseTestLayout("statusCalculationAndRepetition") { koin, userId ->
        val taskUseCases = koin.get<TaskUseCases>()
        val personalTaskUsecase = koin.get<PersonalTaskUsecase>()
        val markTaskDone = koin.get<MarkTaskDone>()

        val today = LocalDate.now()
        val titleBase = "Personal Repetition ${System.currentTimeMillis()}"

        val taskDone = Task(
            uuid = Uuid.random(),
            title = "$titleBase - Done",
            description = "",
            repetition = 2,
            type = TaskType.HABIT,
            status = TaskStatus.TODO,
            priority = TaskPriority.MEDIUM,
            creator = null,
            tags = emptyList(),
            taskCompletionLog = emptyList(),
            startDate = today,
            dueDate = null,
            projectId = null,
        )

        val createdDone = taskUseCases.createTask(taskDone, userId)
        assertTrue("Task Done is not present!", createdDone.isPresent)
        val createdDoneTask = createdDone.get()
        markTaskDone(createdDoneTask, userId, today)
        markTaskDone(createdDoneTask, userId, today)

        val refreshedDoneTasks = personalTaskUsecase.getAllTasks(userId)
        val fetchedDone = refreshedDoneTasks.find { it.uuid == createdDoneTask.uuid }
        assertNotNull("Created DONE task not found", fetchedDone)
        assertEquals(TaskStatus.DONE, fetchedDone!!.status)

        val taskTodo = taskDone.copy(
            uuid = Uuid.random(),
            title = "$titleBase - Todo",
            repetition = 2,
        )

        val createdTodo = taskUseCases.createTask(taskTodo, userId)
        assertTrue("Task Todo is not present!", createdTodo.isPresent)
        val createdTodoTask = createdTodo.get()
        markTaskDone(createdTodoTask, userId, today)

        val refreshedTodoTasks = personalTaskUsecase.getAllTasks(userId)
        val fetchedTodo = refreshedTodoTasks.find { it.uuid == createdTodoTask.uuid }
        assertNotNull("Created TODO task not found", fetchedTodo)
        assertEquals(TaskStatus.TODO, fetchedTodo!!.status)
    }
}
