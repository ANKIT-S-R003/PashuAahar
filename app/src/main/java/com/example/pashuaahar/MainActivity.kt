package com.example.pashuaahar

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material3.*
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.pashuaahar.data.AppDatabase
import com.example.pashuaahar.data.AppRepository
import com.example.pashuaahar.ui.*
import com.example.pashuaahar.ui.theme.PashuAaharTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        val database = AppDatabase.getDatabase(this)
        val repository = AppRepository(database.appDao())
        val factory = PashuViewModelFactory(repository)

        setContent {
            PashuAaharTheme {
                val navController = rememberNavController()
                val viewModel: PashuViewModel = viewModel(factory = factory)

                Scaffold(
                    bottomBar = {
                        NavigationBar(
                            containerColor = MaterialTheme.colorScheme.surface,
                            tonalElevation = 8.dp
                        ) {
                            val navBackStackEntry by navController.currentBackStackEntryAsState()
                            val currentDestination = navBackStackEntry?.destination
                            
                            val items = listOf(
                                Screen.Dashboard,
                                Screen.Cows,
                                Screen.History,
                                Screen.Tips
                            )
                            
                            items.forEach { screen ->
                                NavigationBarItem(
                                    icon = { Icon(screen.icon, contentDescription = null) },
                                    label = { Text(stringResource(screen.resourceId)) },
                                    selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                                    onClick = {
                                        navController.navigate(screen.route) {
                                            popUpTo(navController.graph.findStartDestination().id) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = Screen.Dashboard.route,
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable(Screen.Dashboard.route) {
                            DashboardScreen(
                                viewModel = viewModel,
                                onAddCow = { navController.navigate(Screen.AddCow.route) },
                                onViewRecipe = { navController.navigate(Screen.Recipe.route) }
                            )
                        }
                        composable(Screen.Cows.route) {
                            CowListScreen(viewModel) {
                                navController.navigate(Screen.AddCow.route)
                            }
                        }
                        composable(Screen.AddCow.route) {
                            AddCowScreen(viewModel) {
                                navController.popBackStack()
                            }
                        }
                        composable(Screen.Recipe.route) {
                            FeedRecipeScreen(viewModel) {
                                navController.popBackStack()
                            }
                        }
                        composable(Screen.History.route) {
                            RecipeHistoryScreen(viewModel)
                        }
                        composable(Screen.Tips.route) {
                            VeterinaryTipsScreen(viewModel)
                        }
                    }
                }
            }
        }
    }
}

sealed class Screen(val route: String, val resourceId: Int, val icon: ImageVector) {
    object Dashboard : Screen("dashboard", R.string.nav_home, Icons.Default.Dashboard)
    object Cows : Screen("cows", R.string.nav_cows, Icons.Default.Pets)
    object AddCow : Screen("add_cow", R.string.add_cow_action, Icons.Default.Pets)
    object Recipe : Screen("recipe", R.string.get_feed_action, Icons.AutoMirrored.Filled.MenuBook)
    object History : Screen("history", R.string.nav_history, Icons.Default.History)
    object Tips : Screen("tips", R.string.nav_tips, Icons.Default.HealthAndSafety)
}
