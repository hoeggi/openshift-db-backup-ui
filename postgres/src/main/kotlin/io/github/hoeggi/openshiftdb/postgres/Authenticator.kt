package io.github.hoeggi.openshiftdb.postgres

import io.ktor.application.*
import io.ktor.auth.*
import org.slf4j.LoggerFactory

data class PostgresPrincibal(val username: String, val password: String) : Principal
class Authenticator {

    val logger = LoggerFactory.getLogger(Authenticator::class.java)

    fun validateCredentials(): suspend ApplicationCall.(UserPasswordCredential) -> Principal? =
        { credentials ->
            logger.debug("validating credentials: $credentials")
            val result = ProcessBuilder(Commands.Psql.WithUser.ConnectionCheck(credentials.name).commands)
                .also {
                    it.environment()["PGPASSWORD"] = credentials.password
                }.start().waitFor()
            logger.debug("validation result: $result")
            if (result == 0) {
                PostgresPrincibal(credentials.name, credentials.password)
            } else {
                null
            }
        }

}