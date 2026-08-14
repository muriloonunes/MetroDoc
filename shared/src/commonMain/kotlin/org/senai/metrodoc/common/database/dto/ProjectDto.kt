package org.senai.metrodoc.common.database.dto

import androidx.room3.Embedded
import androidx.room3.Relation
import kotlinx.serialization.Serializable
import org.senai.metrodoc.common.database.entity.MeasurementDataEntity
import org.senai.metrodoc.common.database.entity.ProjectEntity
import org.senai.metrodoc.common.database.entity.ReportDataEntity
import org.senai.metrodoc.features.report.model.PdfItem

data class ProjectDto(
    val id: Long,
    val nomeProjeto: String,
    val cliente: String = "",
    val componente: String = "",
    val modificadoEm: Long,
    val qtdPdfs: Int = 1,
)

@Serializable
data class ReportWithMeasurements(
    @Embedded val reportData: ReportDataEntity,
    @Relation(
        parentColumns = ["id"],
        entityColumns = ["reportId"],
    )
    val measurements: List<MeasurementDataEntity>
)

@Serializable
data class ProjectWithReports(
    @Embedded val project: ProjectEntity,
    @Relation(
        entity = ReportDataEntity::class,
        parentColumns = ["id"],
        entityColumns = ["projectId"],
    )
    val reports: List<ReportWithMeasurements>
)

data class FullProject(
    val projectId: Long,
    val nomeProjeto: String,
    val cliente: String,
    val componente: String,
    val pdfItems: List<PdfItem>
)