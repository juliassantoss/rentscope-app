package com.example.rentscope.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.rentscope.R
import com.example.rentscope.data.remote.dto.history.FavoritoMunicipioDto
import com.example.rentscope.data.repository.AuthRepository
import com.example.rentscope.ui.viewmodel.HistoryViewModel

private val FavoriteYellow = Color(0xFFFFB300)
private val ResultsBlue = Color(0xFF00708E)

@Composable
fun FavoritesScreen(
    padding: PaddingValues,
    onLoginClick: () -> Unit,
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
                items(vm.favoritos, key = { it.codigo_municipio }) { item ->
                    FavoriteMunicipioCard(
                        item = item,
                        onRemove = { vm.removerFavorito(item.codigo_municipio) }
                    )
                }
            }
        }
    }
}

@Composable
private fun FavoriteMunicipioCard(
    item: FavoritoMunicipioDto,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = ResultsBlue.copy(alpha = 0.12f),
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Filled.Place,
                        contentDescription = null,
                        tint = ResultsBlue
                    )
                }
            }

            Spacer(Modifier.size(12.dp))

            Column(Modifier.weight(1f)) {
                Text(
                    text = item.municipio_localidade,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                val area = listOfNotNull(
                    item.regiao?.takeIf { it.isNotBlank() },
                    item.grande_regiao?.takeIf { it.isNotBlank() }
                ).joinToString(" • ")

                if (area.isNotBlank()) {
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = area,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            IconButton(onClick = onRemove) {
                Icon(
                    imageVector = Icons.Filled.Star,
                    contentDescription = stringResource(R.string.remove_from_favorites),
                    tint = FavoriteYellow
                )
            }
        }
    }
}
