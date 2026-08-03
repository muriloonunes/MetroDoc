package org.senai.metrodoc.features.welcome

import org.koin.dsl.module
import org.koin.plugin.module.dsl.viewModel
import org.senai.metrodoc.features.welcome.presentation.WelcomeScreenViewModel

val welcomeModule = module {
        viewModel<WelcomeScreenViewModel>()
}