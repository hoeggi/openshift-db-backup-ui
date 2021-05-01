package io.github.hoeggi.openshiftdb.errorhandler

interface ErrorViewer {
    fun showError(error: Message)
    fun showWarning(warning: Message)

    interface Message {
        val fired: Boolean
    }

    interface Error : Message {
        val thread: Thread?
        val throwable: Throwable?
    }

    interface Warning : Message {
        val message: String
    }

    fun empty() = object : Message {
        override val fired = false
    }

    fun warning(message: String): Warning = Warning(message, true)
    fun error(
        thread: Thread? = Thread.currentThread(),
        throwable: Throwable? = Throwable(),
    ): Error = Error(thread, throwable, true)
}

private data class Warning(
    override val message: String,
    override val fired: Boolean,
) : ErrorViewer.Warning

private data class Error(
    override val thread: Thread?,
    override val throwable: Throwable?,
    override val fired: Boolean,
) : ErrorViewer.Error
