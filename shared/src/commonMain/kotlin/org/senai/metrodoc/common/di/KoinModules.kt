package org.senai.metrodoc.common.di

import org.koin.core.module.Module
import org.koin.dsl.module
import org.senai.metrodoc.features.report.reportModule
import org.senai.metrodoc.features.welcome.welcomeModule

val sharedModule = module {
    includes(appModule, welcomeModule, reportModule)
}

expect val platformModule: Module