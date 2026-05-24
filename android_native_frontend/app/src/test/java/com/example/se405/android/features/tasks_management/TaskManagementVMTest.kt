@file:OptIn(ExperimentalUuidApi::class)

package com.example.se405.android.features.tasks_management

import androidx.compose.ui.graphics.Color
import com.example.se405.android.core.authentication.data.AuthPreferences
import com.example.se405.android.core.presentation.components.BuiltinLabels
import com.example.se405.android.features.tasks_management.domain.entity.Project
import com.example.se405.android.features.tasks_management.domain.entity.Tag
import com.example.se405.android.features.tasks_management.domain.entity.TagOwnershipType
import com.example.se405.android.features.tasks_management.domain.entity.Task
import com.example.se405.android.features.tasks_management.domain.entity.TaskCompletionLog
import com.example.se405.android.features.tasks_management.domain.entity.TaskPriority
import com.example.se405.android.features.tasks_management.domain.entity.TaskStatus
import com.example.se405.android.features.tasks_management.domain.entity.TaskType
import com.example.se405.android.features.tasks_management.domain.use_case.TagUseCases
import com.example.se405.android.features.tasks_management.domain.use_case.TaskUseCases
import com.example.se405.android.features.tasks_management.domain.use_case.WorkspaceUseCases
import com.example.se405.android.features.tasks_management.domain.use_case.crud.MarkTaskDone
import com.example.se405.android.features.tasks_management.domain.use_case.crud.MarkTaskWontDo
import com.example.se405.android.features.tasks_management.presentation.components.CreateTagUiState
import com.example.se405.android.features.tasks_management.presentation.viewmodel.TaskManagementViewModel
import com.example.se405.android.features.tasks_management.presentation.viewmodel.TaskUiEvent
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.Calendar
import java.util.Optional
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Unit tests tập trung vào LOGIC NỘI BỘ của TaskManagementViewModel:
 *
 *  - mergeProjectsWithTasks: phân loại task vào project đúng, tạo ghost project, tạo bucket "Personal"
 *  - resolveWorkspaceId: xác định workspaceId từ tag
 *  - mergeTags: union tag, dedup theo uuid (workspace wins)
 *  - getTaskByDate / applyDateFilter: tài liệu hoá hành vi bypass hiện tại (test=true)
 *  - createTag ownershipType: PERSONAL vs WORKSPACE tuỳ context
 *  - uiEvent channel: đúng event được emit sau mỗi operation
 *  - createTaskDraft: default values và uuid duy nhất
 */
@OptIn(ExperimentalCoroutinesApi::class)
class TaskManagementVMLogicTest {

    // ─── Infrastructure ───────────────────────────────────────────────────────

    private val testDispatcher = StandardTestDispatcher()

    private val mockTaskUseCases: TaskUseCases = mockk()
    private val mockTagUseCases: TagUseCases = mockk()
    private val mockWorkspaceUseCases: WorkspaceUseCases = mockk()
    private val mockAuthPreferences: AuthPreferences = mockk()
    private val mockMarkTaskDone: MarkTaskDone = mockk()
    private val mockMarkTaskWontDo: MarkTaskWontDo = mockk()

    private lateinit var fakeViewModel: TaskManagementViewModel

    // ─── Shared UUIDs ─────────────────────────────────────────────────────────

    private val fakeUserId      = Uuid.parse("b9fda29a-79c3-4c33-bd37-3b9839f51f16")
    private val workspaceId1    = Uuid.parse("aaaaaaaa-0000-0000-0000-000000000001")
    private val projectId1      = Uuid.parse("bbbbbbbb-0000-0000-0000-000000000001")
    private val projectId2      = Uuid.parse("bbbbbbbb-0000-0000-0000-000000000002")
    private val ghostProjectId  = Uuid.parse("cccccccc-dead-beef-0000-000000000001")
    // "cccccccc" → shortId = "cccccccc" → ghost project name = "Project cccccccc"

    // VM nội bộ dùng Uuid.fromLongs(0L, 1L) cho bucket Personal
    private val unassignedProjectId = Uuid.fromLongs(0L, 1L)

    // ─── Lifecycle ────────────────────────────────────────────────────────────

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        mockkStatic(android.util.Log::class)
        every { android.util.Log.d(any(), any<String>()) } returns 0
        every { android.util.Log.i(any(), any<String>()) } returns 0
        every { android.util.Log.w(any(), any<String>()) } returns 0
        every { android.util.Log.e(any(), any<String>()) } returns 0
        every { android.util.Log.e(any(), any<String>(), any()) } returns 0
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    // ─── Builders ─────────────────────────────────────────────────────────────

    /**
     * Tạo Task tối giản. projectId=UNSET_SENTINEL = null.
     */
    private fun task(
        title: String,
        type: TaskType = TaskType.PROJECT,
        projectId: Uuid? = null,
        startDate: LocalDate? = null,
        dueDate: LocalDate? = null,
        status: TaskStatus = TaskStatus.TODO,
        repetition: Int = 1,
        taskLogs: List<TaskCompletionLog> = emptyList()
    ) = Task(
        uuid = Uuid.random(),
        title = title,
        description = "",
        repetition = repetition,
        type = type,
        status = status,
        priority = TaskPriority.MEDIUM,
        creator = null,
        tags = emptyList(),
        taskCompletionLog = taskLogs,
        startDate = startDate,
        dueDate = dueDate,
        projectId = projectId,
    )

    private fun project(id: Uuid, name: String) =
        Project(id = id, name = name, tasks = emptyList())

    private fun tag(
        name: String,
        uuid: Uuid = Uuid.random(),
        workspaceId: Uuid? = null,
    ) = Tag(
        uuid = uuid,
        name = name,
        color = -8825528,
        label = BuiltinLabels[0],
        createdBy = fakeUserId,
        ownershipType = if (workspaceId == null) TagOwnershipType.PERSONAL else TagOwnershipType.WORKSPACE,
        workspaceId = workspaceId,
        taskIds = emptyList(),
        tasks = emptyList(),
        creator = null,
        createdAt = LocalDateTime.now(),
        updatedAt = LocalDateTime.now(),
    )

