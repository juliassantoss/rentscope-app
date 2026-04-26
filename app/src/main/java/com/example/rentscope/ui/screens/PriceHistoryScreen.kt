package com.example.rentscope.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.rentscope.R
import com.example.rentscope.data.remote.dto.MunicipioDto
import com.example.rentscope.ui.components.PriceHistoryChart
import com.example.rentscope.ui.viewmodel.MunicipioViewModel
import com.example.rentscope.ui.viewmodel.PriceHistoryViewModel
import java.util.Locale
import kotlin.math.absoluteValue

/**
 * Renders the price history experience for a selected municipality.
 *
 * @param padding Insets provided by the app scaffold.
 * @param vm View model that loads time-series price data.
 * @param municipioVm View model that exposes available municipalities.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PriceHistoryScreen(
    padding: PaddingValues,
    vm: PriceHistoryViewModel = viewModel(),
    municipioVm: MunicipioViewModel = viewModel()
) {
    var expanded by remember { mutableStateOf(false) }
    var selectedMunicipioCode by rememberSaveable { mutableStateOf<Int?>(null) }
    var searchText by rememberSaveable { mutableStateOf("") }

    LaunchedEffect(Unit) {
        municipioVm.load()
    }

    val filteredMunicipios = remember(municipioVm.municipios, searchText) {
        if (searchText.isBlank()) {
            municipioVm.municipios
        } else {
            municipioVm.municipios.filter {
                it.municipio_localidade.contains(searchText, ignoreCase = true)
            }
        }.take(40)
    }

    val selectedMunicipio = remember(municipioVm.municipios, selectedMunicipioCode) {
        municipioVm.municipios.firstOrNull { it.codigo_municipio == selectedMunicipioCode }
    }

    LaunchedEffect(selectedMunicipioCode) {
        selectedMunicipioCode?.let(vm::load)
    }

    val sortedData = remember(vm.data) {
        vm.data.sortedBy { parseQuarterIndex(it.trimestre) }
    }

    val chartData = remember(sortedData) {
        sortedData.map { it.trimestre to it.valor_medio_m2 }
    }

    val latestValue = sortedData.lastOrNull()?.valor_medio_m2
    val firstValue = sortedData.firstOrNull()?.valor_medio_m2
    val delta = if (latestValue != null && firstValue != null) latestValue - firstValue else null

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .verticalScroll(rememberScrollState())
            .navigationBarsPadding()
            .padding(16.dp)
    ) {
        ScreenHeader(
            title = stringResource(R.string.price_history),
            subtitle = stringResource(R.string.price_history_subtitle),
            icon = Icons.AutoMirrored.Filled.ShowChart
        )

        Spacer(modifier = Modifier.height(16.dp))

        InfoBannerCard(
            title = stringResource(R.string.price_history_intro_title),
            message = stringResource(R.string.price_history_intro_message),
            icon = Icons.AutoMirrored.Filled.ShowChart
        )

        Spacer(modifier = Modifier.height(14.dp))

        SectionCard {
            Text(
                text = stringResource(R.string.search_municipality_label),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(10.dp))

            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = it }
            ) {
                OutlinedTextField(
                    value = searchText,
                    onValueChange = {
                        searchText = it
                        expanded = true
                    },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth(),
                    label = { Text(stringResource(R.string.price_history_search_label)) },
                    placeholder = { Text(stringResource(R.string.price_history_search_placeholder)) },
                    singleLine = true,
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                    }
                )

                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    if (filteredMunicipios.isEmpty()) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.price_history_empty_search)) },
                            onClick = { expanded = false }
                        )
                    } else {
                        filteredMunicipios.forEach { municipio ->
                            DropdownMenuItem(
                                text = { Text(municipio.municipio_localidade) },
                                onClick = {
                                    selectedMunicipioCode = municipio.codigo_municipio
                                    searchText = municipio.municipio_localidade
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        when {
            municipioVm.loading -> {
                LoadingStateCard(
                    title = stringResource(R.string.loading_results_title),
                    message = stringResource(R.string.price_history_loading_municipios)
                )
            }

            vm.loading -> {
                LoadingStateCard(
                    title = stringResource(R.string.loading_results_title),
                    message = stringResource(R.string.price_history_loading_data)
                )
            }

            vm.error != null -> {
                EmptyStateCard(
                    title = stringResource(R.string.price_history_error_title),
                    message = vm.error ?: stringResource(R.string.unexpected_error)
                )
            }

            selectedMunicipio == null -> {
                EmptyStateCard(
                    title = stringResource(R.string.price_history_select_title),
                    message = stringResource(R.string.price_history_select_message)
                )
            }

            vm.data.isEmpty() -> {
                EmptyStateCard(
                    title = stringResource(R.string.price_history_no_data_title),
                    message = stringResource(
                        R.string.price_history_no_data_message,
                        selectedMunicipio?.municipio_localidade ?: stringResource(R.string.search_municipality_label)
                    )
                )
            }

            else -> {
                SectionCard {
                    Text(
                        text = selectedMunicipio?.municipio_localidade.orEmpty(),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = stringResource(R.string.price_history_chart_subtitle),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    StatsGrid(
                        latestValue = latestValue,
                        delta = delta,
                        periods = sortedData.size
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    PriceHistoryChart(data = chartData)
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
private fun StatsGrid(
    latestValue: Float?,
    delta: Float?,
    periods: Int
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            StatTile(
                title = stringResource(R.string.price_history_latest_value),
                value = latestValue?.let { formatEuroPerSquareMeter(it) } ?: "-",
                modifier = Modifier.weight(1f)
            )

            StatTile(
                title = stringResource(R.string.price_history_period_variation),
                value = delta?.let { formatVariation(it) } ?: "-",
                modifier = Modifier.weight(1f)
            )
        }

        StatTile(
            title = stringResource(R.string.price_history_records_count),
            value = periods.toString(),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun StatTile(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    MetricChip(
        label = title,
        value = value,
        modifier = modifier
    )
}

private fun formatEuroPerSquareMeter(value: Float): String {
    return String.format(Locale.getDefault(), "%.2f €/m²", value)
}

private fun formatVariation(value: Float): String {
    val signal = if (value > 0f) "+" else if (value < 0f) "-" else ""
    return "$signal${String.format(Locale.getDefault(), "%.2f €/m²", value.absoluteValue)}"
}

private fun parseQuarterIndex(value: String): Int {
    val quarterMatch = Regex("""(\d)""").find(value)?.groupValues?.getOrNull(1)?.toIntOrNull() ?: 0
    val yearMatch = Regex("""(20\d{2})""").find(value)?.groupValues?.getOrNull(1)?.toIntOrNull() ?: 0
    return (yearMatch * 10) + quarterMatch
}
