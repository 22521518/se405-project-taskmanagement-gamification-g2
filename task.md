# Task: Đồng bộ Entity Frontend ↔ Backend

## Backend — JPA Entities
- [x] MODIFY `TaskEntity.kt` — startDate/dueDate nullable, thêm @ManyToMany tags, @OneToMany completionLogs
- [x] MODIFY `TagEntity.kt` — thêm createdBy, labelId, createdAt, updatedAt, mappedBy tasks
- [x] NEW `WorkspaceEntity.kt`
- [x] NEW `ProjectEntity.kt`
- [x] NEW `WorkspaceMemberEntity.kt`
- [x] NEW `TaskCompletionLogEntity.kt`
- [x] NEW `HabitLabelEntity.kt`
- [x] NEW `UserEntity.kt` (fake user auto-create)

## Backend — Repositories
- [x] NEW `WorkspaceRepository.kt`
- [x] NEW `ProjectRepository.kt`
- [x] NEW `WorkspaceMemberRepository.kt`
- [x] NEW `TaskCompletionLogRepository.kt`
- [x] NEW `HabitLabelRepository.kt`
- [x] NEW `UserRepository.kt`

## Backend — Controllers
- [x] NEW `WorkspaceController.kt`
- [x] MODIFY `TaskController.kt` — thêm projectId, tagIds trong inputs; resolve tags/completionLogs
- [x] MODIFY `TagController.kt` — thêm createdBy, labelId trong inputs

## Backend — GraphQL Schema
- [x] MODIFY `schema.graphqls` — enum types, updated types, full object resolution cho FK

## Frontend — Fix TaskCompletionLog (user removed taskId field)
- [x] MODIFY `TaskCompletionLog.kt` — xác nhận field sau khi user xóa taskId
- [x] MODIFY `PreviewDomainEntityData.kt` — xóa taskId khỏi test data

## Frontend — TaskCompletionLog Use Cases
- [x] NEW `MarkTaskDone.kt` — use case cho onDone
- [x] NEW `MarkTaskWontDo.kt` — use case cho onWontDo
- [x] NEW `TaskCompletionLogRepository.kt` (interface)
- [x] MODIFY `TaskUseCases.kt` — thêm markTaskDone, markTaskWontDo
- [x] MODIFY `taskManagementModule.kt` — đăng ký use cases mới

## Frontend — Fake Default User Test Data
- [x] NEW `FakeUser.kt` — fake user cố định trong __test_data__
