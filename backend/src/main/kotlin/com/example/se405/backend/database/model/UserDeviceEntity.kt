package com.example.se405.backend.database.model

import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "user_devices")
data class UserDeviceEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,

    @Column(name = "user_id", nullable = false)
    val userId: UUID,

    @Column(name = "device_id", nullable = false)
    val deviceId: String,

    @Column(name = "public_key", columnDefinition = "TEXT")
    var publicKey: String? = null,

    @Column(name = "biometric_enabled", nullable = false)
    var biometricEnabled: Boolean = false,

    @Column(name = "last_login")
    var lastLogin: LocalDateTime = LocalDateTime.now()
)
