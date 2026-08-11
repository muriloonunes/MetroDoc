package org.senai.metrodoc.common.database.dao

import androidx.room3.*
import kotlinx.coroutines.flow.Flow
import org.senai.metrodoc.common.database.dto.ProjectDto
import org.senai.metrodoc.common.database.dto.ProjectWithMeasurements
import org.senai.metrodoc.common.database.entity.MeasurementDataEntity
import org.senai.metrodoc.common.database.entity.ReportDataEntity

@Dao
interface ProjectDao {
    @Query("SELECT id, nomeRelatorio AS nomeProjeto, lastModified AS modificadoEm FROM reports ORDER BY lastModified DESC")
    fun getProjectsSummaries(): Flow<List<ProjectDto>>

    @Transaction
    @Query("SELECT * FROM reports WHERE id = :projectId")
    suspend fun getProjectById(projectId: Long): ProjectWithMeasurements?

    @Insert
    suspend fun insertReport(report: ReportDataEntity): Long

    @Update
    suspend fun updateReport(report: ReportDataEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMeasurements(measurements: List<MeasurementDataEntity>)

    @Query("DELETE FROM measurements WHERE reportId = :reportId")
    suspend fun deleteMeasurements(reportId: Long)

    @Transaction
    suspend fun saveFullProject(report: ReportDataEntity, measurements: List<MeasurementDataEntity>): Long {
        val generatedId = if (report.id == 0L) {
            insertReport(report)
        } else {
            updateReport(report)
            report.id
        }
        deleteMeasurements(generatedId)
        val linkedMeasurements = measurements.map { it.copy(reportId = generatedId) }
        insertMeasurements(linkedMeasurements)

        return generatedId
    }

    @Query("DELETE FROM reports WHERE id = :projectId")
    suspend fun deleteProjectById(projectId: Long)

    @Query("DELETE FROM reports")
    suspend fun deleteAllProjects()
}