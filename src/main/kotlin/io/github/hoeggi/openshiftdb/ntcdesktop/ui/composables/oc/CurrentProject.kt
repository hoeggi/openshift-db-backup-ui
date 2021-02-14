package io.github.hoeggi.openshiftdb.ntcdesktop.ui.composables

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.hoeggi.openshiftdb.ntcdesktop.process.OC

@Composable
fun CurrentProject(project: OC.OcResult) {
    Column(
        modifier = Modifier
            //.background(Color.Black.copy(alpha = 0.2f), RoundedCornerShape(5.dp))
            .padding(10.dp)
    ) {
        Text(
            text = "Current project:",
            style = MaterialTheme.typography.caption
        )
        Spacer(
            modifier = Modifier.height(10.dp)
        )
        Text(
            text = when (project) {
                is OC.OcResult.Project -> project.text
                else -> "Unset"
            },
            style = MaterialTheme.typography.overline
        )
    }
}