package io.github.hoeggi.openshiftdb.ntcdesktop

import androidx.compose.desktop.Window
import androidx.compose.desktop.WindowEvents
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import io.github.hoeggi.openshiftdb.ntcdesktop.process.OC
import io.github.hoeggi.openshiftdb.ntcdesktop.process.Postgres
import io.github.hoeggi.openshiftdb.ntcdesktop.ui.composables.OcPane
import io.github.hoeggi.openshiftdb.ntcdesktop.ui.composables.PostgresPane
import io.github.hoeggi.openshiftdb.ntcdesktop.ui.theme.ColorMuskTheme
import io.github.hoeggi.openshiftdb.ntcdesktop.ui.viewmodel.OcViewModel
import io.github.hoeggi.openshiftdb.ntcdesktop.ui.viewmodel.PostgresViewModel
import kotlinx.coroutines.*
import java.util.concurrent.Executors
import javax.swing.UIManager


const val APP_NAME = "Openshift DB Backup GUI"

class Main

val Dispatchers.BACKGROUND: CoroutineDispatcher
    get() = Executors.newFixedThreadPool(4).asCoroutineDispatcher()

val Dispatchers.DUMP: CoroutineDispatcher
    get() = Executors.newFixedThreadPool(4).asCoroutineDispatcher()

val Scope = compositionLocalOf<CoroutineScope>()
val PostgresViewModel = compositionLocalOf<PostgresViewModel>()
val OcViewModel = compositionLocalOf<OcViewModel>()
fun main() {
    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())
    Window(
        title = APP_NAME,
        undecorated = false,
        size = IntSize(1024, 1024),
        events = WindowEvents(
            onClose = {
                ProcessHandle.current()
                    .children()
                    .forEach {
                        it.destroy()
                    }
            }
        )
    ) {
        val scope = rememberCoroutineScope() + Dispatchers.BACKGROUND + Dispatchers.DUMP
        val oc = OC()
        val postgres = Postgres()
        val ocViewModel = OcViewModel(oc, scope)
        val postgresViewModel = PostgresViewModel(postgres, oc, scope)

        CompositionLocalProvider(
            Scope provides scope,
        ) {
            ColorMuskTheme {
                MainScreen(
                    ocViewModel = ocViewModel,
                    postgresViewModel = postgresViewModel,
                )
            }
        }

    }
}

@Composable
private fun MainScreen(
    ocViewModel: OcViewModel,
    postgresViewModel: PostgresViewModel,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(15.dp),
    ) {

        Row {
            CompositionLocalProvider(OcViewModel provides ocViewModel) {
                OcPane()
            }
            CompositionLocalProvider(PostgresViewModel provides postgresViewModel) {
                PostgresPane()
            }
        }
    }
}

