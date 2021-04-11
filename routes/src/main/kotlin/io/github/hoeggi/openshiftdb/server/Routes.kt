package io.github.hoeggi.openshiftdb.server

val Path = Routes("")

data class Routes internal constructor(val path: String)

fun Routes.v1() = copy(path = "$path/v1")
fun Routes.version() = copy(path = "$path/version")

fun Routes.oc() = copy(path = "$path/oc")

fun Routes.server() = copy(path = "$path/server")
fun Routes.services() = copy(path = "$path/services")
fun Routes.context() = copy(path = "$path/context")
fun Routes.login() = copy(path = "$path/login")
fun Routes.projects() = copy(path = "$path/projects")
fun Routes.current() = copy(path = "$path/current")
fun Routes.secrets() = copy(path = "$path/secrets")
fun Routes.password() = copy(path = "$path/password")
fun Routes.portForward() = copy(path = "$path/port-forward")

fun Routes.postgres() = copy(path = "$path/postgres")

fun Routes.tools() = copy(path = "$path/tools")
fun Routes.database() = copy(path = "$path/database")
fun Routes.default() = copy(path = "$path/default")
fun Routes.databases() = copy(path = "$path/databases")
fun Routes.dump() = copy(path = "$path/dump")
fun Routes.restore() = copy(path = "$path/restore")
fun Routes.info() = copy(path = "$path/info")
fun Routes.command() = copy(path = "$path/command")

fun Routes.events() = copy(path = "$path/events")
fun Routes.log() = copy(path = "$path/log")

fun Routes.asPathSegment() = if (path.startsWith("/")) path.substring(1) else path
