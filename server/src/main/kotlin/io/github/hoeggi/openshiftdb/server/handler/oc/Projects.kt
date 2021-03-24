package io.github.hoeggi.openshiftdb.server.handler.oc

import io.github.hoeggi.openshiftdb.api.response.ProjectApi
import io.github.hoeggi.openshiftdb.oc.OC
import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.request.receiveOrNull
import io.ktor.response.respond
import io.ktor.util.pipeline.PipelineContext

internal fun SwitchProject(): suspend PipelineContext<Unit, ApplicationCall>.(Unit) -> Unit =
    {
        val project = call.receiveOrNull<ProjectApi>()
        val projectName = project?.name
        if (projectName == null) {
            call.respond(HttpStatusCode.BadRequest, "missing projectName")
        } else {
            val switchProject = OC.switchProject(projectName)
            authorized(ProjectApi(switchProject.text), switchProject.result)
        }
    }

internal fun Project(): suspend PipelineContext<Unit, ApplicationCall>.(Unit) -> Unit =
    {
        val projects = OC.project()
        authorized(ProjectApi(projects.text), projects.result)
    }

internal fun Projects(): suspend PipelineContext<Unit, ApplicationCall>.(Unit) -> Unit =
    {
        val projects = OC.projects()
        authorized(projects.projects.map { ProjectApi(it) }, projects.result)
    }
