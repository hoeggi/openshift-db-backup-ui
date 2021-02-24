package io.github.hoeggi.openshiftdb.postgres

import io.github.hoeggi.openshiftdb.commons.Command

internal sealed class Commands(vararg commands: String) : Command {

    sealed class Psql(vararg _commands: String) : Commands(*_commands) {
        override val commands = listOf("psql", "-h", "localhost", "-p", "5432", *_commands)

        object Version : Psql("-V")

        sealed class WithUser(username: String, vararg commands: String) : Psql("-U", username, *commands) {
            class ConnectionCheck(username: String) : WithUser(username, "-c", "\\q")
            class List(username: String) : WithUser(
                username, "-q", "-A", "-t",
                "-c", "SELECT datname FROM pg_database"
            )

            class ListPretty(username: String) : WithUser(username, "-c", "\\l")
            class DefaultDb(username: String) : WithUser(
                username, "-q", "-A", "-t",
                "-c", "select current_database();"
            )

            class DatabaseVersion(username: String) : WithUser(
                username, "-q", "-A", "-t",
                "-c", "select version();"
            )
        }
    }

    sealed class PgDump(vararg _commands: String) : Commands(*_commands) {
        override val commands = listOf("pg_dump", *_commands)

        object Version : PgDump("--version")
        class Dump(username: String, database: String) : PgDump(
            "--user=$username",
            "--host=localhost",
            "--port=5432",
            "--clean", "--create", "--format=p",
            database,
        )
    }
}