# Đồng bộ hóa Entity: Frontend ↔ Backend

## Mô tả vấn đề

Frontend (Android) và Backend (Spring Boot + GraphQL) hiện đang có **sự không đồng nhất nghiêm trọng** về entity. Backend thiếu nhiều entity, thiếu nhiều field, và một số kiểu dữ liệu không tương thích.

---

## Phân tích hiện trạng

### So sánh entity hiện tại

#### `Task`

| Field | Frontend | Backend JPA | GraphQL Schema | Trạng thái |
|---|---|---|---|---|
| `uuid` | `Uuid` | `UUID` | `ID!` | ✅ OK |
| `title` | `String` | `String` | `String!` | ✅ OK |
| `description` | `String` | `String?` | `String` | ✅ OK |
| `repetition` | `Int` | `Int?` | `Int` | ✅ OK |
| `type` | `TaskType` (enum) | `String` | `String!` | ⚠️ Nên dùng enum |
| `status` | `TaskStatus` (enum) | `String` | `String!` | ⚠️ Nên dùng enum |
| `priority` | `TaskPriority` (enum) | `String` | `String!` | ⚠️ Nên dùng enum |
| `creator` | `User?` | `creatorId: UUID?` | `User` (partial) | ⚠️ Backend chỉ có FK |
| `tags` | `List<Tag>` | ❌ | `[Tag!]!` | ❌ Thiếu junction table |
| `taskCompletionLog` | `List<TaskCompletionLog>` | ❌ | ❌ | ❌ Thiếu hoàn toàn |
| `startDate` | `LocalDate?` | `String` (not null) | `String!` | ⚠️ Frontend optional, backend bắt buộc |
| `dueDate` | `LocalDate?` | `String` (not null) | `String!` | ⚠️ Tương tự |
| `projectId` | `Uuid?` | `UUID?` | ❌ | ⚠️ GraphQL schema thiếu |

#### `Tag`

| Field | Frontend | Backend JPA | GraphQL Schema | Trạng thái |
|---|---|---|---|---|
| `uuid` | `Uuid` | `UUID` | `ID!` | ✅ OK |
| `name` | `String` | `String` | `String!` | ✅ OK |
| `color` | `Int` | `Int` | `Int!` | ✅ OK |
| `ownershipType` | `TagOwnershipType` | `TagOwnershipType` | `String!` | ⚠️ GraphQL nên dùng enum |
| `workspaceId` | `Uuid?` | `UUID?` | `ID` | ✅ OK |
| `createdBy` | `Uuid` | ❌ | ❌ | ❌ Thiếu |
| `label` | `HabitLabel` (data class) | ❌ | ❌ | ❌ Thiếu |
| `createdAt` | `LocalDateTime` | ❌ | ❌ | ❌ Thiếu |
| `updatedAt` | `LocalDateTime` | ❌ | ❌ | ❌ Thiếu |
| `creator` | `User?` | ❌ | ❌ | ❌ Thiếu relation |
| `tasks` | `List<Task>` | ❌ | ❌ | ❌ Thiếu relation |
| `taskIds` | `List<Uuid>` | ❌ | ❌ | ❌ Thiếu |

#### `Workspace`

| Field | Frontend | Backend JPA | GraphQL Schema | Trạng thái |
|---|---|---|---|---|
| `id` | `Uuid` | ❌ **Entity chưa tồn tại** | `ID!` | ❌ |
| `name` | `String` | ❌ | `String!` | ❌ |
| `projects` | `List<Project>` | ❌ | ❌ | ❌ |
| `members` | `List<WorkspaceMember>` | ❌ | ❌ | ❌ |

#### `Project`

| Field | Frontend | Backend JPA | GraphQL Schema | Trạng thái |
|---|---|---|---|---|
| `id` | `Uuid` | ❌ **Entity chưa tồn tại** | `ID!` | ❌ |
| `name` | `String` | ❌ | `String!` | ❌ |
| `tasks` | `List<Task>` | ❌ | ❌ | ❌ |

