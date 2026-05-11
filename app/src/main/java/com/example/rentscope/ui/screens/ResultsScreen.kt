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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import com.example.rentscope.data.local.LastSearchData
import com.example.rentscope.data.local.LastSearchManager
import com.example.rentscope.data.remote.dto.score.ScoreMunicipioDto
import com.example.rentscope.data.repository.AuthRepository
import com.example.rentscope.ui.components.SkeletonBlock
import com.example.rentscope.ui.viewmodel.HistoryViewModel
import com.example.rentscope.ui.viewmodel.ScoreViewModel
import java.util.Locale

private val ResultsBlue = Color(0xFF00708E)
private val FavoriteYellow = Color(0xFFFFB300)

@Composable
fun ResultsScreen(
    padding: PaddingValues,
    onOpenMapClick: (LastSearchData) -> Unit,
    onLoginClick: () -> Unit,
    historyVm: HistoryViewModel = viewModel(),
    vm: ScoreViewModel = viewModel()
) {
    val lastSearch = LastSearchManager.get()
    val isLoggedIn = AuthRepository.isLoggedIn()

    LaunchedEffect(isLoggedIn) {
        if (isLoggedIn) {
            historyVm.carregarFavoritos()
        }
    }

    LaunchedEffect(Unit) {
        val currentLastSearch = LastSearchManager.get()

        if (currentLastSearch != null && currentLastSearch.countryCode == "PT") {
            vm.carregarScores(
                rendaMin = currentLastSearch.rendaMin,
                rendaMax = currentLastSearch.rendaMax,
                pesoRenda = currentLastSearch.pesoRenda,
                pesoEscolas = currentLastSearch.pesoEscolas,
                pesoHospitais = currentLastSearch.pesoHospitais,
                pesoCriminalidade = currentLastSearch.pesoCriminalidade,
                limite = 10
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(16.dp)
    ) {
        ScreenHeader(
            title = stringResource(R.string.results),
            subtitle = stringResource(R.string.results_subtitle),
            icon = Icons.AutoMirrored.Filled.ShowChart
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Botão proeminente para voltar ao mapa da última pesquisa.
        // Aparece em destaque no topo, logo a seguir ao header, antes da
        // lista de resultados — assim o utilizador identifica rapidamente
        // o caminho de regresso à pesquisa quando precisa de ajustar filtros.
        if (lastSearch != null) {
            Button(
                onClick = { onOpenMapClick(lastSearch) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ResultsBlue)
            ) {
                Icon(
                    imageVector = Icons.Filled.Map,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.size(8.dp))
                Text(
                    text = stringResource(R.string.back_to_search),
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(14.dp))
        }

        if (lastSearch == null) {
            EmptyStateCard(
                title = stringResource(R.string.no_results_title),
                message = stringResource(R.string.no_results_message)
            )
        } else {
            when {
                lastSearch.countryCode != "PT" -> {
                    EmptyStateCard(
                        title = stringResource(R.string.no_data_title),
                        message = stringResource(R.string.results_portugal_only)
                    )
                }

                vm.isLoading -> {
                    LoadingStateCard(
                        title = stringResource(R.string.loading_results_title),
                        message = stringResource(R.string.loading_results_message)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    ResultsLoadingContent()
                }

                !vm.errorMessage.isNullOrBlank() -> {
                    EmptyStateCard(
                        title = stringResource(R.string.error_loading_results_title),
                        message = vm.errorMessage ?: stringResource(R.string.unexpected_error)
                    )
                }

                vm.municipios.isEmpty() -> {
                    EmptyStateCard(
                        title = stringResource(R.string.no_data_title),
                        message = stringResource(R.string.no_data_message)
                    )
                }

                else -> {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(bottom = 24.dp)
                    ) {
                        items(
                            items = vm.municipios.take(10),
                            key = { it.codigoMunicipio }
                        ) { item ->
                            val isFavorite = historyVm.isMunicipioFavorito(item.codigoMunicipio)
                            ResultCityCard(
                                item = item,
                                isLoggedIn = isLoggedIn,
                                isFavorite = isFavorite,
                                onFavoriteClick = {
                                    when {
                                        !isLoggedIn -> onLoginClick()
                                        isFavorite -> historyVm.removerFavorito(item.codigoMunicipio)
                                        else -> historyVm.adicionarFavorito(item.codigoMunicipio)
                                    }
                                }
                            )
                        }

                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                            InfoFootnote(
                                text = stringResource(R.string.rent_data_coverage_note)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ResultsLoadingContent() {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        repeat(2) {
            SectionCard {
                SkeletonBlock(
                    modifier = Modifier
                        .fillMaxWidth(0.38f)
                        .height(16.dp)
                )

                Spacer(modifier = Modifier.height(14.dp))

                SkeletonBlock(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SkeletonBlock(
                        modifier = Modifier
                            .weight(1f)
                            .height(54.dp)
                    )
                    SkeletonBlock(
                        modifier = Modifier
                            .weight(1f)
                            .height(54.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ResultCityCard(
    item: ScoreMunicipioDto,
    isLoggedIn: Boolean,
    isFavorite: Boolean,
    onFavoriteClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                Surface(
                    modifier = Modifier.padding(top = 2.dp),
                    shape = CircleShape,
                    color = ResultsBlue.copy(alpha = 0.12f)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Place,
                        contentDescription = null,
                        tint = ResultsBlue,
                        modifier = Modifier.padding(9.dp)
                    )
                }

                Spacer(modifier = Modifier.padding(6.dp))

                Column(Modifier.weight(1f)) {
                    Text(
                        text = item.municipioLocalidade,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    val area = buildAreaLabel(item.regiao, item.grandeRegiao)
                    if (area.isNotBlank()) {
                        Text(
                            text = area,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = ResultsBlue.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = format1(item.score),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        color = ResultsBlue,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.size(6.dp))

                FavoriteCardButton(
                    isLoggedIn = isLoggedIn,
                    isFavorite = isFavorite,
                    onClick = onFavoriteClick
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MetricChip(
                    label = stringResource(R.string.rent_label_short),
                    value = formatRentEuroPerSquareMeter(item.valorMedioM2),
                    modifier = Modifier.weight(1f)
                )
                MetricChip(
                    label = stringResource(R.string.schools_label_short),
                    value = item.totalEscolas.toString(),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MetricChip(
                    label = stringResource(R.string.hospitals_label_short),
                    value = item.totalHospitais.toString(),
                    modifier = Modifier.weight(1f)
                )
                MetricChip(
                    label = stringResource(R.string.crime_label_short),
                    value = item.totalCrimes.toString(),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun FavoriteCardButton(
    isLoggedIn: Boolean,
    isFavorite: Boolean,
    onClick: () -> Unit
) {
    val tint = when {
        isFavorite -> FavoriteYellow
        isLoggedIn -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    val description = when {
        !isLoggedIn -> stringResource(R.string.login_to_favorite)
        isFavorite -> stringResource(R.string.remove_from_favorites)
        else -> stringResource(R.string.add_to_favorites)
    }

    IconButton(
        onClick = onClick,
        modifier = Modifier.size(36.dp)
    ) {
        Icon(
            imageVector = if (isFavorite) Icons.Filled.Star else Icons.Outlined.StarBorder,
            contentDescription = description,
            tint = tint
        )
    }
}

private fun format1(value: Float): String = String.format(Locale.getDefault(), "%.1f", value)

/** Formata o valor médio por m² com unidade. Devolve "—" quando nulo/inválido. */
private fun formatRentEuroPerSquareMeter(value: Float?): String {
    if (value == null || !value.isFinite()) return "—"
    return String.format(Locale.getDefault(), "%.1f €/m²", value)
}

/**
 * Junta região e grande região para exibição, ignorando valores nulos/em branco.
 * Evita que apareça "null" ou "null • Centro" quando os dados estão incompletos.
 */
private fun buildAreaLabel(regiao: String?, grandeRegiao: String?): String {
    return listOfNotNull(
        regiao?.takeIf { it.isNotBlank() },
        grandeRegiao?.takeIf { it.isNotBlank() }
    ).joinToString(" • ")
}
