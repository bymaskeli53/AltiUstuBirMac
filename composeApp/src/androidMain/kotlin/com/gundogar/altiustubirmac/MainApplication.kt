package com.gundogar.altiustubirmac

import android.app.Application
import com.gundogar.altiustubirmac.di.appModule
import com.gundogar.altiustubirmac.di.viewModelModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

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
