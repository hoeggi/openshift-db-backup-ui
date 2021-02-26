package io.github.hoeggi.openshiftdb

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import com.google.common.collect.EvictingQueue
import io.github.hoeggi.openshiftdb.ui.composables.navigation.GlobalState
import okio.Pipe
import okio.buffer
import java.io.PrintStream
import java.time.LocalTime
import java.time.temporal.ChronoUnit
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean


class Logger(private val globalState: GlobalState) {

    private val stdout: InterceptingStream = InterceptingStream(System.out)
    private val stderr: InterceptingStream = InterceptingStream(System.err)
    private val queue = EvictingQueue.create<AnnotatedString>(250)
    private val running = AtomicBoolean(true)
    private val errorStyle = SpanStyle(
        color = Color.Red
    )

    init {
        System.setOut(stdout)
        forwardStdout()
        System.setErr(stderr)
        forwardStderr()
    }

    fun stop() {
        running.set(false)
        System.setOut(stdout.delegate)
        System.setErr(stderr.delegate)
    }

    private fun forwardStdout() {
        Executors.newSingleThreadExecutor().execute {

            while (running.get()) {
                val line = stdout.source.readUtf8Line()
                if (line != null) {
                    val annotatedString =
                        AnnotatedString("${LocalTime.now().truncatedTo(ChronoUnit.MILLIS)}/System.out: $line")
                    globalState.updateSyslog(queue.apply {
                        add(annotatedString)
                    }.toList())
                }
            }
        }
    }

    private fun forwardStderr() {
        Executors.newSingleThreadExecutor().execute {
            while (running.get()) {
                val line = stderr.source.readUtf8Line()
                if (line != null) {
                    val annotatedString = AnnotatedString(
                        "${LocalTime.now().truncatedTo(ChronoUnit.MILLIS)}/System.err: $line",
                        errorStyle
                    )
                    globalState.updateSyslog(queue.apply {
                        add(annotatedString)
                    }.toList())
                }
            }
        }
    }

    private class InterceptingStream(delegate: PrintStream) : PrintStream(delegate) {

        val delegate: PrintStream = PrintStream(delegate)

        private val pipe = Pipe(1024)

        val source = pipe.source.buffer()
        private val sink = pipe.sink.buffer()

        override fun write(c: Int) {
            sink.write(ByteArray(1).apply {
                set(0, c.toByte())
            }).flush()
            super.write(c)
        }

        override fun write(buf: ByteArray, off: Int, len: Int) {
            sink.write(buf, off, len).flush()
            super.write(buf, off, len)
        }

        override fun write(buf: ByteArray) {
            sink.write(buf).flush()
            super.write(buf)
        }
    }
}