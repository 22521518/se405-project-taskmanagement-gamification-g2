package com.example.se405.android.core

import com.example.se405.android.core.authentication.data.AuthPreferences
import com.example.se405.android.core.authentication.data.AuthRepository
import com.example.se405.android.core.authentication.data.AuthResponse
import com.example.se405.android.core.authentication.data.LoginRequest
import com.example.se405.android.features.tasks_management.utils.generateUsers
import com.example.se405.android.runKoinApp
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.koin.core.Koin
import org.junit.Test
import kotlin.math.abs
import kotlin.random.Random

fun onLogin(runWithAuthentication: suspend (Koin, AuthResponse) -> Unit, onFailed: (Throwable) -> Unit) {
//        val email = "test@example.com"
//    val username = "testuser"
    val username = "le_minh_1779366007946_2"
    val password = "123456"
    runKoinApp { koin ->
        val repository = koin.get<AuthRepository>()
        val prefs = koin.get<AuthPreferences>()
        val realRequest = LoginRequest(username, password, "LocalTest-1")
        run {
            val result = repository.login(realRequest)
            result.onSuccess { response ->
                prefs.saveAuth(
                    response.token,
                    response.userId,
                    response.username,
                    response.displayName,
                    response.biometricEnabled
                )
                runWithAuthentication(koin, response)
            }.onFailure { exception ->
                onFailed(exception)
            }
            print("===ENDING===")
        }
    }
}

fun onRegister(runWithAuthentication: suspend (Koin, AuthResponse) -> Unit, onFailed: (Throwable) -> Unit) {
//        val email = "test@example.com"
    val registerRequestRandom = generateUsers(5)[abs(Random.nextInt() % 5)]
    println("Register with this accounts: $registerRequestRandom")
    runKoinApp { koin ->
        val repository = koin.get<AuthRepository>()
        val prefs = koin.get<AuthPreferences>()
        run {
            val result = repository.register(registerRequestRandom)
            result.onSuccess { response ->
                prefs.saveAuth(
                    response.token,
                    response.userId,
                    response.username,
                    response.displayName,
                    response.biometricEnabled
                )
                runWithAuthentication(koin, response)
            }.onFailure { exception ->
                onFailed(exception)
            }
            print("===ENDING===")
        }
    }
}

class AuthenticationTest {
    @Test
    fun testRegisterAuthenticationFlow() {
        println("Run authentication testing")
        onRegister(runWithAuthentication = { _, response ->
            assertNotNull("Token is NULL!", response.token)
            assertTrue("Token is EMPTY!", response.token.isNotBlank())
            print("Info Success Register: \n $response")
            }, onFailed = {exception ->
            println("Info FAILURE: ${exception.message}")
            fail("Register failed!")
        })
    }

    @Test
    fun testLoginAuthenticationFlow() {
        println("Run authentication testing")
        onLogin(runWithAuthentication = { _, response ->
            assertNotNull("Token is NULL!", response.token)
            assertTrue("Token is EMPTY!", response.token.isNotBlank())
            print("Info Success Login: \n $response")
        }, onFailed = {exception ->
            println("Info FAILURE: ${exception.message}")
            fail("Login failed!")
        })

    }
}