    // ─── Stub helpers ─────────────────────────────────────────────────────────

    /**
     * Stub cho scenario không có workspace (workspaceId = null).
     * Chỉ stub getTask + getTagsByUser; workspace-related calls không xảy ra.
     */
    private fun stubNoWorkspace(
        tasks: List<Task> = emptyList(),
        userTags: List<Tag> = emptyList(),
    ) {
        every { mockAuthPreferences.userId } returns flowOf(fakeUserId.toString())
        coEvery { mockTaskUseCases.getTask(fakeUserId) } returns tasks
        coEvery { mockTagUseCases.getTagsByUser(fakeUserId) } returns userTags
        // workspaceId sẽ null → các call sau không xảy ra → không cần stub
    }

    /**
     * Stub cho scenario CÓ workspace (workspaceId != null).
     * Tag của user phải có workspaceId để resolveWorkspaceId trả về != null,
     * sau đó VM sẽ gọi thêm getTagsByWorkspace, getProjectsByWorkspace, getMembersByWorkspace.
     */
    private fun stubWithWorkspace(
        tasks: List<Task> = emptyList(),
        userTags: List<Tag> = emptyList(),       // phải có ít nhất 1 tag có workspaceId
        workspaceTags: List<Tag> = emptyList(),
        projects: List<Project> = emptyList(),
    ) {
        every { mockAuthPreferences.userId } returns flowOf(fakeUserId.toString())
        coEvery { mockTaskUseCases.getTask(fakeUserId) } returns tasks
        coEvery { mockTagUseCases.getTagsByUser(fakeUserId) } returns userTags
        coEvery { mockTagUseCases.getTagsByWorkspace(workspaceId1) } returns workspaceTags
        coEvery { mockWorkspaceUseCases.getProjectsByWorkspace(workspaceId1) } returns projects
        coEvery { mockWorkspaceUseCases.getMembersByWorkspace(workspaceId1) } returns emptyList()
    }

    private fun TestScope.buildAndIdle() {
        fakeViewModel = TaskManagementViewModel(
            mockTaskUseCases, mockTagUseCases, mockWorkspaceUseCases,
            mockAuthPreferences, mockMarkTaskDone, mockMarkTaskWontDo,
        )
        advanceUntilIdle()
    }

    // =========================================================================
    // NHÓM 1: mergeProjectsWithTasks
    // =========================================================================

    /**
     * [M1] Tất cả tasks có projectId=null, không có project nào → VM tự tạo bucket "Personal".
     * Dùng để chứa task cá nhân như HABIT hoặc task lẻ.
     */
    @Test
    fun `mergeProjectsWithTasks - all tasks have null projectId and no projects - creates Personal bucket`() =
        runTest(testDispatcher) {
            println("\n=== [M1] tasks(null projectId) + no projects → Personal bucket ===")

            val t1 = task("Tập thể dục sáng", type = TaskType.HABIT)
            val t2 = task("Đọc sách 30 phút",  type = TaskType.HABIT)
            stubNoWorkspace(tasks = listOf(t1, t2))

            buildAndIdle()

            val projects = fakeViewModel.workspaces.value.first().projects
            println("[ASSERT] projects=${projects.map { "${it.name}(${it.tasks.size} tasks)" }}")

            assertEquals("phải có đúng 1 project", 1, projects.size)
            assertEquals("project phải có tên 'Personal'", "Personal", projects.first().name)
            assertEquals("Personal bucket phải chứa đủ 2 tasks", 2, projects.first().tasks.size)
            assertTrue(
                "t1 phải có trong bucket",
                projects.first().tasks.any { it.uuid == t1.uuid },
            )
            assertTrue(
                "t2 phải có trong bucket",
                projects.first().tasks.any { it.uuid == t2.uuid },
            )
        }

    /**
     * [M2] Nhiều tasks cùng projectId, project đó tồn tại → tất cả gộp vào đúng project.
     */
    @Test
    fun `mergeProjectsWithTasks - multiple tasks share same projectId - all grouped into that project`() =
        runTest(testDispatcher) {
            println("\n=== [M2] tasks cùng projectId → gộp vào 1 project ===")

            val t1 = task("Viết báo cáo",  projectId = projectId1)
            val t2 = task("Review code",    projectId = projectId1)
            val t3 = task("Họp team",       projectId = projectId1)
            val p1 = project(projectId1, "Dự án SE405")

            stubWithWorkspace(
                tasks = listOf(t1, t2, t3),
                userTags = listOf(tag("work", workspaceId = workspaceId1)),
                projects = listOf(p1),
            )
            buildAndIdle()

            val projects = fakeViewModel.workspaces.value.first().projects
            val targetProject = projects.find { it.id == projectId1 }
            println("[ASSERT] projects=${projects.map { "${it.name}(${it.tasks.size})" }}")

            assertNotNull("project SE405 phải tồn tại", targetProject)
            assertEquals("phải có đúng 3 tasks trong project SE405", 3, targetProject!!.tasks.size)
        }

