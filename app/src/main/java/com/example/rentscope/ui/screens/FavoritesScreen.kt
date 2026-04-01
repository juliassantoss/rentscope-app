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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
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
            title = "Favoritos",
            message = "Precisa fazer login para guardar e visualizar favoritos.",
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
        Text(
            text = "Favoritos",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = "As suas pesquisas favoritas aparecem aqui.",
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(16.dp))

        if (vm.favoritos.isEmpty()) {
            EmptyStateCard(
                title = "Ainda não há favoritos",
                message = "Marque uma pesquisa como favorita no histórico para ela aparecer aqui."
            )
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
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