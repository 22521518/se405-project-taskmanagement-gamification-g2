@file:OptIn(ExperimentalUuidApi::class)
package com.example.se405.android.features.tasks_management

import com.example.se405.android.core.authentication.data.AuthPreferences
import com.example.se405.android.core.onLogin
import com.example.se405.android.features.tasks_management.domain.entity.TaskPriority
import com.example.se405.android.features.tasks_management.domain.use_case.TagUseCases
import com.example.se405.android.features.tasks_management.domain.use_case.TaskUseCases
import com.example.se405.android.features.tasks_management.domain.use_case.WorkspaceUseCases
import com.example.se405.android.features.tasks_management.domain.use_case.crud.MarkTaskDone
import com.example.se405.android.features.tasks_management.domain.use_case.crud.MarkTaskWontDo
import com.example.se405.android.features.tasks_management.utils.generateHabitTasks
import com.example.se405.android.features.tasks_management.utils.generateTags
import com.example.se405.android.features.tasks_management.utils.getTimeSuffix
import com.example.se405.android.features.tasks_management.utils.sampleDescriptions
import com.example.se405.android.features.tasks_management.utils.sampleTitles
import kotlinx.coroutines.flow.firstOrNull
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.random.Random
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

private fun String?.toUuidOrNull(): Uuid? {
    if (this.isNullOrBlank()) return null
    return runCatching { Uuid.parse(this) }.getOrNull()
}

@RunWith(RobolectricTestRunner::class)
class TaskManagementUseCaseTest {

    // ==========================================
    // TASK USE CASES TESTS
    // ==========================================

    @Test
    fun test_getTask() {
        println("=== test_getTask ===")
        onLogin(
            runWithAuthentication = { koin, _ ->
                val taskUseCases = koin.get<TaskUseCases>()
                val authPref = koin.get<AuthPreferences>()
                run {
                    val userIdStr = authPref.userId.firstOrNull()
                    val userIdUuid = userIdStr?.toUuidOrNull()

                    if (userIdUuid != null) {
                        val tasks = taskUseCases.getTask(userIdUuid)
                        println("Xác thực thành công. Số lượng task lấy được: ${tasks.size}")
                        tasks.forEach { println("Task: $it") }
                    } else {
                        println("Lỗi: Không tìm thấy UserId hợp lệ hoặc UserId không đúng định dạng UUID")
                    }
                }
            },
            onFailed = { exception ->
                println("Info FAILURE: ${exception.message}")
            }
        )
        println()
    }

    @Test
    fun test_createTask() {
        println("=== test_createTask ===")
        onLogin(
            runWithAuthentication = { koin, _ ->
                val taskUseCases = koin.get<TaskUseCases>()
                val authPref = koin.get<AuthPreferences>()
                run {
                    val userIdStr = authPref.userId.firstOrNull()
                    val userIdUuid = userIdStr?.toUuidOrNull()

                    if (userIdUuid != null) {
                        val sample = generateHabitTasks(1)[0]
                        val createdTask = taskUseCases.createTask(sample, userIdUuid)
                        assertTrue("Task is not present!", createdTask.isPresent)
                        println("Task created: ${createdTask.get()}")
                    } else {
                        println("Lỗi: Không tìm thấy UserId hợp lệ hoặc UserId không đúng định dạng UUID")
                    }
                }
            },
            onFailed = { exception ->
                println("Info FAILURE: ${exception.message}")
            }
        )
        test_getTask()
        println()
    }

    @Test
    fun test_updateTask() {
        println("=== test_updateTask ===")
        onLogin(
            runWithAuthentication = { koin, _ ->
                val taskUseCases = koin.get<TaskUseCases>()
                val authPref = koin.get<AuthPreferences>()
                run {
                    val userIdStr = authPref.userId.firstOrNull()
                    val userIdUuid = userIdStr?.toUuidOrNull()

                    if (userIdUuid != null) {
                        var taskList = taskUseCases.getTask(userIdUuid)
                        if (taskList.isEmpty()) {
                            taskUseCases.createTask(generateHabitTasks(1)[0], userIdUuid)
                            taskList = taskUseCases.getTask(userIdUuid)
                        }

                        assertNotNull("First task is NULL!", taskList.firstOrNull())

                        val updatedTitle = "Updated Title: " + sampleTitles[153 % sampleTitles.size] + getTimeSuffix()
                        val randomPriority = TaskPriority.entries.toTypedArray()[Random.nextInt(TaskPriority.entries.toTypedArray().size)]
                        val description = sampleDescriptions[153 % sampleDescriptions.size]
                        println("=== Before ===\n${taskList[0]}")
                        val updatedTask = taskUseCases.updateTask(
                            taskList[0].copy(title = updatedTitle, description = description, priority = randomPriority))
                        assertTrue("Task is not present!", updatedTask.isPresent)
                        println("=== After ===\n${updatedTask.get()}")
                    } else {
                        println("Lỗi: Không tìm thấy UserId hợp lệ hoặc UserId không đúng định dạng UUID")
                    }
                }
            },
            onFailed = { exception ->
                println("Info FAILURE: ${exception.message}")
            }
        )
        println()
    }

