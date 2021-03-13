package io.github.hoeggi.openshiftdb.ui.composables.oc

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier


@Composable
internal fun OcPane(
    modifier: Modifier = Modifier.fillMaxWidth(0.5f)
) {

    Column(
        modifier = modifier
    ) {
        OcVersion()
        CurrentProject()
        Row {
            Column(
                modifier = Modifier.fillMaxWidth(0.5f)
            ) {
                AllProjects()
            }
            Column {
                Service()
                PortForward()
            }
        }
    }
}