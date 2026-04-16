package com.example.se405.backend.database.repository

import com.example.se405.backend.database.model.WorkspaceEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface WorkspaceRepository : JpaRepository<WorkspaceEntity, UUID>
