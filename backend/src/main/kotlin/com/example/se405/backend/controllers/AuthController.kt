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
        val token = authHeader.substring(7)
        val userId = jwtUtils.getUserIdFromToken(token)
        authService.enableBiometric(userId, req)
        return ResponseEntity.ok(SimpleMessageResponse("Biometric enabled successfully"))
    }

    @PostMapping("/disable-biometric")
    fun disableBiometric(
        @RequestHeader("Authorization") authHeader: String,
        @RequestParam deviceId: String
    ): ResponseEntity<SimpleMessageResponse> {
        val token = authHeader.substring(7)
        val userId = jwtUtils.getUserIdFromToken(token)
        authService.disableBiometric(userId, deviceId)
        return ResponseEntity.ok(SimpleMessageResponse("Biometric disabled successfully"))
    }

    @PostMapping("/login-biometric")
    fun loginBiometric(@RequestBody req: BiometricLoginRequest): ResponseEntity<AuthResponse> {
        return ResponseEntity.ok(authService.loginBiometric(req))
    }
}
