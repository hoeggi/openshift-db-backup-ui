package io.github.hoeggi.openshiftdb.api.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

typealias FullContext = String

fun FullContext.isSameCluster(other: FullContext): Boolean {
    if (this == other) return true
    return this.substring(this.indexOf("/"), this.lastIndexOf("/")) ==
            other.substring(other.indexOf("/"), other.lastIndexOf("/"))
}

@Serializable
data class SwitchContextApi(val context: FullContext)

@Serializable
data class ContextApi(val current: FullContext, val contexts: List<ContextsApi>)

@Serializable
data class ContextsApi(val cluster: String, val context: List<ContextDetailApi>)

@Serializable
data class ContextDetailApi(val name: FullContext, val user: String, val namespace: String)

@Serializable
data class SecretsApi(val name: String, val data: Map<String, String>)

@Serializable
data class LoginApi(val token: String, val server: String)

@Serializable
sealed class PortForwardMessage {
    abstract val message: String

    companion object {
        fun close(message: String): PortForwardMessage = CloseMessage(message)
        fun error(message: String): PortForwardMessage = ErrorMessage(message)
        fun message(message: String): PortForwardMessage = Message(message)
    }

    @Serializable
    @SerialName("message")
    data class Message(override val message: String) : PortForwardMessage()

    @Serializable
    @SerialName("error")
    data class ErrorMessage(override val message: String) : PortForwardMessage()

    @Serializable
    @SerialName("close")
    data class CloseMessage(override val message: String) : PortForwardMessage()
}

@Serializable
data class VersionApi(
    val oc: String = "",
    val kubernets: String = "",
    val openshift: String = "",
)

@Serializable
data class ClusterApi(
    val name: String = "",
    val server: String = "",
)

@Serializable
data class ServicesApi(
    val name: String = "",
    val ports: List<ServicesPortApi> = listOf(),
)

@Serializable
data class ServicesPortApi(
    val port: Int = -1,
    val targetPort: String = "",
    val protocol: String = "",
)

@Serializable
data class ProjectApi(
    val name: String = "",
)
