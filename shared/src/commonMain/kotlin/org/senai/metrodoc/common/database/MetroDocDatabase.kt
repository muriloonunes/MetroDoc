package org.senai.metrodoc.common.database

import androidx.room3.ConstructedBy
import androidx.room3.Database
import androidx.room3.RoomDatabase
import androidx.room3.RoomDatabaseConstructor
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.coroutines.Dispatchers
import org.senai.metrodoc.common.database.dao.ProjectDao
import org.senai.metrodoc.common.database.entity.MeasurementDataEntity
import org.senai.metrodoc.common.database.entity.ReportDataEntity

@Database(
    entities = [
        ReportDataEntity::class,
        MeasurementDataEntity::class,
    ], version = 1
)
@ConstructedBy(AppDatabaseConstructor::class)
abstract class MetroDocDatabase : RoomDatabase() {
    abstract fun getProjectDao(): ProjectDao
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