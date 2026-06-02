package com.example.se405.android.core

import com.example.se405.android.core.authentication.data.AuthPreferences
import com.example.se405.android.core.authentication.data.AuthRepository
import com.example.se405.android.core.authentication.data.AuthResponse
import com.example.se405.android.core.authentication.data.LoginRequest
import com.example.se405.android.features.tasks_management.utils.generateUsers
import com.example.se405.android.runKoinApp
import org.junit.After
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.koin.core.Koin
import org.junit.Test
import org.koin.core.context.stopKoin
import kotlin.math.abs
import kotlin.random.Random

fun onLogin(runWithAuthentication: suspend (Koin, AuthResponse) -> Unit, onFailed: (Throwable) -> Unit) {
        val email = "test@example.com"
//    val username = "le_minh_1779366007946_2"
    val username = "testuser"
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
                println("========= RUN_WITH ${response.userId}, ${response.username} =========")
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
    @Before
    fun setUp() {
        stopKoin()
    }

    @After
    fun tearDown() {
        stopKoin()
    }

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

    @Test
    fun testRegisterAndLoginFlowRepeatedly() {
        println("Run repeated register and login authentication testing (50 times)")

        for (i in 1..500) {
            println("\n--- Vòng lặp thứ $i ---")

            // 1. Thực hiện Đăng ký
            onRegister(runWithAuthentication = { _, registerResponse ->
                assertNotNull("Token đăng ký là NULL ở vòng lặp $i!", registerResponse.token)
                assertTrue("Token đăng ký bị TRỐNG ở vòng lặp $i!", registerResponse.token.isNotBlank())
                println("Info Success Register ($i): \n $registerResponse")

                // 2. Sau khi đăng ký thành công, thực hiện Đăng nhập ngay lập tức
                onLogin(runWithAuthentication = { _, loginResponse ->
                    assertNotNull("Token đăng nhập là NULL ở vòng lặp $i!", loginResponse.token)
                    assertTrue("Token đăng nhập bị TRỐNG ở vòng lặp $i!", loginResponse.token.isNotBlank())
                    println("Info Success Login ($i): \n $loginResponse")
                }, onFailed = { exception ->
                    println("Info FAILURE Login ($i): ${exception.message}")
                    fail("Login failed ở vòng lặp $i!")
                })

            }, onFailed = { exception ->
                println("Info FAILURE Register ($i): ${exception.message}")
                fail("Register failed ở vòng lặp $i!")
            })
        }
    }
}