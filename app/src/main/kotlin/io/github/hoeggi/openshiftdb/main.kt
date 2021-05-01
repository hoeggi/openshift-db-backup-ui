package io.github.hoeggi.openshiftdb

import com.formdev.flatlaf.FlatDarculaLaf
import io.fabric8.kubernetes.api.model.Secret
import io.fabric8.kubernetes.api.model.SecretList
import io.fabric8.kubernetes.client.dsl.MixedOperation
import io.fabric8.kubernetes.client.dsl.Resource
import io.fabric8.openshift.client.DefaultOpenShiftClient
import io.github.hoeggi.openshiftdb.server.Server
import org.slf4j.LoggerFactory
import java.io.IOException
import java.net.ServerSocket
import javax.swing.UIManager

class Main

@Throws(IOException::class)
private fun port(): Int {
    ServerSocket(0).use { socket -> return socket.localPort }
}

private val logger = LoggerFactory.getLogger(Main::class.java)
fun main() {

    logger.info("initializing")
    val port = port()
    Thread.setDefaultUncaughtExceptionHandler(ExceptionHandler())
    UIManager.setLookAndFeel(FlatDarculaLaf())
    logger.info("starting server")
    Server(port).run()
    logger.info("starting ui")

    val defaultOpenShiftClient = DefaultOpenShiftClient()
    val secrets=defaultOpenShiftClient.services().withName("")
        .portForward(80, 8080)

    UI().show(port) {
        ProcessHandle.current()
            .children()
            .forEach {
                it.destroy()
            }
    }
}

private class ExceptionHandler : Thread.UncaughtExceptionHandler {
    private val logger = LoggerFactory.getLogger(ExceptionHandler::class.java)
    override fun uncaughtException(t: Thread?, e: Throwable?) {
        logger.error("$t", e)
    }
}
