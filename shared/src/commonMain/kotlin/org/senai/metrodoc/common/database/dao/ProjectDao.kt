package org.senai.metrodoc.common.database.dao

import androidx.room3.*
import org.senai.metrodoc.common.database.dto.ProjectDto
import org.senai.metrodoc.common.database.dto.ProjectWithMeasurements
import org.senai.metrodoc.common.database.entity.MeasurementDataEntity
import org.senai.metrodoc.common.database.entity.ReportDataEntity

@Dao
interface ProjectDao {
    @Query("SELECT id, nomeRelatorio AS nomeProjeto, lastModified AS modificadoEm FROM reports ORDER BY lastModified DESC")
    suspend fun getProjectsSummaries(): List<ProjectDto>

    @Transaction
    @Query("SELECT * FROM reports WHERE id = :projectId")
    suspend fun getProjectById(projectId: Long): ProjectWithMeasurements?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReport(report: ReportDataEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMeasurements(measurements: List<MeasurementDataEntity>)

    @Query("DELETE FROM measurements WHERE reportId = :reportId")
    suspend fun deleteMeasurements(reportId: Long)

    @Transaction
    suspend fun saveFullProject(report: ReportDataEntity, measurements: List<MeasurementDataEntity>): Long {
        val generatedId = insertReport(report)
        deleteMeasurements(generatedId)
        val linkedMeasurements = measurements.map { it.copy(reportId = generatedId) }
        insertMeasurements(linkedMeasurements)

        return generatedId
    }
}