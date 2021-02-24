package io.github.hoeggi.openshiftdb.api.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient


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
    val oc: String,
    val kubernets: String,
    val openshift: String
)

@Serializable
data class ClusterApi(
    val name: String,
    val server: String,
)

@Serializable
data class ServicesApi(
    val name: String,
    val ports: List<ServicesPortApi>,
)

@Serializable
data class ServicesPortApi(
    val port: String,
    val targetPort: String,
    val protocol: String,
) {
    @Transient
    val display = "$port:$targetPort/$protocol"
}

@Serializable
data class ProjectApi(
    val name: String,
)
