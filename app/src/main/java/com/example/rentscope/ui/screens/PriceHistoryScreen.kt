package com.example.rentscope.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.rentscope.data.remote.dto.MunicipioDto
import com.example.rentscope.ui.components.PriceHistoryChart
import com.example.rentscope.ui.viewmodel.MunicipioViewModel
import com.example.rentscope.ui.viewmodel.PriceHistoryViewModel
import java.util.Locale
import kotlin.math.absoluteValue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PriceHistoryScreen(
    padding: PaddingValues,
    vm: PriceHistoryViewModel = viewModel(),
    municipioVm: MunicipioViewModel = viewModel()
) {
    var expanded by remember { mutableStateOf(false) }
    var selectedMunicipio by rememberSaveable { mutableStateOf<MunicipioDto?>(null) }
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
        Text(
            text = "Histórico de Preços",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Pesquise e selecione um município para visualizar a evolução do preço médio por metro quadrado ao longo do tempo.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Município",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
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
                        label = { Text("Pesquisar município") },
                        placeholder = { Text("Ex.: Braga, Porto, Aveiro...") },
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
                                text = { Text("Nenhum município encontrado") },
                                onClick = { expanded = false }
                            )
                        } else {
                            filteredMunicipios.forEach { municipio ->
                                DropdownMenuItem(
                                    text = { Text(municipio.municipio_localidade) },
                                    onClick = {
                                        selectedMunicipio = municipio
                                        searchText = municipio.municipio_localidade
                                        expanded = false
                                        vm.load(municipio.codigo_municipio)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        when {
            municipioVm.loading -> {
                LoadingCard(message = "A carregar municípios...")
            }

            vm.loading -> {
                LoadingCard(message = "A carregar histórico de preços...")
            }

            vm.error != null -> {
                MessageCard(
                    title = "Erro ao carregar histórico",
                    message = vm.error ?: "Ocorreu um erro inesperado."
                )
            }

            selectedMunicipio == null -> {
                MessageCard(
                    title = "Selecione um município",
                    message = "Use a caixa de pesquisa acima para encontrar rapidamente o município desejado."
                )
            }

            vm.data.isEmpty() -> {
                MessageCard(
                    title = "Sem dados disponíveis",
                    message = "Não foram encontrados registos de histórico para ${selectedMunicipio?.municipio_localidade ?: "o município selecionado"}."
                )
            }

            else -> {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = selectedMunicipio?.municipio_localidade ?: "",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "Evolução do preço médio por m²",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        StatsRow(
                            latestValue = latestValue,
                            delta = delta,
                            periods = sortedData.size
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        PriceHistoryChart(data = chartData)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
private fun LoadingCard(message: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            CircularProgressIndicator()
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun MessageCard(
    title: String,
    message: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun StatsRow(
    latestValue: Float?,
    delta: Float?,
    periods: Int
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        StatItem(
            title = "Valor mais recente",
            value = latestValue?.let { formatEuroPerSquareMeter(it) } ?: "-"
        )

        StatItem(
            title = "Variação no período",
            value = delta?.let { formatVariation(it) } ?: "-"
        )

        StatItem(
            title = "Número de registos",
            value = periods.toString()
        )
    }
}

@Composable
private fun StatItem(
    title: String,
    value: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
        ),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

private fun formatEuroPerSquareMeter(value: Float): String {
    return String.format(Locale("pt", "PT"), "%.2f €/m²", value)
}

private fun formatVariation(value: Float): String {
    val signal = if (value > 0f) "+" else if (value < 0f) "-" else ""
    return "$signal${String.format(Locale("pt", "PT"), "%.2f €/m²", value.absoluteValue)}"
}

private fun parseQuarterIndex(value: String): Int {
    val quarterMatch = Regex("""(\d)""").find(value)?.groupValues?.getOrNull(1)?.toIntOrNull() ?: 0
    val yearMatch = Regex("""(20\d{2})""").find(value)?.groupValues?.getOrNull(1)?.toIntOrNull() ?: 0
    return (yearMatch * 10) + quarterMatch
}