package org.senai.metrodoc.common.data

import kotlinx.coroutines.flow.Flow
import org.senai.metrodoc.common.database.dao.ProjectDao
import org.senai.metrodoc.common.database.dao.VersionDao
import org.senai.metrodoc.common.database.dto.FullProject
import org.senai.metrodoc.common.database.dto.ProjectDto
import org.senai.metrodoc.common.database.dto.ProjectWithMeasurements
import org.senai.metrodoc.common.database.entity.VersionEntity
import org.senai.metrodoc.common.mapper.metroDocJson
import org.senai.metrodoc.common.mapper.toDomain
import org.senai.metrodoc.common.mapper.toEntity
import org.senai.metrodoc.common.util.toVersionName
import org.senai.metrodoc.features.report.model.ReportData
import org.senai.metrodoc.features.report.model.ReportSection
import kotlin.time.Duration.Companion.minutes

interface RoomProjectRepository {
    fun getRecentProjects(): Flow<List<ProjectDto>>

    suspend fun getProjectById(projectId: Long): FullProject?

    suspend fun saveProject(
        projectId: Long?,
        projectName: String,
        pdfPath: String,
        pdfName: String,
        reportData: ReportData,
        secoes: List<ReportSection>,
    ): Long

    suspend fun deleteProjectById(projectId: Long)

    suspend fun deleteAllProjects()
}

class RoomProjectRepositoryImpl(
    private val projectDao: ProjectDao,
    private val versionDao: VersionDao,
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
        secoes: List<ReportSection>,
    ): Long {
        val agora = System.currentTimeMillis()

        val secoesJson = metroDocJson.encodeToString(secoes)
        val reportEntity = reportData.toEntity(
            id = projectId ?: 0,
            nomeRelatorio = projectName,
            pdfName = pdfName,
            pdfPath = pdfPath,
            lastModified = agora,
            secoesJson = secoesJson
        )

        val measurementEntities = reportData.caracteristicas.map { it.toEntity(0) }

        val projetoSalvoId = projectDao.saveFullProject(reportEntity, measurementEntities)

        val snapshotObj = ProjectWithMeasurements(
            reportData = reportEntity.copy(id = projetoSalvoId),
            measurements = measurementEntities
        )
        val projetoJson = metroDocJson.encodeToString(snapshotObj)

        val ultimaVersao = versionDao.getLatestVersion(projetoSalvoId)
        if (ultimaVersao != null && (agora - ultimaVersao.createdAt) < 2.5.minutes.inWholeMilliseconds) {
            //se a ultima versao foi criada ha menos de 2 min e meio, nao criamos uma nova, mas atualizamos a versao existente
            val versaoAtualizada = ultimaVersao.copy(
                contentJson = projetoJson,
                createdAt = agora
            )
            versionDao.updateVersion(versaoAtualizada)
        } else {
            val novaVersao = VersionEntity(
                id = 0,
                projectId = projetoSalvoId,
                versionName = agora.toVersionName(),
                createdAt = agora,
                contentJson = projetoJson
            )
            versionDao.insertVersion(novaVersao)
        }

        versionDao.trimVersions(projectId = projetoSalvoId, maxVersions = 15)

        return projetoSalvoId
    }

    override suspend fun deleteProjectById(projectId: Long) {
        projectDao.deleteProjectById(projectId)
    }

    override suspend fun deleteAllProjects() {
        projectDao.deleteAllProjects()
    }
}