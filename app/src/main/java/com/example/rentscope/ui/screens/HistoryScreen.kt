package com.example.rentscope.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
fun HistoryScreen(
    padding: PaddingValues,
    onLoginClick: () -> Unit,
    onOpenSearch: (FiltroSalvoDto) -> Unit,
    vm: HistoryViewModel = viewModel()
) {
    val isLoggedIn = AuthRepository.isLoggedIn()

    if (!isLoggedIn) {
        LoginRequiredScreen(
            padding = padding,
            title = stringResource(R.string.search_history),
            message = stringResource(R.string.login_required_history),
            onLoginClick = onLoginClick
        )
        return
    }

    LaunchedEffect(Unit) {
        vm.carregarHistorico()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(16.dp)
    ) {
        ScreenHeader(
            title = stringResource(R.string.search_history),
            subtitle = stringResource(R.string.history_subtitle),
            icon = Icons.Filled.AccessTime
        )

        Spacer(Modifier.height(16.dp))

        if (vm.historico.isEmpty()) {
            EmptyStateCard(
                title = stringResource(R.string.no_saved_searches_title),
                message = stringResource(R.string.no_saved_searches_message)
            )
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                items(vm.historico, key = { it.id }) { item ->
                    HistoryItemCard(
                        item = item,
                        onOpen = { onOpenSearch(item) },
                        onToggleFavorite = {
                            if (item.favorito) {
                                vm.removerFavorito(item.id)
                            } else {
                                vm.adicionarFavorito(item.id)
                            }
                        },
                        onDelete = { vm.removerFiltro(item.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun HistoryItemCard(
    item: FiltroSalvoDto,
    onOpen: () -> Unit,
    onToggleFavorite: () -> Unit,
    onDelete: () -> Unit
) {
    SavedSearchCard(
        item = item,
        trailing = {
            Row {
                IconButton(onClick = onToggleFavorite) {
                    Icon(
                        imageVector = if (item.favorito) Icons.Filled.Star else Icons.Outlined.StarBorder,
                        contentDescription = stringResource(R.string.favorite_action)
                    )
                }

                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Filled.DeleteOutline,
                        contentDescription = stringResource(R.string.remove_action)
                    )
                }
            }
        },
        primaryAction = onOpen,
        primaryText = stringResource(R.string.open_again)
    )
}
