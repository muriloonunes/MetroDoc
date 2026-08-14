package org.senai.metrodoc.common.database.entity

import androidx.room3.Entity
import androidx.room3.ForeignKey
import androidx.room3.Index
import androidx.room3.PrimaryKey
import kotlinx.serialization.Serializable

@Entity(
    tableName = "reports",
    foreignKeys = [
        ForeignKey(
            entity = ProjectEntity::class,
            parentColumns = ["id"],
            childColumns = ["projectId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("projectId")]
)
@Serializable
data class ReportDataEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val projectId: Long = 0,
    val nomeRelatorio: String = "",
    val pdfName: String = "",
    val pdfPath: String = "",
    val lastModified: Long = 0,
    val secoesJson: String = "",

    val cliente: String = "",
    val componente: String = "",
    val identificadorCalypso: String = "",
    val maquina: String = "",
    val numeroMaquina: String = "",
    val software: String = "",
    val operador: String = "",
    val dataHora: String = "",
    val qtdCaracteristicas: String = "",
)