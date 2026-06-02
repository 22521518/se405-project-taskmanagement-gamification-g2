@file:OptIn(ExperimentalUuidApi::class)

package com.example.se405.android.features.workspaces_management

import com.example.se405.android.core.authentication.data.AuthPreferences
import com.example.se405.android.core.onLogin
import com.example.se405.android.features.tasks_management.domain.entity.TaskType
import com.example.se405.android.features.tasks_management.domain.entity.WorkspaceMember
import com.example.se405.android.features.tasks_management.utils.generateHabitTasks
import com.example.se405.android.features.tasks_management.utils.generateTags
import com.example.se405.android.features.workspaces_management.domain.entity.Workspace
import com.example.se405.android.features.workspaces_management.domain.use_case.ExtWorkspaceWithGetCreateUseCases
import com.example.se405.android.graphql.GetUsersExcludingQuery
import com.example.se405.android.graphql.type.WorkspaceRole
import kotlinx.coroutines.flow.firstOrNull
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.Koin
import org.koin.core.context.GlobalContext
import org.robolectric.RobolectricTestRunner
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid


private fun String?.toUuidOrNull(): Uuid? {
    if (this.isNullOrBlank()) return null
    return runCatching { Uuid.parse(this) }.getOrNull()
}

@RunWith(RobolectricTestRunner::class)
class WorkspaceManagementUseCaseTest {

    // ── Helpers ──────────────────────────────────────────────────────────────
    private fun baseTestLayout(name: String, block: suspend (Koin, Uuid) -> Unit) {
        println("=== test_$name ===")
        onLogin(
            runWithAuthentication = { koin, _ ->
                val userId = koin.get<AuthPreferences>().userId.firstOrNull()?.toUuidOrNull()
                if (userId != null) block(koin, userId)
                else println("Lỗi: Không tìm thấy UserId hợp lệ hoặc UserId không đúng định dạng UUID")
            },
            onFailed = { println("Info FAILURE: ${it.message}") }
        )
        println()
    }
    private fun withFirstWorkspace(
        name: String,
        block: suspend (useCases: ExtWorkspaceWithGetCreateUseCases, workspaceId: Uuid, workspace: Workspace?) -> Unit
    ) = baseTestLayout(name) { koin, userId ->
        val useCases = koin.get<ExtWorkspaceWithGetCreateUseCases>()
        val workspaces = useCases.getWorkspaceListByUserId(userId)
        if (workspaces.isEmpty()) println("Không tìm thấy workspace nào")
        else block(useCases, workspaces.first().id, workspaces.first())
    }

    private fun withFirstProjectInFirstWorkspace(
        name: String,
        block: suspend (useCases: ExtWorkspaceWithGetCreateUseCases, projectId: Uuid, workspaceId: Uuid) -> Unit
    ) = withFirstWorkspace(name) { useCases, wsId, _ ->
        val projects = useCases.getProjectsByWorkspace(wsId)
        if (projects.isEmpty()) println("Không tìm thấy project nào")
        else block(useCases, projects.first().id, wsId)
    }

    private fun withUsersNotInFirstWorkspace(
        name: String,
        block: suspend (useCases: ExtWorkspaceWithGetCreateUseCases, currentUserId: Uuid, workspaceId: Uuid, others: List<GetUsersExcludingQuery.GetUsersExcluding>) -> Unit
    ) = baseTestLayout(name) { koin, userId ->
        val useCases = koin.get<ExtWorkspaceWithGetCreateUseCases>()
        val workspaces = useCases.getWorkspaceListByUserId(userId)
        if (workspaces.isEmpty()) println("Không tìm thấy workspace nào")
        else {
            val firstWS = workspaces.first()
            val memberIds = firstWS.members.map { it.userId.toString() }
            val other = useCases.getUsersExcluding(userId).filter { user -> user.uuid !in memberIds }
            for (member in other) {
                println("[withUsersNotInFirstWorkspace] not Member: $member")
            }
            block(useCases, userId, firstWS.id, other)
        }
    }

