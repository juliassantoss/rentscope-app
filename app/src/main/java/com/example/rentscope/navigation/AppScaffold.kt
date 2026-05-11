package com.example.rentscope.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.PriceChange
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.compose.runtime.key
import com.example.rentscope.R
import com.example.rentscope.data.local.TokenManager
import com.example.rentscope.data.repository.AuthRepository
import com.example.rentscope.ui.components.IdleMascotPrompt
import kotlinx.coroutines.launch

private val BrandBlue = Color(0xFF2F86D6)
private val TopLevelRoutes = setOf(
    Routes.HOME,
    Routes.LANGUAGE,
    Routes.RESULTS,
    Routes.HISTORY,
    Routes.FAVORITES,
    Routes.COMPARISON,
    Routes.PRICE_HISTORY,
    Routes.AI_ASSISTANT
)
private val AuthRoutes = setOf(
    Routes.LOGIN,
    Routes.NEW_ACCOUNT,
    Routes.FORGOT_PASSWORD
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppScaffold(
    navController: NavController,
    currentDestination: NavDestination?,
    content: @Composable (PaddingValues) -> Unit
) {
    val context = LocalContext.current
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val isAuthRoute = currentDestination.isRouteIn(AuthRoutes)
    val canGoBack = !isAuthRoute &&
        !currentDestination.isRouteIn(TopLevelRoutes) &&
        navController.previousBackStackEntry != null

    var isLoggedIn by remember { mutableStateOf(false) }
    var userEmail by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(currentDestination) {
        TokenManager.init(context)
        isLoggedIn = TokenManager.isLoggedIn()

        if (isLoggedIn) {
            AuthRepository.me()
                .onSuccess { user ->
                    userEmail = user.email
                }
                .onFailure {
                    userEmail = null
                }
        } else {
            userEmail = null
        }
    }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        ModalNavigationDrawer(
            drawerState = drawerState,
            gesturesEnabled = false,
            drawerContent = {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                    AppDrawer(
                        isLoggedIn = isLoggedIn,
                        userEmail = userEmail,
                        onClose = { scope.launch { drawerState.close() } },
                        onAuthClick = {
                            scope.launch { drawerState.close() }

                            if (isLoggedIn) {
                                AuthRepository.logout()
                                isLoggedIn = false
                                userEmail = null
                                navController.navigate(Routes.LOGIN) {
                                    launchSingleTop = true
                                }
                            } else {
                                navController.navigate(Routes.LOGIN)
                            }
                        },
                        onItemClick = { route ->
                            scope.launch { drawerState.close() }
                            navController.navigateSingleTopTo(route)
                        }
                    )
                }
            }
        ) {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                Scaffold(
                    topBar = {
                        if (!isAuthRoute) {
                            TopAppBar(
                                title = { Text(stringResource(R.string.app_title)) },
                                navigationIcon = {
                                    // Lado esquerdo: apenas a seta de voltar quando aplicável.
                                    if (canGoBack) {
                                        IconButton(onClick = { navController.navigateUp() }) {
                                            Icon(
                                                Icons.AutoMirrored.Filled.ArrowBack,
                                                contentDescription = stringResource(R.string.back)
                                            )
                                        }
                                    }
                                },
                                actions = {
                                    // Lado direito (sempre): globo de idioma + menu hamburguer.
                                    IconButton(onClick = {
                                        navController.navigateSingleTopTo(Routes.LANGUAGE)
                                    }) {
                                        Icon(
                                            Icons.Filled.Language,
                                            contentDescription = stringResource(R.string.language)
                                        )
                                    }
                                    IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                        Icon(
                                            Icons.Filled.Menu,
                                            contentDescription = stringResource(R.string.menu)
                                        )
                                    }
                                },
                                colors = TopAppBarDefaults.topAppBarColors(
                                    containerColor = BrandBlue,
                                    titleContentColor = Color.White,
                                    navigationIconContentColor = Color.White,
                                    actionIconContentColor = Color.White
                                )
                            )
                        }
                    },
                    bottomBar = {
                        if (!isAuthRoute) {
                            val itemColors = NavigationBarItemDefaults.colors(
                                indicatorColor = BrandBlue.copy(alpha = 0.15f),
                                selectedIconColor = Color.Black,
                                selectedTextColor = Color.Black,
                                unselectedIconColor = Color.Black,
                                unselectedTextColor = Color.Black
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .navigationBarsPadding()
                                    .padding(horizontal = 16.dp, vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(24.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = Color.White.copy(alpha = 0.96f)
                                    ),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 4.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // Resultados (esquerda)
                                        NavigationBarItem(
                                            modifier = Modifier.weight(1f),
                                            selected = currentDestination.isRouteSelected(Routes.RESULTS),
                                            onClick = { navController.navigateSingleTopTo(Routes.RESULTS) },
                                            icon = {
                                                Icon(
                                                    Icons.Filled.Assessment,
                                                    contentDescription = stringResource(R.string.results),
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            },
                                            label = {
                                                Text(
                                                    text = stringResource(R.string.results),
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            },
                                            alwaysShowLabel = true,
                                            colors = itemColors
                                        )

                                        // Início (centro)
                                        NavigationBarItem(
                                            modifier = Modifier.weight(1f),
                                            selected = currentDestination.isRouteSelected(Routes.HOME),
                                            onClick = { navController.navigateSingleTopTo(Routes.HOME) },
                                            icon = {
                                                Icon(
                                                    Icons.Filled.Home,
                                                    contentDescription = stringResource(R.string.home),
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            },
                                            label = {
                                                Text(
                                                    text = stringResource(R.string.home),
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            },
                                            alwaysShowLabel = true,
                                            colors = itemColors
                                        )

                                        // Histórico de Preços (direita) — label encurtada para
                                        // caber numa linha no bottom bar; o ícone (gráfico) e
                                        // o subtítulo da própria screen reforçam o significado.
                                        NavigationBarItem(
                                            modifier = Modifier.weight(1f),
                                            selected = currentDestination.isRouteSelected(Routes.PRICE_HISTORY),
                                            onClick = { navController.navigateSingleTopTo(Routes.PRICE_HISTORY) },
                                            icon = {
                                                Icon(
                                                    Icons.Filled.PriceChange,
                                                    contentDescription = stringResource(R.string.price_history),
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            },
                                            label = {
                                                Text(
                                                    text = stringResource(R.string.price_history_short),
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            },
                                            alwaysShowLabel = true,
                                            colors = itemColors
                                        )
                                    }
                                }
                            }
                        }
                    },
                    containerColor = Color(0xFFEAF1F4)
                ) { padding ->
                    if (isAuthRoute) {
                        // Auth screens (login/registo/forgot) não devem ter o
                        // mascote a interromper o fluxo crítico de credenciais.
                        content(padding)
                    } else {
                        // O `key` garante que o estado de inatividade/dismissed
                        // é reposto cada vez que o utilizador muda de ecrã.
                        // Excluímos:
                        //   - HOME: já tem o mascote grande no centro
                        //   - AI_ASSISTANT: é a própria conversa com o mascote
                        //   - mapa: já tem o mascote integrado no canto
                        val routeKey = currentDestination?.route.orEmpty()
                        val showIdlePrompt = routeKey.isNotBlank() &&
                            routeKey != Routes.HOME &&
                            !routeKey.startsWith(Routes.AI_ASSISTANT) &&
                            !routeKey.startsWith("map/")

                        if (showIdlePrompt) {
                            key(routeKey) {
                                IdleMascotPrompt(
                                    onOpenAssistant = {
                                        navController.navigateSingleTopTo(Routes.AI_ASSISTANT)
                                    }
                                ) {
                                    content(padding)
                                }
                            }
                        } else {
                            content(padding)
                        }
                    }
                }
            }
        }
    }
}

private fun NavDestination?.isRouteSelected(route: String): Boolean {
    return this?.hierarchy?.any { destination ->
        destination.route?.startsWith(route) == true
    } == true
}

private fun NavDestination?.isRouteIn(routes: Set<String>): Boolean {
    return routes.any { route -> this.isRouteSelected(route) }
}

/**
 * Navega para uma rota top-level (Home, Resultados, Idioma, etc.) garantindo:
 *
 *  1. Pop up até HOME (a verdadeira raiz da app, não o startDestination do
 *     graph que pode ser LOGIN). Isto garante que clicar em "Início" sai
 *     sempre de qualquer ecrã onde esteja, inclusive Idioma.
 *  2. Quando o destino é HOME, faz pop inclusive (ou seja, descarta também
 *     a HOME atual) e re-cria; assim Home volta sempre ao estado limpo.
 *  3. Para os restantes destinos top-level, mantém HOME na stack para o
 *     botão de voltar do sistema funcionar de forma natural.
 */
private fun NavController.navigateSingleTopTo(route: String) {
    navigate(route) {
        popUpTo(Routes.HOME) {
            inclusive = (route == Routes.HOME)
            saveState = false
        }
        launchSingleTop = true
        restoreState = false
    }
}
