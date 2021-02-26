package io.github.hoeggi.openshiftdb.server

import TestClient
import com.codahale.metrics.jmx.JmxReporter
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
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.websocket.*
import io.micrometer.prometheus.PrometheusConfig
import io.micrometer.prometheus.PrometheusMeterRegistry
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.slf4j.event.Level
import java.io.IOException
import java.net.ServerSocket
import java.util.*
import java.util.concurrent.TimeUnit


class Server

val logging = LoggerFactory.getLogger(Server::class.java)

fun main(args: Array<String>) {
    val randomPort = port()
    logging.info("port used: $randomPort}")
    TestClient().test(0)
    embeddedServer(Netty, environment = applicationEngineEnvironment {
        log = LoggerFactory.getLogger("ktor")
        module {
            val appMicrometerRegistry = PrometheusMeterRegistry(PrometheusConfig.DEFAULT)
            features(appMicrometerRegistry)
            routes(appMicrometerRegistry)
        }
        connector {
            port = randomPort
            host = "localhost"
        }
    }).start(true)

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