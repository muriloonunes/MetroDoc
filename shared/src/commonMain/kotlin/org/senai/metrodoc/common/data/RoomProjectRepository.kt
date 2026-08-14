package org.senai.metrodoc.common.data

import kotlinx.coroutines.flow.Flow
import org.senai.metrodoc.common.database.dao.ProjectDao
import org.senai.metrodoc.common.database.dao.VersionDao
import org.senai.metrodoc.common.database.dto.FullProject
import org.senai.metrodoc.common.database.dto.ProjectDto
import org.senai.metrodoc.common.database.dto.ProjectWithReports
import org.senai.metrodoc.common.database.dto.VersionDto
import org.senai.metrodoc.common.database.entity.ProjectEntity
import org.senai.metrodoc.common.database.entity.VersionEntity
import org.senai.metrodoc.common.mapper.metroDocJson
import org.senai.metrodoc.common.mapper.toDomain
import org.senai.metrodoc.common.mapper.toEntity
import org.senai.metrodoc.common.util.toVersionName
import org.senai.metrodoc.features.report.model.PdfItem
import org.senai.metrodoc.features.report.model.ReportSection
import kotlin.time.Duration.Companion.minutes

interface RoomProjectRepository {
    fun getRecentProjects(): Flow<List<ProjectDto>>

    fun getVersions(projectId: Long): Flow<List<VersionDto>>

    suspend fun getProjectById(projectId: Long): FullProject?

    suspend fun saveProject(
        projectId: Long?,
        projectName: String,
        cliente: String,
        componente: String,
        pdfItems: List<PdfItem>,
    ): Long

    suspend fun deleteProjectById(projectId: Long)

    suspend fun renameVersion(versionId: Long, newName: String)

    suspend fun restoreVersion(versionId: Long): Long

    suspend fun deleteVersion(versionId: Long)

    suspend fun deleteAllProjects()
}

class RoomProjectRepositoryImpl(
    private val projectDao: ProjectDao,
    private val versionDao: VersionDao,
) : RoomProjectRepository {
    override fun getRecentProjects(): Flow<List<ProjectDto>> {
        return projectDao.getProjectsSummaries()
    }

    override fun getVersions(projectId: Long): Flow<List<VersionDto>> {
        return versionDao.getVersions(projectId)
    }

    override suspend fun getProjectById(projectId: Long): FullProject? {
        val entity = projectDao.getProjectById(projectId) ?: return null

        val pdfItems = entity.reports.map { rwm ->
            val measurementData = rwm.measurements.map { it.toDomain() }
            val reportDataDomain = rwm.reportData.toDomain(measurementData)

            val decodedSecoes = runCatching {
                metroDocJson.decodeFromString<List<ReportSection>>(rwm.reportData.secoesJson)
            }.getOrDefault(emptyList())

            val secoesReconstruidas = decodedSecoes.map { secao ->
                when (secao) {
                    is ReportSection.ResultadosDimensionais -> secao.copy(measurements = reportDataDomain.caracteristicas)
                    else -> secao
                }
            }

            PdfItem(
                pdfPath = rwm.reportData.pdfPath,
                pdfName = rwm.reportData.pdfName,
                reportData = reportDataDomain,
                secoes = secoesReconstruidas
            )
        }

        return FullProject(
            projectId = entity.project.id,
            nomeProjeto = entity.project.nomeProjeto,
            cliente = entity.project.cliente,
            componente = entity.project.componente,
            pdfItems = pdfItems
        )
    }

    override suspend fun saveProject(
        projectId: Long?,
        projectName: String,
        cliente: String,
        componente: String,
        pdfItems: List<PdfItem>,
    ): Long {
        val agora = System.currentTimeMillis()

        val projectEntity = ProjectEntity(
            id = projectId ?: 0,
            nomeProjeto = projectName,
            cliente = cliente,
            componente = componente,
            createdAt = agora,
            lastModified = agora
        )

        val reportsWithMeasurements = pdfItems.map { pdfItem ->
            val secoesJson = metroDocJson.encodeToString(pdfItem.secoes)
            val reportEntity = pdfItem.reportData.toEntity(
                id = 0,
                projectId = projectId ?: 0,
                nomeRelatorio = projectName,
                pdfName = pdfItem.pdfName,
                pdfPath = pdfItem.pdfPath,
                lastModified = agora,
                secoesJson = secoesJson
            )
            val measurementEntities = pdfItem.reportData.caracteristicas.map { it.toEntity(0) }
            Pair(reportEntity, measurementEntities)
        }

        val savedProjectId = projectDao.saveFullProject(projectEntity, reportsWithMeasurements)

        val snapshotObj = projectDao.getProjectById(savedProjectId)
        if (snapshotObj != null) {
            val projetoJson = metroDocJson.encodeToString(snapshotObj)

            val ultimaVersao = versionDao.getLatestVersion(savedProjectId)
            if (ultimaVersao != null && (agora - ultimaVersao.createdAt) < 2.5.minutes.inWholeMilliseconds) {
                val versaoAtualizada = ultimaVersao.copy(
                    versionName = agora.toVersionName(),
                    contentJson = projetoJson,
                )
                versionDao.updateVersion(versaoAtualizada)
            } else {
                val novaVersao = VersionEntity(
                    id = 0,
                    projectId = savedProjectId,
                    versionName = agora.toVersionName(),
                    createdAt = agora,
                    contentJson = projetoJson
                )
                versionDao.insertVersion(novaVersao)
            }

            versionDao.trimVersions(projectId = savedProjectId, maxVersions = 15)
        }

        return savedProjectId
    }

    override suspend fun deleteProjectById(projectId: Long) {
        projectDao.deleteProjectById(projectId)
    }

    override suspend fun renameVersion(versionId: Long, newName: String) {
        val versaoEncontrada = versionDao.getVersionById(versionId) ?: return
        val novaVersao = versaoEncontrada.copy(versionName = newName)
        versionDao.updateVersion(novaVersao)
    }

    override suspend fun restoreVersion(versionId: Long): Long {
        val versaoEncontrada = versionDao.getVersionById(versionId) ?: return 0L

        val jsonTexto = versaoEncontrada.contentJson
        val snapshotObj = metroDocJson.decodeFromString<ProjectWithReports>(jsonTexto)

        val agora = System.currentTimeMillis()
        val reportsWithMeasurements = snapshotObj.reports.map { rwm ->
            Pair(rwm.reportData.copy(lastModified = agora), rwm.measurements)
        }
        val projSalvoId = projectDao.saveFullProject(
            snapshotObj.project.copy(lastModified = agora),
            reportsWithMeasurements
        )

        val novaVersao = VersionEntity(
            id = 0,
            projectId = projSalvoId,
            versionName = "${versaoEncontrada.versionName} (restaurada)",
            createdAt = agora,
            contentJson = jsonTexto
        )

        versionDao.insertVersion(novaVersao)
        versionDao.trimVersions(projectId = projSalvoId, maxVersions = 15)

        return projSalvoId
    }

    override suspend fun deleteVersion(versionId: Long) {
        versionDao.deleteVersion(versionId)
    }

    override suspend fun deleteAllProjects() {
        projectDao.deleteAllProjects()
    }
}