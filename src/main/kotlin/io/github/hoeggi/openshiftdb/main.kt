package io.github.hoeggi.openshiftdb

import com.formdev.flatlaf.FlatDarculaLaf
import io.github.hoeggi.openshiftdb.server.Server
import org.slf4j.LoggerFactory
import java.io.IOException
import java.net.ServerSocket
import java.util.concurrent.Executors
import javax.swing.UIManager


class Main

@Throws(IOException::class)
private fun port(): Int {
    ServerSocket(0).use { socket -> return socket.localPort }
}

private val logger = LoggerFactory.getLogger(UI::class.java)
fun main() {
    logger.info("initializing")
    val port = port()
    Logger.start()
    Thread.setDefaultUncaughtExceptionHandler(ExceptionHandler())
    UIManager.setLookAndFeel(FlatDarculaLaf())
    logger.info("starting server")
    Executors.newSingleThreadExecutor().execute(Server(port))
    logger.info("starting ui")
    UI().show(port) {
        ProcessHandle.current()
            .children()
            .forEach {
                it.destroy()
            }
        Logger.stop()
    }
}

private class ExceptionHandler : Thread.UncaughtExceptionHandler {
    private val logger = LoggerFactory.getLogger(ExceptionHandler::class.java)
    override fun uncaughtException(t: Thread?, e: Throwable?) {
        logger.error("$t", e)
    }
}