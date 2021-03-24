package io.github.hoeggi.openshiftdb.server.handler.oc

import io.github.hoeggi.openshiftdb.api.response.LoginApi
import io.github.hoeggi.openshiftdb.oc.OC
import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.request.receiveOrNull
import io.ktor.response.respond
import io.ktor.util.pipeline.PipelineContext
import org.slf4j.LoggerFactory

internal class LoginLogger

private val logger = LoggerFactory.getLogger(LoginLogger::class.java)

internal fun CheckLogin(): suspend PipelineContext<Unit, ApplicationCall>.(Unit) -> Unit =
    {
        logger.debug("checking login state")
        when (val result = OC.checkLogin()) {
            OC.OcResult.LoginState.LoggedIn -> call.respond(HttpStatusCode.NoContent)
            is OC.OcResult.LoginState.NotLogedIn -> {
                logger.debug("unauthorizes, process exited with: ${result.result}")
                call.respond(HttpStatusCode.Unauthorized)
            }
        }
    }

internal fun Login(): suspend PipelineContext<Unit, ApplicationCall>.(Unit) -> Unit =
    {
        val login = call.receiveOrNull<LoginApi>()
        if (login == null) {
            call.respond(HttpStatusCode.Unauthorized)
        } else {
            when (val result = OC.login(login.token, login.server)) {
                OC.OcResult.LoginState.LoggedIn -> call.respond(HttpStatusCode.NoContent)
                is OC.OcResult.LoginState.NotLogedIn -> {
                    logger.debug("unauthorizes, process exited with: ${result.result}")
                    call.respond(HttpStatusCode.Unauthorized)
                }
            }
        }
    }
