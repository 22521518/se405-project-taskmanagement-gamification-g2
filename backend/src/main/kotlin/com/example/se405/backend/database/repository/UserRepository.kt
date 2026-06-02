package com.example.se405.backend.database.repository

import com.example.se405.backend.database.model.UserEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Repository
interface UserRepository : JpaRepository<UserEntity, UUID> {
    fun findByEmail(email: String): UserEntity?
    fun findByUsername(username: String): UserEntity?
    fun findByUuidNot(uuid: UUID): List<UserEntity>

    /**
     * Backfills legacy rows where `is_online` was added as a nullable column
     * (ddl-auto=update never sets a default for existing rows), leaving NULLs
     * that crash hydration into the non-nullable Kotlin `Boolean` property.
     * Native query so it does not hydrate the broken rows. Returns rows fixed.
     */
    @Modifying
    @Transactional
    @Query(value = "UPDATE users SET is_online = false WHERE is_online IS NULL", nativeQuery = true)
    fun backfillNullIsOnline(): Int
}
