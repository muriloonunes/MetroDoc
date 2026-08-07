package org.senai.metrodoc.common.database

import androidx.room3.Room
import androidx.room3.RoomDatabase
import java.io.File

fun getDatabaseBuilder(): RoomDatabase.Builder<MetroDocDatabase> {
    val dbFile = File(System.getProperty("user.home"), ".metrodoc/metrodoc.db")
    return Room.databaseBuilder<MetroDocDatabase>(
        name = dbFile.absolutePath,
    )
}