    @Test
    fun test_deleteTask() {
        println("=== test_deleteTask ===")
        onLogin(
            runWithAuthentication = { koin, _ ->
                val taskUseCases = koin.get<TaskUseCases>()
                val authPref = koin.get<AuthPreferences>()
                run {
                    val userIdStr = authPref.userId.firstOrNull()
                    val userIdUuid = userIdStr?.toUuidOrNull()

                    if (userIdUuid != null) {
                        var taskList = taskUseCases.getTask(userIdUuid)
                        if (taskList.isEmpty()) {
                            generateHabitTasks(5).forEach { t -> taskUseCases.createTask(t, userIdUuid) }
                            taskList = taskUseCases.getTask(userIdUuid)
                        }

                        assertNotNull(taskList.firstOrNull())
                        println("=== Before ===")
                        taskList.forEach { println(it) }
                        val deletedTask = taskUseCases.deleteTask(taskList[0])
                        assertTrue("Task is not present!", deletedTask.isPresent)
                        println("Deleted task: ${deletedTask.get()}")
                        println("=== After ===")
                        taskList = taskUseCases.getTask(userIdUuid)
                        taskList.forEach { println(it) }
                    } else {
                        println("Lỗi: Không tìm thấy UserId hợp lệ hoặc UserId không đúng định dạng UUID")
                    }
                }
            },
            onFailed = { exception ->
                println("Info FAILURE: ${exception.message}")
            }
        )
        println()
    }

    // ==========================================
    // TAG USE CASES TESTS
    // ==========================================

    @Test
    fun test_getTagsByUser() {
        println("=== test_getTagsByUser ===")
        onLogin(
            runWithAuthentication = { koin, _ ->
                val tagUseCases = koin.get<TagUseCases>()
                val authPref = koin.get<AuthPreferences>()
                run {
                    val userIdStr = authPref.userId.firstOrNull()
                    val userIdUuid = userIdStr?.toUuidOrNull()

                    if (userIdUuid != null) {
                        val tags = tagUseCases.getTagsByUser(userIdUuid)
                        println("Xác thực thành công. Số lượng tag lấy được: ${tags.size}")
                        tags.forEach { println("Tag: $it") }
                    } else {
                        println("Lỗi: Không tìm thấy UserId hợp lệ hoặc UserId không đúng định dạng UUID")
                    }
                }
            },
            onFailed = { exception ->
                println("Info FAILURE: ${exception.message}")
            }
        )
        println()
    }

    @Test
    fun test_getTagsByWorkspace() {
        println("=== test_getTagsByWorkspace ===")
        onLogin(
            runWithAuthentication = { koin, _ ->
                val tagUseCases = koin.get<TagUseCases>()
                val authPref = koin.get<AuthPreferences>()
                run {
                    val userIdStr = authPref.userId.firstOrNull()
                    val userIdUuid = userIdStr?.toUuidOrNull()

                    if (userIdUuid != null) {
                        val tags = tagUseCases.getTagsByWorkspace(userIdUuid)
                        println("Xác thực thành công. Số lượng tag lấy được: ${tags.size}")
                        tags.forEach { println("Tag: $it") }
                    } else {
                        println("Lỗi: Không tìm thấy UserId hợp lệ hoặc UserId không đúng định dạng UUID")
                    }
                }
            },
            onFailed = { exception ->
                println("Info FAILURE: ${exception.message}")
            }
        )
        println()
    }

