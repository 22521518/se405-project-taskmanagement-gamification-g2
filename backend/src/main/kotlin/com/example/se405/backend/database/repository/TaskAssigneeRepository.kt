package com.example.se405.backend.database.repository

import com.example.se405.backend.database.model.TaskAssigneeEntity
import com.example.se405.backend.database.model.TaskAssigneeId
import org.springframework.data.jpa.repository.JpaRepository

interface TaskAssigneeRepository: JpaRepository<TaskAssigneeEntity, TaskAssigneeId> {
}