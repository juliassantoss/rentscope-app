package com.example.rentscope.ui.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.rentscope.R
import com.example.rentscope.data.geo.PolygonRings
import com.example.rentscope.data.geo.extractPolygonsFromGeoJson
import com.example.rentscope.data.geo.loadAssetText
import com.example.rentscope.data.local.LastSearchManager
import com.example.rentscope.data.repository.AuthRepository
import com.example.rentscope.ui.viewmodel.HistoryViewModel
import com.example.rentscope.ui.viewmodel.ScoreViewModel
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Polygon
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt

private val ChoroplethColors = listOf(
    Color(0xFFF4FAFF),
    Color(0xFFD5E9FF),
    Color(0xFF9BCBFF),
    Color(0xFF4F9BFF),
    Color(0xFF1D5FE0),
    Color(0xFF082A7A)
)
private val BorderColor = Color(0xFF1E1E1E)
private val EmptyMapFillColor = Color(0xFFE8EEF5)

@Composable
fun MapScreen(
    padding: PaddingValues,
    countryCode: String,
    countryName: String,
    rendaMin: Float? = null,
    rendaMax: Float? = null,
    pesoRenda: Float = 1f,
    pesoEscolas: Float = 1f,
    pesoHospitais: Float = 1f,
    pesoCriminalidade: Float = 1f,
    saveToHistory: Boolean = true,
    onConfigureFiltersClick: () -> Unit = {},
    onViewResultsClick: () -> Unit = {}
) {
    val center = LatLng(39.5, -8.0)
    val zoom = 6f

    val cameraState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(center, zoom)
    }

    val context = LocalContext.current
    val polygonsState = remember { mutableStateOf<List<PolygonRings>>(emptyList()) }
    val scoreViewModel: ScoreViewModel = viewModel()
    val historyViewModel: HistoryViewModel = viewModel()

    val fileName = "caop_portugal_continente_concelhos.json"

    LaunchedEffect(fileName) {
        try {
            val json = withContext(Dispatchers.IO) {
                loadAssetText(context, fileName)
            }

            val parsed = withContext(Dispatchers.Default) {
                extractPolygonsFromGeoJson(json)
            }

            polygonsState.value = parsed
        } catch (e: Throwable) {
            Log.e("MAP", "Erro ao carregar GeoJSON", e)
            polygonsState.value = emptyList()
        }
    }

    LaunchedEffect(
        countryCode,
        countryName,
        rendaMin,
        rendaMax,
        pesoRenda,
        pesoEscolas,
        pesoHospitais,
        pesoCriminalidade,
        saveToHistory
    ) {
        LastSearchManager.save(
            countryCode = countryCode,
            countryName = countryName,
            rendaMin = rendaMin,
            rendaMax = rendaMax,
            pesoRenda = pesoRenda,
            pesoEscolas = pesoEscolas,
            pesoHospitais = pesoHospitais,
            pesoCriminalidade = pesoCriminalidade
        )

        if (countryCode == "PT") {
            scoreViewModel.carregarScores(
                rendaMin = rendaMin,
                rendaMax = rendaMax,
                pesoRenda = pesoRenda,
                pesoEscolas = pesoEscolas,
                pesoHospitais = pesoHospitais,
                pesoCriminalidade = pesoCriminalidade,
                limite = 400
            )

            if (saveToHistory && AuthRepository.isLoggedIn()) {
                historyViewModel.salvarBusca(
                    countryCode = countryCode,
                    countryName = countryName,
                    rendaMin = rendaMin,
                    rendaMax = rendaMax,
                    pesoRenda = pesoRenda,
                    pesoEscolas = pesoEscolas,
                    pesoHospitais = pesoHospitais,
                    pesoCriminalidade = pesoCriminalidade
                )
            }
        }
    }

    val scoreByMunicipalityCode = remember(scoreViewModel.municipios) {
        scoreViewModel.municipios.associate { municipio ->
            toGeoJsonMunicipalityCode(municipio.codigoMunicipio) to municipio.score.toDouble()
        }
    }
    val scoreBreakpoints = remember(scoreViewModel.municipios) {
        buildChoroplethBreakpoints(
            scores = scoreViewModel.municipios.map { it.score.toDouble() },
            bucketCount = ChoroplethColors.size
        )
    }

    Column(
        modifier = Modifier
            .padding(padding)
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .navigationBarsPadding()
            .padding(16.dp)
    ) {
        FilterMapHelp()

        Spacer(Modifier.height(10.dp))

        Button(
            onClick = onConfigureFiltersClick,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Tune, contentDescription = null)
            Spacer(Modifier.width(10.dp))
            Text(
                text = stringResource(R.string.configure_filters),
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(Modifier.height(14.dp))

        if (scoreViewModel.isLoading) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator()
                    Spacer(Modifier.width(12.dp))
                    Text(stringResource(R.string.loading_real_data))
                }
            }

            Spacer(Modifier.height(14.dp))
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(360.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraState
            ) {
                polygonsState.value.forEach { poly ->
                    val code = poly.municipalityCode?.trim().orEmpty()
                    val score = scoreByMunicipalityCode[code] ?: 0.0
                    val fillColor = if (code in scoreByMunicipalityCode) {
                        scoreToFillColor(score, scoreBreakpoints)
                    } else {
                        EmptyMapFillColor
                    }

                    if (poly.outer.size >= 3) {
                        Polygon(
                            points = poly.outer,
                            holes = poly.holes,
                            fillColor = fillColor,
                            strokeColor = Color.Transparent
                        )
                    }

                    if (poly.outer.size >= 2) {
                        Polyline(
                            points = poly.outer,
                            color = BorderColor,
                            width = 2f
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(14.dp))

        Button(
            onClick = onViewResultsClick,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = stringResource(R.string.results),
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(Modifier.height(14.dp))

        LegendPiorMelhor()

        Spacer(Modifier.height(110.dp))
    }
}

@Composable
private fun FilterMapHelp() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFEAF5FF)
        )
    ) {
        Column(Modifier.padding(14.dp)) {
            Text(
                text = stringResource(R.string.map_filter_help_title),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF075985)
            )

            Spacer(Modifier.height(4.dp))

            Text(
                text = stringResource(R.string.map_filter_help_body),
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF164E63)
            )

            Spacer(Modifier.height(12.dp))

            HelpStep(
                number = "1",
                text = stringResource(R.string.map_filter_help_step_map)
            )

            Spacer(Modifier.height(8.dp))

            HelpStep(
                number = "2",
                text = stringResource(R.string.map_filter_help_step_results)
            )
        }
    }
}

