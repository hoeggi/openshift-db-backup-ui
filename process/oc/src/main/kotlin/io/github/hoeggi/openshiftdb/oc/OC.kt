package io.github.hoeggi.openshiftdb.oc

import io.github.hoeggi.openshiftdb.commons.Command
import io.github.hoeggi.openshiftdb.commons.ProcessResult
import io.github.hoeggi.openshiftdb.commons.buffer
import io.github.hoeggi.openshiftdb.commons.bufferError
import io.github.hoeggi.openshiftdb.commons.lines
import io.github.hoeggi.openshiftdb.commons.process
import io.github.hoeggi.openshiftdb.commons.readError
import io.github.hoeggi.openshiftdb.commons.readStdout
import io.github.hoeggi.openshiftdb.commons.result
import io.github.hoeggi.openshiftdb.commons.text
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory

object OC {
    private val logger = LoggerFactory.getLogger(OC::class.java)

    sealed class OcResult(val result: Int) {

        class Version internal constructor(val version: OcVersion?, result: ProcessResult) : OcResult(result.code)
        class Project internal constructor(val text: String, result: ProcessResult) : OcResult(result.code)
        class Projects internal constructor(val projects: List<String>, result: ProcessResult) : OcResult(result.code)
        class Services internal constructor(val services: List<Service>, result: ProcessResult) : OcResult(result.code)
        class Secret internal constructor(val password: String?, result: ProcessResult) : OcResult(result.code)
        class Secrets internal constructor(val secrets: List<SecretItem>, result: ProcessResult) : OcResult(result.code)
        class Server internal constructor(val server: List<Cluster>, result: ProcessResult) : OcResult(result.code)
        class CurrentContext internal constructor(val context: String, result: ProcessResult) : OcResult(result.code)
        class Context internal constructor(val contexts: List<NamedContext>, result: ProcessResult) :
            OcResult(result.code)

        class SwitchContext internal constructor(val newContext: String, result: ProcessResult) : OcResult(result.code)

        sealed class LoginState(result: Int) : OcResult(result) {
            object LoggedIn : LoginState(ProcessResult.Ok.code)
            class NotLogedIn internal constructor(result: ProcessResult) : LoginState(result.code)
        }
    }

    data class PortForwardTarget(val projectName: String, val serviceName: String, val port: String)
    class PortForward internal constructor(private val process: Process) {

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
        object Config : Commands("config", "view", "-ojson")
        object CurrentContext : Commands("config", "current-context")
        class SwitchContext(name: String) : Commands("config", "use-context", name)

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

    suspend fun currentContext() = withContext(Dispatchers.IO) {
        val process = process(Commands.CurrentContext)
        val text = process.readStdout()
        val error = process.readError()
        logger.debug(error)
        process.let {
            OcResult.CurrentContext((if (text.isNotBlank()) text else error).replace("\n", ""), it.result())
        }
    }

    suspend fun listContext() = withContext(Dispatchers.IO) {
        val process = ProcessBuilder(Commands.Config.commands).start().also {
            it.onExit().thenAcceptAsync {
                logger.debug("process onExit: ${it.exitValue()} - ${Thread.currentThread().name}")
            }
        }

        val text = process.readStdout()
        val error = process.readError()
        logger.debug(error)
        process.let {
            OcResult.Context(parseContext(text), it.result())
        }
    }

    suspend fun switchContext(name: String) = withContext(Dispatchers.IO) {
        val process = process(Commands.SwitchContext(name))

        val text = process.readStdout()
        val error = process.readError()
        logger.debug(error)
        process.let {
            OcResult.SwitchContext(if (text.isNotBlank()) text else error, it.result())
        }
    }

    suspend fun listServer() = withContext(Dispatchers.IO) {
        val process = process(Commands.Config)

        val text = process.readStdout()
        val error = process.readError()
        logger.debug(error)
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
        return PortForward(process)
    }

    suspend fun services(): OcResult.Services = withContext(Dispatchers.IO) {
        val process = ProcessBuilder(Commands.Services.commands).start()

        val text = process.readStdout()
        val error = process.readError()
        logger.debug(error)
        process.let {
            OcResult.Services(parseServices(text), it.result())
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

    suspend fun secrets(): OcResult.Secrets = withContext(Dispatchers.IO) {
        val process = ProcessBuilder(Commands.Secrets.commands).start()

        val text = process.readStdout()
        val error = process.readError()
        logger.debug(error)
        process.let {
            OcResult.Secrets(parseSecrets(text), it.result())
        }
    }

    suspend fun password(username: String): OcResult.Secret = withContext(Dispatchers.IO) {
        val process = ProcessBuilder(Commands.Secrets.commands).start()
        val text = process.readStdout()
        val error = process.readError()
        logger.debug(error)
        process.let {
            OcResult.Secret(findPassword(text, username), it.result())
        }
    }
}
