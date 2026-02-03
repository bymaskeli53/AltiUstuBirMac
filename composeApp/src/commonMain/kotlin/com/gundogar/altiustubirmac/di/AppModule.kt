package com.gundogar.altiustubirmac.di

import com.gundogar.altiustubirmac.data.MatchRepository
import com.gundogar.altiustubirmac.data.MatchRepositoryImpl
import com.gundogar.altiustubirmac.ui.MatchViewModel
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val appModule = module {
    single {
        HttpClient {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }
    }

    singleOf(::MatchRepositoryImpl) bind MatchRepository::class

    single { MatchViewModel(get()) }
}
