package com.berkbelhan.indoornavigation.presentation.nav

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.runtime.*
import androidx.navigation.*
import androidx.navigation.compose.*
import com.berkbelhan.indoornavigation.presentation.ar.ArNavigationScreen
import com.berkbelhan.indoornavigation.presentation.auth.AuthScreen
import com.berkbelhan.indoornavigation.presentation.dashboard.DashboardScreen
import com.berkbelhan.indoornavigation.presentation.downloads.DownloadsScreen
import com.berkbelhan.indoornavigation.presentation.maps.IndoorMapScreen
import com.berkbelhan.indoornavigation.presentation.settings.SettingsScreen

@Composable
fun AppNavGraph(
    startDestination: String = NavDestination.Auth.route
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = startDestination,
        enterTransition = {
            slideInHorizontally(initialOffsetX = { it }, animationSpec = tween(300)) +
                    fadeIn(animationSpec = tween(300))
        },
        exitTransition = {
            slideOutHorizontally(targetOffsetX = { -it / 3 }, animationSpec = tween(300)) +
                    fadeOut(animationSpec = tween(300))
        },
        popEnterTransition = {
            slideInHorizontally(initialOffsetX = { -it / 3 }, animationSpec = tween(300)) +
                    fadeIn(animationSpec = tween(300))
        },
        popExitTransition = {
            slideOutHorizontally(targetOffsetX = { it }, animationSpec = tween(300)) +
                    fadeOut(animationSpec = tween(300))
        }
    ) {
        composable(NavDestination.Auth.route) {
            AuthScreen(
                onAuthSuccess = {
                    navController.navigate(NavDestination.Dashboard.route) {
                        popUpTo(NavDestination.Auth.route) { inclusive = true }
                    }
                }
            )
        }

        composable(NavDestination.Dashboard.route) {
            DashboardScreen(
                onOpenMap = { mapId ->
                    navController.navigate(NavDestination.IndoorMap.createRoute(mapId))
                },
                onOpenDownloads = {
                    navController.navigate(NavDestination.Downloads.route)
                },
                onOpenSettings = {
                    navController.navigate(NavDestination.Settings.route)
                }
            )
        }

        composable(
            route = NavDestination.IndoorMap.route,
            arguments = listOf(navArgument("mapId") { type = NavType.StringType })
        ) { backStackEntry ->
            val mapId = backStackEntry.arguments?.getString("mapId") ?: return@composable
            IndoorMapScreen(
                mapId = mapId,
                onOpenAr = {
                    navController.navigate(NavDestination.ArNavigation.createRoute(mapId))
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = NavDestination.ArNavigation.route,
            arguments = listOf(navArgument("mapId") { type = NavType.StringType })
        ) { backStackEntry ->
            val mapId = backStackEntry.arguments?.getString("mapId") ?: return@composable
            ArNavigationScreen(
                mapId = mapId,
                onStop = { navController.popBackStack() }
            )
        }

        composable(NavDestination.Downloads.route) {
            DownloadsScreen(onBack = { navController.popBackStack() })
        }

        composable(NavDestination.Settings.route) {
            SettingsScreen(
                onBack = { navController.popBackStack() },
                onSignOut = {
                    navController.navigate(NavDestination.Auth.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    }
}
