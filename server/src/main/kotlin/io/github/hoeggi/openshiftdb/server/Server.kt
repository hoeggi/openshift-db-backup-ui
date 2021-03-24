package io.github.hoeggi.openshiftdb.server

import com.codahale.metrics.jmx.JmxReporter
import io.github.hoeggi.openshiftdb.api.response.SerializersModule
import io.github.hoeggi.openshiftdb.postgres.Authenticator
import io.github.hoeggi.openshiftdb.server.handler.postgres.TransactionLogger
import io.github.hoeggi.openshiftdb.server.routes.default
import io.github.hoeggi.openshiftdb.server.routes.oc
import io.github.hoeggi.openshiftdb.server.routes.postgres
import io.ktor.application.Application
import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.auth.Authentication
import io.ktor.auth.basic
import io.ktor.features.CallId
import io.ktor.features.CallLogging
import io.ktor.features.Compression
import io.ktor.features.ContentNegotiation
import io.ktor.features.DefaultHeaders
import io.ktor.features.callIdMdc
import io.ktor.features.toLogString
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.metrics.dropwizard.DropwizardMetrics
import io.ktor.metrics.micrometer.MicrometerMetrics
import io.ktor.request.header
import io.ktor.request.queryString
import io.ktor.response.respond
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.routing
import io.ktor.serialization.json
import io.ktor.server.engine.applicationEngineEnvironment
import io.ktor.server.engine.connector
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.websocket.WebSockets
import io.micrometer.prometheus.PrometheusConfig
import io.micrometer.prometheus.PrometheusMeterRegistry
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.slf4j.event.Level
import java.util.UUID
import java.util.concurrent.TimeUnit

class Server(private val listeningPort: Int) : Runnable {
    private val logging = LoggerFactory.getLogger(Server::class.java)

    override fun run() {
        logging.info("port used: $listeningPort}")
        embeddedServer(
            Netty,
            environment = applicationEngineEnvironment {
                log = LoggerFactory.getLogger("ktor")
                module {
                    val appMicrometerRegistry = PrometheusMeterRegistry(PrometheusConfig.DEFAULT)
                    features(appMicrometerRegistry)
                    routes(appMicrometerRegistry)
                }
                connector {
                    port = listeningPort
                    host = "localhost"
                }
            }
        ).start(false)
    }

    private fun Application.routes(appMicrometerRegistry: PrometheusMeterRegistry) {
        routing {
            default(appMicrometerRegistry)
            route(Path.v1()) {
                get {
                    call.respond(HttpStatusCode.OK, "/v1")
                }
                oc()
                postgres()
                route(Path.events()) {
                    route(Path.database()) {
                        get(TransactionLogger.transactions)
                        post(TransactionLogger.newTransaction)
                    }
                    route(Path.portForward()) {
                        get(TransactionLogger.transactions)
                        post(TransactionLogger.newTransaction)
                    }
                }
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
                    serializersModule = SerializersModule
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
            level = Level.INFO
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
            mdc("query") { call ->
                call.request.queryString()
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
}
