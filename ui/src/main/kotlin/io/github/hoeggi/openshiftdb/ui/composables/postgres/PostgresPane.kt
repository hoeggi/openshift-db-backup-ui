package io.github.hoeggi.openshiftdb.ui.composables.postgres

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.hoeggi.openshiftdb.PostgresViewModel
import io.github.hoeggi.openshiftdb.Scope

@ExperimentalFoundationApi
@Composable
fun PostgresPane() {
    val viewModel = PostgresViewModel.current

    val selectedDatabase by viewModel.selectedDatabase.collectAsState(Scope.current.coroutineContext)
    val path by viewModel.dumpPath.collectAsState(Scope.current.coroutineContext)

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