    private fun withUserNotInFirstProjectInFirstWorkspace(
        name: String,
        block: suspend (useCases: ExtWorkspaceWithGetCreateUseCases, currentUserId: Uuid, projectId: Uuid, others: List<WorkspaceMember>) -> Unit
    ) = baseTestLayout(name) { koin, userId ->
        val useCases = koin.get<ExtWorkspaceWithGetCreateUseCases>()
        val workspaces = useCases.getWorkspaceListByUserId(userId)

        val firstWS = workspaces.firstOrNull()
        if (firstWS == null) {
            println("[-] Không tìm thấy workspace nào")
            return@baseTestLayout
        }
        val firstProject = firstWS.projects?.firstOrNull()
        if (firstProject == null) {
            println("[-] Workspace '${firstWS.name}' không có project nào")
            return@baseTestLayout
        }

        val allWorkspaceMembers = useCases.getMembersByWorkspace(firstWS.id)
        val projectMembers = useCases.getMemberByProject(firstProject.id)
        val projectMemberIds = projectMembers.map { it.userId }
        val others = allWorkspaceMembers.filter { member -> member.userId !in projectMemberIds }
        for (member in others) {
            println("[withUserNotInFirstProjectInFirstWorkspace] Not Member: $member")
        }
        block(useCases, userId, firstProject.id, others)
    }


    // ── GET tests ────────────────────────────────────────────────────────────

    @Test
    fun test_getWorkspaceListByUserId() = baseTestLayout("getWorkspaceListByUserId") { koin, userId ->
        val workspaces = koin.get<ExtWorkspaceWithGetCreateUseCases>().getWorkspaceListByUserId(userId)
        println("Số lượng workspace lấy được: ${workspaces.size}")
        workspaces.forEach { println("Workspace: $it") }
    }

    @Test
    fun test_getWorkspaceByWorkspaceId() = withFirstWorkspace("getWorkspaceByWorkspaceId") { useCases, wsId, _ ->
        println("Workspace: ${useCases.getWorkspaceByWorkspaceId(workspaceId = wsId)}")
    }

    @Test
    fun test_getWorkspaceActivity() = withFirstWorkspace("getWorkspaceActivity") { useCases, wsId, _ ->
        val activities = useCases.getWorkspaceActivity(workspaceId = wsId)
        println("Số lượng activity: ${activities.content.size}")
        activities.content.forEach { println("Activity: $it") }
    }

    @Test
    fun test_getMembersByWorkspace() = withFirstWorkspace("getMembersByWorkspace") { useCases, wsId, _ ->
        val members = useCases.getMembersByWorkspace(workspaceId = wsId)
        println("Số lượng member: ${members.size}")
        members.forEach { println("Member: $it") }
    }

    @Test
    fun test_getProjectsByWorkspace() = withFirstWorkspace("getProjectsByWorkspace") { useCases, wsId, _ ->
        println("Projects: ${useCases.getProjectsByWorkspace(workspaceId = wsId)}")
    }

    @Test
    fun test_getProjectByProjectId() = withFirstProjectInFirstWorkspace("getProjectByProjectId") { useCases, proId, _ ->
        println("Project Detail: ${useCases.getProjectByProjectId(projectId = proId)}")
    }

    @Test
    fun test_getTagsByWorkspace() = withFirstWorkspace("getTagsByWorkspace") { useCases, wsId, _ ->
        val tags = useCases.getTagsByWorkspace(workspaceId = wsId)

        println("Số lượng tag: ${tags.size}")
        tags.forEach { println("Tag: $it") }
    }

    @Test
    fun test_getUserExcluded() = baseTestLayout("getUserExcluded") { koin, userId ->
        val users = koin.get<ExtWorkspaceWithGetCreateUseCases>().getUsersExcluding(userId)
        println("Số lượng user: ${users.size}")
        users.forEach { println("User: $it") }
    }

    // ── CREATE tests ─────────────────────────────────────────────────────────

    @Test
    fun test_createWorkspace() = baseTestLayout("createWorkspace") { koin, userId ->
        val workspace = koin.get<ExtWorkspaceWithGetCreateUseCases>().createWorkspace(
            userId = userId,
            workspaceName = "Workspace Test ${System.currentTimeMillis()}"
        )
        println("Workspace được tạo: $workspace")
    }

