package com.theapache64.ntcdesktop.ui.composables

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.theapache64.ntcdesktop.process.OC

@Composable
fun OcVersion(version: OC.OcResult) {

    Column(
        modifier = Modifier
            .padding(10.dp)
    ) {
        Text(
            text = "oc version:",
            style = MaterialTheme.typography.caption
        )
        Spacer(
            modifier = Modifier.height(10.dp)
        )
        Text(
            text = when (version) {
                is OC.OcResult.Version -> version.text
                else -> "Unset"
            },
            style = MaterialTheme.typography.overline
        )
    }

}