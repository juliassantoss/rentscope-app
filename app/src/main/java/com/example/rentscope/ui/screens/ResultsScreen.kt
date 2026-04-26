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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
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
import com.example.rentscope.ui.components.SkeletonBlock
import com.example.rentscope.ui.viewmodel.ScoreViewModel
import java.util.Locale

private val ResultsBlue = Color(0xFF00708E)

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
        ScreenHeader(
            title = stringResource(R.string.results),
            subtitle = stringResource(R.string.results_subtitle),
            icon = Icons.AutoMirrored.Filled.ShowChart
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

            Spacer(modifier = Modifier.height(14.dp))

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
                            ResultCityCard(item = item)
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
private fun LastSearchSummaryCard(
    data: LastSearchData,
    onOpenMapClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = CircleShape,
                    color = ResultsBlue.copy(alpha = 0.12f)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Map,
                        contentDescription = null,
                        tint = ResultsBlue,
                        modifier = Modifier.padding(10.dp)
                    )
                }

                Spacer(modifier = Modifier.padding(6.dp))

                Column(Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.last_search_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = data.countryName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            MetricChip(
                label = stringResource(R.string.rent_label_short),
                value = "${formatNullable(data.rendaMin)} - ${formatNullable(data.rendaMax)} / 20",
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(
                    R.string.weights_summary_string,
                    format1(data.pesoRenda),
                    format1(data.pesoEscolas),
                    format1(data.pesoHospitais),
                    format1(data.pesoCriminalidade)
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onOpenMapClick,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(text = stringResource(R.string.open_map_for_search), fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun ResultCityCard(item: ScoreMunicipioDto) {
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

                    Text(
                        text = "${item.regiao} • ${item.grandeRegiao}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
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
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MetricChip(
                    label = stringResource(R.string.rent_label_short),
                    value = item.valorMedioM2?.let { format1(it) } ?: "-",
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

private fun format1(value: Float): String = String.format(Locale.getDefault(), "%.1f", value)

private fun formatNullable(value: Float?): String = value?.let { format1(it) } ?: "-"
