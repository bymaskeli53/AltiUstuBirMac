package com.gundogar.altiustubirmac

class JsPlatform: Platform {
    override val name: String = "Web with Kotlin/JS"
}

actual fun getPlatform(): Platform = JsPlatform()

actual fun openUrl(url: String) {
    kotlinx.browser.window.open(url, "_blank")
}