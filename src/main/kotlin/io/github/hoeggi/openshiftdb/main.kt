package io.github.hoeggi.openshiftdb

import com.formdev.flatlaf.FlatDarculaLaf
import com.formdev.flatlaf.FlatDarkLaf
import io.github.hoeggi.openshiftdb.errorhandler.ExceptionHandler
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
    val port = port()
    val globalState = GlobalState()
    val logDelegate = Logger(globalState)

    FlatDarculaLaf.install()
    UIManager.setLookAndFeel(FlatDarculaLaf())//UIManager.getSystemLookAndFeelClassName())
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