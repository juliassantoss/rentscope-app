package com.example.rentscope.data.local

import com.example.rentscope.data.remote.dto.MunicipioDto
import com.example.rentscope.data.remote.dto.score.ScoreMunicipioDto
import java.util.Locale
import kotlin.math.abs
import kotlin.math.ln

internal object OfflineScoreCalculator {

    fun buildMunicipios(): List<MunicipioDto> {
        return OfflineMunicipalityBaseline.load()
            .map { item ->
                MunicipioDto(
                    codigo_municipio = item.codigoMunicipio,
                    municipio_localidade = item.municipioLocalidade
                )
            }
            .sortedBy { it.municipio_localidade.lowercase(Locale.ROOT) }
    }

    fun buildScores(
        busca: String? = null,
        rendaMin: Float? = null,
        rendaMax: Float? = null,
        pesoRenda: Float = 1f,
        pesoEscolas: Float = 1f,
        pesoHospitais: Float = 1f,
        pesoCriminalidade: Float = 1f,
        limite: Int = 200
    ): List<ScoreMunicipioDto> {
        val filtered = OfflineMunicipalityBaseline.load()
            .filter { item ->
                busca.isNullOrBlank() || item.municipioLocalidade.contains(
                    busca.trim(),
                    ignoreCase = true
                )
            }

        if (filtered.isEmpty()) {
            return emptyList()
        }

        val normalizedBounds = ScoreNormalizationBounds.from(filtered)
        val safePesoRenda = pesoRenda.coerceAtLeast(0f)
        val safePesoEscolas = pesoEscolas.coerceAtLeast(0f)
        val safePesoHospitais = pesoHospitais.coerceAtLeast(0f)
        val safePesoCriminalidade = pesoCriminalidade.coerceAtLeast(0f)
        val somaPesos = listOf(
            safePesoRenda,
            safePesoEscolas,
            safePesoHospitais,
            safePesoCriminalidade
        )
            .sum()
            .takeIf { it > 0f }
            ?: 1f

        return filtered
            .map { item ->
                val scoreRenda = calculateRentScore(
                    valorMedioM2 = item.valorMedioM2,
                    rendaMin = rendaMin,
                    rendaMax = rendaMax,
                    bounds = normalizedBounds
                )
                val scoreEscolas = normalizedScore(
                    value = ln(1 + item.totalEscolas.toDouble()),
                    min = normalizedBounds.minEscolasSuave,
                    max = normalizedBounds.maxEscolasSuave
                )
                val scoreHospitais = normalizedScore(
                    value = ln(1 + item.totalHospitais.toDouble()),
                    min = normalizedBounds.minHospitaisSuave,
                    max = normalizedBounds.maxHospitaisSuave
                )
                val scoreCriminalidade = normalizedInverseScore(
                    value = item.totalCrimes.toDouble(),
                    min = normalizedBounds.minCrimes,
                    max = normalizedBounds.maxCrimes
                )
                val score = (
                    (scoreRenda * safePesoRenda) +
                        (scoreEscolas * safePesoEscolas) +
                        (scoreHospitais * safePesoHospitais) +
                        (scoreCriminalidade * safePesoCriminalidade)
                    ) / somaPesos

                ScoreMunicipioDto(
                    codigoMunicipio = item.codigoMunicipio,
                    municipioLocalidade = item.municipioLocalidade,
                    regiao = item.regiao,
                    grandeRegiao = item.grandeRegiao,
                    rendaTrimestre = item.rendaTrimestre,
                    valorMedioM2 = item.valorMedioM2,
                    totalEscolas = item.totalEscolas,
                    totalHospitais = item.totalHospitais,
                    totalCrimes = item.totalCrimes,
                    scoreRenda = scoreRenda,
                    scoreEscolas = scoreEscolas,
                    scoreHospitais = scoreHospitais,
                    scoreCriminalidade = scoreCriminalidade,
                    score = score
                )
            }
            .sortedWith(
                compareByDescending<ScoreMunicipioDto> { it.score }
                    .thenBy { it.municipioLocalidade.lowercase(Locale.ROOT) }
            )
            .take(limite.coerceAtLeast(0))
    }

    private fun normalizedScore(
        value: Double,
        min: Double,
        max: Double
    ): Float {
        if (max == min) {
            return 1f
        }

        return ((value - min) / (max - min))
            .coerceIn(0.0, 1.0)
            .toFloat()
    }

