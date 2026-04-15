package com.example.rentscope.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.rentscope.R
import com.example.rentscope.data.remote.dto.history.FiltroSalvoDto
import com.example.rentscope.data.repository.AuthRepository
import com.example.rentscope.ui.viewmodel.HistoryViewModel

@Composable
fun FavoritesScreen(
    padding: PaddingValues,
    onLoginClick: () -> Unit,
    onOpenSearch: (FiltroSalvoDto) -> Unit,
    vm: HistoryViewModel = viewModel()
) {
    val isLoggedIn = AuthRepository.isLoggedIn()

    if (!isLoggedIn) {
        LoginRequiredScreen(
            padding = padding,
            title = stringResource(R.string.favorites),
            message = stringResource(R.string.login_required_favorites),
            onLoginClick = onLoginClick
        )
        return
    }

    LaunchedEffect(Unit) {
        vm.carregarFavoritos()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(16.dp)
    ) {
        ScreenHeader(
            title = stringResource(R.string.favorites),
            subtitle = stringResource(R.string.favorites_subtitle),
            icon = Icons.Filled.Star
        )

        Spacer(Modifier.height(16.dp))

        if (vm.favoritos.isEmpty()) {
            EmptyStateCard(
                title = stringResource(R.string.no_favorites_title),
                message = stringResource(R.string.no_favorites_message)
            )
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                items(vm.favoritos, key = { it.id }) { item ->
                    FavoriteItemCardRemote(
                        item = item,
                        onOpen = { onOpenSearch(item) },
                        onRemoveFavorite = { vm.removerFavorito(item.id) }
                    )
                }
            }
        }
    }
}
