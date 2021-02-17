package io.github.hoeggi.openshiftdb.ui.composables.postgres

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.*
import io.github.hoeggi.openshiftdb.PostgresViewModel
import io.github.hoeggi.openshiftdb.Scope

//var finished by mutableStateOf("")

@Composable
fun PostgresPane() {
    val viewModel = PostgresViewModel.current

//    val text by viewModel.databases.collectAsState("", Scope.current.coroutineContext)

    val selectedDatabase by viewModel.selectedDatabase.collectAsState(-1, Scope.current.coroutineContext)
    val path by viewModel.dumpPath.collectAsState("", Scope.current.coroutineContext)


    Column {
        PsqlVersion()
        PostgresPassword()
        ConnectButtons()
        DatabaseChooser()

        if (path.isNotEmpty() && selectedDatabase != -1) {
            PostgresDump()
        }
        ConsoleOutput()

//        Text(
//            text = text,
//            style = MaterialTheme.typography.caption.copy(
//                fontFamily = FontFamily.Monospace
//            ),
//            modifier = Modifier.fillMaxWidth(1f).padding(10.dp)
//        )
    }
}

