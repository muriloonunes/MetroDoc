package org.senai.metrodoc.common.database.entity

import androidx.room3.Entity
import androidx.room3.ForeignKey
import androidx.room3.PrimaryKey

@Entity(
    tableName = "versions",
    foreignKeys = [ForeignKey(
        entity = ReportDataEntity::class,
        parentColumns = ["id"],
        childColumns = ["projectId"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class VersionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val projectId: Long,
    val versionName: String,
    val createdAt: Long,
    val contentJson: String,
)