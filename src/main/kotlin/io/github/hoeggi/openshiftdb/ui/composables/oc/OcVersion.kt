package io.github.hoeggi.openshiftdb.ui.composables.oc

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.outlined.ArrowDownward
import androidx.compose.material.icons.outlined.ArrowUpward
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.KeyboardArrowUp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.hoeggi.openshiftdb.process.OC
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import io.github.hoeggi.openshiftdb.ui.composables.ExpandableText

//private var expanded by mutableStateOf(false)

@Composable
fun OcVersion(version: OC.OcResult) {

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
                    is OC.OcResult.Version -> version.text
                    else -> "Unset"
                },
                style = MaterialTheme.typography.caption
            )
        }
    }

}