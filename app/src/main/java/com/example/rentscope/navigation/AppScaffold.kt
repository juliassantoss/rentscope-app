package com.example.rentscope.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import kotlinx.coroutines.launch

private val BrandBlue = Color(0xFF2F86D6)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppScaffold(
    navController: NavController,
    currentDestination: NavDestination?,
    content: @Composable (PaddingValues) -> Unit
) {
    val canGoBack = navController.previousBackStackEntry != null

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    // Drawer abrindo pela direita
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                    AppDrawer(
                        onClose = { scope.launch { drawerState.close() } },
                        onLoginClick = {
                            scope.launch { drawerState.close() }
                            navController.navigate(Routes.LOGIN)
                        },
                        onItemClick = {
                            scope.launch { drawerState.close() }
                            // depois liga rotas aqui
                        }
                    )
                }
            }
        ) {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = { Text("RentScope") },

                            navigationIcon = {
                                if (canGoBack) {
                                    IconButton(onClick = { navController.popBackStack() }) {
                                        Icon(
                                            Icons.AutoMirrored.Filled.ArrowBack,
                                            contentDescription = "Voltar"
                                        )
                                    }
                                }
                            },

                            actions = {
                                IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                    Icon(Icons.Filled.Menu, contentDescription = "Menu")
                                }
                            },

                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = BrandBlue,
                                titleContentColor = Color.White,
                                navigationIconContentColor = Color.White,
                                actionIconContentColor = Color.White
                            )
                        )
                    },

                    bottomBar = {
                        NavigationBar {
                            NavigationBarItem(
                                selected = currentDestination.isRouteSelected(Routes.LANGUAGE),
                                onClick = { navController.navigateSingleTopTo(Routes.LANGUAGE) },
                                icon = { Icon(Icons.Filled.Language, contentDescription = "Idioma") },
                                label = { Text("Idioma") }
                            )

                            NavigationBarItem(
                                selected = currentDestination.isRouteSelected(Routes.HOME),
                                onClick = { navController.navigateSingleTopTo(Routes.HOME) },
                                icon = { Icon(Icons.Filled.Home, contentDescription = "Início") },
                                label = { Text("Início") }
                            )

                            NavigationBarItem(
                                selected = currentDestination.isRouteSelected(Routes.RESULTS),
                                onClick = { navController.navigateSingleTopTo(Routes.RESULTS) },
                                icon = { Icon(Icons.Filled.ShowChart, contentDescription = "Resultados") },
                                label = { Text("Resultados") }
                            )
                        }
                    }
                ) { padding ->
                    content(padding)
                }
            }
        }
    }
}

private fun NavDestination?.isRouteSelected(route: String): Boolean {
    return this?.hierarchy?.any { it.route == route } == true
}

private fun NavController.navigateSingleTopTo(route: String) {
    navigate(route) {
        launchSingleTop = true
        restoreState = true
        popUpTo(graph.startDestinationId) { saveState = true }
    }
}
