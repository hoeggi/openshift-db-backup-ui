package io.github.hoeggi.openshiftdb.server

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.routing.*
import io.ktor.util.pipeline.*
import io.ktor.websocket.*

internal fun Route.webSocket(
    path: Routes,
    handler: suspend DefaultWebSocketServerSession.() -> Unit,
) = webSocket(path.path, null, handler)

/**
 * Builds a route to match specified [path]
 */
@ContextDsl
internal fun Route.route(path: Routes, build: Route.() -> Unit): Route = createRouteFromPath(path.path).apply(build)

/**
 * Builds a route to match specified [method] and [path]
 */
@ContextDsl
internal fun Route.route(path: Routes, method: HttpMethod, build: Route.() -> Unit): Route {
    val selector = HttpMethodRouteSelector(method)
    return createRouteFromPath(path.path).createChild(selector).apply(build)
}

/**
 * Builds a route to match `GET` requests with specified [path]
 */
@ContextDsl
internal fun Route.get(path: Routes, body: PipelineInterceptor<Unit, ApplicationCall>): Route {
    return route(path, HttpMethod.Get) { handle(body) }
}

/**
 * Builds a route to match `POST` requests with specified [path]
 */
@ContextDsl
internal fun Route.post(path: Routes, body: PipelineInterceptor<Unit, ApplicationCall>): Route {
    return route(path, HttpMethod.Post) { handle(body) }
}

/**
 * Builds a route to match `POST` requests with specified [path] receiving request body content of type [R]
 */
@ContextDsl
@JvmName("postTyped")
internal inline fun <reified R : Any> Route.post(
    path: Routes,
    crossinline body: suspend PipelineContext<Unit, ApplicationCall>.(R) -> Unit,
): Route {
    return route(path, HttpMethod.Post) {
        handle {
            body(call.receive())
        }
    }
}
