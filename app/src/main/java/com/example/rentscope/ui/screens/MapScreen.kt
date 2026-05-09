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
import androidx.compose.runtime.key
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
import com.example.rentscope.data.geo.GeoJsonCache
import com.example.rentscope.data.geo.PolygonRings
import com.example.rentscope.data.local.LastSearchManager
import com.example.rentscope.data.repository.AuthRepository
import com.example.rentscope.data.remote.dto.score.ScoreMunicipioDto
import com.example.rentscope.ui.viewmodel.HistoryViewModel
import com.example.rentscope.ui.viewmodel.ScoreViewModel
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Polygon
import com.google.maps.android.compose.rememberCameraPositionState
import java.text.NumberFormat
import java.text.Normalizer
import java.util.Locale
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

private data class MunicipalityTooltipData(
    val name: String,
    val area: String?,
    val averageRentPerSquareMeter: Float?,
    val schoolsCount: Int?,
    val hospitalsCount: Int?,
    val crimesCount: Int?
)

private data class PolygonMatchData(
    val polygon: PolygonRings,
    val municipalityData: ScoreMunicipioDto?
)

private data class MapPolygonUiData(
    val id: String,
    val outer: List<LatLng>,
    val holes: List<List<LatLng>>,
    val fillColor: Color,
    val tooltipData: MunicipalityTooltipData?,
    val isClickable: Boolean
)

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
    val municipalityFallbackName = stringResource(R.string.search_municipality_label)
    val polygonsState = remember { mutableStateOf<List<PolygonRings>>(emptyList()) }
    val scoreViewModel: ScoreViewModel = viewModel()
    val historyViewModel: HistoryViewModel = viewModel()
    val selectedMunicipalityState = remember { mutableStateOf<MunicipalityTooltipData?>(null) }

    LaunchedEffect(Unit) {
        // Cache singleton: o GeoJSON é parseado uma única vez por sessão da app.
        // Visitas seguintes ao mapa ficam quase instantâneas.
        try {
            polygonsState.value = GeoJsonCache.loadPortugalConcelhos(context)
        } catch (e: OutOfMemoryError) {
            Log.e("MAP", "Sem memória ao carregar GeoJSON", e)
            polygonsState.value = emptyList()
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
    val municipalityDataByName = remember(scoreViewModel.municipios) {
        scoreViewModel.municipios.associateBy { municipio ->
            normalizeMunicipalityKey(municipio.municipioLocalidade)
        }
    }
    val municipalityDataByCode = remember(scoreViewModel.municipios) {
        scoreViewModel.municipios.associateBy { municipio ->
            toGeoJsonMunicipalityCode(municipio.codigoMunicipio)
        }
    }
    val polygonMatches = remember(
        polygonsState.value,
        municipalityDataByCode,
        municipalityDataByName
    ) {
        polygonsState.value.map { polygon ->
            PolygonMatchData(
                polygon = polygon,
                municipalityData = findMunicipalityData(
                    polygon = polygon,
                    municipalityDataByCode = municipalityDataByCode,
                    municipalityDataByName = municipalityDataByName
                )
            )
        }
    }
    val scoreBreakpoints = remember(polygonMatches) {
        buildChoroplethBreakpoints(
            scores = polygonMatches.mapNotNull { match ->
                match.municipalityData?.score?.toDouble()
            },
            bucketCount = ChoroplethColors.size
        )
    }
    val polygonUiData = remember(
        polygonMatches,
        scoreBreakpoints,
        municipalityFallbackName
    ) {
        polygonMatches.mapIndexed { index, match ->
            val polygon = match.polygon
            val municipalityData = match.municipalityData
            val score = municipalityData?.score?.toDouble()
            val fillColor = if (score != null) {
                scoreToFillColor(score, scoreBreakpoints)
            } else {
                EmptyMapFillColor
            }

            MapPolygonUiData(
                id = "${polygon.municipalityCode.orEmpty()}-$index",
                outer = polygon.outer,
                holes = polygon.holes,
                fillColor = fillColor,
                tooltipData = buildTooltipData(
                    polygon = polygon,
                    municipalityData = municipalityData,
                    municipalityFallbackName = municipalityFallbackName
                ),
                isClickable = !polygon.municipalityName.isNullOrBlank() || municipalityData != null
            )
        }
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
            Box(modifier = Modifier.fillMaxSize()) {
                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraState,
                    onMapClick = { selectedMunicipalityState.value = null }
                ) {
                    polygonUiData.forEach { item ->
                        key(item.id, item.fillColor) {
                            if (item.outer.size >= 3) {
                                Polygon(
                                    points = item.outer,
                                    holes = item.holes,
                                    fillColor = item.fillColor,
                                    strokeColor = BorderColor,
                                    strokeWidth = 1f,
                                    clickable = item.isClickable,
                                    onClick = {
                                        selectedMunicipalityState.value = item.tooltipData
                                    }
                                )
                            }
                        }
                    }
                }

                selectedMunicipalityState.value?.let { municipality ->
                    MunicipalityTooltipCard(
                        item = municipality,
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(12.dp)
                    )
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

        Spacer(Modifier.height(12.dp))

        InfoFootnote(text = stringResource(R.string.rent_data_coverage_note))

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
    if (breakpoints.isEmpty()) {
        return ChoroplethColors.first().copy(alpha = 0.96f)
    }

    val safeScore = score.takeIf { it.isFinite() } ?: 0.0
    val colorIndex = breakpoints.indexOfFirst { safeScore <= it }
        .let { index -> if (index == -1) ChoroplethColors.lastIndex else index }
        .coerceIn(0, ChoroplethColors.lastIndex)

    return ChoroplethColors[colorIndex].copy(alpha = 0.96f)
}

private fun buildScoreAreaLabel(item: ScoreMunicipioDto): String? {
    return listOfNotNull(
        item.regiao?.takeIf { it.isNotBlank() },
        item.grandeRegiao?.takeIf { it.isNotBlank() }
    )
        .distinct()
        .joinToString(" / ")
        .ifBlank { null }
}

private fun buildTooltipData(
    polygon: PolygonRings,
    municipalityData: ScoreMunicipioDto?,
    municipalityFallbackName: String
): MunicipalityTooltipData {
    return MunicipalityTooltipData(
        name = municipalityData?.municipioLocalidade
            ?: polygon.municipalityName
            ?: municipalityFallbackName,
        area = municipalityData?.let(::buildScoreAreaLabel)
            ?: buildPolygonAreaLabel(polygon),
        averageRentPerSquareMeter = municipalityData?.valorMedioM2,
        schoolsCount = municipalityData?.totalEscolas,
        hospitalsCount = municipalityData?.totalHospitais,
        crimesCount = municipalityData?.totalCrimes
    )
}

private fun findMunicipalityData(
    polygon: PolygonRings,
    municipalityDataByCode: Map<String, ScoreMunicipioDto>,
    municipalityDataByName: Map<String, ScoreMunicipioDto>
): ScoreMunicipioDto? {
    val code = polygon.municipalityCode?.trim().orEmpty()
    return municipalityDataByCode[code]
        ?: municipalityDataByName[normalizeMunicipalityKey(polygon.municipalityName)]
}

private fun buildPolygonAreaLabel(item: PolygonRings): String? {
    return listOfNotNull(
        item.districtName?.takeIf { it.isNotBlank() },
        item.subRegionName?.takeIf { it.isNotBlank() },
        item.regionName?.takeIf { it.isNotBlank() }
    )
        .distinct()
        .joinToString(" / ")
        .ifBlank { null }
}

private fun normalizeMunicipalityKey(value: String?): String {
    val normalizedValue = Normalizer.normalize(
        value
            .orEmpty()
            .trim(),
        Normalizer.Form.NFD
    )

    return normalizedValue
        .replace("\\p{InCombiningDiacriticalMarks}+".toRegex(), "")
        .orEmpty()
        .lowercase(Locale.ROOT)
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

@Composable
private fun MunicipalityTooltipCard(
    item: MunicipalityTooltipData,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(0.72f),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.96f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = item.name,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(4.dp))

            Text(
                text = item.area ?: "-",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(10.dp))

            TooltipMetricRow(
                label = stringResource(R.string.crime_label_short),
                value = item.crimesCount?.let(::formatMapCount) ?: "—"
            )
            TooltipMetricRow(
                label = stringResource(R.string.rent_label_short),
                value = formatMapRent(item.averageRentPerSquareMeter)
            )
            TooltipMetricRow(
                label = stringResource(R.string.schools_label_short),
                value = item.schoolsCount?.let(::formatMapCount) ?: "—"
            )
            TooltipMetricRow(
                label = stringResource(R.string.hospitals_label_short),
                value = item.hospitalsCount?.let(::formatMapCount) ?: "—"
            )
        }
    }
}

@Composable
private fun TooltipMetricRow(
    label: String,
    value: String
) {
    Text(
        text = "$label: $value",
        style = MaterialTheme.typography.bodySmall
    )
}

private fun formatMapCount(value: Int): String {
    return NumberFormat.getIntegerInstance(Locale.getDefault()).format(value)
}

/**
 * Formata renda média por m² para o tooltip do mapa.
 *
 * Devolve "—" para municípios sem dados de renda atualizados (em vez de
 * deixar o tooltip aparentemente vazio com só o label "Renda:").
 */
private fun formatMapRent(value: Float?): String {
    if (value == null || !value.isFinite()) return "—"
    return String.format(Locale.getDefault(), "%.1f €/m²", value)
}
