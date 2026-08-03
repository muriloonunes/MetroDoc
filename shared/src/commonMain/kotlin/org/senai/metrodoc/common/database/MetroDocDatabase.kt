package org.senai.metrodoc.common.database

import androidx.room3.ConstructedBy
import androidx.room3.Database
import androidx.room3.RoomDatabase
import androidx.room3.RoomDatabaseConstructor
import org.senai.metrodoc.common.database.entity.MeasurementDataEntity
import org.senai.metrodoc.common.database.entity.ReportDataEntity

@Database(
    entities = [
        ReportDataEntity::class,
        MeasurementDataEntity::class,
    ], version = 1
)
@ConstructedBy(AppDatabaseConstructor::class)
abstract class MetroDocDatabase : RoomDatabase()

expect object AppDatabaseConstructor : RoomDatabaseConstructor<MetroDocDatabase> {
    override fun initialize(): MetroDocDatabase
}