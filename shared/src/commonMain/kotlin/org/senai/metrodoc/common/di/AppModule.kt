package org.senai.metrodoc.common.di

import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module
import org.senai.metrodoc.common.data.RoomProjectRepository
import org.senai.metrodoc.common.data.RoomProjectRepositoryImpl
import org.senai.metrodoc.common.database.MetroDocDatabase
import org.senai.metrodoc.common.util.PdfGenerator
import org.senai.metrodoc.common.util.PdfParser
import org.senai.metrodoc.common.util.PdfRenderEngine
import org.senai.metrodoc.features.report.data.InMemoryMemoryReportRepository
import org.senai.metrodoc.features.report.data.MemoryReportRepository

val appModule = module {
    singleOf(::PdfGenerator)
    singleOf(::PdfRenderEngine)
    singleOf(::PdfParser)

    single { get<MetroDocDatabase>().getProjectDao() }

    singleOf(::InMemoryMemoryReportRepository) bind MemoryReportRepository::class
    singleOf(::RoomProjectRepositoryImpl) bind RoomProjectRepository::class
}