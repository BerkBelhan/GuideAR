package com.berkbelhan.indoornavigation.presentation.nav

/** All navigation destinations defined in one place. */
sealed class NavDestination(val route: String) {
    object Auth : NavDestination("auth")
    object Dashboard : NavDestination("dashboard")
    object IndoorMap : NavDestination("map/{mapId}") {
        fun createRoute(mapId: String) = "map/$mapId"
    }
    object ArNavigation : NavDestination("ar/{mapId}") {
        fun createRoute(mapId: String) = "ar/$mapId"
    }
    object Downloads : NavDestination("downloads")
    object Settings : NavDestination("settings")
}
