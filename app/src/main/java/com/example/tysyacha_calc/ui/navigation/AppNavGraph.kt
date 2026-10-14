package com.example.tysyacha_calc.ui.navigation

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.tysyacha_calc.ui.calculator.CalculatorScreen
import com.example.tysyacha_calc.ui.history.HistoryScreen
import com.example.tysyacha_calc.ui.home.HomeScreen
import com.example.tysyacha_calc.ui.settings.SettingsScreen

/** Граф навигации: главная → новая партия → калькулятор ↔ история. */
@Composable
fun AppNavGraph(navController: NavHostController = rememberNavController()) {
    NavHost(
        navController = navController,
        startDestination = HomeRoute,
        enterTransition = { slideInHorizontally { it / 4 } + fadeIn() },
        exitTransition = { fadeOut() },
        popEnterTransition = { fadeIn() },
        popExitTransition = { slideOutHorizontally { it / 4 } + fadeOut() },
    ) {
        composable<HomeRoute> {
            HomeScreen(
                onNewGame = { navController.navigate(NewGameRoute) },
                onOpenGame = { game ->
                    navController.navigate(if (game.isFinished) HistoryRoute(game.id) else CalculatorRoute(game.id))
                },
            )
        }
        composable<NewGameRoute> {
            SettingsScreen(
                onBack = { navController.popBackStack() },
                onGameStarted = { id ->
                    navController.navigate(CalculatorRoute(id)) { popUpTo(HomeRoute) }
                },
            )
        }
        composable<CalculatorRoute> {
            CalculatorScreen(
                onBack = { navController.popBackStack() },
                onOpenHistory = { id -> navController.navigate(HistoryRoute(id)) },
                onNewGame = { navController.navigate(NewGameRoute) { popUpTo(HomeRoute) } },
            )
        }
        composable<HistoryRoute> { entry ->
            val route = entry.toRoute<HistoryRoute>()
            HistoryScreen(
                gameId = route.gameId,
                onBack = { navController.popBackStack() },
                onContinue = { id ->
                    val fromCalculator = navController.previousBackStackEntry?.destination?.hasRoute(CalculatorRoute::class) == true
                    if (fromCalculator) navController.popBackStack()
                    else navController.navigate(CalculatorRoute(id)) { popUpTo(HomeRoute) }
                },
            )
        }
    }
}
