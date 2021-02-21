package io.github.hoeggi.openshiftdb.process

import kotlinx.coroutines.*
import java.util.concurrent.Executors


class PortForward(
    private val onNewLine: suspend (String) -> Unit,
    private val onNewErrorLine: suspend (String) -> Unit
) {
    private val oc = OC()
    private var portForward: OC.PortForward? = null

//    private val dispatcher = Dispatchers.IO//Executors.newFixedThreadPool(3).asCoroutineDispatcher()

    fun stop() {
        portForward?.stop()
    }

    suspend fun open(target: OC.PortForwardTarget) = withContext(Dispatchers.IO + CoroutineName("$target")) {
        if (portForward != null) throw IllegalStateException("port-forward is already running")
        try {
            portForward = oc.portForward(target.projectName, target.serviceName, target.port)
            val stream = async(
                Dispatchers.IO + CoroutineName("port-forward-stream")
            ) {
                while (isActive) {
                    val readUtf8Line = portForward?.buffer?.readUtf8Line() ?: break
                    onNewLine(readUtf8Line)
                }
            }

            val error = async(
                Dispatchers.IO + CoroutineName("port-forward-errorstream")
            ) {
//                val buffer = portForward?.bufferError()
                while (isActive) {
                    val readUtf8Line = portForward?.bufferError?.readUtf8Line() ?: break
                    onNewErrorLine(readUtf8Line)
                }
            }
            stream.await()
            error.await()
        } catch (e: Throwable) {
            e.printStackTrace()
        } finally {
            portForward?.stop()
        }
    }

}
