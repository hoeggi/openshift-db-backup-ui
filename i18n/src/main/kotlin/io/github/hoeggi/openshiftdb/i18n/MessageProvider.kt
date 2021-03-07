package io.github.hoeggi.openshiftdb.i18n

import java.text.MessageFormat
import java.util.*

object MessageProvider {

    private val messages: ResourceBundle = ResourceBundle.getBundle("messages")
    private val formatter = MessageFormat("")

    fun message(message: String, vararg arguments: Any?): String {
        if (arguments.isEmpty()) return messages.getString(message)
        formatter.applyPattern(messages.getString(message))
        return formatter.format(arguments)
    }


    const val ERROR_MESSAGE = "error.message"
    const val ERROR_HEADER = "error.header"
    const val GLOBAL_EXIT = "global.exit"
    const val GLOBAL_OK = "global.ok"
    const val SECRETS_LABEL = "secrets.label"


    const val THEME_LABEL = "theme.label"
    const val THEME_LIGHT = "theme.light"
    const val THEME_DARK = "theme.dark"

    const val LOGIN_TOKEN_HINT = "login.token.hint"
    const val LOGIN_LABEL = "login.label"

    const val SYSLOG_LABEL = "syslog.label"

    const val OC_PORTFORWARD_LABEL = "oc.portforward.label"
    const val OC_PORTFORWARD_PROJECT = "oc.portforward.project"
    const val OC_PORTFORWARD_SERVICE = "oc.portforward.service"
    const val OC_PORTFORWARD_PORT = "oc.portforward.port"
    const val OC_PORTFORWARD_STREAM_LABEL = "oc.portforward.stream.label"

    const val OC_SERVICE_AVAILABLE = "oc.service.available"
    const val OC_VERSION_LABEL = "oc.version.label"
    const val OC_VERSION_TEXT = "oc.version.text"
    const val OC_PROJECT_CURRENT_LABEL = "oc.project.current.label"
    const val OC_PROJECT_ALL_LABEL = "oc.project.all.label"

    const val POSTGRES_EXPORT_FORMAT_LABEL = "postgres.export.format.label"
    const val POSTGRES_EXPORT_FORMAT_CUSTOM = "postgres.export.format.custom"
    const val POSTGRES_EXPORT_FORMAT_PLAIN = "postgres.export.format.plain"
    const val POSTGRES_PASSWORD_DETECT = "postgres.password.detect"
    const val POSTGRES_PASSWORD_LABEL = "postgres.password.label"
    const val POSTGRES_DUMP_LOADING = "postgres.dump.loading"
    const val POSTGRES_DUMP_SUCCESS = "postgres.dump.success"
    const val POSTGRES_DUMP_LABEL = "postgres.dump.label"
    const val POSTGRES_TABLE_LABEL = "postgres.table.label"
    const val POSTGRES_CONNECT_LABEL = "postgres.connect.label"
    const val POSTGRES_CONNECTION_LABEL = "postgres.connection.label"
    const val POSTGRES_USERNAME_HINT = "postgres.username.hint"
    const val POSTGRES_USERNAME_LABEL = "postgres.username.label"
    const val POSTGRES_VERSION_LABEL = "postgres.version.label"
}