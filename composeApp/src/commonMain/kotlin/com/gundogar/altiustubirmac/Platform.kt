package com.gundogar.altiustubirmac

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform

expect fun openUrl(url: String)