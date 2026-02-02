package com.gundogar.altiustubirmac

class WasmPlatform: Platform {
    override val name: String = "Web with Kotlin/Wasm"
}

actual fun getPlatform(): Platform = WasmPlatform()

actual fun openUrl(url: String) {
    kotlinx.browser.window.open(url, "_blank")
}