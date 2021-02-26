package io.github.hoeggi.openshiftdb

import io.github.hoeggi.openshiftdb.server.Server
import io.github.hoeggi.openshiftdb.ui.composables.navigation.GlobalState
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
    val globalState = GlobalState()
    val logDelegate = Logger(globalState)
    val port = port()
    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())
    Thread.setDefaultUncaughtExceptionHandler(ExceptionHandler(globalState))
    logger.info("starting server")
    Executors.newSingleThreadExecutor().execute(Server(port))
    logger.info("starting ui")
    UI().show(port, globalState) {
        ProcessHandle.current()
            .children()
            .forEach {
                it.destroy()
            }
        logDelegate.stop()
    }
}