package io.github.hoeggi.openshiftdb.server.handler.oc

import io.github.hoeggi.openshiftdb.api.response.LoginApi
import io.github.hoeggi.openshiftdb.oc.OC
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.util.pipeline.*
import org.slf4j.LoggerFactory

class LoginLogger

private val logger = LoggerFactory.getLogger(LoginLogger::class.java)

fun CheckLogin(): suspend PipelineContext<Unit, ApplicationCall>.(Unit) -> Unit =
    {
        send { OC.checkLogin() }
    }

fun Login(): suspend PipelineContext<Unit, ApplicationCall>.(Unit) -> Unit =
    {
        val login = call.receiveOrNull<LoginApi>()
        if (login == null) {
            call.respond(HttpStatusCode.Unauthorized)
        } else {
            send { OC.login(login.token, login.server) }
        }

    }

private fun send(block: suspend () -> OC.OcResult.LoginState): suspend PipelineContext<Unit, ApplicationCall>.(Unit) -> Unit =
    {
        when (val result = block()) {
            OC.OcResult.LoginState.LoggedIn -> call.respond(HttpStatusCode.NoContent)
            is OC.OcResult.LoginState.NotLogedIn -> {
                logger.debug("unauthorizes, process exited with: ${result.result}")
                call.respond(HttpStatusCode.Unauthorized)
            }
        }
    }