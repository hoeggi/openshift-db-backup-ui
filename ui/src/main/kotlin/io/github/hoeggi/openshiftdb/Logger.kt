package io.github.hoeggi.openshiftdb

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import com.google.common.collect.EvictingQueue
import io.github.hoeggi.openshiftdb.ui.composables.ColorMapping
import io.github.hoeggi.openshiftdb.ui.composables.navigation.GlobalState
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import net.rubygrapefruit.ansi.AnsiParser
import net.rubygrapefruit.ansi.token.ForegroundColor
import net.rubygrapefruit.ansi.token.NewLine
import net.rubygrapefruit.ansi.token.Text
import okio.Pipe
import okio.buffer
import java.io.PrintStream
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean

class Logger(private val globalState: GlobalState) {

    private val stdout: InterceptingStream = InterceptingStream(System.out)
    private val stderr: InterceptingStream = InterceptingStream(System.err)
    private val queue = EvictingQueue.create<AnnotatedString>(250)
    private val running = AtomicBoolean(true)

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
                    val parsed = parseLine(line)
                    globalState.updateSyslog(queue.apply {
                        add(parsed)
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
                    AnnotatedString(text = "", spanStyle = SpanStyle())
                    val parsed = parseLine(line)
                    globalState.updateSyslog(queue.apply {
                        add(parsed)
                    }.toList())
                }
            }
        }
    }

    fun parseLine(line: String): AnnotatedString {
        return runBlocking {
            val rendezvousChannel = Channel<AnnotatedString>()
            val builder = AnnotatedString.Builder()
            var pushCount = 0
            val ansiParser = AnsiParser().newParser("utf-8") {
                when (it) {
                    is ForegroundColor -> {
                        val color = ColorMapping.colors[it.color]
                        if (color != null) {
                            builder.pushStyle(
                                SpanStyle(
                                    color = color
                                )
                            )
                            pushCount++
                        } else if (it == ForegroundColor.DEFAULT) {
                            try {
                                if (pushCount > 0) {
                                    pushCount--
                                    builder.pop()
                                }
                            } catch (ex: IllegalStateException) {
                                ex.printStackTrace()
                            }
                        }
                    }
                    is Text -> {
                        builder.append(it.text.trim())
                    }
                    is NewLine -> {
                        pushCount = 0
                        launch {
                            rendezvousChannel.send(builder.toAnnotatedString())
                        }
                    }
                }
            }.writer()
            ansiParser.appendLine(line)
            ansiParser.flush()
            return@runBlocking withContext(Dispatchers.Default) {
                val receive = rendezvousChannel.receive()
                receive
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
    }
}