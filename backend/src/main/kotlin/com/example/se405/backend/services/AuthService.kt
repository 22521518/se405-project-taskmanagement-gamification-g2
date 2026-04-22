package com.example.se405.backend.services

import com.example.se405.backend.controllers.dtos.*
import com.example.se405.backend.database.model.UserDeviceEntity
import com.example.se405.backend.database.model.UserEntity
import com.example.se405.backend.database.repository.UserDeviceRepository
import com.example.se405.backend.database.repository.UserRepository
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.*

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val userDeviceRepository: UserDeviceRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtUtils: JwtUtils,
    private val rsaService: RSAService
) {
    fun register(req: RegisterRequest): AuthResponse {
        if (userRepository.findByUsername(req.username) != null) {
            throw RuntimeException("Username already exists")
        }
        if (userRepository.findByEmail(req.email) != null) {
            throw RuntimeException("Email already exists")
        }

        val user = UserEntity(
            uuid = UUID.randomUUID(),
            email = req.email,
            username = req.username,
            passwordHash = passwordEncoder.encode(req.password),
            displayName = req.displayName
        )
        val savedUser = userRepository.save(user)
        val token = jwtUtils.generateToken(savedUser.uuid, savedUser.username)
        
        return AuthResponse(token, savedUser.uuid, savedUser.username, savedUser.displayName, false)
    }

    fun login(req: LoginRequest): AuthResponse {
        val user = userRepository.findByUsername(req.username)
            ?: throw RuntimeException("User not found")

        if (!passwordEncoder.matches(req.password, user.passwordHash)) {
            throw RuntimeException("Invalid credentials")
        }

        val token = jwtUtils.generateToken(user.uuid, user.username)

        // Update device if provided
        req.deviceId?.let { deviceId ->
            val device = userDeviceRepository.findByUserIdAndDeviceId(user.uuid, deviceId)
                .orElse(UserDeviceEntity(userId = user.uuid, deviceId = deviceId))
            device.lastLogin = LocalDateTime.now()
            userDeviceRepository.save(device)
        }

        val biometricEnabled = req.deviceId?.let { deviceId ->
            userDeviceRepository.findByUserIdAndDeviceId(user.uuid, deviceId)
                .map { it.biometricEnabled }
                .orElse(false)
        } ?: false

        return AuthResponse(token, user.uuid, user.username, user.displayName, biometricEnabled)
    }

    fun enableBiometric(currentUserId: UUID, req: BiometricEnableRequest) {
        // Disable biometric for any other user/entry on this device to ensure uniqueness
        val devices = userDeviceRepository.findAllByDeviceId(req.deviceId)
        devices.forEach { it.biometricEnabled = false }
        userDeviceRepository.saveAll(devices)

        // Now enable/update for the current user
        val device = userDeviceRepository.findByUserIdAndDeviceId(currentUserId, req.deviceId)
            .orElse(UserDeviceEntity(userId = currentUserId, deviceId = req.deviceId))
        
        device.publicKey = req.publicKey
        device.biometricEnabled = true
        device.lastLogin = LocalDateTime.now()
        userDeviceRepository.save(device)
    }

    fun disableBiometric(currentUserId: UUID, deviceId: String) {
        val device = userDeviceRepository.findByUserIdAndDeviceId(currentUserId, deviceId)
            .orElseThrow { RuntimeException("Device not found") }
        
        device.biometricEnabled = false
        userDeviceRepository.save(device)
    }

    fun loginBiometric(req: BiometricLoginRequest): AuthResponse {
        val device = userDeviceRepository.findFirstByDeviceIdAndBiometricEnabledTrue(req.deviceId)
            .orElseThrow { RuntimeException("Biometric not enabled for this device") }

        val publicKey = device.publicKey ?: throw RuntimeException("Public key not found")
        
        val isValid = rsaService.verify(req.payload, req.signature, publicKey)
        if (!isValid) {
            throw RuntimeException("Biometric verification failed")
        }

        val user = userRepository.findById(device.userId)
            .orElseThrow { RuntimeException("User not found") }

        device.lastLogin = LocalDateTime.now()
        userDeviceRepository.save(device)

        val token = jwtUtils.generateToken(user.uuid, user.username)
        return AuthResponse(token, user.uuid, user.username, user.displayName, true)
    }
}
