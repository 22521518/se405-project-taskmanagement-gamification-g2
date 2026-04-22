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
val appModule = module {
    single {
        com.apollographql.apollo.ApolloClient.Builder()
            .serverUrl("http://localhost:8080/graphql")
//            .serverUrl("http://10.0.2.2:8080/graphql") // if using android emulator, uncommenting this line
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
