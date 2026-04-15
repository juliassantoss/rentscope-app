package com.example.rentscope.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Menu
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
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import com.example.rentscope.R
import com.example.rentscope.data.local.TokenManager
import com.example.rentscope.data.repository.AuthRepository
import kotlinx.coroutines.launch

private val BrandBlue = Color(0xFF2F86D6)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppScaffold(
    navController: NavController,
    currentDestination: NavDestination?,
    content: @Composable (PaddingValues) -> Unit
) {
    val context = LocalContext.current
    val canGoBack = navController.previousBackStackEntry != null
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

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
                        TopAppBar(
                            title = { Text(stringResource(R.string.app_title)) },
                            navigationIcon = {
                                if (canGoBack) {
                                    IconButton(onClick = { navController.popBackStack() }) {
                                        Icon(
                                            Icons.AutoMirrored.Filled.ArrowBack,
                                            contentDescription = stringResource(R.string.back)
                                        )
                                    }
                                }
                            },
                            actions = {
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
                    },
                    bottomBar = {
                        Box(
                            modifier = Modifier
                                .navigationBarsPadding()
                                .padding(horizontal = 16.dp, vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Card(
                                shape = RoundedCornerShape(24.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = Color.White.copy(alpha = 0.96f)
                                ),
                                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                                ) {
                                    NavigationBarItem(
                                        selected = currentDestination.isRouteSelected(Routes.LANGUAGE),
                                        onClick = { navController.navigateSingleTopTo(Routes.LANGUAGE) },
                                        icon = {
                                            Icon(
                                                Icons.Filled.Language,
                                                contentDescription = stringResource(R.string.language),
                                                modifier = Modifier.size(20.dp)
                                            )
                                        },
                                        label = { Text(stringResource(R.string.language)) },
                                        colors = NavigationBarItemDefaults.colors(
                                            indicatorColor = BrandBlue.copy(alpha = 0.15f),
                                            selectedIconColor = Color.Black,
                                            selectedTextColor = Color.Black,
                                            unselectedIconColor = Color.Black,
                                            unselectedTextColor = Color.Black
                                        )
                                    )

                                    NavigationBarItem(
                                        selected = currentDestination.isRouteSelected(Routes.HOME),
                                        onClick = { navController.navigateSingleTopTo(Routes.HOME) },
                                        icon = {
                                            Icon(
                                                Icons.Filled.Home,
                                                contentDescription = stringResource(R.string.home),
                                                modifier = Modifier.size(20.dp)
                                            )
                                        },
                                        label = { Text(stringResource(R.string.home)) },
                                        colors = NavigationBarItemDefaults.colors(
                                            indicatorColor = BrandBlue.copy(alpha = 0.15f),
                                            selectedIconColor = Color.Black,
                                            selectedTextColor = Color.Black,
                                            unselectedIconColor = Color.Black,
                                            unselectedTextColor = Color.Black
                                        )
                                    )

                                    NavigationBarItem(
                                        selected = currentDestination.isRouteSelected(Routes.RESULTS),
                                        onClick = { navController.navigateSingleTopTo(Routes.RESULTS) },
                                        icon = {
                                            Icon(
                                                Icons.AutoMirrored.Filled.ShowChart,
                                                contentDescription = stringResource(R.string.results),
                                                modifier = Modifier.size(20.dp)
                                            )
                                        },
                                        label = { Text(stringResource(R.string.results)) },
                                        colors = NavigationBarItemDefaults.colors(
                                            indicatorColor = BrandBlue.copy(alpha = 0.15f),
                                            selectedIconColor = Color.Black,
                                            selectedTextColor = Color.Black,
                                            unselectedIconColor = Color.Black,
                                            unselectedTextColor = Color.Black
                                        )
                                    )
                                }
                            }
                        }
                    },
                    containerColor = Color(0xFFEAF1F4)
                ) { padding ->
                    content(padding)
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

private fun NavController.navigateSingleTopTo(route: String) {
    navigate(route) {
        launchSingleTop = true
        restoreState = true
        popUpTo(graph.startDestinationId) { saveState = true }
    }
}
