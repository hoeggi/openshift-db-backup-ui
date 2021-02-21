package io.github.hoeggi.openshiftdb.backend

import com.codahale.metrics.jmx.JmxReporter
import io.github.hoeggi.openshiftdb.process.Postgres
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.features.*
import io.ktor.gson.*
import io.ktor.http.*
import io.ktor.metrics.dropwizard.*
import io.ktor.metrics.micrometer.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.websocket.*
import io.micrometer.prometheus.PrometheusConfig
import io.micrometer.prometheus.PrometheusMeterRegistry
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.slf4j.event.Level
import java.io.IOException
import java.net.ServerSocket
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit


class Server

fun main(args: Array<String>) {
    val port = port()

    Executors.newSingleThreadExecutor().execute {
        Thread.sleep(5000)
        var testClient = TestClient()
//        testClient.runVersion()
//        testClient.runVersionBasicAuth()
        testClient.runWebsocket()
    }
    println("port used: $port}")
    val start = embeddedServer(Netty, port = 32000, host = "localhost") {
        val appMicrometerRegistry = PrometheusMeterRegistry(PrometheusConfig.DEFAULT)
        features(appMicrometerRegistry)
        routes(appMicrometerRegistry)

    }.start(wait = true)
}

private fun Application.routes(appMicrometerRegistry: PrometheusMeterRegistry) {
    routing {
        get("/metrics") {
            call.respond(appMicrometerRegistry.scrape())
        }
        get("/test") {
            call.respondText("test endpoint")
        }
        route("/v1") {
            oc()
        }
    }
}

private fun Application.features(appMicrometerRegistry: PrometheusMeterRegistry) {
    install(WebSockets)
    install(DefaultHeaders)
    install(Compression)
    install(ContentNegotiation) {
        gson()
    }

    install(Authentication) {
        basic(name = "postgres") {
            realm = "Ktor Postgres"
            validate(Postgres.validateCredentials())
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