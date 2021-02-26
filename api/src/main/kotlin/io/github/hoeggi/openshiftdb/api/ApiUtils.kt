package io.github.hoeggi.openshiftdb.api

import io.github.hoeggi.openshiftdb.api.response.ApiResponse
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.*
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody

typealias ApiClient = Pair<OkHttpClient, BasePath>
typealias BasePath = String


fun Api(port: Int, baseUrl: BasePath = "http://localhost"): Api = ApiImp(
    OcApi.api(port, baseUrl),
    PostgresApi.api(port, baseUrl)
)

interface Api : OcApi, PostgresApi
private class ApiImp(val oc: OcApi, val postgres: PostgresApi) : Api, OcApi by oc, PostgresApi by postgres


operator fun OkHttpClient.plus(baseUrl: BasePath): ApiClient = this to baseUrl

internal fun String.withPath(path: BasePath) =
    toHttpUrl().newBuilder().addEncodedPathSegments(path).build()

internal fun HttpUrl.withQuery(vararg params: Pair<String, String>) =
    newBuilder().withQuery(*params).build()

internal fun HttpUrl.Builder.withQuery(vararg params: Pair<String, String>) = apply {
    for (p in params) {
        addQueryParameter(p.first, p.second)
    }
}

internal fun HttpUrl.toGetRequest(credentials: String? = null) = Request.Builder()
    .get()
    .url(this)
    .apply {
        if (credentials != null) header("Authorization", credentials)
    }
    .build()

internal inline fun <reified U> HttpUrl.toPostRequest(body: U) = Request.Builder()
    .post(
        Json.encodeToString(body)
            .toRequestBody("application/json".toMediaType())
    )
    .url(this).build()

internal inline fun <reified T> Response.get(): Result<T> = if (isSuccessful && body != null) {
    val data = Json.decodeFromString<ApiResponse<T>>(body!!.string()).data
    Result.success(data)
} else {
    Result.failure(RuntimeException("$code - $message"))
}

internal inline fun <reified T> ApiClient.get(path: String, credentials: String? = null) = try {
    val request = first.newCall(second.withPath(path).toGetRequest(credentials)).execute()
    request.get<T>()
} catch (ex: Exception) {
    Result.failure(ex)
}

internal inline fun <reified T, reified U> ApiClient.post(path: String, body: U) = try {
    val request = first.newCall(second.withPath(path).toPostRequest(body)).execute()
    request.get<T>()
} catch (ex: Exception) {
    Result.failure(ex)
}

internal inline fun <reified T, reified U> ApiClient.websocket(path: String, vararg params: Pair<String, String>) =
    try {
        val request = first.newCall(second.withPath(path).withQuery(*params).toGetRequest()).execute()
        request.get<T>()
    } catch (ex: Exception) {
        Result.failure(ex)
    }