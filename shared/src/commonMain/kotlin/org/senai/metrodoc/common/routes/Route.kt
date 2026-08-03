package org.senai.metrodoc.common.routes

import kotlinx.serialization.Serializable

sealed interface Route {
    @Serializable
    data object Welcome : Route

    @Serializable
    data class RelatoryCreator(
        val path: String,
        val name: String,
    ) : Route
}