    /**
     * [M3] Tasks với projectId khác nhau, cả 2 project đều tồn tại → phân loại đúng project.
     */
    @Test
    fun `mergeProjectsWithTasks - tasks with different projectIds - each goes to correct project`() =
        runTest(testDispatcher) {
            println("\n=== [M3] tasks → phân loại đúng 2 project ===")

            val t1 = task("Feature A", projectId = projectId1)
            val t2 = task("Feature B", projectId = projectId1)
            val t3 = task("Bug fix",   projectId = projectId2)
            val p1 = project(projectId1, "Dự án SE405")
            val p2 = project(projectId2, "Dự án AI")

            stubWithWorkspace(
                tasks = listOf(t1, t2, t3),
                userTags = listOf(tag("ws", workspaceId = workspaceId1)),
                projects = listOf(p1, p2),
            )
            buildAndIdle()

            val projects = fakeViewModel.workspaces.value.first().projects
            val proj1 = projects.find { it.id == projectId1 }
            val proj2 = projects.find { it.id == projectId2 }
            println("[ASSERT] proj1.tasks=${proj1?.tasks?.map { it.title }}")
            println("[ASSERT] proj2.tasks=${proj2?.tasks?.map { it.title }}")

            assertNotNull("Dự án SE405 phải tồn tại", proj1)
            assertNotNull("Dự án AI phải tồn tại", proj2)
            assertEquals("SE405 phải có 2 tasks", 2, proj1!!.tasks.size)
            assertEquals("AI phải có 1 task", 1, proj2!!.tasks.size)
            assertTrue("proj2 phải chứa Bug fix", proj2.tasks.any { it.title == "Bug fix" })
        }

    /**
     * [M4] Task có projectId không khớp project nào (ghost) → VM tự tạo "Project {shortId}".
     * ShortId = 8 ký tự đầu của UUID.
     */
    @Test
    fun `mergeProjectsWithTasks - task has unknown projectId - creates ghost project with shortId name`() =
        runTest(testDispatcher) {
            println("\n=== [M4] task ghost projectId → tạo Project {shortId} ===")

            val ghostTask = task("Task bị mồ côi", projectId = ghostProjectId)
            val expectedShortId = ghostProjectId.toString().take(8) // "cccccccc"
            val expectedProjectName = "Project $expectedShortId"

            stubNoWorkspace(tasks = listOf(ghostTask))
            buildAndIdle()

            val projects = fakeViewModel.workspaces.value.first().projects
            println("[ASSERT] projects=${projects.map { it.name }}")
            println("[ASSERT] expectedName=$expectedProjectName")

            val ghostProject = projects.find { it.name == expectedProjectName }
            assertNotNull("ghost project '$expectedProjectName' phải được tạo", ghostProject)
            assertEquals("ghost project phải chứa task bị mồ côi", 1, ghostProject!!.tasks.size)
        }

    /**
     * [M5] Mix: một số task có projectId hợp lệ, một số null
     * → task hợp lệ vào project đúng, task null vào "Personal".
     */
    @Test
    fun `mergeProjectsWithTasks - mix of assigned and unassigned tasks - splits into project and Personal`() =
        runTest(testDispatcher) {
            println("\n=== [M5] mix assigned + null → project + Personal ===")

            val tAssigned1 = task("Sprint planning",  projectId = projectId1)
            val tAssigned2 = task("Code review",      projectId = projectId1)
            val tPersonal1 = task("Tập thể dục",      projectId = null, type = TaskType.HABIT)
            val tPersonal2 = task("Đọc sách",         projectId = null, type = TaskType.HABIT)
            val p1 = project(projectId1, "Sprint")

            stubWithWorkspace(
                tasks = listOf(tAssigned1, tAssigned2, tPersonal1, tPersonal2),
                userTags = listOf(tag("ws", workspaceId = workspaceId1)),
                projects = listOf(p1),
            )
            buildAndIdle()

            val projects = fakeViewModel.workspaces.value.first().projects
            val sprintProject  = projects.find { it.id == projectId1 }
            val personalBucket = projects.find { it.id == unassignedProjectId }
            println("[ASSERT] sprint.tasks=${sprintProject?.tasks?.map { it.title }}")
            println("[ASSERT] personal.tasks=${personalBucket?.tasks?.map { it.title }}")

            assertNotNull("Sprint project phải tồn tại", sprintProject)
            assertNotNull("Personal bucket phải tồn tại", personalBucket)
            assertEquals("Sprint phải có 2 tasks", 2, sprintProject!!.tasks.size)
            assertEquals("Personal phải có 2 tasks", 2, personalBucket!!.tasks.size)
            assertTrue("Personal phải chứa task Tập thể dục",
                personalBucket.tasks.any { it.uuid == tPersonal1.uuid })
        }

    /**
     * [M6] Không có task nào nhưng projects tồn tại → projects hiện trong workspace với tasks=rỗng.
     */
    @Test
    fun `mergeProjectsWithTasks - no tasks but projects exist - projects appear with empty task lists`() =
        runTest(testDispatcher) {
            println("\n=== [M6] no tasks + projects exist → projects với tasks rỗng ===")

            val p1 = project(projectId1, "Dự án SE405")
            val p2 = project(projectId2, "Dự án AI")

            stubWithWorkspace(
                tasks = emptyList(),
                userTags = listOf(tag("ws", workspaceId = workspaceId1)),
                projects = listOf(p1, p2),
            )
            buildAndIdle()

            val projects = fakeViewModel.workspaces.value.first().projects
            println("[ASSERT] projects=${projects.map { "${it.name}(${it.tasks.size})" }}")

            assertEquals("phải có đúng 2 projects", 2, projects.size)
            assertTrue("Dự án SE405 phải có tasks rỗng",
                projects.find { it.id == projectId1 }?.tasks?.isEmpty() == true)
            assertTrue("Dự án AI phải có tasks rỗng",
                projects.find { it.id == projectId2 }?.tasks?.isEmpty() == true)
        }

    /**
     * [M7] Không có task, không có project → projects là danh sách rỗng.
     */
    @Test
    fun `mergeProjectsWithTasks - no tasks and no projects - workspace has empty project list`() =
        runTest(testDispatcher) {
            println("\n=== [M7] no tasks + no projects → projects rỗng ===")

            stubNoWorkspace(tasks = emptyList(), userTags = emptyList())
            buildAndIdle()

            val projects = fakeViewModel.workspaces.value.first().projects
            println("[ASSERT] projects.size=${projects.size}")

            assertTrue("project list phải rỗng khi không có task và project", projects.isEmpty())
        }

