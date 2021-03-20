package io.github.hoeggi.openshiftdb.api

import io.github.hoeggi.openshiftdb.api.response.DatabaseEventApi
import io.github.hoeggi.openshiftdb.api.response.EventApi
import io.github.hoeggi.openshiftdb.api.response.PortForwardEventApi
import io.github.hoeggi.openshiftdb.server.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient

interface EventsApi {

    suspend fun <T : EventApi> newEvent(event: T): Result<Int>
    suspend fun databaseEvents(): Result<List<DatabaseEventApi>>
    suspend fun portForwardEvents(): Result<List<PortForwardEventApi>>


    companion object {
        fun api(port: Int, baseUrl: BasePath): EventsApi {
            return EventsApiImpl("$baseUrl:$port${Path.v1().events().path}")
        }
    }
}

private class EventsApiImpl(url: BasePath) : EventsApi {

    private val client = OkHttpClient.Builder()
        .build() + url

    private class EventWrapper<T : EventApi>(val path: Routes, val event: T)

    override suspend fun <T : EventApi> newEvent(event: T): Result<Int> =
        withContext(Dispatchers.IO) {
            val path = when (event) {
                is DatabaseEventApi -> EventWrapper<DatabaseEventApi>(Path.database(), event)
                is PortForwardEventApi -> EventWrapper<PortForwardEventApi>(Path.portForward(), event)
                else -> throw UnsupportedOperationException("unknown type, ${event::class.java}")
            }
            client.post(path.path, path.event)
        }

    override suspend fun databaseEvents(): Result<List<DatabaseEventApi>> =
        withContext(Dispatchers.IO) {
            val get = client.get<List<EventApi>>(Path.database())
            return@withContext get as Result<List<DatabaseEventApi>>
        }

    override suspend fun portForwardEvents(): Result<List<PortForwardEventApi>> =
        withContext(Dispatchers.IO) {
            val get = client.get<List<EventApi>>(Path.portForward())
            return@withContext get as Result<List<PortForwardEventApi>>
        }
}
