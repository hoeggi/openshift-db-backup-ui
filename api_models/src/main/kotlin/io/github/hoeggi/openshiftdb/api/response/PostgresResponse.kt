package io.github.hoeggi.openshiftdb.api.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.LocalDateTime


@Serializable
data class DatabaseEventApi(
    val dbname: String,
    val path: String,
    val username: String,
    val format: String,
    @Serializable(with = LocalDateTimeSerializer::class)
    override val startTime: LocalDateTime,
    @Serializable(with = LocalDateTimeSerializer::class)
    override val endTime: LocalDateTime,
    override val eventType: EventTypeApi,
    override val result: EventResultApi,
) : EventApi


@Serializable
sealed class DatabaseRestoreMessage : Trackable {
    override val eventType = Trackable.Type.Restore

    companion object {
        fun unspecified(): DatabaseRestoreMessage = Unspecified
        fun start(): DatabaseRestoreMessage = StartMessage
        fun requestConfirm(message: String): DatabaseRestoreMessage = RequestConfirmation(message)
        fun confirm(): DatabaseRestoreMessage = ConfirmRestore
        fun finish(): DatabaseRestoreMessage = FinishMessage
        fun error(message: String): DatabaseRestoreMessage = ErrorMessage(message)
        fun inprogress(message: String): DatabaseRestoreMessage = InProgressMessage(message)
    }

    @Serializable
    @SerialName("unspecified")
    object Unspecified : DatabaseRestoreMessage()

    @Serializable
    @SerialName("start")
    object StartMessage : DatabaseRestoreMessage(), Trackable.Start

    @Serializable
    @SerialName("confirm-restore")
    object ConfirmRestore : DatabaseRestoreMessage()

    @Serializable
    @SerialName("request-confirmation")
    data class RequestConfirmation(val message: String) : DatabaseRestoreMessage()

    @Serializable
    @SerialName("error")
    data class ErrorMessage(val message: String) : DatabaseRestoreMessage(), Trackable.Error

    @Serializable
    @SerialName("inprogress")
    data class InProgressMessage(val message: String) : DatabaseRestoreMessage()

    @Serializable
    @SerialName("finish")
    object FinishMessage : DatabaseRestoreMessage(), Trackable.Finish
}

@Serializable
sealed class DatabaseDownloadMessage : Trackable {
    override val eventType = Trackable.Type.Dump
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
    data class StartMessage(override val message: String = "start") : DatabaseDownloadMessage(), Trackable.Start

    @Serializable
    @SerialName("error")
    data class ErrorMessage(override val message: String) : DatabaseDownloadMessage(), Trackable.Error

    @Serializable
    @SerialName("inprogress")
    data class InProgressMessage(override val message: String) : DatabaseDownloadMessage()

    @Serializable
    @SerialName("finish")
    data class FinishMessage(override val message: String) : DatabaseDownloadMessage(), Trackable.Finish
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
data class RestoreCommandApi(
    val command: String = "",
    val existing: Boolean = true,
    val defaultDatabase: String = "",
    val database: String = "",
)

@Serializable
data class DefaultDatabaseApi(
    val database: String = "",
)

@Serializable
data class DatabaseVersionApi(
    val database: String = "",
)

//@Serializable
//inline class StringList(val databases: List<String>)

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