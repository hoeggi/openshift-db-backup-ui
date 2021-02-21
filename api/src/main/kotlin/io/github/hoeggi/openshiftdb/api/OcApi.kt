package io.github.hoeggi.openshiftdb.api

import kotlinx.coroutines.flow.Flow


interface OcApi {

    suspend fun version(): VersionApi
    suspend fun server(): List<ClusterApi>
    suspend fun projects(): List<ProjectApi>
    suspend fun switchProject(project: ProjectApi): ProjectApi
    suspend fun switchProject(project: String): ProjectApi
    suspend fun currentProject(): ProjectApi
    suspend fun password(username: String): String

    suspend fun postForward(
        project: String,
        svc: String,
        port: String
    ): Flow<String>

}