package io.github.hoeggi.openshiftdb.ui.composables.oc

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.hoeggi.openshiftdb.OcViewModel
import io.github.hoeggi.openshiftdb.Scope
import io.github.hoeggi.openshiftdb.api.response.ProjectApi
import io.github.hoeggi.openshiftdb.i18n.MessageProvider
import io.github.hoeggi.openshiftdb.i18n.MessageProvider.OC_PROJECT_CURRENT_LABEL


@Composable
fun CurrentProject() {

    val currentProject by OcViewModel.current.currentProject.collectAsState(Scope.current.coroutineContext)

    Column(
        modifier = Modifier
            .padding(10.dp)
    ) {
        Text(
            text = MessageProvider.message(OC_PROJECT_CURRENT_LABEL),
            style = MaterialTheme.typography.body1
        )
        Spacer(
            modifier = Modifier.height(10.dp)
        )
        Text(
            text = when (currentProject) {
                is ProjectApi -> currentProject.name
                else -> "Unset"
            },
            style = MaterialTheme.typography.caption
        )
    }
}