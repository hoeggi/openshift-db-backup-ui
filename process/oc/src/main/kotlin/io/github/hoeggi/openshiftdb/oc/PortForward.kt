package io.github.hoeggi.openshiftdb.oc

import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory

class PortForward(
    private val onStart: suspend (String) -> Unit,
    private val onNewLine: suspend (String) -> Unit,
    private val onNewErrorLine: suspend (String) -> Unit,
    private val onClosed: suspend (String) -> Unit,
) {
    private val logger = LoggerFactory.getLogger(PortForward::class.java)

    private val oc = OC
    private var portForward: OC.PortForward? = null

    fun stop() {
        portForward?.stop()
        logger.info("port-forward: $portForward stopped")
    }

    suspend fun open(target: OC.PortForwardTarget) = withContext(Dispatchers.IO + CoroutineName("$target")) {
        if (portForward != null) throw IllegalStateException("port-forward is already running")
        try {
            portForward = oc.portForward(target.projectName, target.serviceName, target.port)
            logger.info("port-forward: $portForward openend")
            onStart("Forwarding port ${target.port}")
            val stream = async(
                Dispatchers.IO + CoroutineName("port-forward-stream")
            ) {
                while (isActive) {
                    val readUtf8Line = portForward?.buffer?.readUtf8Line() ?: break
                    logger.debug("read line from stream $readUtf8Line")
                    onNewLine(readUtf8Line)
                }
            }
            val error = async(
                Dispatchers.IO + CoroutineName("port-forward-errorstream")
            ) {
                while (isActive) {
                    val readUtf8Line = portForward?.bufferError?.readUtf8Line() ?: break
                    logger.debug("read line from errorstream $readUtf8Line")
                    onNewErrorLine(readUtf8Line)
                }
            }
            awaitAll(stream, error)
        } catch (e: Throwable) {
            logger.error("error forwarding port", e)
        } finally {
            logger.debug("finally close")
            portForward?.stop()
            onClosed(portForward?.exitMessage ?: "closed for unknown reason")
        }
    }
}
