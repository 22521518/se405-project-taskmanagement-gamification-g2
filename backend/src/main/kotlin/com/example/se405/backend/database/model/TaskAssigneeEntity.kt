package com.example.se405.backend.database.model

import jakarta.persistence.Column
import jakarta.persistence.EmbeddedId
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.MapsId
import jakarta.persistence.Table
import java.io.Serializable
import java.util.UUID

data class TaskAssigneeId(
    @Column(name = "task_id")
    val taskId: UUID,

    @Column(name = "user_id")
    val userId: UUID
) : Serializable

@Entity
@Table(name = "task_assignee")
class TaskAssigneeEntity (
    @EmbeddedId
    val id: TaskAssigneeId,

    @ManyToOne(fetch = FetchType.EAGER)
    @MapsId("taskId")
    @JoinColumn(name = "task_id")
    var task: TaskEntity,

    @ManyToOne(fetch = FetchType.EAGER)
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    var user: UserEntity,

)