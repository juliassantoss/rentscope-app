package com.example.rentscope.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.rentscope.ui.screens.CountrySearchScreen
import com.example.rentscope.ui.screens.DebugPaisesScreen
import com.example.rentscope.ui.screens.FiltersScreen
import com.example.rentscope.ui.screens.HomeScreen
import com.example.rentscope.ui.screens.LoginScreen
import com.example.rentscope.ui.screens.MapScreen
import com.example.rentscope.ui.screens.NewAccountScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val backStackEntry = navController.currentBackStackEntryAsState().value
    val currentDestination = backStackEntry?.destination

    AppScaffold(
        navController = navController,
        currentDestination = currentDestination
    ) { padding ->

        NavHost(
            navController = navController,
            startDestination = Routes.HOME
        ) {

            composable(Routes.HOME) {
                HomeScreen(
                    padding = padding,
                    onContinue = { continent ->
                        navController.navigate(Routes.countries(continent))
                    }
                )
            }

            composable(
                route = Routes.COUNTRIES_WITH_ARG,
                arguments = listOf(navArgument("continent") { type = NavType.StringType })
            ) { entry ->
                val continent = entry.arguments?.getString("continent") ?: "Europa"

                CountrySearchScreen(
                    padding = padding,
                    continent = continent,
                    onCountryClick = { country ->
                        navController.navigate(Routes.map(country.code, country.name))
                    }
                )
            }

            composable(
                route = Routes.MAP,
                arguments = listOf(
                    navArgument("countryCode") { type = NavType.StringType },
                    navArgument("countryName") { type = NavType.StringType }
                )
            ) { entry ->
                val countryCode = entry.arguments?.getString("countryCode") ?: ""
                val countryName = entry.arguments?.getString("countryName") ?: "País"

                MapScreen(
                    padding = padding,
                    countryCode = countryCode,
                    countryName = countryName,
                    onConfigureFiltersClick = {
                        navController.navigate(Routes.filters(countryCode, countryName))
                    }
                )
            }

            composable(
                route = Routes.FILTERS,
                arguments = listOf(
                    navArgument("countryCode") { type = NavType.StringType },
                    navArgument("countryName") { type = NavType.StringType }
                )
            ) { entry ->
                val countryCode = entry.arguments?.getString("countryCode") ?: ""
                val countryName = entry.arguments?.getString("countryName") ?: "País"

                FiltersScreen(
                    padding = padding,
                    countryCode = countryCode,
                    countryName = countryName,
                    onSaveClick = { navController.popBackStack() }
                )
            }

            composable(Routes.LOGIN) {
                LoginScreen(
                    padding = padding,
                    onForgotPasswordClick = { /* placeholder */ },
                    onCreateAccountClick = { navController.navigate(Routes.NEW_ACCOUNT) },
                    onLoginClick = { _, _ -> /* placeholder */ }
                )
            }

            composable(Routes.NEW_ACCOUNT) {
                NewAccountScreen(
                    padding = padding,
                    onBackToLoginClick = { navController.popBackStack() },
                    onCreateAccountClick = { _, _, _ ->
                        navController.popBackStack()
                    }
                )
            }

            // Tela de teste da API (/paises)
            composable(Routes.DEBUG_PAISES) {
                DebugPaisesScreen()
            }

            // Footer placeholders
            composable(Routes.LANGUAGE) {
                PlaceholderScreen(padding = padding, text = "Idioma (em construção)")
            }
            composable(Routes.RESULTS) {
                PlaceholderScreen(padding = padding, text = "Resultados (em construção)")
            }
        }
    }
}

@Composable
fun PlaceholderScreen(
    padding: PaddingValues,
    text: String
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding),
        contentAlignment = Alignment.Center
    ) {
        Text(text)
    }
}