    @Test
    fun test_createTag() {
        println("=== test_createTag ===")
        onLogin(
            runWithAuthentication = { koin, _ ->
                val tagUseCases = koin.get<TagUseCases>()
                val authPref = koin.get<AuthPreferences>()
                run {
                    val userIdStr = authPref.userId.firstOrNull()
                    val userIdUuid = userIdStr?.toUuidOrNull()

                    if (userIdUuid != null) {
                        val sample = generateTags(1, userIdUuid)[0]
                        val createdTag = tagUseCases.createTag(sample)
                        assertTrue("Tag is not present!", createdTag.isPresent)
                        println("Tag created: ${createdTag.get()}")
                    } else {
                        println("Lỗi: Không tìm thấy UserId hợp lệ hoặc UserId không đúng định dạng UUID")
                    }
                }
            },
            onFailed = { exception ->
                println("Info FAILURE: ${exception.message}")
            }
        )
        println()
    }

    @Test
    fun test_updateTag() {
        println("=== test_updateTag ===")
        onLogin(
            runWithAuthentication = { koin, _ ->
                val tagUseCases = koin.get<TagUseCases>()
                val authPref = koin.get<AuthPreferences>()
                run {
                    val userIdStr = authPref.userId.firstOrNull()
                    val userIdUuid = userIdStr?.toUuidOrNull()

                    if (userIdUuid != null) {
                        var tagList = tagUseCases.getTagsByUser(userIdUuid)
                        if (tagList.isEmpty()) {
                            tagUseCases.createTag(generateTags(1, userIdUuid)[0])
                            tagList = tagUseCases.getTagsByUser(userIdUuid)
                        }

                        assertNotNull("First tag is NULL!", tagList.firstOrNull())

                        val targetTag = tagList[0]
                        println("=== Before ===\n$targetTag")

                        val updatedTag = tagUseCases.updateTag(
                            targetTag.copy(name = "Updated Tag " + getTimeSuffix())
                        )

                        assertTrue("Updated tag is not present!", updatedTag.isPresent)
                        println("=== After ===\n${updatedTag.get()}")
                    } else {
                        println("Lỗi: Không tìm thấy UserId hợp lệ hoặc UserId không đúng định dạng UUID")
                    }
                }
            },
            onFailed = { exception ->
                println("Info FAILURE: ${exception.message}")
            }
        )
        println()
    }

    @Test
    fun test_deleteTag() {
        println("=== test_deleteTag ===")
        onLogin(
            runWithAuthentication = { koin, _ ->
                val tagUseCases = koin.get<TagUseCases>()
                val authPref = koin.get<AuthPreferences>()
                run {
                    val userIdStr = authPref.userId.firstOrNull()
                    val userIdUuid = userIdStr?.toUuidOrNull()

                    if (userIdUuid != null) {
                        var tagList = tagUseCases.getTagsByUser(userIdUuid)
                        if (tagList.isEmpty()) {
                            generateTags(3, userIdUuid).forEach { t -> tagUseCases.createTag(t) }
                            tagList = tagUseCases.getTagsByUser(userIdUuid)
                        }

                        assertNotNull("First tag is NULL!", tagList.firstOrNull())

                        println("=== Before ===")
                        tagList.forEach { println(it) }

                        val targetTag = tagList[0]
                        val deletedTagId = tagUseCases.deleteTag(targetTag.uuid)

                        assertTrue("Deleted tag id is not present!", deletedTagId.isPresent)
                        println("Deleted tag id: ${deletedTagId.get()}")

                        println("=== After ===")
                        tagUseCases.getTagsByUser(userIdUuid).forEach { println(it) }
                    } else {
                        println("Lỗi: Không tìm thấy UserId hợp lệ hoặc UserId không đúng định dạng UUID")
                    }
                }
            },
            onFailed = { exception ->
                println("Info FAILURE: ${exception.message}")
            }
        )
        println()
    }

    // ==========================================
    // WORKSPACE USE CASES TESTS
    // ==========================================

    @Test
    fun test_getProjectsByWorkspace() {
        println("=== test_getProjectsByWorkspace ===")
        onLogin(
            runWithAuthentication = { koin, _ ->
                val workspaceUseCases = koin.get<WorkspaceUseCases>()
                val authPref = koin.get<AuthPreferences>()
                run {
                    val userIdStr = authPref.userId.firstOrNull()
                    val userIdUuid = userIdStr?.toUuidOrNull()

                    if (userIdUuid != null) {
                        val projects = workspaceUseCases.getProjectsByWorkspace(userIdUuid)
                        println("Xác thực thành công. Số lượng project lấy được: ${projects.size}")
                        projects.forEach { println("Project: $it") }
                    } else {
                        println("Lỗi: Không tìm thấy UserId hợp lệ hoặc UserId không đúng định dạng UUID")
                    }
                }
            },
            onFailed = { exception ->
                println("Info FAILURE: ${exception.message}")
            }
        )
        println()
    }

