package io.github.hoeggi.openshiftdb.ui.composables.postgres

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import io.github.hoeggi.openshiftdb.ViewModelProvider
import io.github.hoeggi.openshiftdb.collectAsState

@Composable
internal fun PostgresPane() {
    val viewModel = ViewModelProvider.current.postgresViewModel

    val selectedDatabase by viewModel.collectAsState(viewModel.selectedDatabase)
    val path by viewModel.collectAsState(viewModel.dumpPath)

    Column {
        PsqlVersion()
        PostgresPassword()
        ConnectButtons()
        DatabaseChooser()

        if (path.isNotEmpty() && selectedDatabase != -1) {
            PostgresDump()
        }
        ConsoleOutput()
    }
}
