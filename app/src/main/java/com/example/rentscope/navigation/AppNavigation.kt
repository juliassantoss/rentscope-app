package com.example.rentscope.navigation

import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.rentscope.data.local.LastSearchData
import com.example.rentscope.data.local.LastSearchManager
import com.example.rentscope.data.local.TokenManager
import com.example.rentscope.data.remote.dto.history.FiltroSalvoDto
import com.example.rentscope.ui.screens.AiAssistantScreen
import com.example.rentscope.ui.screens.CountrySearchScreen
import com.example.rentscope.ui.screens.DebugPaisesScreen
import com.example.rentscope.ui.screens.FavoritesScreen
import com.example.rentscope.ui.screens.FiltersScreen
import com.example.rentscope.ui.screens.HistoryScreen
import com.example.rentscope.ui.screens.HomeScreen
import com.example.rentscope.ui.screens.LanguageScreen
import com.example.rentscope.ui.screens.LoginScreen
import com.example.rentscope.ui.screens.MapScreen
import com.example.rentscope.ui.screens.NewAccountScreen
import com.example.rentscope.ui.screens.PriceHistoryScreen
import com.example.rentscope.ui.screens.ResultsScreen
import com.example.rentscope.ui.viewmodel.AuthViewModel

@Composable
fun AppNavigation() {
    val context = LocalContext.current
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel()

    LaunchedEffect(Unit) {
        TokenManager.init(context)
        LastSearchManager.init(context)
    }

    val backStackEntry = navController.currentBackStackEntryAsState().value
    val currentDestination = backStackEntry?.destination

    fun openSearchFromHistory(item: FiltroSalvoDto) {
        navController.navigate(
            Routes.map(
                countryCode = item.country_code,
                countryName = item.country_name,
                rendaMin = item.renda_min,
                rendaMax = item.renda_max,
                pesoRenda = item.peso_renda,
                pesoEscolas = item.peso_escolas,
                pesoHospitais = item.peso_hospitais,
                pesoCriminalidade = item.peso_criminalidade,
                saveToHistory = false
            )
        )
    }

    fun openLastSearchOnMap(data: LastSearchData) {
        navController.navigate(
            Routes.map(
                countryCode = data.countryCode,
                countryName = data.countryName,
                rendaMin = data.rendaMin,
                rendaMax = data.rendaMax,
                pesoRenda = data.pesoRenda,
                pesoEscolas = data.pesoEscolas,
                pesoHospitais = data.pesoHospitais,
                pesoCriminalidade = data.pesoCriminalidade,
                saveToHistory = false
            )
        )
    }

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
                    },
                    onOpenAssistant = {
                        navController.navigate(Routes.AI_ASSISTANT)
                    }
                )
            }

            composable(
                route = Routes.COUNTRIES_WITH_ARG,
                arguments = listOf(
                    navArgument("continent") { type = NavType.StringType }
                )
            ) { entry ->
                val continent = Uri.decode(entry.arguments?.getString("continent") ?: "Europa")

                CountrySearchScreen(
                    padding = padding,
                    continent = continent,
                    onCountryClick = { country ->
                        navController.navigate(
                            Routes.map(
                                countryCode = country.code,
                                countryName = country.name,
                                saveToHistory = true
                            )
                        )
                    }
                )
            }

            composable(
                route = Routes.MAP,
                arguments = listOf(
                    navArgument("countryCode") { type = NavType.StringType },
                    navArgument("countryName") { type = NavType.StringType },
                    navArgument("rendaMin") {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    },
                    navArgument("rendaMax") {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    },
                    navArgument("pesoRenda") {
                        type = NavType.FloatType
                        defaultValue = 1f
                    },
                    navArgument("pesoEscolas") {
                        type = NavType.FloatType
                        defaultValue = 1f
                    },
                    navArgument("pesoHospitais") {
                        type = NavType.FloatType
                        defaultValue = 1f
                    },
                    navArgument("pesoCriminalidade") {
                        type = NavType.FloatType
                        defaultValue = 1f
                    },
                    navArgument("saveToHistory") {
                        type = NavType.BoolType
                        defaultValue = true
                    }
                )
            ) { entry ->
                val countryCode = Uri.decode(entry.arguments?.getString("countryCode") ?: "")
                val countryName = Uri.decode(entry.arguments?.getString("countryName") ?: "País")

                val routeRendaMin = entry.arguments?.getString("rendaMin")
                    ?.takeIf { it.isNotBlank() }
                    ?.toFloatOrNull()

                val routeRendaMax = entry.arguments?.getString("rendaMax")
                    ?.takeIf { it.isNotBlank() }
                    ?.toFloatOrNull()

                val stateHandle = entry.savedStateHandle

                val rendaMinFlow = stateHandle.getStateFlow<Float?>("rendaMin", routeRendaMin)
                val rendaMaxFlow = stateHandle.getStateFlow<Float?>("rendaMax", routeRendaMax)
                val pesoRendaFlow = stateHandle.getStateFlow(
                    "pesoRenda",
                    entry.arguments?.getFloat("pesoRenda") ?: 1f
                )
                val pesoEscolasFlow = stateHandle.getStateFlow(
                    "pesoEscolas",
                    entry.arguments?.getFloat("pesoEscolas") ?: 1f
                )
                val pesoHospitaisFlow = stateHandle.getStateFlow(
                    "pesoHospitais",
                    entry.arguments?.getFloat("pesoHospitais") ?: 1f
                )
                val pesoCriminalidadeFlow = stateHandle.getStateFlow(
                    "pesoCriminalidade",
                    entry.arguments?.getFloat("pesoCriminalidade") ?: 1f
                )

                val rendaMin by rendaMinFlow.collectAsState()
                val rendaMax by rendaMaxFlow.collectAsState()
                val pesoRenda by pesoRendaFlow.collectAsState()
                val pesoEscolas by pesoEscolasFlow.collectAsState()
                val pesoHospitais by pesoHospitaisFlow.collectAsState()
                val pesoCriminalidade by pesoCriminalidadeFlow.collectAsState()

                val saveToHistory = entry.arguments?.getBoolean("saveToHistory") ?: true

                MapScreen(
                    padding = padding,
                    countryCode = countryCode,
                    countryName = countryName,
                    rendaMin = rendaMin,
                    rendaMax = rendaMax,
                    pesoRenda = pesoRenda,
                    pesoEscolas = pesoEscolas,
                    pesoHospitais = pesoHospitais,
                    pesoCriminalidade = pesoCriminalidade,
                    saveToHistory = saveToHistory,
                    onConfigureFiltersClick = {
                        navController.navigate(Routes.filters(countryCode, countryName))
                    },
                    onViewResultsClick = {
                        navController.navigate(Routes.RESULTS)
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
                val countryCode = Uri.decode(entry.arguments?.getString("countryCode") ?: "")
                val countryName = Uri.decode(entry.arguments?.getString("countryName") ?: "País")

                val previousStateHandle = navController.previousBackStackEntry?.savedStateHandle

                val rendaMin = previousStateHandle?.get<Float>("rendaMin") ?: 0f
                val rendaMax = previousStateHandle?.get<Float>("rendaMax") ?: 20f
                val pesoRenda = previousStateHandle?.get<Float>("pesoRenda") ?: 1f
                val pesoEscolas = previousStateHandle?.get<Float>("pesoEscolas") ?: 1f
                val pesoHospitais = previousStateHandle?.get<Float>("pesoHospitais") ?: 1f
                val pesoCriminalidade = previousStateHandle?.get<Float>("pesoCriminalidade") ?: 1f

                FiltersScreen(
                    padding = padding,
                    countryCode = countryCode,
                    countryName = countryName,
                    initialRendaMin = rendaMin,
                    initialRendaMax = rendaMax,
                    initialPesoRenda = pesoRenda,
                    initialPesoEscolas = pesoEscolas,
                    initialPesoHospitais = pesoHospitais,
                    initialPesoCriminalidade = pesoCriminalidade,
                    onSaveClick = { newRendaMin, newRendaMax, newPesoRenda, newPesoEscolas, newPesoHospitais, newPesoCriminalidade ->
                        navController.previousBackStackEntry?.savedStateHandle?.set("rendaMin", newRendaMin)
                        navController.previousBackStackEntry?.savedStateHandle?.set("rendaMax", newRendaMax)
                        navController.previousBackStackEntry?.savedStateHandle?.set("pesoRenda", newPesoRenda)
                        navController.previousBackStackEntry?.savedStateHandle?.set("pesoEscolas", newPesoEscolas)
                        navController.previousBackStackEntry?.savedStateHandle?.set("pesoHospitais", newPesoHospitais)
                        navController.previousBackStackEntry?.savedStateHandle?.set("pesoCriminalidade", newPesoCriminalidade)
                        navController.popBackStack()
                    }
                )
            }

            composable(Routes.LOGIN) {
                LoginScreen(
                    padding = padding,
                    isLoading = authViewModel.loginLoading,
                    errorMessage = authViewModel.loginError,
                    onForgotPasswordClick = { },
                    onCreateAccountClick = {
                        navController.navigate(Routes.NEW_ACCOUNT)
                    },
                    onLoginClick = { email, password ->
                        authViewModel.login(
                            email = email,
                            password = password,
                            onSuccess = {
                                Toast.makeText(
                                    context,
                                    "Login realizado com sucesso.",
                                    Toast.LENGTH_SHORT
                                ).show()

                                navController.navigate(Routes.HOME) {
                                    popUpTo(Routes.LOGIN) { inclusive = true }
                                }
                            }
                        )
                    }
                )
            }

            composable(Routes.NEW_ACCOUNT) {
                NewAccountScreen(
                    padding = padding,
                    isLoading = authViewModel.registerLoading,
                    errorMessage = authViewModel.registerError,
                    onBackToLoginClick = { navController.popBackStack() },
                    onCreateAccountClick = { email, password, confirmPassword ->
                        authViewModel.register(
                            email = email,
                            password = password,
                            confirmPassword = confirmPassword,
                            onSuccess = {
                                Toast.makeText(
                                    context,
                                    "Conta criada. Verifica o teu email antes de iniciar sessão.",
                                    Toast.LENGTH_LONG
                                ).show()

                                navController.popBackStack()
                            }
                        )
                    }
                )
            }

            composable(Routes.RESULTS) {
                ResultsScreen(
                    padding = padding,
                    onOpenMapClick = { data -> openLastSearchOnMap(data) }
                )
            }

            composable(Routes.HISTORY) {
                HistoryScreen(
                    padding = padding,
                    onLoginClick = { navController.navigate(Routes.LOGIN) },
                    onOpenSearch = { item -> openSearchFromHistory(item) }
                )
            }

            composable(Routes.FAVORITES) {
                FavoritesScreen(
                    padding = padding,
                    onLoginClick = { navController.navigate(Routes.LOGIN) },
                    onOpenSearch = { item -> openSearchFromHistory(item) }
                )
            }

            composable(Routes.PRICE_HISTORY) {
                PriceHistoryScreen(
                    padding = padding
                )
            }

            composable(Routes.AI_ASSISTANT) {
                AiAssistantScreen(
                    padding = padding
                )
            }

            composable(Routes.DEBUG_PAISES) {
                DebugPaisesScreen()
            }

            composable(Routes.LANGUAGE) {
                LanguageScreen(padding = padding)
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