@Composable
private fun HelpStep(
    number: String,
    text: String
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(Color(0xFF0369A1)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = number,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        Spacer(Modifier.width(10.dp))

        Text(
            text = text,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF0F172A)
        )
    }
}

private fun toGeoJsonMunicipalityCode(codigoMunicipio: Int): String {
    return codigoMunicipio
        .toString()
        .takeLast(4)
        .padStart(4, '0')
}

private fun buildChoroplethBreakpoints(
    scores: List<Double>,
    bucketCount: Int
): List<Double> {
    val sortedScores = scores
        .filter { it.isFinite() }
        .sorted()

    if (sortedScores.isEmpty() || bucketCount <= 1) {
        return emptyList()
    }

    return (1 until bucketCount).map { bucket ->
        val percentile = bucket.toFloat() / bucketCount.toFloat()
        val index = (percentile * sortedScores.lastIndex)
            .roundToInt()
            .coerceIn(0, sortedScores.lastIndex)

        sortedScores[index]
    }
}

private fun scoreToFillColor(
    score: Double,
    breakpoints: List<Double>
): Color {
    val safeScore = score.takeIf { it.isFinite() } ?: 0.0
    val colorIndex = breakpoints.indexOfFirst { safeScore <= it }
        .let { index -> if (index == -1) ChoroplethColors.lastIndex else index }
        .coerceIn(0, ChoroplethColors.lastIndex)

    return ChoroplethColors[colorIndex].copy(alpha = 0.96f)
}

@Composable
private fun LegendPiorMelhor() {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(14.dp)) {
            Text(
                text = stringResource(R.string.legend_title),
                fontWeight = FontWeight.SemiBold
            )

            Spacer(Modifier.height(10.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(R.string.lower_fit))
                Spacer(Modifier.width(12.dp))

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(10.dp)
                        .clip(RoundedCornerShape(999.dp))
                ) {
                    Row(Modifier.fillMaxSize()) {
                        ChoroplethColors.forEach { color ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxSize()
                                    .background(color)
                            )
                        }
                    }
                }

                Spacer(Modifier.width(12.dp))
                Text(stringResource(R.string.higher_fit))
            }
        }
    }
}