    @Test
    fun test_getMembersByWorkspace() {
        println("=== test_getMembersByWorkspace ===")
        onLogin(
            runWithAuthentication = { koin, _ ->
                val workspaceUseCases = koin.get<WorkspaceUseCases>()
                val authPref = koin.get<AuthPreferences>()
                run {
                    val userIdStr = authPref.userId.firstOrNull()
                    val userIdUuid = userIdStr?.toUuidOrNull()

                    if (userIdUuid != null) {
                        val members = workspaceUseCases.getMembersByWorkspace(userIdUuid)
                        println("Xác thực thành công. Số lượng member lấy được: ${members.size}")
                        members.forEach { println("Member: $it") }
                    } else {
                        println("Lỗi: Không tìm thấy UserId hợp lệ hoặc UserId không đúng định dạng UUID")
                    }
                }
            },
            onFailed = { exception ->
                println("Info FAILURE: ${exception.message}")
            }
        )
        println()
    }

    // ==========================================
    // INDEPENDENT CRUDS / LOGS TESTS
    // ==========================================

    @Test
    fun test_markTaskDone() {
        println("=== test_markTaskDone ===")
        onLogin(
            runWithAuthentication = { koin, _ ->
                val markTaskDone = koin.get<MarkTaskDone>()
                val taskUseCases = koin.get<TaskUseCases>()
                val authPref = koin.get<AuthPreferences>()
                run {
                    val userIdStr = authPref.userId.firstOrNull()
                    val userIdUuid = userIdStr?.toUuidOrNull()

                    if (userIdUuid != null) {
                        var taskList = taskUseCases.getTask(userIdUuid)
                        if (taskList.isEmpty()) {
                            taskUseCases.createTask(generateHabitTasks(1)[0], userIdUuid)
                            taskList = taskUseCases.getTask(userIdUuid)
                        }

                        assertNotNull("First task is NULL!", taskList.firstOrNull())

                        val targetTask = taskList[0]
                        println("=== Before ===\n$targetTask")

                        val result = markTaskDone(targetTask, userIdUuid)
                        val listAfter = taskUseCases.getTask(userIdUuid).filter { it -> it.uuid == targetTask.uuid }

                        assertTrue("Marked-done task is not present!", result.isPresent)
                        println("=== Result ===\n${result.get()}")
                        println("=== After ===\n${listAfter.map{ it -> it.toString() + '\n'}}")
                    } else {
                        println("Lỗi: Không tìm thấy UserId hợp lệ hoặc UserId không đúng định dạng UUID")
                    }
                }
            },
            onFailed = { exception ->
                println("Info FAILURE: ${exception.message}")
            }
        )
        println()
    }

    @Test
    fun test_markTaskWontDo() {
        println("=== test_markTaskWontDo ===")
        onLogin(
            runWithAuthentication = { koin, _ ->
                val markTaskWontDo = koin.get<MarkTaskWontDo>()
                val taskUseCases = koin.get<TaskUseCases>()
                val authPref = koin.get<AuthPreferences>()
                run {
                    val userIdStr = authPref.userId.firstOrNull()
                    val userIdUuid = userIdStr?.toUuidOrNull()

                    if (userIdUuid != null) {
                        var taskList = taskUseCases.getTask(userIdUuid)
                        if (taskList.isEmpty()) {
                            taskUseCases.createTask(generateHabitTasks(1)[0], userIdUuid)
                            taskList = taskUseCases.getTask(userIdUuid)
                        }

                        assertNotNull("First task is NULL!", taskList.firstOrNull())

                        val targetTask = taskList[0]
                        println("=== Before ===\n$targetTask")

                        val result = markTaskWontDo(targetTask, userIdUuid)
                        val listAfter = taskUseCases.getTask(userIdUuid).filter { it -> it.uuid == targetTask.uuid }
                        assertTrue("Marked-wont-do task is not present!", result.isPresent)
                        println("=== Result ===\n${result.get()}")
                        println("=== After ===\n${listAfter.map{ it -> it.toString() + '\n'}}")
                    } else {
                        println("Lỗi: Không tìm thấy UserId hợp lệ hoặc UserId không đúng định dạng UUID")
                    }
                }
            },
            onFailed = { exception ->
                println("Info FAILURE: ${exception.message}")
            }
        )
        println()
    }
}