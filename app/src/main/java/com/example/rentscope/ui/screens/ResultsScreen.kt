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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
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

    LaunchedEffect(lastSearch) {
        if (lastSearch != null && lastSearch.countryCode == "PT") {
            vm.carregarScores(
                rendaMin = lastSearch.rendaMin,
                rendaMax = lastSearch.rendaMax,
                pesoRenda = lastSearch.pesoRenda,
                pesoEscolas = lastSearch.pesoEscolas,
                pesoHospitais = lastSearch.pesoHospitais,
                pesoCriminalidade = lastSearch.pesoCriminalidade,
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
            text = "Resultados",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = "Top 10 cidades com melhor adequação de acordo com a última pesquisa.",
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(16.dp))

        if (lastSearch == null) {
            EmptyStateCard(
                title = "Ainda não há resultados",
                message = "Faça uma pesquisa no mapa para ver aqui as 10 melhores cidades."
            )
            return
        }

        LastSearchSummaryCard(
            data = lastSearch,
            onOpenMapClick = { onOpenMapClick(lastSearch) }
        )

        Spacer(Modifier.height(16.dp))

        when {
            vm.isLoading -> {
                EmptyStateCard(
                    title = "A carregar resultados",
                    message = "Estamos a calcular as 10 melhores cidades da última pesquisa."
                )
            }

            !vm.errorMessage.isNullOrBlank() -> {
                EmptyStateCard(
                    title = "Erro ao carregar resultados",
                    message = vm.errorMessage ?: "Erro inesperado."
                )
            }

            vm.municipios.isEmpty() -> {
                EmptyStateCard(
                    title = "Sem resultados",
                    message = "Não foi possível encontrar cidades para esta pesquisa."
                )
            }

            else -> {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(vm.municipios.take(10), key = { it.codigoMunicipio }) { item ->
                        ResultCityCard(item = item)
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
                text = "Última pesquisa",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(Modifier.height(8.dp))

            Text(text = "País: ${data.countryName}")
            Text(
                text = "Renda: ${formatNullable(data.rendaMin)} até ${formatNullable(data.rendaMax)} €/m²"
            )
            Text(
                text = "Pesos — Renda: ${format1(data.pesoRenda)} | Escolas: ${format1(data.pesoEscolas)} | Hospitais: ${format1(data.pesoHospitais)} | Criminalidade: ${format1(data.pesoCriminalidade)}"
            )

            Spacer(Modifier.height(12.dp))

            Button(
                onClick = onOpenMapClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Abrir mapa desta pesquisa")
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

            Spacer(Modifier.height(4.dp))

            Text(
                text = "${item.regiao} • ${item.grandeRegiao}",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(8.dp))

            Text(text = "Score: ${format1(item.score)}")
            Text(text = "Renda: ${item.valorMedioM2?.let { format1(it) } ?: "-"} €/m²")
            Text(text = "Escolas: ${item.totalEscolas}")
            Text(text = "Hospitais: ${item.totalHospitais}")
            Text(text = "Criminalidade: ${item.totalCrimes}")
        }
    }
}

private fun format1(value: Float): String = String.format(Locale.US, "%.1f", value)
private fun formatNullable(value: Float?): String = value?.let { format1(it) } ?: "-"