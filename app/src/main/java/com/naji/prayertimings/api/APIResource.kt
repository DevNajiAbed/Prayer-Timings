package com.naji.prayertimings.api

sealed class APIResource<T>(
    val data: T? = null,
    val message: String? = null
) {
    class Loading<T> : APIResource<T>()
    class Success<T>(data: T) : APIResource<T>(data = data)
    class Error<T>(message: String) : APIResource<T>(message = message)
}
