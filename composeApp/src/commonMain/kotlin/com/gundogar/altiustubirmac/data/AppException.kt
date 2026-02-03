package com.gundogar.altiustubirmac.data

import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.client.network.sockets.SocketTimeoutException
import io.ktor.client.plugins.HttpRequestTimeoutException
import kotlinx.io.IOException
import kotlinx.serialization.SerializationException

/**
 * Sealed class provides type-safe error handling with exhaustive `when` expressions.
 * Turkish messages are used because this is the target audience.
 *
 * The companion object's [from] function centralizes exception mapping,
 * ensuring consistent error handling across the app.
 */
sealed class AppException(override val message: String) : Exception(message) {

    data object NoInternet : AppException("Internetiniz yok. Lutfen baglantinizi kontrol edin.")

    data object Timeout : AppException("Baglanti zaman asimina ugradi. Tekrar deneyin.")

    data object ServerError : AppException("Sunucuya ulasilamiyor. Daha sonra tekrar deneyin.")

    data object ParseError : AppException("Veri isleme hatasi olustu.")

    data object EmptyData : AppException("Gosterilecek mac bulunamadi.")

    data class Unknown(val originalMessage: String?) :
        AppException(originalMessage ?: "Bilinmeyen bir hata olustu.")

    companion object {
        fun from(throwable: Throwable): AppException = when (throwable) {
            is AppException -> throwable
            is ConnectTimeoutException,
            is SocketTimeoutException,
            is HttpRequestTimeoutException -> Timeout

            is IOException -> NoInternet
            is SerializationException -> ParseError
            else -> Unknown(throwable.message)
        }
    }
}
