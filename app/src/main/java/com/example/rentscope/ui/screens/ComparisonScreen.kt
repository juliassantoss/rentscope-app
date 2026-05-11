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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.CompareArrows
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.rentscope.R
import com.example.rentscope.data.local.LastSearchManager
import com.example.rentscope.data.remote.dto.score.ScoreMunicipioDto
import com.example.rentscope.ui.components.SkeletonBlock
import com.example.rentscope.ui.viewmodel.ScoreViewModel
import java.util.Locale

/**
 * Tela de comparação multi-localidade.
 *
 * O utilizador pesquisa por nome do município, marca cada localidade
 * pretendida (sem limite teórico, embora o ideal seja 2 a 4) e a
 * comparação dos cartões aparece automaticamente abaixo, em coluna,
 * mantendo o mesmo formato visual que existia para a comparação 1-1.
 */
@Composable
fun ComparisonScreen(
    padding: PaddingValues,
    vm: ScoreViewModel = viewModel()
) {
    val lastSearch = LastSearchManager.get()

    val municipios = remember(vm.municipios) {
        vm.municipios.sortedWith(
            compareByDescending<ScoreMunicipioDto> { it.score }
                .thenBy { it.municipioLocalidade }
        )
    }

    var searchQuery by rememberSaveable { mutableStateOf("") }
    val selectedCodes = remember { mutableStateListOf<Int>() }

    LaunchedEffect(
        lastSearch?.countryCode,
        lastSearch?.rendaMin,
        lastSearch?.rendaMax,
        lastSearch?.pesoRenda,
        lastSearch?.pesoEscolas,
        lastSearch?.pesoHospitais,
        lastSearch?.pesoCriminalidade
    ) {
        if (lastSearch != null && lastSearch.countryCode == "PT") {
            vm.carregarScores(
                rendaMin = lastSearch.rendaMin,
                rendaMax = lastSearch.rendaMax,
                pesoRenda = lastSearch.pesoRenda,
                pesoEscolas = lastSearch.pesoEscolas,
                pesoHospitais = lastSearch.pesoHospitais,
                pesoCriminalidade = lastSearch.pesoCriminalidade,
                limite = 400
            )
        }
    }

    // Limpa seleções de municípios que já não estão nos dados (ex: nova busca filtra fora).
    LaunchedEffect(municipios) {
        val validCodes = municipios.map { it.codigoMunicipio }.toSet()
        selectedCodes.retainAll(validCodes)
    }

    val selectedItems by remember(municipios, selectedCodes.toList()) {
        derivedStateOf {
            municipios.filter { it.codigoMunicipio in selectedCodes }
        }
    }

    val filteredOptions = remember(municipios, searchQuery) {
        val q = searchQuery.trim()
        if (q.isBlank()) {
            municipios
        } else {
            municipios.filter {
                it.municipioLocalidade.contains(q, ignoreCase = true)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .verticalScroll(rememberScrollState())
            .navigationBarsPadding()
            .padding(16.dp)
    ) {
        ScreenHeader(
            title = stringResource(R.string.comparison_title),
            subtitle = stringResource(R.string.comparison_subtitle),
            icon = Icons.AutoMirrored.Filled.CompareArrows
        )

        Spacer(modifier = Modifier.height(16.dp))

        when {
            lastSearch == null -> {
                EmptyStateCard(
                    title = stringResource(R.string.comparison_no_search_title),
                    message = stringResource(R.string.comparison_no_search_message)
                )
            }

            lastSearch.countryCode != "PT" -> {
                EmptyStateCard(
                    title = stringResource(R.string.no_data_title),
                    message = stringResource(R.string.comparison_portugal_only)
                )
            }

            vm.isLoading -> {
                LoadingStateCard(
                    title = stringResource(R.string.comparison_loading_title),
                    message = stringResource(R.string.comparison_loading_message)
                )
                Spacer(modifier = Modifier.height(12.dp))
                ComparisonLoadingContent()
            }

            !vm.errorMessage.isNullOrBlank() -> {
                EmptyStateCard(
                    title = stringResource(R.string.error_loading_results_title),
                    message = vm.errorMessage ?: stringResource(R.string.unexpected_error)
                )
            }

            municipios.size < 2 -> {
                EmptyStateCard(
                    title = stringResource(R.string.no_data_title),
                    message = stringResource(R.string.comparison_not_enough_message)
                )
            }

            else -> {
                LocalitySelectorCard(
                    searchQuery = searchQuery,
                    onSearchQueryChange = { searchQuery = it },
                    options = filteredOptions,
                    selectedCodes = selectedCodes,
                    onToggleSelect = { code ->
                        if (code in selectedCodes) selectedCodes.remove(code)
                        else selectedCodes.add(code)
                    },
                    onClearSelection = { selectedCodes.clear() },
                    selectedCount = selectedCodes.size
                )

                Spacer(modifier = Modifier.height(14.dp))

                if (selectedItems.size < 2) {
                    EmptyStateCard(
                        title = stringResource(R.string.no_data_title),
                        message = stringResource(R.string.comparison_min_two_required)
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        selectedItems.forEach { item ->
                            ComparisonLocalityCard(
                                title = item.municipioLocalidade,
                                item = item,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
private fun LocalitySelectorCard(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    options: List<ScoreMunicipioDto>,
    selectedCodes: List<Int>,
    onToggleSelect: (Int) -> Unit,
    onClearSelection: () -> Unit,
    selectedCount: Int
) {
    SectionCard {
        Text(
            text = stringResource(R.string.comparison_pick_localities),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = stringResource(R.string.comparison_pick_helper),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(stringResource(R.string.comparison_search_placeholder)) },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.comparison_selected_count, selectedCount),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (selectedCount > 0) {
                TextButton(onClick = onClearSelection) {
                    Text(stringResource(R.string.comparison_clear_selection))
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Lista limitada em altura para não dominar o ecrã. O utilizador
        // pode scrollar dentro deste bloco enquanto a comparação fica visível
        // imediatamente abaixo.
        if (options.isEmpty()) {
            Text(
                text = stringResource(R.string.comparison_no_matches),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 12.dp)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 280.dp)
            ) {
                items(options, key = { it.codigoMunicipio }) { municipio ->
                    LocalityRow(
                        item = municipio,
                        checked = municipio.codigoMunicipio in selectedCodes,
                        onToggle = { onToggleSelect(municipio.codigoMunicipio) }
                    )
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                    )
                }
            }
        }
    }
}

@Composable
private fun LocalityRow(
    item: ScoreMunicipioDto,
    checked: Boolean,
    onToggle: () -> Unit
) {
    Surface(
        onClick = onToggle,
        color = Color.Transparent,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = checked,
                onCheckedChange = { onToggle() }
            )
            Spacer(modifier = Modifier.size(4.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.municipioLocalidade,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = buildAreaSummary(item),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Box(modifier = Modifier.padding(end = 4.dp)) {
                Text(
                    text = format1(item.score),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun ComparisonLoadingContent() {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        repeat(2) {
            SectionCard {
                SkeletonBlock(
                    modifier = Modifier
                        .fillMaxWidth(0.42f)
                        .height(14.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                SkeletonBlock(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                SkeletonBlock(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                )
            }
        }
    }
}

@Composable
private fun ComparisonLocalityCard(
    title: String,
    item: ScoreMunicipioDto,
    modifier: Modifier = Modifier
) {
    SectionCard(modifier = modifier, contentPadding = PaddingValues(16.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = buildAreaSummary(item),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(12.dp))

        MetricChip(
            label = stringResource(R.string.comparison_score_title),
            value = format1(item.score),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        MetricChip(
            label = stringResource(R.string.rent_label_short),
            value = formatPricePerSquareMeter(item.valorMedioM2),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        MetricChip(
            label = stringResource(R.string.schools_label_short),
            value = item.totalEscolas.toString(),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        MetricChip(
            label = stringResource(R.string.hospitals_label_short),
            value = item.totalHospitais.toString(),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        MetricChip(
            label = stringResource(R.string.crime_label_short),
            value = item.totalCrimes.toString(),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

private fun buildAreaSummary(item: ScoreMunicipioDto): String {
    val parts = listOfNotNull(
        item.regiao?.takeIf { it.isNotBlank() },
        item.grandeRegiao?.takeIf { it.isNotBlank() }
    )
    return if (parts.isEmpty()) "—" else parts.joinToString(" / ")
}

private fun format1(value: Float): String = String.format(Locale.getDefault(), "%.1f", value)

private fun formatPricePerSquareMeter(value: Float?): String {
    return value?.let { String.format(Locale.getDefault(), "%.1f €/m²", it) } ?: "—"
}
