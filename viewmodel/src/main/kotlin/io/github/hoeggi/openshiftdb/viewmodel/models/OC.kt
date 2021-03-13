package io.github.hoeggi.openshiftdb.viewmodel.models

import io.github.hoeggi.openshiftdb.api.response.*

internal fun ContextApi.toContext(): Context {
    return Context(current, contexts.map { it.toContexts() })
}

private fun ContextsApi.toContexts(): Contexts {
    return Contexts(cluster, context.map { it.toContextDetail() })
}

private fun ContextDetailApi.toContextDetail(): ContextDetail {
    return ContextDetail(name, user, namespace)
}

data class Context(val current: String, val contexts: List<Contexts>)
data class Contexts(val cluster: String, val context: List<ContextDetail>)
data class ContextDetail(val name: String, val user: String, val namespace: String)

enum class LoginState {
    UNCHECKED, LOGGEDIN, NOT_LOGGEDIN
}

internal fun ServicesApi.toService() =
    Service(name, ports.map { ServicePort(it.port, it.targetPort, it.protocol) })

class Service(val name: String = "", val ports: List<ServicePort> = listOf())
class ServicePort(val port: Int, targetPort: String, protocol: String) {
    val display = "$port:$targetPort/$protocol"
}

data class PortForwardTarget(val project: String, val svc: String, val port: Int)
class OpenPortForward(val target: PortForwardTarget, val messages: List<PortForwardMessage>)

interface PortForwardMessage {
    val message: String

    inline class Message(override val message: String) : PortForwardMessage
    inline class Close(override val message: String) : PortForwardMessage
    inline class Error(override val message: String) : PortForwardMessage

    companion object {
        fun portForwardMessage(from: io.github.hoeggi.openshiftdb.api.response.PortForwardMessage): PortForwardMessage =
            when (from) {
                is io.github.hoeggi.openshiftdb.api.response.PortForwardMessage.CloseMessage -> Close(from.message)
                is io.github.hoeggi.openshiftdb.api.response.PortForwardMessage.ErrorMessage -> Error(from.message)
                is io.github.hoeggi.openshiftdb.api.response.PortForwardMessage.Message -> Message(from.message)
            }
    }
}