package org.senai.metrodoc.features.report

import org.koin.dsl.module
import org.koin.plugin.module.dsl.viewModel
import org.senai.metrodoc.common.util.PdfParser
import org.senai.metrodoc.common.util.PdfRenderEngine
import org.senai.metrodoc.features.report.presentation.ReportCreatorViewModel

val reportModule = module {
    viewModel<ReportCreatorViewModel>()
    single { PdfParser() }
    single { PdfRenderEngine() }
}