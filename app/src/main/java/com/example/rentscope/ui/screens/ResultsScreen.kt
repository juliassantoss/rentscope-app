package com.example.rentscope.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.rentscope.R
import com.example.rentscope.data.local.LastSearchData
import com.example.rentscope.data.local.LastSearchManager
import com.example.rentscope.data.remote.dto.score.ScoreMunicipioDto
import com.example.rentscope.ui.viewmodel.ScoreViewModel
import java.util.Locale

@Composable
fun ResultsScreen(
    padding: PaddingValues,
    onOpenMapClick: (LastSearchData) -> Unit,
    vm: ScoreViewModel = viewModel()
) {
    val lastSearch = LastSearchManager.get()

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
        Text(
            text = stringResource(R.string.results),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = stringResource(R.string.results_subtitle),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (lastSearch == null) {
            EmptyStateCard(
                title = stringResource(R.string.no_results_title),
                message = stringResource(R.string.no_results_message)
            )
        } else {
            LastSearchSummaryCard(
                data = lastSearch,
                onOpenMapClick = { onOpenMapClick(lastSearch) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            when {
                lastSearch.countryCode != "PT" -> {
                    EmptyStateCard(
                        title = stringResource(R.string.no_data_title),
                        message = "De momento, os resultados detalhados estão disponíveis apenas para Portugal."
                    )
                }

                vm.isLoading -> {
                    EmptyStateCard(
                        title = stringResource(R.string.loading_results_title),
                        message = stringResource(R.string.loading_results_message)
                    )
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
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(
                            items = vm.municipios.take(10),
                            key = { it.codigoMunicipio }
                        ) { item ->
                            ResultCityCard(item = item)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LastSearchSummaryCard(
    data: LastSearchData,
    onOpenMapClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.last_search_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(text = stringResource(R.string.country_label, data.countryName))
            Text(
                text = stringResource(
                    R.string.rent_range_label,
                    formatNullable(data.rendaMin),
                    formatNullable(data.rendaMax)
                )
            )
            Text(
                text = stringResource(
                    R.string.weights_summary_string,
                    format1(data.pesoRenda),
                    format1(data.pesoEscolas),
                    format1(data.pesoHospitais),
                    format1(data.pesoCriminalidade)
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onOpenMapClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = stringResource(R.string.open_map_for_search))
            }
        }
    }
}

@Composable
private fun ResultCityCard(item: ScoreMunicipioDto) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = item.municipioLocalidade,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "${item.regiao} • ${item.grandeRegiao}",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(text = stringResource(R.string.score_label, format1(item.score)))
            Text(
                text = stringResource(
                    R.string.city_rent_label,
                    item.valorMedioM2?.let { format1(it) } ?: "-"
                )
            )
            Text(text = stringResource(R.string.schools_label, item.totalEscolas))
            Text(text = stringResource(R.string.hospitals_label, item.totalHospitais))
            Text(text = stringResource(R.string.crime_label, item.totalCrimes))
        }
    }
}

private fun format1(value: Float): String = String.format(Locale.US, "%.1f", value)

private fun formatNullable(value: Float?): String = value?.let { format1(it) } ?: "-"