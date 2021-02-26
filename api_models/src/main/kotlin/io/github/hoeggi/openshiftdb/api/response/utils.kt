package io.github.hoeggi.openshiftdb.api.response

import kotlinx.serialization.Serializable

@Serializable
data class ApiResponse<T>(val data: T, val result: Int)