package org.senai.metrodoc.common.database.dto

import androidx.room3.Embedded
import androidx.room3.Relation
import org.senai.metrodoc.common.database.entity.MeasurementDataEntity
import org.senai.metrodoc.common.database.entity.ReportDataEntity
import org.senai.metrodoc.features.report.model.ReportData
import org.senai.metrodoc.features.report.model.ReportSection

data class ProjectDto(
    val id: Long,
    val nomeProjeto: String,
    val modificadoEm: Long,
)

data class ProjectWithMeasurements(
    @Embedded val reportData: ReportDataEntity,
    @Relation(
        parentColumns = ["id"],
        entityColumns = ["reportId"],
    )
    val measurements: List<MeasurementDataEntity>
)

data class FullProject(
    val projectId: Long,
    val reportName: String,
    val pdfPath: String,
    val pdfName: String,
    val reportData: ReportData,
    val secoes: List<ReportSection>
)