    // =========================================================================
    // NHÓM 2: resolveWorkspaceId
    // =========================================================================

    /**
     * [R1] Tags của user có workspaceId → currentWorkspaceId được set đúng workspaceId đó.
     */
    @Test
    fun `resolveWorkspaceId - user tags contain workspaceId - currentWorkspaceId is set`() =
        runTest(testDispatcher) {
            println("\n=== [R1] tag có workspaceId → currentWorkspaceId được set ===")

            val wsTag = tag("Feature", workspaceId = workspaceId1)

            stubWithWorkspace(
                userTags = listOf(wsTag),
                workspaceTags = emptyList(),
                projects = emptyList(),
            )
            buildAndIdle()

            println("[ASSERT] currentWorkspaceId=${fakeViewModel.currentWorkspaceId.value}")

            assertEquals(
                "currentWorkspaceId phải là workspaceId1",
                workspaceId1,
                fakeViewModel.currentWorkspaceId.value,
            )
        }

    /**
     * [R2] Tags của user không có workspaceId nào → currentWorkspaceId là null.
     */
    @Test
    fun `resolveWorkspaceId - user tags have no workspaceId - currentWorkspaceId is null`() =
        runTest(testDispatcher) {
            println("\n=== [R2] tags không có workspaceId → currentWorkspaceId=null ===")

            val personalTag = tag("Urgent", workspaceId = null)
            stubNoWorkspace(userTags = listOf(personalTag))
            buildAndIdle()

            println("[ASSERT] currentWorkspaceId=${fakeViewModel.currentWorkspaceId.value}")

            assertNull(
                "currentWorkspaceId phải null khi tag không thuộc workspace nào",
                fakeViewModel.currentWorkspaceId.value,
            )
        }

    /**
     * [R3] Không có tag nào → currentWorkspaceId là null.
     */
    @Test
    fun `resolveWorkspaceId - no tags at all - currentWorkspaceId is null`() =
        runTest(testDispatcher) {
            println("\n=== [R3] không có tag nào → currentWorkspaceId=null ===")

            stubNoWorkspace(userTags = emptyList())
            buildAndIdle()

            assertNull("currentWorkspaceId phải null khi không có tag", fakeViewModel.currentWorkspaceId.value)
        }

    // =========================================================================
    // NHÓM 3: mergeTags
    // =========================================================================

    /**
     * [T1] Chỉ có user tags (không có workspace) → availableTags = user tags.
     */
    @Test
    fun `mergeTags - user tags only no workspace - availableTags equals user tags`() =
        runTest(testDispatcher) {
            println("\n=== [T1] chỉ user tags → availableTags = user tags ===")

            val t1 = tag("Urgent",  uuid = Uuid.random())
            val t2 = tag("Feature", uuid = Uuid.random())
            stubNoWorkspace(userTags = listOf(t1, t2))
            buildAndIdle()

            println("[ASSERT] availableTags=${fakeViewModel.availableTags.value.map { it.name }}")

            assertEquals("phải có 2 tags", 2, fakeViewModel.availableTags.value.size)
            assertTrue("Urgent phải có", fakeViewModel.availableTags.value.any { it.uuid == t1.uuid })
            assertTrue("Feature phải có", fakeViewModel.availableTags.value.any { it.uuid == t2.uuid })
        }

    /**
     * [T2] User tags + workspace tags, không trùng uuid → union cả hai.
     */
    @Test
    fun `mergeTags - user and workspace tags with no overlap - availableTags is union of both`() =
        runTest(testDispatcher) {
            println("\n=== [T2] user + workspace tags không trùng → union ===")

            val userTag = tag("Urgent",  uuid = Uuid.random(), workspaceId = null)
            val wsTag   = tag("Feature", uuid = Uuid.random(), workspaceId = workspaceId1)

            stubWithWorkspace(
                userTags = listOf(userTag),
                workspaceTags = listOf(wsTag),
            )
            buildAndIdle()

            println("[ASSERT] availableTags=${fakeViewModel.availableTags.value.map { it.name }}")

            assertEquals("phải có 2 tags (union)", 2, fakeViewModel.availableTags.value.size)
            assertTrue("user tag phải có", fakeViewModel.availableTags.value.any { it.uuid == userTag.uuid })
            assertTrue("ws tag phải có",   fakeViewModel.availableTags.value.any { it.uuid == wsTag.uuid })
        }

    /**
     * [T3] User tag và workspace tag có cùng uuid → workspace version ghi đè (dedup).
     * Hành vi của LinkedHashMap: key trùng → value sau thắng.
     * Logic: (primary + secondary).forEach → secondary = workspace → ghi đè primary = user.
     */
    @Test
    fun `mergeTags - user and workspace tags share uuid - workspace version overwrites user version`() =
        runTest(testDispatcher) {
            println("\n=== [T3] tag trùng uuid → workspace wins ===")

            val sharedUuid  = Uuid.random()
            val userTag     = tag("Urgent (personal)",  uuid = sharedUuid, workspaceId = null)
            val wsTag       = tag("Urgent (workspace)", uuid = sharedUuid, workspaceId = workspaceId1)

            stubWithWorkspace(
                userTags = listOf(userTag),
                workspaceTags = listOf(wsTag),
            )
            buildAndIdle()

            println("[ASSERT] availableTags=${fakeViewModel.availableTags.value.map { "${it.uuid}:${it.name}" }}")

            assertEquals("phải có đúng 1 tag sau dedup", 1, fakeViewModel.availableTags.value.size)
            assertEquals(
                "workspace version phải thắng (secondary ghi đè primary)",
                "Urgent (workspace)",
                fakeViewModel.availableTags.value.first().name,
            )
        }

