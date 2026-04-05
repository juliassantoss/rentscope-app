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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
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

private val LightBlue = Color(0xFFBBDEFB)
private val DarkBlue = Color(0xFF0D47A1)
private val BorderColor = Color(0xFF1E1E1E)
private val HeaderBlue = Color(0xFF2F86D6)

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
    onConfigureFiltersClick: () -> Unit = {}
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

    Column(
        modifier = Modifier
            .padding(padding)
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Button(
            onClick = onConfigureFiltersClick,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Tune, contentDescription = null)
            Spacer(Modifier.width(10.dp))
            Text(stringResource(R.string.configure_filters), fontWeight = FontWeight.SemiBold)
        }

        Spacer(Modifier.height(14.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = HeaderBlue)
        ) {
            Column(Modifier.padding(16.dp)) {
                Text(
                    text = stringResource(R.string.map_preferences_title),
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = countryName,
                    color = Color.White.copy(alpha = 0.9f)
                )
            }
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
                    val fillColor = scoreToFillColor(score)

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
        LegendPiorMelhor()
    }
}

private fun toGeoJsonMunicipalityCode(codigoMunicipio: Int): String {
    return codigoMunicipio
        .toString()
        .takeLast(4)
        .padStart(4, '0')
}

private fun scoreToFillColor(score: Double): Color {
    val normalized = score.coerceIn(0.0, 1.0).toFloat()
    return androidx.compose.ui.graphics.lerp(
        LightBlue,
        DarkBlue,
        normalized
    ).copy(alpha = 0.78f)
}

@Composable
private fun LegendPiorMelhor() {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(14.dp)) {
            Text(stringResource(R.string.legend_title), fontWeight = FontWeight.SemiBold)

            Spacer(Modifier.height(10.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(R.string.lower_fit))
                Spacer(Modifier.width(12.dp))

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(10.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(LightBlue, DarkBlue)
                            )
                        )
                )

                Spacer(Modifier.width(12.dp))
                Text(stringResource(R.string.higher_fit))
            }
        }
    }
}