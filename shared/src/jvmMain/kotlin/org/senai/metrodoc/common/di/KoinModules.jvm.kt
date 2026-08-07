package org.senai.metrodoc.common.di

import org.koin.core.module.Module
import org.koin.dsl.module
import org.senai.metrodoc.common.database.getDatabaseBuilder
import org.senai.metrodoc.common.database.getRoomDatabase

actual val platformModule: Module = module {
    single {
        getRoomDatabase(
            getDatabaseBuilder()
        )
    }
}