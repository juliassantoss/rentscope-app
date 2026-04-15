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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.rentscope.R
import java.util.Locale

private val BrandBlue = Color(0xFF2F86D6)
private val DeepBlue = Color(0xFF006D8F)

@Composable
fun FiltersScreen(
    padding: PaddingValues,
    countryCode: String,
    countryName: String,
    initialRendaMin: Float = 0f,
    initialRendaMax: Float = 20f,
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        HeaderCard(countryName = countryName)

        Spacer(Modifier.height(12.dp))

        ScaleInfoCard()

        Spacer(Modifier.height(14.dp))

        SliderCard(
            title = stringResource(R.string.rent_min_title),
            valueText = "${formatScale(rendaMin)}/20",
            subtitle = stringResource(R.string.rent_min_subtitle),
            value = rendaMin,
            onValueChange = {
                rendaMin = it
                if (rendaMax < it) rendaMax = it
            },
            range = 0f..20f,
            steps = 19,
            minLabel = "0",
            maxLabel = "20"
        )

        Spacer(Modifier.height(12.dp))

        SliderCard(
            title = stringResource(R.string.rent_max_title),
            valueText = "${formatScale(rendaMax)}/20",
            subtitle = stringResource(R.string.rent_max_subtitle),
            value = rendaMax,
            onValueChange = {
                rendaMax = it.coerceAtLeast(rendaMin)
            },
            range = 0f..20f,
            steps = 19,
            minLabel = "0",
            maxLabel = "20"
        )

        Spacer(Modifier.height(18.dp))

        SectionTitle(stringResource(R.string.filters_weights_title))

        Spacer(Modifier.height(10.dp))

        SliderCard(
            title = stringResource(R.string.weight_rent_title),
            valueText = format1(pesoRenda),
            subtitle = stringResource(R.string.weight_rent_subtitle),
            value = pesoRenda,
            onValueChange = { pesoRenda = it },
            range = 0f..3f,
            steps = 5,
            minLabel = "0",
            maxLabel = "3"
        )

        Spacer(Modifier.height(12.dp))

        SliderCard(
            title = stringResource(R.string.weight_schools_title),
            valueText = format1(pesoEscolas),
            subtitle = stringResource(R.string.weight_schools_subtitle),
            value = pesoEscolas,
            onValueChange = { pesoEscolas = it },
            range = 0f..3f,
            steps = 5,
            minLabel = "0",
            maxLabel = "3"
        )

        Spacer(Modifier.height(12.dp))

        SliderCard(
            title = stringResource(R.string.weight_hospitals_title),
            valueText = format1(pesoHospitais),
            subtitle = stringResource(R.string.weight_hospitals_subtitle),
            value = pesoHospitais,
            onValueChange = { pesoHospitais = it },
            range = 0f..3f,
            steps = 5,
            minLabel = "0",
            maxLabel = "3"
        )

        Spacer(Modifier.height(12.dp))

        SliderCard(
            title = stringResource(R.string.weight_crime_title),
            valueText = format1(pesoCriminalidade),
            subtitle = stringResource(R.string.weight_crime_subtitle),
            value = pesoCriminalidade,
            onValueChange = { pesoCriminalidade = it },
            range = 0f..3f,
            steps = 5,
            minLabel = "0",
            maxLabel = "3"
        )

        Spacer(Modifier.height(18.dp))

        SummaryCard(
            rendaMin = rendaMin,
            rendaMax = rendaMax,
            pesoRenda = pesoRenda,
            pesoEscolas = pesoEscolas,
            pesoHospitais = pesoHospitais,
            pesoCriminalidade = pesoCriminalidade
        )

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
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(14.dp)
        ) {
            Text(
                text = stringResource(R.string.apply_filters),
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(Modifier.height(28.dp))
    }
}

@Composable
private fun HeaderCard(countryName: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.filters_title, countryName),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = stringResource(R.string.filters_intro),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ScaleInfoCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFEAF7FB)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(Modifier.padding(14.dp)) {
            Text(
                text = stringResource(R.string.rent_scale_title),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = DeepBlue
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.rent_scale_body),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold
    )
}

@Composable
private fun SummaryCard(
    rendaMin: Float,
    rendaMax: Float,
    pesoRenda: Float,
    pesoEscolas: Float,
    pesoHospitais: Float,
    pesoCriminalidade: Float
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.filters_summary_title),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = stringResource(
                    R.string.filters_summary_rent,
                    format1(rendaMin),
                    format1(rendaMax)
                ),
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(Modifier.height(4.dp))

            Text(
                text = stringResource(
                    R.string.filters_summary_weights,
                    format1(pesoRenda),
                    format1(pesoEscolas),
                    format1(pesoHospitais),
                    format1(pesoCriminalidade)
                ),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun SliderCard(
    title: String,
    valueText: String,
    subtitle: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    range: ClosedFloatingPointRange<Float>,
    steps: Int,
    minLabel: String,
    maxLabel: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(5.dp))
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = BrandBlue.copy(alpha = 0.12f),
                    contentColor = BrandBlue
                ) {
                    Text(
                        text = valueText,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            Slider(
                value = value,
                onValueChange = onValueChange,
                valueRange = range,
                steps = steps,
                colors = SliderDefaults.colors(
                    thumbColor = DeepBlue,
                    activeTrackColor = DeepBlue,
                    inactiveTrackColor = Color(0xFFD8EDF5)
                )
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = minLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = maxLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private fun format1(value: Float): String = String.format(Locale.US, "%.1f", value)

private fun formatScale(value: Float): String {
    val rounded = value.toInt()
    return if (value == rounded.toFloat()) {
        rounded.toString()
    } else {
        format1(value)
    }
}
