package io.github.hoeggi.openshiftdb.server

import com.codahale.metrics.jmx.JmxReporter
import io.github.hoeggi.openshiftdb.api.CancelableFlow
import io.github.hoeggi.openshiftdb.api.OcApi
import io.github.hoeggi.openshiftdb.api.PostgresApi
import io.github.hoeggi.openshiftdb.api.response.DatabaseDownloadMessage
import io.github.hoeggi.openshiftdb.api.response.DatabasesApi
import io.github.hoeggi.openshiftdb.api.response.PortForwardMessage
import io.github.hoeggi.openshiftdb.postgres.Authenticator
import io.github.hoeggi.openshiftdb.server.routes.default
import io.github.hoeggi.openshiftdb.server.routes.oc
import io.github.hoeggi.openshiftdb.server.routes.postgres
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.metrics.dropwizard.*
import io.ktor.metrics.micrometer.*
import io.ktor.request.*
import io.ktor.routing.*
import io.ktor.serialization.*
import io.ktor.server.cio.backend.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.websocket.*
import io.micrometer.prometheus.PrometheusConfig
import io.micrometer.prometheus.PrometheusMeterRegistry
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.serialization.json.Json
import okhttp3.HttpUrl.Companion.toHttpUrl
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.slf4j.event.Level
import java.io.IOException
import java.net.ServerSocket
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.coroutines.CoroutineContext


class Server

val logging = LoggerFactory.getLogger(Server::class.java)

fun main(args: Array<String>) {
    val randomPort = port()
    println("port used: $randomPort}")
//----------------------
    test()
//----------------------
    println("starting server")
    embeddedServer(Netty, environment = applicationEngineEnvironment {
        log = LoggerFactory.getLogger("ktor")
        module {
            val appMicrometerRegistry = PrometheusMeterRegistry(PrometheusConfig.DEFAULT)
            features(appMicrometerRegistry)
            routes(appMicrometerRegistry)
        }
        connector {
            port = 32000
            host = "localhost"
        }
    }).start(true)

}

fun test() {

    val build = "http://example.com/v1".toHttpUrl().newBuilder()
        .addEncodedPathSegments("test/test")
        .addPathSegments("test2/test2").build()
    val toString = build.toString()
    Executors.newSingleThreadExecutor().execute {
        GlobalScope.launch(newFixedThreadPoolContext(4, "test")) {
            logging.debug("waiting for server")
            delay(3000)
            logging.debug("making api request")
            val take = AtomicBoolean(true)
            val oc = OcApi.api()

            val async = async(Dispatchers.IO + CoroutineName("pf")) {
                val response = oc.portForward("playground-hoeggi", "postgres12", "5432")
                response.collect {
                    ensureActive()
                    logging.debug("message from port-forward: $it")
                }
            }
            val pw = async(Dispatchers.IO + CoroutineName("pw")) {
                oc.password("postgres")
            }.await()
            logging.debug("$pw")
            delay(3000)
            val version = oc.version()
            logging.debug("$version")
            val api = PostgresApi.api()
            val defaultDatabases = api.defaultDatabases("postgres", pw.getOrNull() ?: "")
            logging.debug("$defaultDatabases")
//            val databasestable = api.databases("postgres", pw.getOrNull() ?: "", PostgresApi.DatabaseViewFormat.Table)
//            logging.debug("$databasestable")
//            delay(1000)
//            val databasessingle = api.databases("postgres", pw.getOrNull() ?: "", PostgresApi.DatabaseViewFormat.Text)
//            logging.debug("$databasessingle")
//            delay(1000)
//            val databases = api.databases("postgres", pw.getOrNull() ?: "")
//            logging.debug("$databases")
//            delay(1000)
//            val databaseslist = api.databases("postgres", pw.getOrNull() ?: "", PostgresApi.DatabaseViewFormat.List)
//            logging.debug("$databaseslist")
//            delay(1000)
//
//            val dump = async(Dispatchers.IO + CoroutineName("pf")) {
//                val dumpDatabases = api.dumpDatabases(
//                    "postgres",
//                    pw.getOrNull() ?: "",
//                    "postgres",
//                    "/home/hoeggi/repos/name-that-color-desktop/server"
//                )
//                dumpDatabases.collect {
//                    ensureActive()
//                    logging.debug("---------$it---------")
//                    if (it is DatabaseDownloadMessage.FinishMessage || it is DatabaseDownloadMessage.ErrorMessage) {
//                        cancel()
//                    }
//                }
//            }.await()
//
//            async.cancel()
        }
    }
}

private fun Application.routes(appMicrometerRegistry: PrometheusMeterRegistry) {
    routing {
        default(appMicrometerRegistry)
        route("/v1") {
            oc()
            postgres()
        }
    }
}

private fun Application.features(appMicrometerRegistry: PrometheusMeterRegistry) {
    install(WebSockets)
    install(DefaultHeaders)
    install(Compression)
    install(ContentNegotiation) {
        json(
            json = Json {
                useArrayPolymorphism = false
            }
        )
    }
    val authenticator = Authenticator()
    install(Authentication) {
        basic(name = "postgres") {
            realm = "Ktor Postgres"
            validate(authenticator.validateCredentials())
        }
    }

    install(CallLogging) {
        logger = LoggerFactory.getLogger("ktor.calls")
        level = Level.DEBUG
        callIdMdc(HttpHeaders.XRequestId)
        mdc("request") { call ->
            call.request.toLogString()
        }
        mdc(HttpHeaders.XRequestId) { call ->
            call.response.headers[HttpHeaders.XRequestId]
        }
        mdc(HttpHeaders.UserAgent) { call ->
            call.request.header(HttpHeaders.UserAgent)
        }
        format { call ->
            "[${
                MDC.getCopyOfContextMap().apply {
                    put("status", "${call.response.status()}")
                }
            }]"
        }
    }
    install(CallId) {
        retrieve { call: ApplicationCall ->
            call.request.header(HttpHeaders.XRequestId)
        }
        generate {
            "${UUID.randomUUID()}-server-generated"
        }

        verify { callId: String ->
            callId.isNotEmpty()
        }
        replyToHeader(HttpHeaders.XRequestId)

    }
    install(DropwizardMetrics) {
        JmxReporter.forRegistry(registry)
            .convertRatesTo(TimeUnit.SECONDS)
            .convertDurationsTo(TimeUnit.MILLISECONDS)
            .build()
            .start()
    }
    install(MicrometerMetrics) {
        registry = appMicrometerRegistry
    }
}

@Throws(IOException::class)
private fun port(): Int {
    ServerSocket(0).use { socket -> return socket.localPort }
}