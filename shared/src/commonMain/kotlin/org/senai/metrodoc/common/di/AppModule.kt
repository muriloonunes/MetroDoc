package org.senai.metrodoc.common.di

import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module
import org.senai.metrodoc.common.data.RoomProjectRepository
import org.senai.metrodoc.common.data.RoomProjectRepositoryImpl
import org.senai.metrodoc.common.database.MetroDocDatabase
import org.senai.metrodoc.common.util.PdfGenerator
import org.senai.metrodoc.features.report.data.InMemoryMemoryReportRepository
import org.senai.metrodoc.features.report.data.MemoryReportRepository

val appModule = module {
    single { PdfGenerator() }
    single<MemoryReportRepository> { InMemoryMemoryReportRepository() }
    single { get<MetroDocDatabase>().getProjectDao() }
    singleOf(::RoomProjectRepositoryImpl) bind RoomProjectRepository::class
}