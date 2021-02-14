package io.github.hoeggi.openshiftdb.ntcdesktop.ui.composables

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import io.github.hoeggi.openshiftdb.ntcdesktop.PostgresViewModel
import io.github.hoeggi.openshiftdb.ntcdesktop.Scope
import io.github.hoeggi.openshiftdb.ntcdesktop.ui.composables.postgres.ConsoleOutput
import java.io.Console

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


