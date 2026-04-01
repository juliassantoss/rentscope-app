package com.example.rentscope.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun FiltersScreen(
    padding: PaddingValues,
    countryCode: String,
    countryName: String,
    initialRendaMin: Float = 0f,
    initialRendaMax: Float = 15f,
    initialPesoRenda: Float = 1f,
    initialPesoEscolas: Float = 1f,
    initialPesoHospitais: Float = 1f,
    initialPesoCriminalidade: Float = 1f,
    onSaveClick: (
        rendaMin: Float,
        rendaMax: Float,
        pesoRenda: Float,
        pesoEscolas: Float,
        pesoHospitais: Float,
        pesoCriminalidade: Float
    ) -> Unit
) {
    var rendaMin by remember { mutableFloatStateOf(initialRendaMin) }
    var rendaMax by remember { mutableFloatStateOf(initialRendaMax) }

    var pesoRenda by remember { mutableFloatStateOf(initialPesoRenda) }
    var pesoEscolas by remember { mutableFloatStateOf(initialPesoEscolas) }
    var pesoHospitais by remember { mutableFloatStateOf(initialPesoHospitais) }
    var pesoCriminalidade by remember { mutableFloatStateOf(initialPesoCriminalidade) }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        Text(
            text = "Filtros — $countryName",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(12.dp))

        Text(
            text = "Todos os municípios continuam no mapa. Os filtros e pesos servem para ajustar o grau de adequação de cada um.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(16.dp))

        SliderCard(
            title = "Renda mínima (€ / m²)",
            valueText = String.format("%.1f", rendaMin),
            subtitle = "Valor mínimo desejado para o arrendamento médio por m².",
            value = rendaMin,
            onValueChange = {
                rendaMin = it
                if (rendaMax < it) rendaMax = it
            },
            range = 0f..20f
        )

        Spacer(Modifier.height(12.dp))

        SliderCard(
            title = "Renda máxima (€ / m²)",
            valueText = String.format("%.1f", rendaMax),
            subtitle = "Valor máximo desejado para o arrendamento médio por m².",
            value = rendaMax,
            onValueChange = {
                rendaMax = it.coerceAtLeast(rendaMin)
            },
            range = 0f..20f
        )

        Spacer(Modifier.height(18.dp))

        Text(
            text = "Importância de cada critério",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(10.dp))

        SliderCard(
            title = "Peso da renda",
            valueText = String.format("%.1f", pesoRenda),
            subtitle = "Quanto maior, mais o score valoriza municípios com renda próxima do intervalo desejado.",
            value = pesoRenda,
            onValueChange = { pesoRenda = it },
            range = 0f..3f
        )

        Spacer(Modifier.height(12.dp))

        SliderCard(
            title = "Peso das escolas",
            valueText = String.format("%.1f", pesoEscolas),
            subtitle = "Quanto maior, mais o score valoriza a oferta de escolas.",
            value = pesoEscolas,
            onValueChange = { pesoEscolas = it },
            range = 0f..3f
        )

        Spacer(Modifier.height(12.dp))

        SliderCard(
            title = "Peso dos hospitais",
            valueText = String.format("%.1f", pesoHospitais),
            subtitle = "Quanto maior, mais o score valoriza a oferta de hospitais.",
            value = pesoHospitais,
            onValueChange = { pesoHospitais = it },
            range = 0f..3f
        )

        Spacer(Modifier.height(12.dp))

        SliderCard(
            title = "Peso da criminalidade",
            valueText = String.format("%.1f", pesoCriminalidade),
            subtitle = "Quanto maior, mais o score penaliza municípios com maior criminalidade.",
            value = pesoCriminalidade,
            onValueChange = { pesoCriminalidade = it },
            range = 0f..3f
        )

        Spacer(Modifier.height(18.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(Modifier.padding(14.dp)) {
                Text(
                    text = "Resumo dos filtros",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    text = "Renda desejada: ${String.format("%.1f", rendaMin)} até ${String.format("%.1f", rendaMax)} €/m²",
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(Modifier.height(4.dp))

                Text(
                    text = "Pesos — Renda: ${String.format("%.1f", pesoRenda)} | Escolas: ${String.format("%.1f", pesoEscolas)} | Hospitais: ${String.format("%.1f", pesoHospitais)} | Criminalidade: ${String.format("%.1f", pesoCriminalidade)}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        Spacer(Modifier.height(20.dp))

        Button(
            onClick = {
                onSaveClick(
                    rendaMin,
                    rendaMax,
                    pesoRenda,
                    pesoEscolas,
                    pesoHospitais,
                    pesoCriminalidade
                )
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp)
        ) {
            Text(
                text = "Aplicar filtros",
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(Modifier.height(28.dp))
    }
}

@Composable
private fun SliderCard(
    title: String,
    valueText: String,
    subtitle: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    range: ClosedFloatingPointRange<Float>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Text(
                    text = valueText,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(Modifier.height(10.dp))

            Slider(
                value = value,
                onValueChange = onValueChange,
                valueRange = range
            )
        }
    }
}