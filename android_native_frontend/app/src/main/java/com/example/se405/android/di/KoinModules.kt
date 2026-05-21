package com.example.se405.android.di

import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.accept
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import org.koin.dsl.module
import io.ktor.serialization.kotlinx.json.*

/**
 * Defines the core application dependencies that should live for the entire app lifecycle.
 *
 * This module typically includes:
 * - Singleton instances (e.g., Network clients, Database providers).
 * - Shared Preferences or Local Storage managers.
 * - Repositories that act as a single source of truth.
 */
import okhttp3.OkHttpClient
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.flow.firstOrNull
import com.example.se405.android.core.authentication.data.AuthPreferences

import com.apollographql.apollo.network.okHttpClient

object NetworkConfig {
//    const val BASE_IP = "192.168.1.227"
    const val BASE_IP = "192.168.1.42"
    const val GRAPHQL_URL = "http://$BASE_IP:8080/graphql"
    const val AUTH_URL = "http://$BASE_IP:8080/api/auth"
}

val appModule = module {
    single {
        val authPreferences = get<AuthPreferences>()
        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val token = runBlocking {
                    authPreferences.authToken.firstOrNull()
                }
                val requestBuilder = chain.request().newBuilder()
                if (!token.isNullOrBlank()) {
                    requestBuilder.addHeader("Authorization", "Bearer $token")
                }
                val response = chain.proceed(requestBuilder.build())
                if (response.code == 401 || response.code == 403) {
                    runBlocking {
                        authPreferences.clearAuth()
                    }
                }
                response
            }
            .build()

        com.apollographql.apollo.ApolloClient.Builder()
            .serverUrl(NetworkConfig.GRAPHQL_URL)
            .okHttpClient(okHttpClient)
            .build()
    }
}

val networkModule = module {
    single {
        io.ktor.client.HttpClient(OkHttp) {
            install(ContentNegotiation) {
                json(kotlinx.serialization.json.Json {
                    ignoreUnknownKeys = true
                    prettyPrint = true
                    isLenient = true
                })
            }
            defaultRequest {
                accept(ContentType.Application.Json)
            }
            expectSuccess = false
        }
    }
}

/**
 * Defines the dependency injection rules for ViewModels.
 * * Each ViewModel is declared using a factory pattern, ensuring that the UI components
 * (Activities/Fragments) receive a fresh instance or the correctly scoped instance
 * according to the ViewModelStoreOwner's lifecycle.
 */
val viewModelModule = module {
}