#### `WorkspaceMember`

| Field | Frontend | Backend JPA | GraphQL Schema | Trạng thái |
|---|---|---|---|---|
| `workspaceId` | `Uuid` | ❌ **Entity chưa tồn tại** | `ID!` (partial) | ❌ |
| `userId` | `Uuid` | ❌ | ❌ | ❌ |
| `role` | `WorkspaceRole` (enum) | ❌ | `String!` | ❌ |
| `joinedAt` | `LocalDateTime` | ❌ | ❌ | ❌ |
| `user` | `User?` | ❌ | `User!` | ❌ |

#### `TaskCompletionLog`

| Field | Frontend | Backend JPA | GraphQL Schema | Trạng thái |
|---|---|---|---|---|
| `taskCompletionId` | `Uuid` | ❌ **Entity chưa tồn tại** | ❌ | ❌ |
| `taskId` | `Uuid` | ❌ | ❌ | ❌ |
| `date` | `LocalDate` | ❌ | ❌ | ❌ |
| `completedAt` | `LocalDateTime` | ❌ | ❌ | ❌ |
| `task` | `Task` | ❌ | ❌ | ❌ |
| `user` | `User` | ❌ | ❌ | ❌ |

#### `HabitLabel` (data class, **KHÔNG phải enum**)

`HabitLabel` là một **data class** (không phải enum) với các field: `id: Uuid`, `name: String`, `description: String`, `icon: Int` (drawable resource ID). Có 7 bản ghi cố định (`BuiltinLabels`).

> [!IMPORTANT]
> Vì `icon` là Android drawable resource ID (không có nghĩa gì ở backend), backend sẽ **chỉ lưu `labelId: UUID`**, map tới bảng `habit_labels` chứa `name`, `description`. Frontend sẽ map `labelId` → icon drawable riêng.

---

## User Review Required

> [!WARNING]
> **`startDate`/`dueDate` nullable**: Frontend định nghĩa cả hai là `LocalDate?` (optional), nhưng backend JPA hiện đang là `nullable = false`. Kế hoạch là đổi thành nullable. Xác nhận OK?

> [!IMPORTANT]
> **`HabitLabel`**: Không phải enum — là data class với 7 bản ghi builtin. Backend sẽ có bảng `habit_labels` với seed data cố định. Tag lưu `labelId: UUID?` tham chiếu tới bảng này. Confirm không?

> [!NOTE]
> **`User` entity**: Chưa có `UserEntity` ở backend. Kế hoạch là tạm thời **chỉ expose `creatorId`** (UUID) trong GraphQL response mà không resolve full `User` object — giữ nguyên pattern hiện tại.

> [!WARNING]
> **Quan hệ `Task ↔ Tag`**: Cần tạo bảng junction `task_tags(task_id, tag_id)`. Sẽ ảnh hưởng schema DB nếu đang có data.

---

## Các thay đổi cần thực hiện

### Component: Database Models (`backend/src/main/kotlin/.../database/model/`)

#### [MODIFY] [TaskEntity.kt](file:///d:/Code/SE405/do-an/backend/src/main/kotlin/com/example/se405/backend/database/model/TaskEntity.kt)
- Đổi `startDate`/`dueDate` từ `String` (not null) → `LocalDate?` (nullable)
- Thêm `@ManyToMany` với `TagEntity` (junction table `task_tags`)
- Thêm `@OneToMany` với `TaskCompletionLogEntity`

#### [MODIFY] [TagEntity.kt](file:///d:/Code/SE405/do-an/backend/src/main/kotlin/com/example/se405/backend/database/model/TagEntity.kt)
- Thêm: `createdBy: UUID`, `labelId: UUID?`, `createdAt: LocalDateTime`, `updatedAt: LocalDateTime`
- Thêm `@ManyToMany(mappedBy = "tags")` với `TaskEntity` (inverse side)