    /**
     * [T4] Tạo tag mới thành công → tag được merge vào availableTags, không bị duplicate.
     * Khi uuid của tag mới không trùng với tag hiện có → kích thước tăng thêm 1.
     */
    @Test
    fun `mergeTags - after createTag succeeds - new tag merged without duplicates`() =
        runTest(testDispatcher) {
            println("\n=== [T4] createTag → tag được thêm vào availableTags, không duplicate ===")

            val existingTag = tag("Urgent")
            stubNoWorkspace(userTags = listOf(existingTag))
            buildAndIdle()

            assertEquals("ban đầu có 1 tag", 1, fakeViewModel.availableTags.value.size)

            val newTag = tag("Feature")
            coEvery { mockTagUseCases.createTag(any()) } returns Optional.of(newTag)

            fakeViewModel.createTag(CreateTagUiState(name = "Feature", label = BuiltinLabels[0], color = Color(-14575885))) {}
            advanceUntilIdle()

            println("[ASSERT] availableTags=${fakeViewModel.availableTags.value.map { it.name }}")

            assertEquals("phải có 2 tags sau khi tạo", 2, fakeViewModel.availableTags.value.size)
            assertTrue("tag mới phải có trong list",
                fakeViewModel.availableTags.value.any { it.uuid == newTag.uuid })
            // Gọi createTag lần nữa với cùng tag → kiểm tra không thêm duplicate
            coEvery { mockTagUseCases.createTag(any()) } returns Optional.of(newTag)
            fakeViewModel.createTag(CreateTagUiState(name = "Feature", label = BuiltinLabels[0], color = Color(-14575885))) {}
            advanceUntilIdle()

            // LinkedHashMap dedup: newTag.uuid giống → vẫn 2
            assertEquals("gọi lại với cùng uuid → không thêm duplicate", 2, fakeViewModel.availableTags.value.size)
        }

    /**
     * [T5] Xóa tag → tag bị xóa khỏi availableTags VÀ bị strip khỏi tasks.tags.
     */
    @Test
    fun `mergeTags - after deleteTag succeeds - tag removed from availableTags and stripped from tasks`() =
        runTest(testDispatcher) {
            println("\n=== [T5] deleteTag → xóa khỏi availableTags + strip khỏi tasks ===")

            val tagA = tag("Urgent",  uuid = Uuid.random())
            val tagB = tag("Feature", uuid = Uuid.random())
            val taskWithBothTags = Task(
                uuid = Uuid.random(), title = "Task có 2 tags",
                description = "", repetition = 0, type = TaskType.PROJECT,
                status = TaskStatus.TODO, priority = TaskPriority.MEDIUM,
                creator = null, tags = listOf(tagA, tagB),
                taskCompletionLog = emptyList(), startDate = null, dueDate = null, projectId = null,
            )

            stubNoWorkspace(tasks = listOf(taskWithBothTags), userTags = listOf(tagA, tagB))
            buildAndIdle()

            println("[BEFORE] availableTags=${fakeViewModel.availableTags.value.map { it.name }}")
            println("[BEFORE] task.tags=${fakeViewModel.tasks.value.first().tags.map { it.name }}")

            coEvery { mockTagUseCases.deleteTag(tagA.uuid) } returns Optional.of(tagA.uuid)
            fakeViewModel.deleteTag(tagA.uuid) {}
            advanceUntilIdle()

            val tagsAfter    = fakeViewModel.availableTags.value
            val taskTagsAfter = fakeViewModel.tasks.value.first().tags
            println("[AFTER] availableTags=${tagsAfter.map { it.name }}")
            println("[AFTER] task.tags=${taskTagsAfter.map { it.name }}")

            assertFalse("tagA phải bị xóa khỏi availableTags",
                tagsAfter.any { it.uuid == tagA.uuid })
            assertTrue("tagB phải vẫn còn trong availableTags",
                tagsAfter.any { it.uuid == tagB.uuid })
            assertFalse("tagA phải bị strip khỏi task",
                taskTagsAfter.any { it.uuid == tagA.uuid })
            assertTrue("tagB phải vẫn còn trong task",
                taskTagsAfter.any { it.uuid == tagB.uuid })
        }

    // =========================================================================
    // NHÓM 4: getTaskByDate + applyDateFilter (document bypass behavior)
    // =========================================================================

    /**
     * [D1] Gọi getTaskByDate → workspaces vẫn trả về TẤT CẢ tasks bất kể ngày.
     *
     * ⚠️ BUG DOCUMENTATION: filterWorkspacesByDate có tham số `test: Boolean = true`
     * hardcode khiến filter date KHÔNG BAO GIỜ thực sự lọc. Đây là hành vi HIỆN TẠI
     * (có thể là feature flag chưa tắt). Test này tài liệu hoá hành vi đó.
     */
    @Test
    fun `getTaskByDate - KNOWN BYPASS - all tasks visible regardless of date because test=true hardcoded`() =
        runTest(testDispatcher) {
            println("\n=== [D1] getTaskByDate bypass: test=true hardcoded → tất cả task hiện ===")

            // Task PROJECT chỉ match ngày trong range 2026-05-20..2026-05-22
            val taskInRange    = task("Trong range",   type = TaskType.PROJECT,
                startDate = LocalDate.of(2026, 5, 20), dueDate = LocalDate.of(2026, 5, 22))
            // Task này KHÔNG match ngày 2026-05-25 (ngoài range)
            val taskOutOfRange = task("Ngoài range",   type = TaskType.PROJECT,
                startDate = LocalDate.of(2026, 5, 1),  dueDate = LocalDate.of(2026, 5, 10))
            // HABIT luôn match
            val habitTask      = task("Tập thể dục",   type = TaskType.HABIT)

            stubNoWorkspace(tasks = listOf(taskInRange, taskOutOfRange, habitTask))
            buildAndIdle()

            // Lọc theo ngày 2026-05-25 (taskOutOfRange KHÔNG nên có nếu filter hoạt động)
            val cal = Calendar.getInstance().apply {
                set(2026, Calendar.MAY, 25)
            }

            // refresh được gọi bên trong getTaskByDate → stub lại
            coEvery { mockTaskUseCases.getTask(fakeUserId) } returns
                    listOf(taskInRange, taskOutOfRange, habitTask)

            fakeViewModel.getTaskByDate(cal)
            advanceUntilIdle()

            val allTasksInWorkspace = fakeViewModel.workspaces.value
                .flatMap { it.projects }
                .flatMap { it.tasks }

            println("[ASSERT] tasks trong workspaces: ${
                allTasksInWorkspace.map { task -> task.title to task.status }
            }")

            // Hành vi THỰC TẾ (bypass): cả 3 task đều xuất hiện
            assertEquals(
                "⚠️  Do bypass test=true: tất cả 3 tasks phải xuất hiện dù đã filter ngày",
                3,
                allTasksInWorkspace.size,
            )
        }

