package com.example.se405.android

import com.example.se405.android.core.authentication.data.AuthPreferences
import com.example.se405.android.core.authentication.data.AuthRepository
import com.example.se405.android.di.appModule
import com.example.se405.android.di.networkModule
import com.example.se405.android.features.tasks_management.MockAuthPreferences
import com.example.se405.android.features.tasks_management.taskDataModule
import com.example.se405.android.features.tasks_management.taskDomainModule

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.accept
import io.ktor.http.ContentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import org.koin.core.Koin
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.mp.KoinPlatformTools

fun runKoinApp(runWithKoin: suspend (Koin) -> Unit) = runBlocking {
    startKoin {
        modules(
            taskDomainModule, taskDataModule,
            networkModule,
            appModule,
            module {
                single<AuthPreferences> { MockAuthPreferences() }
                single { AuthRepository(get(), get()) }

                single {
                    HttpClient(CIO) {
                        install(ContentNegotiation) {
                            json(Json {
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
        )
    }
    val koin = KoinPlatformTools.defaultContext().get()
    try {
        runWithKoin(koin)
    } finally {
        stopKoin()
    }
}