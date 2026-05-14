package com.berkbelhan.indoornavigation.core.common

/** Generic result wrapper used across all layers. */
sealed class Result<out T> {
    data class Success<T>(val value: T) : Result<T>()
    data class Failure(val error: AppError) : Result<Nothing>()

    val isSuccess: Boolean get() = this is Success
    val isFailure: Boolean get() = this is Failure

    fun getOrNull(): T? = (this as? Success)?.value

    inline fun <R> map(transform: (T) -> R): Result<R> = when (this) {
        is Success -> Success(transform(value))
        is Failure -> this
    }

    inline fun onSuccess(block: (T) -> Unit): Result<T> {
        if (this is Success) block(value)
        return this
    }

    inline fun onFailure(block: (AppError) -> Unit): Result<T> {
        if (this is Failure) block(error)
        return this
    }
}

/** App-wide typed errors. */
sealed interface AppError {
    data class Network(val message: String? = null, val code: Int? = null) : AppError
    data class Auth(val message: String? = null) : AppError
    data class Localization(val message: String? = null) : AppError
    data class Storage(val message: String? = null) : AppError
    data class Download(val message: String? = null) : AppError
    data class Unknown(val throwable: Throwable? = null) : AppError
}
