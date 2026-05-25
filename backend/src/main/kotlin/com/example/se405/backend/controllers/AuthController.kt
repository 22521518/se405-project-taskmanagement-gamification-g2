package com.example.se405.backend.controllers

import com.example.se405.backend.controllers.dtos.*
import com.example.se405.backend.services.AuthService
import com.example.se405.backend.services.JwtUtils
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
class HelloWorldController {
    @GetMapping("/hello")
    fun hello(): ResponseEntity<String> {
        return ResponseEntity.ok("Hello World")
    }
}

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val authService: AuthService,
    private val jwtUtils: JwtUtils
) {

    @PostMapping("/register")
    fun register(@RequestBody req: RegisterRequest): ResponseEntity<AuthResponse> {
        return ResponseEntity.ok(authService.register(req))
    }

    @PostMapping("/login")
    fun login(@RequestBody req: LoginRequest): ResponseEntity<AuthResponse> {
        return ResponseEntity.ok(authService.login(req))
    }

    @PostMapping("/enable-biometric")
    fun enableBiometric(
        @RequestHeader("Authorization") authHeader: String,
        @RequestBody req: BiometricEnableRequest
    ): ResponseEntity<SimpleMessageResponse> {
        val cleanHeader = authHeader.substringBefore(",").trim()
        val token = cleanHeader.substring(7)
        val userId = jwtUtils.getUserIdFromToken(token)
        authService.enableBiometric(userId, req)
        return ResponseEntity.ok(SimpleMessageResponse("Biometric enabled successfully"))
    }

    @PostMapping("/disable-biometric")
    fun disableBiometric(
        @RequestHeader("Authorization") authHeader: String,
        @RequestParam deviceId: String
    ): ResponseEntity<SimpleMessageResponse> {
        val cleanHeader = authHeader.substringBefore(",").trim()
        val token = cleanHeader.substring(7)
        val userId = jwtUtils.getUserIdFromToken(token)
        authService.disableBiometric(userId, deviceId)
        return ResponseEntity.ok(SimpleMessageResponse("Biometric disabled successfully"))
    }

    @PostMapping("/login-biometric")
    fun loginBiometric(@RequestBody req: BiometricLoginRequest): ResponseEntity<AuthResponse> {
        return ResponseEntity.ok(authService.loginBiometric(req))
    }

    @GetMapping("/me")
    fun getMe(@RequestHeader("Authorization") authHeader: String): ResponseEntity<UserProfileResponse> {
        val cleanHeader = authHeader.substringBefore(",").trim()
        val token = cleanHeader.substring(7)
        val userId = jwtUtils.getUserIdFromToken(token)
        return ResponseEntity.ok(authService.getUserProfile(userId))
    }

    @PutMapping("/profile")
    fun updateProfile(
        @RequestHeader("Authorization") authHeader: String,
        @RequestBody req: UpdateProfileRequest
    ): ResponseEntity<UserProfileResponse> {
        val userId = jwtUtils.getUserIdFromToken(authHeader.substring(7))
        return ResponseEntity.ok(authService.updateProfile(userId, req))
    }
}
