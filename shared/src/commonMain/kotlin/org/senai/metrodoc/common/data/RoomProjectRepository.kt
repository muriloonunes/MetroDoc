package org.senai.metrodoc.common.data

import kotlinx.coroutines.flow.Flow
import org.senai.metrodoc.common.database.dao.ProjectDao
import org.senai.metrodoc.common.database.dto.FullProject
import org.senai.metrodoc.common.database.dto.ProjectDto
import org.senai.metrodoc.common.mapper.metroDocJson
import org.senai.metrodoc.common.mapper.toDomain
import org.senai.metrodoc.common.mapper.toEntity
import org.senai.metrodoc.features.report.model.ReportData
import org.senai.metrodoc.features.report.model.ReportSection

interface RoomProjectRepository {
    fun getRecentProjects(): Flow<List<ProjectDto>>

    suspend fun getProjectById(projectId: Long): FullProject?

    suspend fun saveProject(
        projectId: Long?,
        projectName: String,
        pdfPath: String,
        pdfName: String,
        reportData: ReportData,
        secoes: List<ReportSection>
    ): Long
}

class RoomProjectRepositoryImpl(
    private val projectDao: ProjectDao
) : RoomProjectRepository {
    override fun getRecentProjects(): Flow<List<ProjectDto>> {
        return projectDao.getProjectsSummaries()
    }

    override suspend fun getProjectById(projectId: Long): FullProject? {
        val entity = projectDao.getProjectById(projectId) ?: return null

        val measurementData = entity.measurements.map { it.toDomain() }
        val reportDataDomain = entity.reportData.toDomain(measurementData)

        val decodedSecoes = runCatching {
            metroDocJson.decodeFromString<List<ReportSection>>(entity.reportData.secoesJson)
        }.getOrDefault(emptyList())

        val secoesReconstruidas = decodedSecoes.map { secao ->
            when (secao) {
                is ReportSection.Identificacao -> secao.copy(reportData = reportDataDomain)
                is ReportSection.ResultadosDimensionais -> secao.copy(measurements = reportDataDomain.caracteristicas)
                else -> secao
            }
        }

        return FullProject(
            projectId = entity.reportData.id,
            reportName = entity.reportData.nomeRelatorio,
            pdfPath = entity.reportData.pdfPath,
            pdfName = entity.reportData.pdfName,
            reportData = reportDataDomain,
            secoes = secoesReconstruidas
        )
    }

    override suspend fun saveProject(
        projectId: Long?,
        projectName: String,
        pdfPath: String,
        pdfName: String,
        reportData: ReportData,
        secoes: List<ReportSection>
    ): Long {
        val secoesJson = metroDocJson.encodeToString(secoes)
        val reportEntity = reportData.toEntity(
            id = projectId ?: 0,
            nomeRelatorio = projectName,
            pdfName = pdfName,
            pdfPath = pdfPath,
            lastModified = System.currentTimeMillis(),
            secoesJson = secoesJson
        )

        val measurementEntities = reportData.caracteristicas.map { it.toEntity(0) }

        return projectDao.saveFullProject(reportEntity, measurementEntities)
    }
}