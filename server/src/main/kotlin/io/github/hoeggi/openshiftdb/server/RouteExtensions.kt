package io.github.hoeggi.openshiftdb.server

import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.http.HttpMethod
import io.ktor.request.receive
import io.ktor.routing.HttpMethodRouteSelector
import io.ktor.routing.Route
import io.ktor.routing.createRouteFromPath
import io.ktor.util.pipeline.ContextDsl
import io.ktor.util.pipeline.PipelineContext
import io.ktor.util.pipeline.PipelineInterceptor
import io.ktor.websocket.DefaultWebSocketServerSession
import io.ktor.websocket.webSocket

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
