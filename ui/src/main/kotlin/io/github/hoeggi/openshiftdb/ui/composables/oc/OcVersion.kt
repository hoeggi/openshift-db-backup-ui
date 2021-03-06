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
import io.github.hoeggi.openshiftdb.api.response.VersionApi
import io.github.hoeggi.openshiftdb.i18n.MessageProvider
import io.github.hoeggi.openshiftdb.i18n.MessageProvider.OC_VERSION_LABEL
import io.github.hoeggi.openshiftdb.i18n.MessageProvider.OC_VERSION_TEXT
import io.github.hoeggi.openshiftdb.ui.composables.ExpandableText

@Composable
fun OcVersion() {

    val version by OcViewModel.current.version.collectAsState(Scope.current.coroutineContext)

    Column(
        modifier = Modifier
            .padding(10.dp)
    ) {
        ExpandableText(
            text = MessageProvider.message(OC_VERSION_LABEL),
        ) {
            Spacer(
                modifier = Modifier.height(5.dp)
            )
            Text(
                text = when (version) {
                    is VersionApi -> MessageProvider.message(
                        OC_VERSION_TEXT,
                        version.oc,
                        version.openshift,
                        version.kubernets
                    )
                    else -> "Unset"
                },
                style = MaterialTheme.typography.caption
            )
        }
    }

}