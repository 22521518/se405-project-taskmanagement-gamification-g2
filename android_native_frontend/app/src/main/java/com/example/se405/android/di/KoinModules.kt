package com.example.se405.android.di

import android.content.Context
import com.apollographql.apollo.network.okHttpClient
import com.example.se405.android.core.authentication.data.AuthPreferences
import com.example.se405.android.core.authentication.data.AuthPreferencesImpl
import com.example.se405.android.features.chat_management.data.repositoryImpl.ChatRepositoryImpl
import com.example.se405.android.features.chat_management.domain.repository.ChatRepository
import com.example.se405.android.features.chat_management.presentation.viewmodel.NewMessageViewModel
import com.example.se405.android.features.tasks_management.presentation.viewmodel.TaskManagementViewModel
import com.example.se405.android.features.users_management.data.repositoryImpl.UserRepositoryImpl
import com.example.se405.android.features.users_management.domain.repository.UserRepository
import com.example.se405.android.features.users_management.presentation.viewmodel.EditProfileViewModel
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
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.annotations.ApolloExperimental
import com.apollographql.apollo.network.websocket.WebSocketNetworkTransport
import com.apollographql.apollo.network.ws.SubscriptionWsProtocol
import com.apollographql.apollo.network.ws.GraphQLWsProtocol
import com.example.se405.android.core.authentication.BiometricViewmodel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.android.ext.koin.androidApplication

object NetworkConfig {
    const val BASE_IP = "192.168.1.56"
//    const val BASE_IP = "localhost"
    const val GRAPHQL_URL = "http://$BASE_IP:8080/graphql"
    const val AUTH_URL = "http://$BASE_IP:8080/api/auth"
}

val appModule = module {

    single<AuthPreferences> {
        AuthPreferencesImpl(get<Context>())
    }
}

/**
 * Network module
 */
@OptIn(ApolloExperimental::class)
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
        val okHttpClient = get<OkHttpClient>()
        ApolloClient.Builder()
            .serverUrl(NetworkConfig.GRAPHQL_URL) // URL cho API thường
            .webSocketServerUrl("ws://${NetworkConfig.BASE_IP}:8080/graphql")
            .wsProtocol(GraphQLWsProtocol.Factory())
            .okHttpClient(okHttpClient)
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
    viewModelOf(::EditProfileViewModel)
    viewModel {
        BiometricViewmodel(
            repository = get(),
            prefs = get(),
            cryptoManager = get(),
            deviceAuthManager = get(),
            accountBiometricManager = get(),
            apolloClient = get(),
            application = androidApplication()
        )
    }
}