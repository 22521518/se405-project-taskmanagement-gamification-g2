package com.example.se405.android.di

import android.content.Context
import com.apollographql.apollo.network.okHttpClient
import com.apollographql.apollo.network.ws.WebSocketNetworkTransport
import com.example.se405.android.core.authentication.data.AuthPreferences
import com.example.se405.android.core.authentication.data.AuthPreferencesImpl
import com.example.se405.android.features.chat_management.presentation.viewmodel.NewMessageViewModel
import com.example.se405.android.features.tasks_management.presentation.viewmodel.TaskManagementViewModel
import com.example.se405.android.features.users_management.data.repositoryImpl.UserRepositoryImpl
import com.example.se405.android.features.users_management.domain.repository.UserRepository
import com.example.se405.android.features.users_management.presentation.viewmodel.PersonalViewModel
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.accept
import io.ktor.http.ContentType
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

object NetworkConfig {

    // const val BASE_IP = "192.168.1.227"

    // Chọn IP đúng theo backend đang chạy
    const val BASE_IP = "192.168.1.5"

    const val GRAPHQL_URL =
        "http://$BASE_IP:8080/graphql"

    const val AUTH_URL =
        "http://$BASE_IP:8080/api/auth"
}

val appModule = module {

    single<AuthPreferences> {
        AuthPreferencesImpl(get<Context>())
    }
}

/**
 * Network module
 */
val networkModule = module {

    // OkHttpClient
    single {

        val authPreferences: AuthPreferences = get()

        OkHttpClient.Builder()
            .addInterceptor { chain ->

                val token = runBlocking {
                    authPreferences.authToken.firstOrNull()
                }

                val requestBuilder =
                    chain.request().newBuilder()

                if (!token.isNullOrBlank()) {
                    requestBuilder.addHeader(
                        "Authorization",
                        "Bearer $token"
                    )
                }

                val response =
                    chain.proceed(requestBuilder.build())

                if (
                    response.code == 401 ||
                    response.code == 403
                ) {
                    runBlocking {
                        authPreferences.clearAuth()
                    }
                }

                response
            }
            .build()
    }

    // Ktor Client
    single {

        io.ktor.client.HttpClient(OkHttp) {

            engine {
                preconfigured = get<OkHttpClient>()
            }

            install(ContentNegotiation) {
                json(
                    kotlinx.serialization.json.Json {
                        ignoreUnknownKeys = true
                        prettyPrint = true
                        isLenient = true
                    }
                )
            }

            defaultRequest {
                accept(ContentType.Application.Json)
            }

            expectSuccess = false
        }
    }

    single<UserRepository> {
        UserRepositoryImpl(get())
    }

    // Apollo Client
    single {

        val authPreferences: AuthPreferences = get()

        val token = runBlocking {
            authPreferences.authToken.first()
        } ?: ""

        val okHttpClient = get<OkHttpClient>()

        val webSocketTransport =
            WebSocketNetworkTransport.Builder()
                .serverUrl(
                    "ws://${NetworkConfig.BASE_IP}:8080/graphql"
                )
                .addHeader(
                    "Authorization",
                    "Bearer $token"
                )
                .build()

        val httpTransport =
            com.apollographql.apollo.network.http.HttpNetworkTransport.Builder()
                .serverUrl(NetworkConfig.GRAPHQL_URL)
                .okHttpClient(okHttpClient)
                .build()

        com.apollographql.apollo.ApolloClient.Builder()
            .networkTransport(httpTransport)
            .subscriptionNetworkTransport(webSocketTransport)
            .build()
    }
}

/**
 * ViewModels
 */
val viewModelModule = module {
    viewModelOf(::NewMessageViewModel)
    viewModelOf(::TaskManagementViewModel)
    viewModelOf(::PersonalViewModel)
}