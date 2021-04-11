package io.github.hoeggi.openshiftdb.ext

import java.awt.Desktop
import java.io.File
import java.io.IOException
import java.net.URI
import java.util.ArrayList

object DesktopApi {
    fun browse(uri: URI): Boolean {
        if (openSystemSpecific(uri.toString())) return true
        return browseDESKTOP(uri)
    }

    fun open(file: File): Boolean {
        if (openSystemSpecific(file.path)) return true
        return openDESKTOP(file)
    }

    fun edit(file: File): Boolean {

        // you can try something like
        // runCommand("gimp", "%s", file.getPath())
        // based on user preferences.
        if (openSystemSpecific(file.path)) return true
        return editDESKTOP(file)
    }

    private fun openSystemSpecific(what: String): Boolean {
        val os = os
        if (os.isLinux) {
            if (runCommand("kde-open", "%s", what)) return true
            if (runCommand("gnome-open", "%s", what)) return true
            if (runCommand("xdg-open", "%s", what)) return true
        }
        if (os.isMac) {
            if (runCommand("open", "%s", what)) return true
        }
        if (os.isWindows) {
            if (runCommand("explorer", "%s", what)) return true
        }
        return false
    }

    private fun browseDESKTOP(uri: URI): Boolean {
        logOut("Trying to use Desktop.getDesktop().browse() with $uri")
        return try {
            if (!Desktop.isDesktopSupported()) {
                logErr("Platform is not supported.")
                return false
            }
            if (!Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                logErr("BROWSE is not supported.")
                return false
            }
            Desktop.getDesktop().browse(uri)
            true
        } catch (t: Throwable) {
            logErr("Error using desktop browse.", t)
            false
        }
    }

    private fun openDESKTOP(file: File): Boolean {
        logOut("Trying to use Desktop.getDesktop().open() with $file")
        return try {
            if (!Desktop.isDesktopSupported()) {
                logErr("Platform is not supported.")
                return false
            }
            if (!Desktop.getDesktop().isSupported(Desktop.Action.OPEN)) {
                logErr("OPEN is not supported.")
                return false
            }
            Desktop.getDesktop().open(file)
            true
        } catch (t: Throwable) {
            logErr("Error using desktop open.", t)
            false
        }
    }

    private fun editDESKTOP(file: File): Boolean {
        logOut("Trying to use Desktop.getDesktop().edit() with $file")
        return try {
            if (!Desktop.isDesktopSupported()) {
                logErr("Platform is not supported.")
                return false
            }
            if (!Desktop.getDesktop().isSupported(Desktop.Action.EDIT)) {
                logErr("EDIT is not supported.")
                return false
            }
            Desktop.getDesktop().edit(file)
            true
        } catch (t: Throwable) {
            logErr("Error using desktop edit.", t)
            false
        }
    }

    private fun runCommand(command: String, args: String, file: String): Boolean {
        logOut("Trying to exec:\n   cmd = $command\n   args = $args\n   %s = $file")
        val parts = prepareCommand(command, args, file)
        return try {
            val p = Runtime.getRuntime().exec(parts) ?: return false
            try {
                val retval = p.exitValue()
                if (retval == 0) {
                    logErr("Process ended immediately.")
                    false
                } else {
                    logErr("Process crashed.")
                    false
                }
            } catch (itse: IllegalThreadStateException) {
                logErr("Process is running.")
                true
            }
        } catch (e: IOException) {
            logErr("Error running command.", e)
            false
        }
    }

    private fun prepareCommand(command: String, args: String?, file: String): Array<String> {
        val parts: MutableList<String> = ArrayList()
        parts.add(command)
        if (args != null) {
            for (s in args.split(" ").toTypedArray()) {
                parts.add(String.format(s, file).trim { it <= ' ' }) // put in the filename thing
            }
        }
        return parts.toTypedArray()
    }

    private fun logErr(msg: String, t: Throwable) {
        System.err.println(msg)
        t.printStackTrace()
    }

    private fun logErr(msg: String) {
        System.err.println(msg)
    }

    private fun logOut(msg: String) {
        println(msg)
    }

    val os: EnumOS
        get() {
            val s = System.getProperty("os.name").toLowerCase()
            if (s.contains("win")) {
                return EnumOS.WINDOWS
            }
            if (s.contains("mac")) {
                return EnumOS.MACOS
            }
            if (s.contains("solaris")) {
                return EnumOS.SOLARIS
            }
            if (s.contains("sunos")) {
                return EnumOS.SOLARIS
            }
            if (s.contains("linux")) {
                return EnumOS.LINUX
            }
            return if (s.contains("unix")) {
                EnumOS.LINUX
            } else {
                EnumOS.UNKNOWN
            }
        }

    enum class EnumOS {
        LINUX, MACOS, SOLARIS, UNKNOWN, WINDOWS;

        val isLinux: Boolean
            get() = this == LINUX || this == SOLARIS
        val isMac: Boolean
            get() = this == MACOS
        val isWindows: Boolean
            get() = this == WINDOWS
    }
}