#### [NEW] `WorkspaceEntity.kt`
```kotlin
@Entity @Table(name = "workspaces")
data class WorkspaceEntity(
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    val uuid: UUID = UUID.randomUUID(),
    val name: String,
    @OneToMany(mappedBy = "workspaceId") val projects: List<ProjectEntity> = emptyList(),
    @OneToMany(mappedBy = "workspaceId") val members: List<WorkspaceMemberEntity> = emptyList(),
)
```

#### [NEW] `ProjectEntity.kt`
```kotlin
@Entity @Table(name = "projects")
data class ProjectEntity(
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    val uuid: UUID = UUID.randomUUID(),
    val name: String,
    @Column(name = "workspace_id") val workspaceId: UUID,
    @OneToMany(mappedBy = "projectId") val tasks: List<TaskEntity> = emptyList(),
)
```

#### [NEW] `WorkspaceMemberEntity.kt`
```kotlin
@Entity @Table(name = "workspace_members")
@IdClass(WorkspaceMemberId::class)
data class WorkspaceMemberEntity(
    @Id @Column(name = "workspace_id") val workspaceId: UUID,
    @Id @Column(name = "user_id") val userId: UUID,
    val role: WorkspaceRole,
    @Column(name = "joined_at") val joinedAt: LocalDateTime,
)
enum class WorkspaceRole { OWNER, MEMBER }
```

#### [NEW] `TaskCompletionLogEntity.kt`
```kotlin
@Entity @Table(name = "task_completion_logs")
data class TaskCompletionLogEntity(
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    val taskCompletionId: UUID = UUID.randomUUID(),
    @Column(name = "task_id", nullable = false) val taskId: UUID,
    @Column(name = "user_id", nullable = false) val userId: UUID,
    val date: LocalDate,
    @Column(name = "completed_at") val completedAt: LocalDateTime,
)
```

#### [NEW] `HabitLabelEntity.kt`
```kotlin
@Entity @Table(name = "habit_labels")
data class HabitLabelEntity(
    @Id val uuid: UUID,
    val name: String,
    val description: String,
)
```

---

### Component: Repositories (`database/repository/`)

#### [NEW] `WorkspaceRepository.kt`
```kotlin
interface WorkspaceRepository : JpaRepository<WorkspaceEntity, UUID>
```

#### [NEW] `ProjectRepository.kt`
```kotlin
interface ProjectRepository : JpaRepository<ProjectEntity, UUID> {
    fun findByWorkspaceId(workspaceId: UUID): List<ProjectEntity>
}
```

#### [NEW] `WorkspaceMemberRepository.kt`
```kotlin
interface WorkspaceMemberRepository : JpaRepository<WorkspaceMemberEntity, WorkspaceMemberId> {
    fun findByWorkspaceId(workspaceId: UUID): List<WorkspaceMemberEntity>
}
```

#### [NEW] `TaskCompletionLogRepository.kt`
```kotlin
interface TaskCompletionLogRepository : JpaRepository<TaskCompletionLogEntity, UUID> {
    fun findByTaskId(taskId: UUID): List<TaskCompletionLogEntity>
}
```

#### [NEW] `HabitLabelRepository.kt`
```kotlin
interface HabitLabelRepository : JpaRepository<HabitLabelEntity, UUID>
```

---

### Component: Controllers (`controllers/`)

#### [NEW] `WorkspaceController.kt`
Xử lý:
- `getMembersByWorkspace(workspaceId)` → `WorkspaceMemberRepository.findByWorkspaceId`
- `getProjectsByWorkspace(workspaceId)` → `ProjectRepository.findByWorkspaceId`

