package org.senai.metrodoc.features.report

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import org.senai.metrodoc.features.report.presentation.ReportCreatorViewModel

val reportModule = module {
    viewModel { ReportCreatorViewModel(
        memoryReportRepository = get(),
        roomProjectRepository = get(),
        renderEngine = get(),
        pdfGenerator = get()
    ) }
}