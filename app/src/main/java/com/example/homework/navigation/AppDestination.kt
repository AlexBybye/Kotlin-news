package com.example.homework.navigation

sealed class AppDestination(val route: String) {
    data object Home : AppDestination("home")
    data object Discover : AppDestination("discover")
    data object Favorite : AppDestination("favorite")
    data object Profile : AppDestination("profile")
    data object Search : AppDestination("search")
    data object Settings : AppDestination("settings")
}
