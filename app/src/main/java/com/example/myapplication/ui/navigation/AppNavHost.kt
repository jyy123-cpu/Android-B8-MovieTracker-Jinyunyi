package com.example.myapplication.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.compose.ui.Modifier
import com.example.myapplication.ui.detail.DetailRoute
import com.example.myapplication.ui.discover.DiscoverRoute
import com.example.myapplication.ui.reminder.ReminderScreen
import com.example.myapplication.ui.search.SearchRoute
import com.example.myapplication.ui.watchlist.WatchlistRoute

/**
 * 应用总导航：底部四个一级入口 + 二级详情页。
 */
@Composable
fun AppNavHost() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            // 详情页等二级页面隐藏底部导航栏
            val showBottomBar = bottomItems.any { it.route == currentRoute }
            if (showBottomBar) {
                NavigationBar {
                    bottomItems.forEach { item ->
                        NavigationBarItem(
                            selected = currentRoute == item.route,
                            onClick = {
                                navController.navigate(item.route) {
                                    // 避免在顶部重复创建实例
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(item.icon, contentDescription = item.label) },
                            label = { Text(text = item.label) }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Routes.DISCOVER,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Routes.DISCOVER) {
                DiscoverRoute(
                    onMovieClick = { movieId ->
                        navController.navigate(Routes.detail(movieId))
                    }
                )
            }
            composable(Routes.SEARCH) {
                SearchRoute(
                    onMovieClick = { movieId ->
                        navController.navigate(Routes.detail(movieId))
                    }
                )
            }
            composable(Routes.WATCHLIST) {
                WatchlistRoute(
                    onMovieClick = { movieId ->
                        navController.navigate(Routes.detail(movieId))
                    }
                )
            }
            composable(Routes.REMINDER) {
                ReminderScreen()
            }
            composable(
                route = Routes.DETAIL,
                arguments = listOf(
                    navArgument(Routes.DETAIL_ARG_MOVIE_ID) {
                        type = NavType.IntType
                    }
                )
            ) { entry ->
                val movieId = entry.arguments?.getInt(Routes.DETAIL_ARG_MOVIE_ID) ?: 0
                DetailRoute(
                    movieId = movieId,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}
