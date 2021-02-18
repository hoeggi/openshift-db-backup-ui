package io.github.hoeggi.openshiftdb.process

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
data class ServiceSpec(val ports: List<Port>?)

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
            it?.items?.filter { it.spec.ports != null }?.map {
                Service(
                    name = it.metadata.name,
                    ports = it.spec.ports!!.map { it.port }
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

fun findPassword(json: String?, userName: String): String? = try {
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
                    val pw = k.equals("password", true)
                    val user = matchesUsername(k, v, userName)
                    matchesUsername(k, v, userName) || k.equals("password", true)
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

@JsonClass(generateAdapter = true)
data class Clusters(val clusters: List<Cluster>)

@JsonClass(generateAdapter = true)
data class Cluster(val cluster: Server)

@JsonClass(generateAdapter = true)
data class Server(val server: String)

fun parseServer(json: String?) = try {
    if (json.isNullOrBlank()) listOf()
    else Moshi.Builder()
        .build()
        .adapter(Clusters::class.java)
        .fromJson(json)?.clusters?.map { it.cluster } ?: listOf()
} catch (ex: Exception) {
    ex.printStackTrace()
    listOf()
}

private fun matchesUsername(key: String, value: String, userName: String) =
    (key.equals("username", true)
            || key.equals("user", true)
            || key.equals("database-user", true))
            && value.decodeBase64()?.utf8().equals(userName, true)