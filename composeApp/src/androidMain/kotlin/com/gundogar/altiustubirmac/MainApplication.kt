package com.gundogar.altiustubirmac

import android.app.Application
import com.gundogar.altiustubirmac.di.appModule
import com.gundogar.altiustubirmac.di.viewModelModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

/**
 * Android initializes Koin here instead of KoinApplication composable
 * to provide androidContext() which is needed for platform-specific features
 * like opening URLs via Intent. App.kt is called with useKoinApplication=false.
 */
class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger()
            androidContext(this@MainApplication)
            modules(appModule, viewModelModule)
        }
    }
}
