package org.senai.metrodoc.common.di

import org.koin.dsl.module
import org.senai.metrodoc.common.util.PdfGenerator

val appModule = module {
    single { PdfGenerator() }
}