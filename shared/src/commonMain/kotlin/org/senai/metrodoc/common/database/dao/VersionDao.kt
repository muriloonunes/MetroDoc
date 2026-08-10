package org.senai.metrodoc.common.database.dao

import androidx.room3.*
import org.senai.metrodoc.common.database.entity.VersionEntity

@Dao
interface VersionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertVersion(version: VersionEntity)

    @Query("SELECT * FROM versions WHERE projectId = :projectId")
    fun getVersions(projectId: Long): List<VersionEntity>

    @Query("SELECT * FROM versions WHERE projectId = :projectId ORDER BY createdAt DESC LIMIT 1")
    fun getLatestVersion(projectId: Long): VersionEntity?

    @Update
    fun updateVersion(version: VersionEntity)

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
}