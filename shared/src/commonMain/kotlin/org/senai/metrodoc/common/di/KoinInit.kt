package org.senai.metrodoc.common.di

import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.includes

fun initKoin(config: KoinAppDeclaration? = null): KoinApplication {
    return startKoin {
        printLogger(Level.DEBUG)
        includes(config)
        modules(sharedModule, platformModule)
    }
}