package io.github.hoeggi.openshiftdb.process

import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import okio.ByteString.Companion.decodeBase64


private val jsonParser = Json {
    ignoreUnknownKeys = true
}

@Serializable
data class OcVersion(
    val releaseClientVersion: String,
    val openshiftVersion: String,
    val serverVersion: VersionDetail
)

@Serializable
data class VersionDetail(
    val major: String,
    val minor: String,
    val gitVersion: String
)

internal fun parseVersion(version: String) = try {
    if (version.isNullOrBlank()) null
    else jsonParser.decodeFromString<OcVersion>(version)
} catch (ex: Exception) {
    ex.printStackTrace()
    null
}

@Serializable
internal data class ServiceResult(val items: List<ServiceItem>)

@Serializable
internal data class ServiceItem(val metadata: Metadata, val spec: ServiceSpec)

@Serializable
internal data class Metadata(val name: String)

@Serializable
internal data class ServiceSpec(val ports: List<Port>?)

@Serializable
data class Port(val port: String, val targetPort: String, val protocol: String)

data class Service(val name: String, val ports: List<Port>)

internal fun parseServices(json: String?) = try {
    if (json.isNullOrBlank()) listOf()
    else jsonParser.decodeFromString<ServiceResult>(json)
        .let {
            it.items.filter { it.spec.ports != null }.map {
                Service(
                    name = it.metadata.name,
                    ports = it.spec.ports ?: listOf()
                )
            }
        }
} catch (ex: Exception) {
    ex.printStackTrace()
    listOf()
}

@Serializable
internal data class SecretResult(val items: List<SecretItem>)

@Serializable
internal data class SecretItem(val data: Map<String, String>)

internal fun findPassword(json: String?, userName: String): String? = try {
    if (json.isNullOrBlank()) {
        null
    } else {
        jsonParser.decodeFromString<SecretResult>(json)
            .items
            .asSequence()
            .map {
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

@Serializable
internal data class Clusters(val clusters: List<Cluster>)

@Serializable
data class Cluster(val name: String, val cluster: Server)

@Serializable
data class Server(val server: String)

internal fun parseServer(json: String?) = try {
    if (json.isNullOrBlank()) listOf()
    else jsonParser.decodeFromString<Clusters>(json)
        .clusters
} catch (ex: Exception) {
    ex.printStackTrace()
    listOf()
}

private fun matchesUsername(key: String, value: String, userName: String) =
    (key.equals("username", true)
            || key.equals("user", true)
            || key.equals("database-user", true))
            && value.decodeBase64()?.utf8().equals(userName, true)