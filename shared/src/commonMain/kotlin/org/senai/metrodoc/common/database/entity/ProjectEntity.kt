package org.senai.metrodoc.common.database.entity

import androidx.room3.Entity
import androidx.room3.PrimaryKey
import kotlinx.serialization.Serializable

@Entity(tableName = "projects")
@Serializable
data class ProjectEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nomeProjeto: String,
    val cliente: String = "",
    val componente: String = "",
    val createdAt: Long,
    val lastModified: Long,
)
