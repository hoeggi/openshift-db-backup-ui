package io.github.hoeggi.openshiftdb.postgres

import io.ktor.application.*
import io.ktor.auth.*
import org.slf4j.LoggerFactory

data class PostgresPrincibal(val username: String, val password: String) : Principal
class Authenticator {

    private fun authenticationCommand(username: String) =
        listOf("psql", "-h", "localhost", "-p", "5432", "-U", username, "-c", "\\q")

    val logger = LoggerFactory.getLogger(Authenticator::class.java)

    fun validateCredentials(): suspend ApplicationCall.(UserPasswordCredential) -> Principal? =
        { credentials ->
            logger.debug("validating credentials: $credentials")
            val result = ProcessBuilder(authenticationCommand(credentials.name))
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