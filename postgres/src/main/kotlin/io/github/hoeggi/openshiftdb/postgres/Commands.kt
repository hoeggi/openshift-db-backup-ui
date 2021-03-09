package io.github.hoeggi.openshiftdb.postgres

import io.github.hoeggi.openshiftdb.commons.Command
import java.io.File
import java.time.LocalDateTime

internal sealed class Commands(override val commands: List<String>) : Command {

    sealed class Psql(vararg _commands: String) :
        Commands(listOf("psql", "-h", "localhost", "-p", "5432", *_commands)) {

        object Version : Psql("-V")

        sealed class WithUser(username: String, vararg commands: String) : Psql("-U", username, *commands) {
            class ConnectionCheck(username: String) : WithUser(username, "-c", "\\q")
            class List(username: String) : WithUser(
                username, "-q", "-A", "-t",
                "-c", "SELECT datname FROM pg_database;"
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


    sealed class PgRestore(vararg _commands: String) :
        Commands(listOf("pg_restore", "-h", "localhost", "-p", "5432", *_commands)) {

        class Info(path: String) : PgRestore() {
            override val commands = listOf("pg_restore", "--list", path)
        }

        class RestoreExisting(user: String, database: String, path: String) : PgRestore(
            "-U", user, "-c", "-d", database, path
        )

        class RestoreNew(user: String, path: String) : PgRestore(
            "-U", user, "-C", "-d", Postgres.DEFAULT_DB, path
        )
    }

    sealed class PgDump(vararg _commands: String) :
        Commands(listOf("pg_dump", *_commands)) {

        object Version : PgDump("--version")
        class DumpPlain(username: String, database: String) : PgDump(
            "--user=$username",
            "--host=localhost",
            "--port=5432",
            "--clean", "--create", "--format=p",
            "--dbname=$database",
        )

        class DumpCustom(username: String, database: String, path: File) : PgDump(
            "--user=$username",
            "--host=localhost",
            "--port=5432",
            "--clean", "--create", "--format=c",
            "--dbname=$database",
            "--file=${path.absolutePath}"
        )
    }
}