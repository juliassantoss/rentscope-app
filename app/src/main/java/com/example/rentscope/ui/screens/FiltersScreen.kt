package com.example.rentscope.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

private val BrandBlue = Color(0xFF2F86D6)

@Composable
fun FiltersScreen(
    padding: PaddingValues,
    countryCode: String,
    countryName: String,
    onSaveClick: () -> Unit
) {
    // ===== States (interativos) =====
    var pricePerM2 by remember { mutableFloatStateOf(1200f) } // exemplo
    var minM2 by remember { mutableFloatStateOf(50f) }        // exemplo
    val totalValue = (pricePerM2 * minM2).roundToInt()

    var museums by remember { mutableStateOf(false) }
    var artGalleries by remember { mutableStateOf(false) }
    var libraries by remember { mutableStateOf(false) }
    var hospitals by remember { mutableStateOf(false) }
    var schools by remember { mutableStateOf(false) }

    var crimeRate by remember { mutableFloatStateOf(40f) } // 0..100

    // ✅ Scroll pra conseguir ver tudo
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

        // ===== Preço por m² =====
        SliderCard(
            title = "Preço por m²",
            valueText = "${pricePerM2.roundToInt()}",
            subtitle = "Ajuste o preço médio por m² (placeholder)",
            value = pricePerM2,
            onValueChange = { pricePerM2 = it },
            range = 0f..3000f
        )

        Spacer(Modifier.height(12.dp))

        // ===== Número mínimo de m² =====
        SliderCard(
            title = "Número mínimo de m²",
            valueText = "${minM2.roundToInt()} m²",
            subtitle = "Ajuste o mínimo de m² (placeholder)",
            value = minM2,
            onValueChange = { minM2 = it },
            range = 0f..300f
        )

        Spacer(Modifier.height(16.dp))

        // ===== Valor total =====
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(Modifier.padding(14.dp)) {
                Text(
                    text = "Valor total",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(8.dp))

                Text(
                    text = "$totalValue (preço/m² × m²)",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(Modifier.height(6.dp))

                Text(
                    text = "Resultado automático só pra UI (depois tu troca pelo cálculo real/€).",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(Modifier.height(18.dp))

        // ===== Lazer e utilidades =====
        Text(
            text = "Lazer e utilidades",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(10.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(Modifier.padding(vertical = 6.dp)) {
                CheckRow(label = "Museus", checked = museums, onCheckedChange = { museums = it })
                DividerSoft()
                CheckRow(label = "Galerias de arte", checked = artGalleries, onCheckedChange = { artGalleries = it })
                DividerSoft()
                CheckRow(label = "Bibliotecas", checked = libraries, onCheckedChange = { libraries = it })
                DividerSoft()
                CheckRow(label = "Hospitais", checked = hospitals, onCheckedChange = { hospitals = it })
                DividerSoft()
                CheckRow(label = "Escolas", checked = schools, onCheckedChange = { schools = it })
            }
        }

        Spacer(Modifier.height(18.dp))

        // ===== Taxa de criminalidade =====
        Text(
            text = "Taxa de criminalidade",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(10.dp))

        CrimeCard(
            value = crimeRate,
            onValueChange = { crimeRate = it }
        )

        Spacer(Modifier.height(20.dp))

        // ✅ Botão Salvar (volta para MapScreen)
        Button(
            onClick = onSaveClick,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp)
        ) {
            Text(
                text = "Salvar",
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

@Composable
private fun CheckRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyLarge
        )
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
private fun DividerSoft() {
    HorizontalDivider(
        color = Color(0xFFE6E9EE),
        modifier = Modifier.padding(horizontal = 14.dp)
    )
}

@Composable
private fun CrimeCard(
    value: Float,                 // 0..100
    onValueChange: (Float) -> Unit
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
                Text(
                    text = "Indicador (0–100)",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = value.roundToInt().toString(),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(Modifier.height(10.dp))

            ColumnChartBar(value = value)

            Spacer(Modifier.height(12.dp))

            Slider(
                value = value,
                onValueChange = onValueChange,
                valueRange = 0f..100f
            )
        }
    }
}

@Composable
private fun ColumnChartBar(value: Float) {
    val bars = remember(value) {
        val t = value / 100f // 0..1
        List(10) { i ->
            val base = (i + 1) / 10f
            (0.15f + 0.85f * (0.6f * base + 0.4f * t)).coerceIn(0.12f, 1f)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFFE9EEF5))
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.BottomStart
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Bottom
        ) {
            bars.forEach { h ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 3.dp)
                        .fillMaxHeight(h)
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    BrandBlue.copy(alpha = 0.30f),
                                    BrandBlue
                                )
                            )
                        )
                )
            }
        }
    }
}
