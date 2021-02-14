package com.theapache64.ntcdesktop.ui.composables

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.theapache64.ntcdesktop.OcViewModel
import com.theapache64.ntcdesktop.Scope
import com.theapache64.ntcdesktop.process.OC
import com.theapache64.ntcdesktop.ui.viewmodel.OcViewModel
import kotlinx.coroutines.launch


@Composable
fun OcPane() {

    val viewModel = OcViewModel.current

    val currentProject: OC.OcResult by viewModel.currentProject.collectAsState(Scope.current.coroutineContext)
    val version: OC.OcResult by viewModel.version.collectAsState(Scope.current.coroutineContext)
    val projects: OC.OcResult by viewModel.projects.collectAsState(Scope.current.coroutineContext)
    val services: OC.OcResult by viewModel.services.collectAsState(Scope.current.coroutineContext)
    val portForward: OC.PortForward? by viewModel.portForward.collectAsState(Scope.current.coroutineContext)

    Column(
        modifier = Modifier.fillMaxWidth(0.5f)
    ) {
        OcVersion(version)
        CurrentProject(currentProject)
        Row {
            Column(
                modifier = Modifier.fillMaxWidth(0.5f)
            ) {
                AllProjects(projects){ clicked, coroutineScope ->
                    coroutineScope.launch {
                        viewModel.switchProject(clicked)
                    }
                }
            }
            Column {
                Service(services) { service, port, coroutineScope ->
                    val project = (currentProject as OC.OcResult.Project).text
                    coroutineScope.launch {
                        portForward?.stop()
                        viewModel.portForward(project, service.name, port)
                    }
                }
                portForward?.run {
                    if (isAlive) {
                        PortForward(this) {
                            viewModel.closePortForward(it)
                        }
                    }
                }
            }
        }
    }
}