#### [MODIFY] [TaskController.kt](file:///d:/Code/SE405/do-an/backend/src/main/kotlin/com/example/se405/backend/controllers/TaskController.kt)
- Cập nhật `CreateTaskInput`: thêm `projectId: UUID?`, `tagIds: List<UUID>?`, đổi `startDate`/`dueDate` thành optional
- Cập nhật `UpdateTaskInput`: thêm `projectId`, `tagIds`

#### [MODIFY] [TagController.kt](file:///d:/Code/SE405/do-an/backend/src/main/kotlin/com/example/se405/backend/controllers/TagController.kt)
- Cập nhật `CreateTagInput`: thêm `createdBy: UUID`, `labelId: UUID?`
- Cập nhật `UpdateTagInput`: thêm `labelId`

---

### Component: GraphQL Schema (`resources/graphql/schema.graphqls`)

#### [MODIFY] [schema.graphqls](file:///d:/Code/SE405/do-an/backend/src/main/resources/graphql/schema.graphqls)

**Thêm enum types:**
```graphql
enum TaskType { PROJECT HABIT }
enum TaskStatus { TODO IN_PROGRESS DONE CANCELLED }
enum TaskPriority { LOW MEDIUM HIGH }
enum TagOwnershipType { WORKSPACE PERSONAL }
enum WorkspaceRole { OWNER MEMBER }
```

**Cập nhật `Task` type:**
```graphql
type Task {
    uuid: ID!
    title: String!
    description: String
    repetition: Int
    type: TaskType!
    status: TaskStatus!
    priority: TaskPriority!
    startDate: String        # nullable, ISO date
    dueDate: String          # nullable
    projectId: ID
    creator: User
    tags: [Tag!]!
    taskCompletionLogs: [TaskCompletionLog!]!
}
```

**Cập nhật `Tag` type:**
```graphql
type Tag {
    uuid: ID!
    name: String!
    color: Int!
    ownershipType: TagOwnershipType!
    workspaceId: ID
    createdBy: ID!
    labelId: ID
    createdAt: String!
    updatedAt: String!
}
```

**Thêm `TaskCompletionLog` type:**
```graphql
type TaskCompletionLog {
    taskCompletionId: ID!
    taskId: ID!
    userId: ID!
    date: String!
    completedAt: String!
}
```

**Cập nhật `Workspace`:**
```graphql
type Workspace {
    uuid: ID!
    name: String!
    projects: [Project!]!
    members: [WorkspaceMember!]!
}
```

**Cập nhật `Project`:**
```graphql
type Project {
    uuid: ID!
    name: String!
    workspaceId: ID!
}
```

**Cập nhật `WorkspaceMember`:**
```graphql
type WorkspaceMember {
    workspaceId: ID!
    userId: ID!
    role: WorkspaceRole!
    joinedAt: String!
    user: User
}
```

**Thêm `HabitLabel` type:**
```graphql
type HabitLabel {
    uuid: ID!
    name: String!
    description: String!
}
```

**Cập nhật Query root:**
```graphql
type Query {
    getTasks(userId: ID!): [Task!]!
    getMembersByWorkspace(workspaceId: ID!): [WorkspaceMember!]!
    getProjectsByWorkspace(workspaceId: ID!): [Project!]!
    getTagsByUser(userId: ID!): [Tag!]!
    getTagsByWorkspace(workspaceId: ID!): [Tag!]!
    getTagsForTaskOwnership(taskId: ID!): [Tag!]!
    getHabitLabels: [HabitLabel!]!
    getTaskCompletionLogs(taskId: ID!): [TaskCompletionLog!]!
}
```

---

## Verification Plan

### Automated
- Build backend: chạy `./gradlew build` (trong `backend/`)
- GraphQL schema validation tự động bởi Spring Boot GraphQL khi startup

### Manual
- Khởi động server và mở GraphiQL tại `http://localhost:8080/graphiql`
- Test query `getTasks`, `getTagsByUser`, `getMembersByWorkspace`
- Test mutation `createTask` với `projectId` và `tagIds`