    @Test
    fun test_createProject() = withFirstWorkspace("createProject") { useCases, wsId, _ ->
        val authPreferences = GlobalContext.get().get<AuthPreferences>()
        val userId = authPreferences.userId.firstOrNull()?.toUuidOrNull()

        if (userId != null) {
            val projectName = "Project Test ${System.currentTimeMillis()}"
            val projectOptional = useCases.createProject(
                userId = userId,
                workspaceId = wsId,
                projectName = projectName
            )
            println("Kết quả tạo Project (Optional): $projectOptional")
        } else {
            println("Lỗi: Không tìm thấy UserId để tạo Project")
        }
    }

    @Test
    fun test_createTag() = withFirstWorkspace("createTag") { useCases, wsId, _ ->
        val authPreferences = GlobalContext.get().get<AuthPreferences>()
        val creatorId = authPreferences.userId.firstOrNull()?.toUuidOrNull()
        if (creatorId == null) {
            println("Không tìm thấy creatorId")
            return@withFirstWorkspace
        }

        val tag = generateTags(1, userId = creatorId, workspaceId = wsId).first()
        val createdTag = useCases.createTag(tag)

        println("Tag được tạo: $createdTag")
    }

    @Test
    fun test_createTask() = withFirstProjectInFirstWorkspace("createTask") { useCases, projectId, wsId ->

        val authPreferences = GlobalContext.get().get<AuthPreferences>()
        val creatorId = authPreferences.userId.firstOrNull()?.toUuidOrNull()
        if (creatorId == null) {
            println("Không tìm thấy creatorId")
            return@withFirstProjectInFirstWorkspace
        }

        val projects = useCases.getProjectsByWorkspace(wsId)

        if (projects.isEmpty()) {
            println("Workspace không có project nào")
            return@withFirstProjectInFirstWorkspace
        }
        val tags = useCases.getTagsByWorkspace(wsId)
        val task = generateHabitTasks(1, type = TaskType.PROJECT, projectId = projectId, tags = tags.take(2)).first()
        val createdTask = useCases.createTask(task = task, creatorId = creatorId)

        println("Task được tạo: $createdTask")
    }

    @Test
    fun test_addWorkspaceMember() = withUsersNotInFirstWorkspace("addWorkspaceMember") { useCases, currentUserId, wsId, others ->
        val addMemberToWorkspace = useCases.addMemberToWorkspace(workspaceId = wsId, userId = others.first().uuid.toUuidOrNull()!!,)
        println("Kết quả thêm thành viên vào workspace: $addMemberToWorkspace")
    }

    @Test
    fun test_addProjectMember() = withUserNotInFirstProjectInFirstWorkspace("addProjectMember") { useCases, _, projectId, others ->
        if (others.isEmpty()) {
            println("Không có thành viên nào trong workspace chưa tham gia project này (tất cả đã là member).")
            return@withUserNotInFirstProjectInFirstWorkspace
        }
        val addMemberToWorkspace = useCases.addMemberToProject(projectId = projectId, userId = others.first().userId)
        println("Kết quả thêm thành viên vào project: $addMemberToWorkspace")
    }

    @Test
    fun test_addWorkspaceMembers() = withUsersNotInFirstWorkspace("addWorkspaceMembers") { useCases, currentUserId, wsId, others ->
        val userIds = others.mapNotNull { it.uuid.toUuidOrNull() }
        val addMembersToWorkspace = useCases.addMembersToWorkspace(
            workspaceId = wsId,
            userIds = userIds.take(5)
        )
        println("Kết quả thêm thành viên vào workspace: $addMembersToWorkspace")
    }

    @Test
    fun test_addProjectMembers() = withUserNotInFirstProjectInFirstWorkspace("addProjectMembers") { useCases, _, projectId, others ->
        if (others.isEmpty()) {
            println("Không có thành viên nào trong workspace chưa tham gia project này (tất cả đã là member).")
            return@withUserNotInFirstProjectInFirstWorkspace
        }
        val userIds = others.map { it.userId }
        val addMembersToProject = useCases.addMembersToProject(
            projectId = projectId,
            userIds = userIds.take(3)
        )
        println("Kết quả thêm thành viên vào project: $addMembersToProject")
    }

//    fun findAnotherMemberId(workspaceId: Uuid, currentUserId: Uuid, runWithWSMember: (List<WorkspaceMember>) -> Unit) = withFirstWorkspace("findAnotherMemberId") { useCases, wsId, _ ->
//        runWithWSMember(useCases.getMembersByWorkspace(workspaceId).filter { it.userId != currentUserId })
//    }
}