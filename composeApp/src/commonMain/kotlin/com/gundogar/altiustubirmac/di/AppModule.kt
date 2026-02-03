package com.gundogar.altiustubirmac.di

import com.gundogar.altiustubirmac.data.MatchRepository
import com.gundogar.altiustubirmac.data.MatchRepositoryImpl
import com.gundogar.altiustubirmac.ui.MatchViewModel
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

/**
 * Singleton dependencies - HttpClient is expensive to create and benefits from
 * connection pooling across requests.
 */
val appModule = module {
    single {
        HttpClient {
            install(ContentNegotiation) {
                /**
                 * ignoreUnknownKeys = true is required because the API returns
                 * many fields we don't use. Without this, deserialization would fail.
                 */
                json(Json { ignoreUnknownKeys = true })
            }
        }
    }

    singleOf(::MatchRepositoryImpl) bind MatchRepository::class
}

/**
 * ViewModels are in a separate module for cleaner organization.
 * factoryOf() is used instead of viewModelOf() because viewModelOf() causes
 * IrLinkageError on WASM due to ViewModelStoreOwner linkage issues.
 */
val viewModelModule = module {
    factoryOf(::MatchViewModel)
}
