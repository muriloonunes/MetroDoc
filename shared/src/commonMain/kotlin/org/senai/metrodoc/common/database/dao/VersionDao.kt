package org.senai.metrodoc.common.database.dao

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import org.senai.metrodoc.common.database.entity.VersionEntity

@Dao
interface VersionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertVersion(version: VersionEntity)
}