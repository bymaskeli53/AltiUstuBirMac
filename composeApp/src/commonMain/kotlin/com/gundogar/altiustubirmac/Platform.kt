package com.gundogar.altiustubirmac

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform

/**
 * expect/actual pattern allows platform-specific URL opening:
 * - Android: Intent with ACTION_VIEW
 * - iOS: UIApplication.shared.open()
 * - Web: window.open()
 */
expect fun openUrl(url: String)