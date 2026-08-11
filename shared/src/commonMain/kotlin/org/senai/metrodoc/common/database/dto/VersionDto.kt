package org.senai.metrodoc.common.database.dto

data class VersionDto(
    val id: Long,
    val versionName: String,
    val createdAt: Long,
)
