package org.senai.metrodoc

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import io.github.vinceglb.filekit.FileKit
import org.senai.metrodoc.common.di.initKoin
import org.senai.metrodoc.main.App

fun main() {
    val appName = "MetroDoc"
    FileKit.init(appId = appName)

    initKoin {
        printLogger()
    }

    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = appName,
        ) {
            App()
        }
    }
}