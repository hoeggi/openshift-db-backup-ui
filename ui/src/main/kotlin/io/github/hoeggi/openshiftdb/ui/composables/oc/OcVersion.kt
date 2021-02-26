package io.github.hoeggi.openshiftdb.ui.composables.oc

import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.hoeggi.openshiftdb.OcViewModel
import io.github.hoeggi.openshiftdb.Scope
import io.github.hoeggi.openshiftdb.api.response.VersionApi
import io.github.hoeggi.openshiftdb.ui.composables.ExpandableText
import androidx.compose.runtime.getValue

@Composable
fun OcVersion() {

    val version by OcViewModel.current.version.collectAsState(Scope.current.coroutineContext)

    Column(
        modifier = Modifier
            .padding(10.dp)
    ) {
        ExpandableText(
            text = "oc version",
        ) {
            Spacer(
                modifier = Modifier.height(5.dp)
            )
            Text(
                text = when (version) {
                    is VersionApi -> "oc: ${version.oc}\nopenshift: ${version.openshift}\nkubernetes: ${version.kubernets}"
                    else -> "Unset"
                },
                style = MaterialTheme.typography.caption
            )
        }
    }

}