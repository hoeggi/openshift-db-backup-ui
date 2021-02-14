package io.github.hoeggi.openshiftdb.ui.composables.oc

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.github.hoeggi.openshiftdb.Scope
import io.github.hoeggi.openshiftdb.process.OC
import kotlinx.coroutines.CoroutineScope

@Composable
fun AllProjects(
    projects: OC.OcResult,
    onProjectClicked: (String, CoroutineScope) -> Unit
) {
    Column(
        modifier = Modifier
            .padding(10.dp)
    ) {
        Text(
            text = "All projects:",
            style = MaterialTheme.typography.caption
        )
        Spacer(
            modifier = Modifier.height(10.dp)
        )
        LazyColumn {
            items(when(projects){
                is OC.OcResult.Projects -> projects.projects
                else -> listOf()
            }) { item ->
                Project(
                    name = item,
                    onProjectClicked = onProjectClicked
                )
            }
        }
    }
}

@Composable
fun Project(
    name: String,
    onProjectClicked: (String, CoroutineScope) -> Unit
) {
    val scope = Scope.current
    Column (
        modifier = Modifier
            .clickable {
                onProjectClicked(name, scope)
            }
            .fillMaxWidth(),
    ) {
        Text(text = name, modifier = Modifier.fillMaxSize().padding(2.dp), style = MaterialTheme.typography.overline)
        Divider(color = Color.White, thickness = 1.dp)
    }
}