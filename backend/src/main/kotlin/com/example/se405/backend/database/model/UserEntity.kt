package com.example.se405.backend.database.model

import jakarta.persistence.*
import org.hibernate.annotations.ColumnDefault
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "users")
data class UserEntity(
    @Id
    val uuid: UUID,

    @Column(nullable = false, unique = true)
    val email: String,

    @Column(nullable = false, unique = true)
    val username: String,

    @Column(name = "password_hash", nullable = false)
    val passwordHash: String,

    @Column(name = "display_name", nullable = false)
    val displayName: String,

    @Column(name = "avatar_url")
    val avatarUrl: String? = null,

    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at", nullable = false)
    val updatedAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "fcm_token")
    var fcmToken: String? = null,

    @Column(name = "is_online", nullable = false)
    @ColumnDefault("false")
    var isOnline: Boolean = false,

    @Column(name = "last_seen")
    var lastSeen: java.time.LocalDateTime? = null
){
    override fun toString(): String {
        return "UserEntity(uuid=$uuid, username='$username', email='$email', displayName='$displayName')"
    }

    override fun hashCode(): Int = uuid.hashCode() ?: 0
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as UserEntity

        if (uuid != other.uuid) return false
        if (email != other.email) return false
        if (username != other.username) return false
        if (passwordHash != other.passwordHash) return false
        if (displayName != other.displayName) return false
        if (avatarUrl != other.avatarUrl) return false
        if (createdAt != other.createdAt) return false
        if (updatedAt != other.updatedAt) return false
        if (fcmToken != other.fcmToken) return false

        return true
    }
}