    /**
     * [D2] Gọi getTaskByDate → _selectedDate được cập nhật, ảnh hưởng đến markTaskDone.
     * markTaskDone dùng _selectedDate.value.toLocalDate() làm targetDate gửi lên server.
     */
    @Test
    fun `getTaskByDate - selectedDate is updated - markTaskDone uses the new date`() =
        runTest(testDispatcher) {
            println("\n=== [D2] getTaskByDate cập nhật selectedDate → markTaskDone dùng ngày đó ===")

            var habitTask = task("Chạy bộ buổi sáng", type = TaskType.HABIT)

            stubNoWorkspace(tasks = listOf(habitTask))
            buildAndIdle()

            val targetDate = LocalDate.of(2026, 5, 23)
            val targetCalendar = Calendar.getInstance().apply { set(2026, Calendar.MAY, 23) }

            fakeViewModel.getTaskByDate(targetCalendar)
            advanceUntilIdle()

            var allTasksInWorkspace = fakeViewModel.workspaces.value.flatMap { it.projects }.flatMap { it.tasks }
            println("[ASSERT] tasks trong workspaces detail\n: ${allTasksInWorkspace.map { it }}")

            println("[ASSERT] tasks trong workspaces before mark done: ${
                allTasksInWorkspace.map { task -> task.title to task.status }
            }")

            val fakeLog = TaskCompletionLog(
                taskCompletionId = Uuid.random(),
                status = TaskStatus.DONE,
                date = targetDate,
                completedAt = LocalDateTime.now(),
                task = habitTask.uuid,
                user = fakeUserId,
            )
            coEvery { mockMarkTaskDone(any(), any(), any()) } returns Optional.of(fakeLog)
            val doneTask = habitTask.copy(taskCompletionLog = listOf(fakeLog))
            stubNoWorkspace(tasks = listOf(doneTask))
            buildAndIdle()

            fakeViewModel.markTaskDone(habitTask) {}
            advanceUntilIdle()
            allTasksInWorkspace = fakeViewModel.getTaskByDate(targetCalendar)
                .flatMap { it.projects }.flatMap { it.tasks }
            advanceUntilIdle()

            allTasksInWorkspace = fakeViewModel.workspaces.value.flatMap { it.projects }.flatMap { it.tasks }

            println("[ASSERT] tasks trong workspaces detail\n: ${allTasksInWorkspace.map { it }}")
            println("[ASSERT] tasks trong workspaces: ${
                allTasksInWorkspace.map { task -> task.title to task.status }
            }")

            val targetTask = allTasksInWorkspace.filter { it.uuid == habitTask.uuid }

            // Kiểm tra xem task đó đã chuyển sang DONE chưa
            assertEquals(
                "markTaskDone phải dùng ngày đã set qua getTaskByDate",
                TaskStatus.DONE,
                targetTask[0].status
            )
        }

    // =========================================================================
    // NHÓM 5: createTag ownershipType
    // =========================================================================

    /**
     * [O1] Không có workspace context (currentWorkspaceId=null) → tag tạo ra có ownershipType=PERSONAL.
     */
    @Test
    fun `createTag ownership - no workspace context - tag created with PERSONAL ownershipType`() =
        runTest(testDispatcher) {
            println("\n=== [O1] no workspace → PERSONAL tag ===")

            stubNoWorkspace(userTags = emptyList()) // → workspaceId = null
            buildAndIdle()

            assertNull("currentWorkspaceId phải null", fakeViewModel.currentWorkspaceId.value)

            // Capture tag được truyền vào createTag usecase
            val capturedTagSlot = slot<Tag>()
            coEvery { mockTagUseCases.createTag(capture(capturedTagSlot)) } answers {
                Optional.of(capturedTagSlot.captured)
            }

            fakeViewModel.createTag(CreateTagUiState(name = "Urgent", label = BuiltinLabels[0], color = Color(-10453621))) {}
            advanceUntilIdle()

            val createdTag = capturedTagSlot.captured
            println("[ASSERT] ownershipType=${createdTag.ownershipType} | workspaceId=${createdTag.workspaceId}")

            assertEquals(
                "tag phải có ownershipType=PERSONAL khi không có workspace",
                TagOwnershipType.PERSONAL,
                createdTag.ownershipType,
            )
            assertNull("tag phải có workspaceId=null khi PERSONAL", createdTag.workspaceId)
        }

