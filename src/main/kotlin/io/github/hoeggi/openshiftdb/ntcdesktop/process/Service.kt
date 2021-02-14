package io.github.hoeggi.openshiftdb.ntcdesktop.process

import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import okio.ByteString.Companion.decodeBase64

@JsonClass(generateAdapter = true)
data class ServiceResult(val items: List<ServiceItem>)

@JsonClass(generateAdapter = true)
data class ServiceItem(val metadata: Metadata, val spec: ServiceSpec)

@JsonClass(generateAdapter = true)
data class Metadata(val name: String)

@JsonClass(generateAdapter = true)
data class ServiceSpec(val ports: List<Port>)

@JsonClass(generateAdapter = true)
data class Port(val port: String)

data class Service(val name: String, val ports: List<String>)

fun parseServices(json: String?) = try {
    if (json.isNullOrBlank()) listOf()
    else Moshi.Builder()
        .build()
        .adapter(ServiceResult::class.java)
        .fromJson(json)
        .let {
            it?.items?.map {
                Service(
                    name = it.metadata.name,
                    ports = it.spec.ports.map { it.port }
                )
            } ?: listOf()
        }
} catch (ex: Exception) {
    ex.printStackTrace()
    listOf()
}

@JsonClass(generateAdapter = true)
data class SecretResult(val items: List<SecretItem>)

@JsonClass(generateAdapter = true)
data class SecretItem(val data: Map<String, String>)

fun findPassword(json: String?): String? = try {
    if (json.isNullOrBlank()) {
        null
    } else {
        Moshi.Builder()
            .build()
            .adapter(SecretResult::class.java)
            .fromJson(json)
            ?.items
            ?.asSequence()
            ?.map {
                val map = it.data.filter { (k, v) ->
                    matchesUsername(k, v) || k.equals("password", true)
                }.map { (k, v) ->
                    k to v.decodeBase64()?.utf8()
                }.toMap()
                map["password"]
            }?.filterNotNull()?.first()
    }
} catch (ex: java.lang.Exception) {
    ex.printStackTrace()
    null
}

private fun matchesUsername(key: String, value: String) =
    (key.equals("username", true)
            || key.equals("user", true))
            && value.equals("postgres", true)