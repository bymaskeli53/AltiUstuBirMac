package com.gundogar.altiustubirmac

import android.content.Intent
import android.net.Uri
import android.os.Build
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import android.content.Context

class AndroidPlatform : Platform {
    override val name: String = "Android ${Build.VERSION.SDK_INT}"
}

actual fun getPlatform(): Platform = AndroidPlatform()

/**
 * KoinComponent allows accessing Koin dependencies outside of composables.
 * Context is injected from androidContext() set in MainApplication.
 */
private object UrlOpener : KoinComponent {
    val context: Context by inject()
}

actual fun openUrl(url: String) {
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    UrlOpener.context.startActivity(intent)
}