    /**
     * [O2] Có workspace context (tags user có workspaceId) → tag tạo ra có ownershipType=WORKSPACE.
     */
    @Test
    fun `createTag ownership - workspace context active - tag created with WORKSPACE ownershipType`() =
        runTest(testDispatcher) {
            println("\n=== [O2] có workspace context → WORKSPACE tag ===")

            val wsTag = tag("Existing", workspaceId = workspaceId1)
            stubWithWorkspace(
                userTags = listOf(wsTag),
                workspaceTags = emptyList(),
                projects = emptyList(),
            )
            buildAndIdle()

            assertEquals(
                "currentWorkspaceId phải được set",
                workspaceId1,
                fakeViewModel.currentWorkspaceId.value,
            )

            val capturedTagSlot = slot<Tag>()
            coEvery { mockTagUseCases.createTag(capture(capturedTagSlot)) } answers {
                Optional.of(capturedTagSlot.captured)
            }

            fakeViewModel.createTag(CreateTagUiState(name = "Feature", label = BuiltinLabels[0], color = Color(-14575885))) {}
            advanceUntilIdle()

            val createdTag = capturedTagSlot.captured
            println("[ASSERT] ownershipType=${createdTag.ownershipType} | workspaceId=${createdTag.workspaceId}")

            assertEquals(
                "tag phải có ownershipType=WORKSPACE khi có workspace context",
                TagOwnershipType.WORKSPACE,
                createdTag.ownershipType,
            )
            assertEquals(
                "tag phải có workspaceId đúng",
                workspaceId1,
                createdTag.workspaceId,
            )
        }

    // =========================================================================
    // NHÓM 6: uiEvent channel
    // =========================================================================

    /**
     * Helper: collect N events từ uiEvent channel trong scope hiện tại.
     */
    private fun TestScope.collectEvents(count: Int): List<TaskUiEvent> {
        val collected = mutableListOf<TaskUiEvent>()
        val job = launch {
            fakeViewModel.uiEvent.collect { event ->
                collected.add(event)
                if (collected.size >= count) return@collect
            }
        }
        return collected.also { job.cancel() }
    }

    /**
     * [E1] createTask thành công → emit TaskCreated + ShowToast với title.
     */
    @Test
    fun `uiEvent - createTask success - emits TaskCreated then ShowToast with title`() =
        runTest(testDispatcher) {
            println("\n=== [E1] createTask success → TaskCreated + ShowToast ===")

            stubNoWorkspace()
            buildAndIdle()

            val draft = fakeViewModel.createTaskDraft().copy(title = "Học Unit Test")
            coEvery { mockTaskUseCases.createTask(any(), any()) } returns Optional.of(draft)

            val events = mutableListOf<TaskUiEvent>()
            val collectJob = launch { fakeViewModel.uiEvent.collect { events.add(it) } }

            fakeViewModel.createTask(draft) {}
            advanceUntilIdle()
            collectJob.cancel()

            println("[ASSERT] events=${events.map { it::class.simpleName }}")

            assertTrue("TaskCreated phải được emit",
                events.any { it is TaskUiEvent.TaskCreated })
            assertTrue("ShowToast với title phải được emit",
                events.filterIsInstance<TaskUiEvent.ShowToast>()
                    .any { it.message.contains(draft.title) })
        }

    /**
     * [E2] updateTask thành công → emit TaskUpdated + ShowToast.
     */
    @Test
    fun `uiEvent - updateTask success - emits TaskUpdated then ShowToast`() =
        runTest(testDispatcher) {
            println("\n=== [E2] updateTask success → TaskUpdated + ShowToast ===")

            val existingTask = task("Task gốc")
            stubNoWorkspace(tasks = listOf(existingTask))
            buildAndIdle()

            val updatedTask = existingTask.copy(title = "Task đã update")
            coEvery { mockTaskUseCases.updateTask(any()) } returns Optional.of(updatedTask)

            val events = mutableListOf<TaskUiEvent>()
            val collectJob = launch { fakeViewModel.uiEvent.collect { events.add(it) } }

            fakeViewModel.updateTask(updatedTask) {}
            advanceUntilIdle()
            collectJob.cancel()

            println("[ASSERT] events=${events.map { it::class.simpleName }}")

            assertTrue("TaskUpdated phải được emit",
                events.any { it is TaskUiEvent.TaskUpdated })
            assertTrue("ShowToast phải được emit",
                events.any { it is TaskUiEvent.ShowToast })
        }

    /**
     * [E3] deleteTask thành công → emit TaskDeleted + ShowToast.
     */
    @Test
    fun `uiEvent - deleteTask success - emits TaskDeleted then ShowToast`() =
        runTest(testDispatcher) {
            println("\n=== [E3] deleteTask success → TaskDeleted + ShowToast ===")

            val taskToDelete = task("Task cần xóa")
            stubNoWorkspace(tasks = listOf(taskToDelete))
            buildAndIdle()

            coEvery { mockTaskUseCases.deleteTask(any()) } returns Optional.of(taskToDelete.uuid)

            val events = mutableListOf<TaskUiEvent>()
            val collectJob = launch { fakeViewModel.uiEvent.collect { events.add(it) } }

            fakeViewModel.deleteTask(taskToDelete) {}
            advanceUntilIdle()
            collectJob.cancel()

            println("[ASSERT] events=${events.map { it::class.simpleName }}")

            assertTrue("TaskDeleted phải được emit",
                events.any { it is TaskUiEvent.TaskDeleted })
            assertTrue("ShowToast phải được emit",
                events.any { it is TaskUiEvent.ShowToast })
        }

    /**
     * [E4] createTag thành công → emit TagCreated + ShowToast với tên tag.
     */
    @Test
    fun `uiEvent - createTag success - emits TagCreated then ShowToast with tag name`() =
        runTest(testDispatcher) {
            println("\n=== [E4] createTag success → TagCreated + ShowToast ===")

            stubNoWorkspace()
            buildAndIdle()

            val newTag = tag("Urgent")
            coEvery { mockTagUseCases.createTag(any()) } returns Optional.of(newTag)

            val events = mutableListOf<TaskUiEvent>()
            val collectJob = launch { fakeViewModel.uiEvent.collect { events.add(it) } }

            fakeViewModel.createTag(CreateTagUiState(name = "Urgent", label = BuiltinLabels[0], color = Color(-10453621))) {}
            advanceUntilIdle()
            collectJob.cancel()

            println("[ASSERT] events=${events.map { it::class.simpleName }}")
            val toasts = events.filterIsInstance<TaskUiEvent.ShowToast>()
            println("[ASSERT] toast messages=${toasts.map { it.message }}")

            assertTrue("TagCreated phải được emit",
                events.any { it is TaskUiEvent.TagCreated })
            assertTrue("ShowToast phải chứa tên tag",
                toasts.any { it.message.contains("Urgent") })
        }

