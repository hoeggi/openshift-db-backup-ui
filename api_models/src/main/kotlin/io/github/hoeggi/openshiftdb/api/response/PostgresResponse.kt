package io.github.hoeggi.openshiftdb.api.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed class DatabaseDownloadMessage {
    abstract val message: String

    companion object {
        fun unspecified(): DatabaseDownloadMessage = Unspecified()
        fun start(): DatabaseDownloadMessage = StartMessage()
        fun finish(message: String): DatabaseDownloadMessage = FinishMessage(message)
        fun error(message: String): DatabaseDownloadMessage = ErrorMessage(message)
        fun inprogress(message: String): DatabaseDownloadMessage = InProgressMessage(message)
    }

    @Serializable
    @SerialName("unspecified")
    class Unspecified(override val message: String = "") : DatabaseDownloadMessage()

    @Serializable
    @SerialName("start")
    data class StartMessage(override val message: String = "start") : DatabaseDownloadMessage()

    @Serializable
    @SerialName("error")
    data class ErrorMessage(override val message: String) : DatabaseDownloadMessage()

    @Serializable
    @SerialName("inprogress")
    data class InProgressMessage(override val message: String) : DatabaseDownloadMessage()

    @Serializable
    @SerialName("finish")
    data class FinishMessage(override val message: String) : DatabaseDownloadMessage()
}


@Serializable
data class ToolsVersionApi(
    val psql: String = "",
    val pgDump: String = "",
)

@Serializable
data class RestoreInfoApi(
    val info: List<String> = listOf(),
)

@Serializable
data class DefaultDatabaseApi(
    val database: String = "",
)

@Serializable
data class DatabaseVersionApi(
    val database: String = "",
)

@Serializable
sealed class DatabasesApi {

    object Default : DatabasesApi()

    @Serializable
    @SerialName("tabel")
    data class Tabel(val databases: String = "") : DatabasesApi()

    @Serializable
    @SerialName("text")
    data class Text(val databases: String = "") : DatabasesApi()

    @Serializable
    @SerialName("list")
    data class List(val databases: Array<String> = arrayOf()) : DatabasesApi()
}