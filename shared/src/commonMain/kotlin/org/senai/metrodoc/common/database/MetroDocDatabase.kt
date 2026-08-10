package org.senai.metrodoc.common.database

import androidx.room3.*
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.coroutines.Dispatchers
import org.senai.metrodoc.common.database.dao.ProjectDao
import org.senai.metrodoc.common.database.dao.VersionDao
import org.senai.metrodoc.common.database.entity.DrawShapeConverters
import org.senai.metrodoc.common.database.entity.MeasurementDataEntity
import org.senai.metrodoc.common.database.entity.ReportDataEntity
import org.senai.metrodoc.common.database.entity.VersionEntity

@Database(
    entities = [
        ReportDataEntity::class,
        MeasurementDataEntity::class,
        VersionEntity::class
    ], version = 2
)
@ConstructedBy(AppDatabaseConstructor::class)
@ColumnTypeConverters(DrawShapeConverters::class)
abstract class MetroDocDatabase : RoomDatabase() {
    abstract fun getProjectDao(): ProjectDao

    abstract fun getVersionDao(): VersionDao
}

expect object AppDatabaseConstructor : RoomDatabaseConstructor<MetroDocDatabase> {
    override fun initialize(): MetroDocDatabase
}

fun getRoomDatabase(
    builder: RoomDatabase.Builder<MetroDocDatabase>
): MetroDocDatabase {
    return builder.
            setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(Dispatchers.IO)
        .build()
}