package io.github.hoeggi.openshiftdb.oc

import io.github.hoeggi.openshiftdb.commons.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory

object OC {
    val logger = LoggerFactory.getLogger(OC::class.java)

    sealed class OcResult(private val _result: ProcessResult) {
        val result: Int
            get() = _result.code

        object Unset : OcResult(ProcessResult.Ok)
        class Version(val version: OcVersion?, result: ProcessResult) : OcResult(result)
        class Project(val text: String, result: ProcessResult) : OcResult(result)
        class Projects(val projects: List<String>, result: ProcessResult) : OcResult(result)
        class Services(val services: List<Service>, result: ProcessResult) : OcResult(result)
        class Secret(val password: String?, result: ProcessResult) : OcResult(result)
        class Server(val server: List<Cluster>, result: ProcessResult) : OcResult(result)

        sealed class LoginState(result: ProcessResult = ProcessResult.Ok) : OcResult(result) {
            //            object Unchecked : LoginState()
            object LoggedIn : LoginState()
            class NotLogedIn(result: ProcessResult) : LoginState(result)
        }
    }

    data class PortForwardTarget(val projectName: String, val serviceName: String, val port: String)
    class PortForward(private val process: Process, val target: PortForwardTarget) {
        val isAlive
            get() = process.isAlive

        val exitMessage: String
            get() = process.run {
                "port forward closed, pid: ${pid()} reason: ${exitValue()}"
            }

        val buffer = process.buffer()
        val bufferError = process.bufferError()
        fun stop() {
            if (process.supportsNormalTermination()) process.destroy()
            else process.destroyForcibly()
        }
    }

    private sealed class Commands(vararg _commands: String) : Command {

        override val commands = listOf("oc", *_commands)

        object Version : Commands("version", "-ojson")
        object Projects : Commands("projects", "-q")
        object Services : Commands("get", "svc", "-ojson")
        object Secrets : Commands("get", "secrets", "-ojson")

        class Login(token: String, server: String) : Commands("login", "--token=$token", "--server=$server")
        object ListServer : Commands("config", "view", "-ojson")
        object CheckLogin : Commands("whoami")

        sealed class Project(name: String = "") : Commands("project", "-q", name) {
            object Show : Project()
            class Switch(name: String) : Project(name)
        }

        class PortForward(projectName: String, serviceName: String, port: String) :
            Commands("-n", projectName, "port-forward", "svc/$serviceName", "$port:$port")
    }

    suspend fun checkLogin() = withContext(Dispatchers.IO) {
        val process = process(Commands.CheckLogin)
        when (process.exitValue()) {
            0 -> OcResult.LoginState.LoggedIn
            else -> OcResult.LoginState.NotLogedIn(process.result())
        }
    }

    suspend fun login(token: String, server: String) = withContext(Dispatchers.IO) {
        val process = process(Commands.Login(token, server))
        when (process.exitValue()) {
            0 -> OcResult.LoginState.LoggedIn
            else -> OcResult.LoginState.NotLogedIn(process.result())
        }
    }

    suspend fun listServer() = withContext(Dispatchers.IO) {
        val process = process(Commands.ListServer)

        val text = process.readStdout()
        val error = process.readError()
        process.let {
            OcResult.Server(parseServer(text), it.result())
        }
    }

    fun portForward(projectName: String, serviceName: String, port: String): PortForward {
        logger.debug("opening port forward")
        val process = ProcessBuilder(Commands.PortForward(projectName, serviceName, port).commands).start().also {
            it.onExit().thenApply {
                logger.debug("oc port forward (pid: ${it.pid()}: onExit: ${it.exitValue()} - ${Thread.currentThread().name}")
            }
        }
        logger.debug("opened port forward: ${process.pid()}")
        return PortForward(process, PortForwardTarget(projectName, serviceName, port))
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
            OcResult.Version(parseVersion(it.text()), it.result())
        }
    }

    suspend fun switchProject(name: String): OcResult.Project = withContext(Dispatchers.IO) {
        val process = process(Commands.Project.Switch(name))
        process.let {
            OcResult.Project(it.text().trimEnd('\n'), it.result())
        }
    }

    suspend fun project(): OcResult.Project = withContext(Dispatchers.IO) {
        Dispatchers.IO
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

    suspend fun secrets(username: String): OcResult.Secret = withContext(Dispatchers.IO) {
        val process = ProcessBuilder(Commands.Secrets.commands).start()

        val text = process.readStdout()
        val error = process.readError()
        logger.debug(error)
        process.let {
            OcResult.Secret(findPassword(text, username), it.result())
        }
    }
}
