package com.example.se405.backend.database.model

import jakarta.persistence.*
import java.util.UUID

enum class ConversationType {
    DIRECT, GROUP, TASK
}

@Entity
@Table(name = "conversations")
data class ConversationEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val uuid: UUID? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val type: ConversationType,

    // Tên nhóm (Chỉ dùng cho GROUP, còn DIRECT thì FE tự lấy tên người kia)
    @Column(name = "name", nullable = true)
    val name: String? = null,

    // Trỏ về Task nếu type = TASK (Nullable)
    @Column(name = "task_uuid", nullable = true)
    val taskUuid: UUID? = null
)