    private fun normalizedInverseScore(
        value: Double,
        min: Double,
        max: Double
    ): Float {
        if (max == min) {
            return 1f
        }

        return (1.0 - ((value - min) / (max - min)))
            .coerceIn(0.0, 1.0)
            .toFloat()
    }

    private fun calculateRentScore(
        valorMedioM2: Float?,
        rendaMin: Float?,
        rendaMax: Float?,
        bounds: ScoreNormalizationBounds
    ): Float {
        // Caso novo (UI atual): a UI já não recolhe min/max — ambos chegam null.
        // Tratamos a renda como criminalidade: "menos é melhor" — município com
        // a renda mais baixa nos dados disponíveis recebe 1.0; o de renda mais
        // alta recebe 0.0. Municípios sem dados de renda contribuem com 0.
        if (rendaMin == null && rendaMax == null) {
            val current = valorMedioM2?.toDouble() ?: return 0f
            val minData = bounds.minRendaData ?: return 1f
            val maxData = bounds.maxRendaData ?: return 1f
            if (maxData == minData) return 1f
            return (1.0 - ((current - minData) / (maxData - minData)))
                .coerceIn(0.0, 1.0)
                .toFloat()
        }

        // Caso legacy (histórico antigo guardou min/max): mantemos a lógica
        // original baseada em intervalo, para que abrir uma pesquisa antiga
        // produza o mesmo ranking que produziu na altura.
        val currentRent = valorMedioM2?.toDouble()
        val minRent = rendaMin?.toDouble()
        val maxRent = rendaMax?.toDouble()

        if (minRent != null && maxRent != null) {
            if (currentRent != null && currentRent in minRent..maxRent) {
                return 1f
            }

            val distance = when {
                currentRent == null -> 0.0
                currentRent < minRent -> minRent - currentRent
                currentRent > maxRent -> currentRent - maxRent
                else -> 0.0
            }
            val denominator = maxOf(
                1.0,
                bounds.minRendaData?.let { minRent - it } ?: Double.NEGATIVE_INFINITY,
                bounds.maxRendaData?.let { it - maxRent } ?: Double.NEGATIVE_INFINITY
            )

            return (1.0 - (distance / denominator))
                .coerceIn(0.0, 1.0)
                .toFloat()
        }

        val denominator = maxOf(
            1.0,
            (bounds.maxRendaData ?: 0.0) - (bounds.minRendaData ?: 0.0)
        )

        if (minRent != null) {
            if (currentRent == null) {
                return 0f
            }

            return (1.0 - (abs(currentRent - minRent) / denominator))
                .coerceIn(0.0, 1.0)
                .toFloat()
        }

        if (maxRent != null) {
            if (currentRent == null) {
                return 0f
            }

            return (1.0 - (abs(currentRent - maxRent) / denominator))
                .coerceIn(0.0, 1.0)
                .toFloat()
        }

        return 1f
    }
}

private data class ScoreNormalizationBounds(
    val minEscolasSuave: Double,
    val maxEscolasSuave: Double,
    val minHospitaisSuave: Double,
    val maxHospitaisSuave: Double,
    val minCrimes: Double,
    val maxCrimes: Double,
    val minRendaData: Double?,
    val maxRendaData: Double?
) {
    companion object {
        fun from(items: List<OfflineMunicipalitySeedDto>): ScoreNormalizationBounds {
            val escolasSuaves = items.map { ln(1 + it.totalEscolas.toDouble()) }
            val hospitaisSuaves = items.map { ln(1 + it.totalHospitais.toDouble()) }
            val crimes = items.map { it.totalCrimes.toDouble() }
            val rendas = items.mapNotNull { it.valorMedioM2?.toDouble() }

            return ScoreNormalizationBounds(
                minEscolasSuave = escolasSuaves.minOrNull() ?: 0.0,
                maxEscolasSuave = escolasSuaves.maxOrNull() ?: 0.0,
                minHospitaisSuave = hospitaisSuaves.minOrNull() ?: 0.0,
                maxHospitaisSuave = hospitaisSuaves.maxOrNull() ?: 0.0,
                minCrimes = crimes.minOrNull() ?: 0.0,
                maxCrimes = crimes.maxOrNull() ?: 0.0,
                minRendaData = rendas.minOrNull(),
                maxRendaData = rendas.maxOrNull()
            )
        }
    }
}
