package io.github.hoeggi.openshiftdb.ui.composables.oc

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.hoeggi.openshiftdb.OcViewModel
import io.github.hoeggi.openshiftdb.collectAsState
import io.github.hoeggi.openshiftdb.i18n.MessageProvider
import io.github.hoeggi.openshiftdb.i18n.MessageProvider.OC_PROJECT_ALL_LABEL

@Composable
internal fun AllProjects() {
    val viewModel = OcViewModel.current
    val projects: List<String> by viewModel.collectAsState(viewModel.projects)

    Column(
        modifier = Modifier
            .padding(10.dp)
    ) {
        Text(
            text = MessageProvider.message(OC_PROJECT_ALL_LABEL),
            style = MaterialTheme.typography.body1
        )
        Spacer(
            modifier = Modifier.height(10.dp)
        )
        LazyColumn {
            items(projects) {
                Project(
                    name = it,
                    onProjectClicked = {
                        viewModel.switchProject(it)
                    }
                )
            }
        }
    }
}

@Composable
private fun Project(
    name: String,
    onProjectClicked: (String) -> Unit,
) {
    Column(
        modifier = Modifier
            .clickable {
                onProjectClicked(name)
            }
            .fillMaxWidth(),
    ) {
        Text(text = name, modifier = Modifier.fillMaxSize().padding(2.dp), style = MaterialTheme.typography.caption)
        Divider()
    }
}