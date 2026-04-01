package com.example.rentscope.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
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
            title = "Histórico de pesquisas",
            message = "Precisa fazer login para aceder ao histórico das suas pesquisas.",
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
        Text(
            text = "Histórico de Pesquisas",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = "As suas pesquisas guardadas aparecem aqui.",
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(16.dp))

        if (vm.historico.isEmpty()) {
            EmptyStateCard(
                title = "Ainda não há pesquisas guardadas",
                message = "Depois de aplicar filtros no mapa com sessão iniciada, as buscas aparecem aqui."
            )
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
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
                        onDelete = {
                            vm.removerFiltro(item.id)
                        }
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
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.country_name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Código: ${item.country_code}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = item.criado_em,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row {
                    IconButton(onClick = onToggleFavorite) {
                        Icon(
                            imageVector = if (item.favorito) Icons.Filled.Star else Icons.Outlined.StarBorder,
                            contentDescription = "Favoritar"
                        )
                    }

                    IconButton(onClick = onDelete) {
                        Icon(
                            imageVector = Icons.Filled.DeleteOutline,
                            contentDescription = "Remover"
                        )
                    }
                }
            }

            Spacer(Modifier.height(10.dp))

            Text(
                text = "Renda: ${item.renda_min ?: "-"} até ${item.renda_max ?: "-"} €/m²",
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(Modifier.height(4.dp))

            Text(
                text = "Pesos — Renda: ${item.peso_renda} | Escolas: ${item.peso_escolas} | Hospitais: ${item.peso_hospitais} | Criminalidade: ${item.peso_criminalidade}",
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(Modifier.height(14.dp))

            Button(
                onClick = onOpen,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Ver novamente")
            }
        }
    }
}