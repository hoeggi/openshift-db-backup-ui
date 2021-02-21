package io.github.hoeggi.openshiftdb.api

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class VersionApi(
    val oc: String,
    val kubernets: String,
    val openshift: String
)

@Serializable
data class ClusterApi(
    val name: String,
    val server: String,
)

@Serializable
data class ServicesApi(
    val name: String,
    val ports: List<ServicesPortApi>,
)

@Serializable
data class ServicesPortApi(
    val port: String,
    val targetPort: String,
    val protocol: String,
) {
    @Transient
    val display = "$port:$targetPort/$protocol"
}

@Serializable
data class ProjectApi(
    val name: String,
)