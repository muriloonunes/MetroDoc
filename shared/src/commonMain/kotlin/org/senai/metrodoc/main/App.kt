package org.senai.metrodoc.main

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import org.senai.metrodoc.common.routes.MetroDocNavHost
import org.senai.metrodoc.common.theme.MetroDocTheme

@Composable
@Preview
fun App() {
    val navController = rememberNavController()
    MetroDocTheme {
        Scaffold(

        ) { paddingValues ->
            MetroDocNavHost(
                navController = navController,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            )
        }
    }
}