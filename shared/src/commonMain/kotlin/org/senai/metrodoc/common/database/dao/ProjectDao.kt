package org.senai.metrodoc.common.database.dao

import androidx.room3.*
import kotlinx.coroutines.flow.Flow
import org.senai.metrodoc.common.database.dto.ProjectDto
import org.senai.metrodoc.common.database.dto.ProjectWithReports
import org.senai.metrodoc.common.database.entity.MeasurementDataEntity
import org.senai.metrodoc.common.database.entity.ProjectEntity
import org.senai.metrodoc.common.database.entity.ReportDataEntity

@Dao
interface ProjectDao {
    @Query("""
        SELECT p.id, p.nomeProjeto, p.cliente, p.componente, p.lastModified AS modificadoEm, COUNT(r.id) AS qtdPdfs
        FROM projects p
        LEFT JOIN reports r ON r.projectId = p.id
        GROUP BY p.id
        ORDER BY p.lastModified DESC
    """)
    fun getProjectsSummaries(): Flow<List<ProjectDto>>

    @Transaction
    @Query("SELECT * FROM projects WHERE id = :projectId")
    suspend fun getProjectById(projectId: Long): ProjectWithReports?

    @Insert
    suspend fun insertProject(project: ProjectEntity): Long

    @Update
    suspend fun updateProject(project: ProjectEntity)

    @Insert
    suspend fun insertReport(report: ReportDataEntity): Long

    @Update
    suspend fun updateReport(report: ReportDataEntity)

    @Query("DELETE FROM reports WHERE projectId = :projectId")
    suspend fun deleteReportsByProjectId(projectId: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMeasurements(measurements: List<MeasurementDataEntity>)

    @Query("DELETE FROM measurements WHERE reportId = :reportId")
    suspend fun deleteMeasurements(reportId: Long)

    @Transaction
    suspend fun saveFullProject(
        project: ProjectEntity,
        reportsWithMeasurements: List<Pair<ReportDataEntity, List<MeasurementDataEntity>>>
    ): Long {
        val savedProjectId = if (project.id == 0L) {
            insertProject(project)
        } else {
            updateProject(project)
            project.id
        }

        deleteReportsByProjectId(savedProjectId)

        reportsWithMeasurements.forEach { (report, measurements) ->
            val reportEntity = report.copy(id = 0, projectId = savedProjectId)
            val savedReportId = insertReport(reportEntity)
            deleteMeasurements(savedReportId)
            val linkedMeasurements = measurements.map { it.copy(id = 0, reportId = savedReportId) }
            insertMeasurements(linkedMeasurements)
        }

        return savedProjectId
    }

    @Query("DELETE FROM projects WHERE id = :projectId")
    suspend fun deleteProjectById(projectId: Long)

    @Query("DELETE FROM projects")
    suspend fun deleteAllProjects()
}