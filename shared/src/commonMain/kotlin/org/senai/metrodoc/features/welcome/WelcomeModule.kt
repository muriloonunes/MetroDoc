package org.senai.metrodoc.features.welcome

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import org.senai.metrodoc.features.welcome.presentation.WelcomeScreenViewModel

val welcomeModule = module {
    viewModel { WelcomeScreenViewModel(
        roomProjectRepository = get(),
        memoryReportRepository = get(),
        pdfParser = get(),
        pdfGenerator = get()
    ) }
}