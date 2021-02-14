package com.theapache64.ntcdesktop.process

import com.theapache64.ntcdesktop.BACKGROUND
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.withContext
import okio.Okio
import okio.buffer
import okio.source
import java.util.concurrent.Executors

class OC {
    sealed class OcResult(val result: ProcessResult) {
        object Unset : OcResult(ProcessResult.Ok)
        class Version(val text: String, result: ProcessResult) : OcResult(result)
        class Project(val text: String, result: ProcessResult) : OcResult(result)
        class Projects(val projects: List<String>, result: ProcessResult) : OcResult(result)
        class Services(val services: List<Service>, result: ProcessResult) : OcResult(result)
        class Secret(val json: String, result: ProcessResult) : OcResult(result)
    }

    class PortForwardTarget(val projectName: String, val serviceName: String, val port: String)
    class PortForward(private val process: Process, val target: PortForwardTarget) {
        val isAlive
            get() = process.isAlive
        val stream = process.inputStream.bufferedReader()
        val errorStream = process.errorStream.bufferedReader()
        fun stop() {
            if (process.supportsNormalTermination()) process.destroy()
            else process.destroyForcibly()
        }
    }

    private sealed class Commands(vararg _commands: String) : Command {

        override val commands = listOf("oc", *_commands)

        object Version : Commands("version")
        object Projects : Commands("projects", "-q")
        object Services : Commands("get", "svc", "-ojson")
        object Secrets : Commands("get", "secrets", "-ojson")
        sealed class Project(name: String = "") : Commands("project", "-q", "$name") {
            object Show : Project()
            class Switch(name: String) : Project(name)
        }

        class PortForward(projectName: String, serviceName: String, port: String) :
            Commands("-n", "$projectName", "port-forward", "svc/$serviceName", "$port:$port")
    }

    suspend fun portForward(projectName: String, serviceName: String, port: String): PortForward =
        withContext(Dispatchers.BACKGROUND + CoroutineName("portForward")) {
            val process = ProcessBuilder(Commands.PortForward(projectName, serviceName, port).commands).start().also {
                it.onExit().thenApply {
                    println("oc port forward: onExit: ${it.exitValue()} - ${Thread.currentThread().name}")
                    if (it.exitValue() != 0) {
                        System.err.println(it.errorStream.bufferedReader().readText())
                    }
                }
            }

            PortForward(process, PortForwardTarget(projectName, serviceName, port))
        }

    suspend fun services(): OcResult.Services = withContext(Dispatchers.IO) {
        val process = process(Commands.Services)
        process.let {
            OcResult.Services(parseServices(it.text()), it.result())
        }
    }

    suspend fun version(): OcResult.Version = withContext(Dispatchers.IO) {
        val process = process(Commands.Version)
        process.let {
            OcResult.Version(it.text().trimEnd('\n'), it.result())
        }
    }

    suspend fun switchProject(name: String): OcResult.Project = withContext(Dispatchers.IO) {
        val process = process(Commands.Project.Switch(name))
        process.let {
            OcResult.Project(it.text().trimEnd('\n'), it.result())
        }
    }

    suspend fun project(): OcResult.Project = withContext(Dispatchers.IO) {
        val process = process(Commands.Project.Show)
        process.let {
            OcResult.Project(it.text().trimEnd('\n'), it.result())
        }
    }

    suspend fun projects(): OcResult.Projects = withContext(Dispatchers.IO) {
        val process = process(Commands.Projects)
        process.let {
            OcResult.Projects(it.lines(), it.result())
        }
    }

    suspend fun secrets(): OcResult.Secret = withContext(Dispatchers.IO) {
        val process = ProcessBuilder(Commands.Secrets.commands).start()

        val text = process.inputStream.source().buffer().readUtf8()
        val error = process.errorStream.source().buffer().readUtf8()
        println(error)
        process.let {
            OcResult.Secret(text, it.result())
        }
    }
}
