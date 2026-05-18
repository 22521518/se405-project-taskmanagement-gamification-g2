package com.example.se405.android.di

import android.content.Context
import com.apollographql.apollo.network.okHttpClient
import com.apollographql.apollo.network.ws.WebSocketNetworkTransport
import com.example.se405.android.core.authentication.data.AuthPreferences
import com.example.se405.android.features.users_management.data.repositoryImpl.UserRepositoryImpl
import com.example.se405.android.features.users_management.domain.repository.UserRepository
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.accept
import io.ktor.http.ContentType
import org.koin.dsl.module
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient

/**
 * Defines the core application dependencies that should live for the entire app lifecycle.
 */
val appModule = module {
    single {
        AuthPreferences(get<Context>())
    }
}

/**
 * Defines network clients (Ktor, Apollo) and their configurations.
 */
val networkModule = module {
    // 1. Khởi tạo OkHttpClient (Kẹp sẵn Token vào HTTP Headers)
    single {
        val authPreferences: AuthPreferences = get()

        OkHttpClient.Builder()
            .addInterceptor { chain ->
                val token = runBlocking { authPreferences.authToken.first() }

                val requestBuilder = chain.request().newBuilder()
                if (!token.isNullOrEmpty()) {
                    requestBuilder.addHeader("Authorization", "Bearer $token")
                }

                chain.proceed(requestBuilder.build())
            }
            .build()
    }

    // 2. Khởi tạo Ktor Client (Dành cho RESTful APIs như Login/Register)
    single {
        io.ktor.client.HttpClient(OkHttp) {
            engine {
                preconfigured = get<OkHttpClient>()
            }

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

    single<UserRepository> { UserRepositoryImpl(get()) }

    // 3. Khởi tạo Apollo Client (HTTP cho Query/Mutation + WebSocket cho Subscription)
    single {
        val authPreferences: AuthPreferences = get()
        val token = runBlocking { authPreferences.authToken.first() } ?: ""
        val okHttpClient = get<OkHttpClient>()

        // Khởi tạo đường ống WebSocket cho Real-time (Subscription)
        val webSocketTransport = WebSocketNetworkTransport.Builder()
            .serverUrl("ws://10.0.2.2:8080/graphql")
            .addHeader("Authorization", "Bearer $token")
            .build()

        // Khởi tạo đường ống HTTP riêng biệt cho Query/Mutation
        val httpTransport = com.apollographql.apollo.network.http.HttpNetworkTransport.Builder()
            .serverUrl("http://10.0.2.2:8080/graphql")
            .okHttpClient(okHttpClient) // Gắn OkHttpClient vào đúng đường ống HTTP
            .build()


        com.apollographql.apollo.ApolloClient.Builder()
            .networkTransport(httpTransport) // Dùng cho HTTP
            .subscriptionNetworkTransport(webSocketTransport) // Dùng cho WebSocket
            .build()
    }
}

/**
 * Defines the dependency injection rules for ViewModels.
 */
val viewModelModule = module {
    // Các khai báo viewModelOf() của bạn nằm ở đây
}