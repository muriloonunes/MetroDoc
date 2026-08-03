package org.senai.metrodoc.common.routes

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import org.koin.compose.viewmodel.koinViewModel
import org.senai.metrodoc.features.report.presentation.ReportCreatorIntent
import org.senai.metrodoc.features.report.presentation.ReportCreatorViewModel
import org.senai.metrodoc.features.report.presentation.ui.ReportCreatorScreen
import org.senai.metrodoc.features.welcome.presentation.WelcomeScreenViewModel
import org.senai.metrodoc.features.welcome.presentation.ui.WelcomeScreen

@Composable
fun MetroDocNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = Route.Welcome,
        modifier = modifier
    ) {
        composable<Route.Welcome> {
            val welcomeViewModel = koinViewModel<WelcomeScreenViewModel>()
            val state by welcomeViewModel.state.collectAsState()
            WelcomeScreen(
                state = state,
                onIntent = { intent -> welcomeViewModel.handleIntent(intent) },
                onNavigateToRelatoryCreator = { path, name ->
                    navController.navigate(Route.RelatoryCreator(path = path, name = name))
                },
                effect = welcomeViewModel.effect,
            )
        }
        composable<Route.RelatoryCreator> { backStackEntry ->
            val route = backStackEntry.toRoute<Route.RelatoryCreator>()
            val relatoryViewModel =koinViewModel<ReportCreatorViewModel>()
            val state by relatoryViewModel.state.collectAsState()

            LaunchedEffect(route.path) {
                relatoryViewModel.handleIntent(ReportCreatorIntent.OnInit(path = route.path, name = route.name))
            }

            ReportCreatorScreen(
                state = state,
                effect = relatoryViewModel.effect,
                onIntent = { intent -> relatoryViewModel.handleIntent(intent) },
                onBack = { navController.popBackStack() }
            )
        }
    }
}