package io.github.hoeggi.openshiftdb.api.response

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

val SerializersModule = SerializersModule {
    polymorphic(EventApi::class) {
        subclass(DatabaseEventApi::class)
        subclass(PortForwardEventApi::class)
    }
}

val Json = Json { serializersModule = SerializersModule }

@Serializable
data class ApiResponse<T>(val data: T, val result: Int)

interface Trackable {
    interface Start
    interface Finish
    interface Error

    val eventType: Type

    enum class Type {
        Restore, Dump, PortForward
    }
}

interface EventApi {
    @Serializable(with = LocalDateTimeSerializer::class)
    val startTime: LocalDateTime

    @Serializable(with = LocalDateTimeSerializer::class)
    val endTime: LocalDateTime?
    val eventType: EventTypeApi
    val result: EventResultApi?
}

@Serializable
sealed class EventResultApi {
    @Serializable
    @SerialName("success")
    object Success : EventResultApi()

    @Serializable
    @SerialName("error")
    object Error : EventResultApi()
}

@Serializable
sealed class EventTypeApi {
    @Serializable
    @SerialName("restore")
    object Restore : EventTypeApi()

    @Serializable
    @SerialName("dump")
    object Dump : EventTypeApi()

    @Serializable
    @SerialName("portforward")
    object PortForward : EventTypeApi()
}

object LocalDateTimeSerializer : KSerializer<LocalDateTime> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("LocalDateTime", PrimitiveKind.STRING)
    override fun serialize(encoder: Encoder, value: LocalDateTime) =
        encoder.encodeString(value.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))

    override fun deserialize(decoder: Decoder): LocalDateTime =
        LocalDateTime.parse(decoder.decodeString(), DateTimeFormatter.ISO_LOCAL_DATE_TIME)
}
