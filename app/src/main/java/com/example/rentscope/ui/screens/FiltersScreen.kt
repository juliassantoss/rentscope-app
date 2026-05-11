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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
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

/**
 * Tela de configuração de filtros (pesos).
 *
 * Esta versão simplifica a UX: removeu-se a entrada de "Renda mínima" e
 * "Renda máxima" porque misturavam-se o conceito de filtro (intervalo) com o
 * conceito de score (peso). Agora apenas existem os 4 pesos das dimensões
 * (renda, escolas, hospitais, criminalidade), todos com a mesma escala
 * 0–3 e o mesmo significado de importância. Cada peso tem um ícone (i) que
 * abre um diálogo com a explicação detalhada do que o filtro mede.
 *
 * **Compatibilidade**: a callback continua a expor `rendaMin`/`rendaMax` para
 * que o resto do app (rotas, ViewModels, repositórios) não tenha de mudar;
 * passamos `null` em ambos. O cálculo do score interpreta esse `null` como
 * "ordenar por renda inversa" (renda mais baixa = melhor).
 */
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
        rendaMin: Float?,
        rendaMax: Float?,
        pesoRenda: Float,
        pesoEscolas: Float,
        pesoHospitais: Float,
        pesoCriminalidade: Float
    ) -> Unit
) {
    @Suppress("UNUSED_PARAMETER")
    val unusedCountryCode = countryCode
    @Suppress("UNUSED_PARAMETER")
    val unusedInitialRendaMin = initialRendaMin
    @Suppress("UNUSED_PARAMETER")
    val unusedInitialRendaMax = initialRendaMax

    var pesoRenda by remember { mutableFloatStateOf(initialPesoRenda) }
    var pesoEscolas by remember { mutableFloatStateOf(initialPesoEscolas) }
    var pesoHospitais by remember { mutableFloatStateOf(initialPesoHospitais) }
    var pesoCriminalidade by remember { mutableFloatStateOf(initialPesoCriminalidade) }

    var dialogTitleRes by remember { mutableStateOf<Int?>(null) }
    var dialogBodyRes by remember { mutableStateOf<Int?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        HeaderCard(countryName = countryName)

        Spacer(Modifier.height(14.dp))

        SectionTitle(stringResource(R.string.filters_weights_title))

        Spacer(Modifier.height(10.dp))

        WeightSliderCard(
            title = stringResource(R.string.weight_rent_title),
            subtitle = stringResource(R.string.weight_rent_subtitle),
            value = pesoRenda,
            onValueChange = { pesoRenda = it },
            onInfoClick = {
                dialogTitleRes = R.string.weight_rent_info_title
                dialogBodyRes = R.string.weight_rent_info_body
            }
        )

        Spacer(Modifier.height(12.dp))

        WeightSliderCard(
            title = stringResource(R.string.weight_schools_title),
            subtitle = stringResource(R.string.weight_schools_subtitle),
            value = pesoEscolas,
            onValueChange = { pesoEscolas = it },
            onInfoClick = {
                dialogTitleRes = R.string.weight_schools_info_title
                dialogBodyRes = R.string.weight_schools_info_body
            }
        )

        Spacer(Modifier.height(12.dp))

        WeightSliderCard(
            title = stringResource(R.string.weight_hospitals_title),
            subtitle = stringResource(R.string.weight_hospitals_subtitle),
            value = pesoHospitais,
            onValueChange = { pesoHospitais = it },
            onInfoClick = {
                dialogTitleRes = R.string.weight_hospitals_info_title
                dialogBodyRes = R.string.weight_hospitals_info_body
            }
        )

        Spacer(Modifier.height(12.dp))

        WeightSliderCard(
            title = stringResource(R.string.weight_crime_title),
            subtitle = stringResource(R.string.weight_crime_subtitle),
            value = pesoCriminalidade,
            onValueChange = { pesoCriminalidade = it },
            onInfoClick = {
                dialogTitleRes = R.string.weight_crime_info_title
                dialogBodyRes = R.string.weight_crime_info_body
            }
        )

        Spacer(Modifier.height(18.dp))

        SummaryCard(
            pesoRenda = pesoRenda,
            pesoEscolas = pesoEscolas,
            pesoHospitais = pesoHospitais,
            pesoCriminalidade = pesoCriminalidade
        )

        Spacer(Modifier.height(20.dp))

        Button(
            onClick = {
                // rendaMin/rendaMax ficam null para que o cálculo do score
                // use o modo inverso normalizado (renda mais baixa = melhor).
                onSaveClick(
                    null,
                    null,
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

    val titleRes = dialogTitleRes
    val bodyRes = dialogBodyRes
    if (titleRes != null && bodyRes != null) {
        AlertDialog(
            onDismissRequest = {
                dialogTitleRes = null
                dialogBodyRes = null
            },
            title = { Text(stringResource(titleRes)) },
            text = {
                Text(
                    text = stringResource(bodyRes),
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    dialogTitleRes = null
                    dialogBodyRes = null
                }) {
                    Text(stringResource(R.string.info_dialog_close))
                }
            }
        )
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
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold
    )
}

@Composable
private fun SummaryCard(
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
private fun WeightSliderCard(
    title: String,
    subtitle: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    onInfoClick: () -> Unit
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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )

                        IconButton(
                            onClick = onInfoClick,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Info,
                                contentDescription = stringResource(R.string.info_action),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

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
                        text = format1(value),
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
                valueRange = 0f..3f,
                steps = 5,
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
                    text = "0",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "3",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private fun format1(value: Float): String = String.format(Locale.US, "%.1f", value)
