package com.example.se405.backend.database.repository

import com.example.se405.backend.database.model.UserDeviceEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID
import java.util.Optional

@Repository
interface UserDeviceRepository : JpaRepository<UserDeviceEntity, UUID> {
    fun findByUserIdAndDeviceId(userId: UUID, deviceId: String): Optional<UserDeviceEntity>
    fun findByDeviceId(deviceId: String): Optional<UserDeviceEntity>
    fun findAllByDeviceId(deviceId: String): List<UserDeviceEntity>
    fun findFirstByDeviceIdAndBiometricEnabledTrue(deviceId: String): Optional<UserDeviceEntity>
}
