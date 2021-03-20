@file:Suppress("UNCHECKED_CAST")

package io.github.hoeggi.openshiftdb.api

class Result<out T> private constructor(internal val value: Any?) {

    companion object {
        fun <T> success(value: T): Result<T> = Result(value)
        fun <T> failure(exception: Exception): Result<T> = Result(Failure(exception))
    }

    val isSuccess: Boolean get() = value !is Failure
    val isFailure: Boolean get() = value is Failure

    fun exceptionOrNull(): Throwable? =
        when (value) {
            is Failure -> value.exception
            else -> null
        }

    class Failure(val exception: Exception)

}

fun <R, T : R> Result<T>.getOrDefault(defaultValue: R): R {
    if (isFailure) return defaultValue
    return value as T
}

fun <T> Result<T>.onSuccess(action: (value: T) -> Unit): Result<T> {
    if (isSuccess) action(value as T)
    return this
}

fun <T> Result<T>.onFailure(action: (exception: Throwable) -> Unit): Result<T> {
    exceptionOrNull()?.let { action(it) }
    return this
}