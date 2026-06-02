@file:OptIn(ExperimentalUuidApi::class)

package com.example.se405.android.features.tasks_management.domain.validation

import com.example.se405.android.features.tasks_management.domain.entity.Tag
import com.example.se405.android.features.tasks_management.domain.entity.TagOwnershipType
import com.example.se405.android.features.tasks_management.domain.entity.Task
import com.example.se405.android.features.tasks_management.domain.entity.TaskType
import com.example.se405.android.features.tasks_management.domain.entity.WorkspaceMember
import com.example.se405.android.features.users_management.domain.entity.User
import com.example.se405.android.features.workspaces_management.domain.entity.Workspace
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Single source of truth for Create / Edit / Update task business rules.
 *
 * Both the presentation layer (to decide which tags/assignees are *selectable*)
 * and [com.example.se405.android.features.tasks_management.presentation.viewmodel.TaskManagementViewModel]
 * (to *enforce* the rules before a use-case mutation) delegate here, so create
 * and edit can never drift apart.
 *
 * Rules enforced (derived from `schema.graphqls` `CreateTaskInput`/`UpdateTaskInput`
 * and the workspace/tag ownership model):
 *  1. PROJECT tasks require a valid project (`projectId` is required when
 *     `type = PROJECT`) and may only be assigned to members of that project's
 *     workspace.
 *  2. Tags have ownership separation: HABIT tasks may only use PERSONAL tags,
 *     PROJECT tasks may only use WORKSPACE tags belonging to the task's workspace.
 */
object TaskFormRules {

    const val MAX_TAGS = 3

    // ── Tag ownership ────────────────────────────────────────────────────────

    /**
     * Tags that may be *selected* for a task of [taskType] scoped to [workspaceId]
     * (the workspace of the currently selected project for PROJECT tasks, or `null`
     * for HABIT / personal tasks). When [workspaceId] is `null` for a PROJECT task
     * (no project chosen yet) every workspace tag is offered; the concrete scope is
     * applied once a project is picked, and again on submit.
     */
    fun availableTags(taskType: TaskType, workspaceId: Uuid?, allTags: List<Tag>): List<Tag> =
        allTags.filter { isTagAllowed(it, taskType, workspaceId) }

    fun isTagAllowed(tag: Tag, taskType: TaskType, workspaceId: Uuid?): Boolean =
        when (taskType) {
            TaskType.HABIT -> tag.ownershipType == TagOwnershipType.PERSONAL
            TaskType.PROJECT ->
                tag.ownershipType == TagOwnershipType.WORKSPACE &&
                    (workspaceId == null || tag.workspaceId == workspaceId)
        }

    /** Drops any already-selected tag that is not valid for the given scope. */
    fun sanitizeTags(selected: List<Tag>, taskType: TaskType, workspaceId: Uuid?): List<Tag> =
        selected.filter { isTagAllowed(it, taskType, workspaceId) }

    // ── Assignee restriction ─────────────────────────────────────────────────

    fun isAssigneeAllowed(userId: Uuid, validMembers: List<WorkspaceMember>): Boolean =
        validMembers.any { it.userId == userId }

    /** Drops any assignee that is not a member of the project's workspace. */
    fun sanitizeAssignees(assignees: List<User>, validMembers: List<WorkspaceMember>): List<User> =
        assignees.filter { isAssigneeAllowed(it.uuid, validMembers) }

    // ── Workspace lookups (project ↔ workspace ↔ members) ────────────────────

    fun workspaceIdForProject(workspaces: List<Workspace>, projectId: Uuid?): Uuid? {
        if (projectId == null) return null
        return workspaces.firstOrNull { ws -> ws.projects.any { it.id == projectId } }?.id
    }

    fun membersForWorkspace(workspaces: List<Workspace>, workspaceId: Uuid?): List<WorkspaceMember> {
        if (workspaceId == null) return emptyList()
        return workspaces.firstOrNull { it.id == workspaceId }?.members ?: emptyList()
    }

    fun membersForProject(workspaces: List<Workspace>, projectId: Uuid?): List<WorkspaceMember> =
        membersForWorkspace(workspaces, workspaceIdForProject(workspaces, projectId))

    // ── Submit-time enforcement ──────────────────────────────────────────────

    /**
     * Hard validation applied to both create and update before hitting the
     * use-cases. Returns a user-facing error message, or `null` when the task is
     * valid. Soft normalization (clearing project/assignee for HABIT, sanitizing
     * tags) is done by [normalizeForSubmit]; this only blocks the cases the
     * backend contract rejects or that violate ownership.
     */
    fun validateForSubmit(
        task: Task,
        knownProjectIds: Set<Uuid>,
        validMembers: List<WorkspaceMember>,
    ): String? {
        if (task.title.isBlank()) return "Title cannot be empty"
        if (task.type == TaskType.PROJECT) {
            val projectId = task.projectId ?: return "Please select a project for this task"
            if (knownProjectIds.isNotEmpty() && projectId !in knownProjectIds) {
                return "Selected project is not valid"
            }
            val invalidAssignee = task.assignees.firstOrNull { !isAssigneeAllowed(it.uuid, validMembers) }
            if (invalidAssignee != null) {
                return "Assignee must be a member of the project's workspace"
            }
        }
        return null
    }

    /**
     * Normalizes a task so the payload always satisfies ownership rules regardless
     * of stale UI state:
     *  - HABIT → no project, no assignees, PERSONAL tags only.
     *  - PROJECT → WORKSPACE tags of the task's workspace, assignees restricted to
     *    that workspace's members.
     */
    fun normalizeForSubmit(
        task: Task,
        workspaceId: Uuid?,
        validMembers: List<WorkspaceMember>,
    ): Task = when (task.type) {
        TaskType.HABIT -> task.copy(
            projectId = null,
            assignees = emptyList(),
            tags = sanitizeTags(task.tags, TaskType.HABIT, null),
        )
        TaskType.PROJECT -> task.copy(
            tags = sanitizeTags(task.tags, TaskType.PROJECT, workspaceId),
            assignees = sanitizeAssignees(task.assignees, validMembers),
        )
    }
}