    /**
     * [E5] createTask thất bại (empty Optional) → chỉ emit ShowToast failure, KHÔNG emit TaskCreated.
     */
    @Test
    fun `uiEvent - createTask failure - emits only ShowToast failure without TaskCreated`() =
        runTest(testDispatcher) {
            println("\n=== [E5] createTask fail → chỉ ShowToast, không có TaskCreated ===")

            stubNoWorkspace()
            buildAndIdle()

            val draft = fakeViewModel.createTaskDraft().copy(title = "Task lỗi")
            coEvery { mockTaskUseCases.createTask(any(), any()) } returns Optional.empty()

            val events = mutableListOf<TaskUiEvent>()
            val collectJob = launch { fakeViewModel.uiEvent.collect { events.add(it) } }

            fakeViewModel.createTask(draft) {}
            advanceUntilIdle()
            collectJob.cancel()

            println("[ASSERT] events=${events.map { it::class.simpleName }}")

            assertFalse("TaskCreated KHÔNG được emit khi thất bại",
                events.any { it is TaskUiEvent.TaskCreated })
            assertTrue("ShowToast failure phải được emit",
                events.filterIsInstance<TaskUiEvent.ShowToast>()
                    .any { it.message.contains("Failed", ignoreCase = true) ||
                            it.message.contains("fail",   ignoreCase = true) })
        }

    /**
     * [E6] loadInitialData gặp network error → emit ShowToast chứa "Network error".
     */
    @Test
    fun `uiEvent - network error on init - emits ShowToast with network error message`() =
        runTest(testDispatcher) {
            println("\n=== [E6] network error on init → ShowToast network error ===")

            every { mockAuthPreferences.userId } returns flowOf(fakeUserId.toString())
            coEvery { mockTaskUseCases.getTask(any()) } throws Exception("Connection timeout")

            val events = mutableListOf<TaskUiEvent>()

            fakeViewModel = TaskManagementViewModel(
                mockTaskUseCases, mockTagUseCases, mockWorkspaceUseCases,
                mockAuthPreferences, mockMarkTaskDone, mockMarkTaskWontDo,
            )
            val collectJob = launch { fakeViewModel.uiEvent.collect { events.add(it) } }
            advanceUntilIdle()
            collectJob.cancel()

            println("[ASSERT] events=${events.filterIsInstance<TaskUiEvent.ShowToast>().map { it.message }}")

            assertTrue("ShowToast chứa 'Network error' phải được emit",
                events.filterIsInstance<TaskUiEvent.ShowToast>()
                    .any { it.message.contains("Network error", ignoreCase = true) })
        }

    // =========================================================================
    // NHÓM 7: createTaskDraft
    // =========================================================================

    /**
     * [DR1] Không truyền tham số → draft có HABIT, TODO, MEDIUM, title rỗng, ngày null.
     */
    @Test
    fun `createTaskDraft - no arguments - returns HABIT draft with correct defaults`() =
        runTest(testDispatcher) {
            println("\n=== [DR1] createTaskDraft() defaults ===")

            stubNoWorkspace()
            buildAndIdle()

            val draft = fakeViewModel.createTaskDraft()
            println("[ASSERT] type=${draft.type} | status=${draft.status} | priority=${draft.priority}")
            println("[ASSERT] title='${draft.title}' | startDate=${draft.startDate} | dueDate=${draft.dueDate}")

            assertEquals("type phải là HABIT",      TaskType.HABIT,       draft.type)
            assertEquals("status phải là TODO",     TaskStatus.TODO,      draft.status)
            assertEquals("priority phải là MEDIUM", TaskPriority.MEDIUM,  draft.priority)
            assertEquals("title phải rỗng",         "",                   draft.title)
            assertTrue("tags phải rỗng",            draft.tags.isEmpty())
            assertTrue("logs phải rỗng",            draft.taskCompletionLog.isEmpty())
            assertNull("startDate phải null",        draft.startDate)
            assertNull("dueDate phải null",          draft.dueDate)
            assertNull("projectId phải null",        draft.projectId)
        }

    /**
     * [DR2] Truyền TaskType.PROJECT → draft có type=PROJECT.
     */
    @Test
    fun `createTaskDraft - PROJECT type - returns PROJECT draft`() =
        runTest(testDispatcher) {
            println("\n=== [DR2] createTaskDraft(PROJECT) ===")

            stubNoWorkspace()
            buildAndIdle()

            val draft = fakeViewModel.createTaskDraft(TaskType.PROJECT)
            println("[ASSERT] type=${draft.type}")

            assertEquals("type phải là PROJECT", TaskType.PROJECT, draft.type)
        }

    /**
     * [DR3] Mỗi lần gọi → uuid khác nhau (random).
     */
    @Test
    fun `createTaskDraft - called twice - returns drafts with different uuids`() =
        runTest(testDispatcher) {
            println("\n=== [DR3] createTaskDraft() × 2 → uuid khác nhau ===")

            stubNoWorkspace()
            buildAndIdle()

            val draft1 = fakeViewModel.createTaskDraft()
            val draft2 = fakeViewModel.createTaskDraft()
            println("[ASSERT] uuid1=${draft1.uuid} | uuid2=${draft2.uuid}")

            assertNotEquals("hai draft phải có uuid khác nhau", draft1.uuid, draft2.uuid)
        }
}