package org.senai.metrodoc.common.database.dao

import androidx.room3.*
import kotlinx.coroutines.flow.Flow
import org.senai.metrodoc.common.database.dto.VersionDto
import org.senai.metrodoc.common.database.entity.VersionEntity

@Dao
interface VersionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVersion(version: VersionEntity)

    @Query("SELECT id, versionName, createdAt FROM versions WHERE projectId = :projectId ORDER BY createdAt DESC")
    fun getVersions(projectId: Long): Flow<List<VersionDto>>

    @Query("SELECT * FROM versions WHERE projectId = :projectId ORDER BY createdAt DESC LIMIT 1")
    suspend fun getLatestVersion(projectId: Long): VersionEntity?

    @Query("SELECT * FROM versions WHERE id = :versionId")
    suspend fun getVersionById(versionId: Long): VersionEntity?

    @Update
    suspend fun updateVersion(version: VersionEntity)

    @Query(
        """
        DELETE FROM versions 
        WHERE projectId = :projectId 
        AND id NOT IN (
            SELECT id FROM versions 
            WHERE projectId = :projectId 
            ORDER BY createdAt DESC 
            LIMIT :maxVersions
        )
    """
    )
    suspend fun trimVersions(projectId: Long, maxVersions: Int = 10)

    @Query("DELETE FROM versions WHERE id = :versionId")
    suspend fun deleteVersion(versionId: Long)
}