package com.example.rentscope.data.geo

import android.util.JsonReader
import android.util.JsonToken
import com.google.android.gms.maps.model.LatLng
import java.io.Reader
import java.io.StringReader

data class PolygonRings(
    val municipalityName: String?,
    val municipalityCode: String?,
    val districtName: String?,
    val subRegionName: String?,
    val regionName: String?,
    val outer: List<LatLng>,
    val holes: List<List<LatLng>> = emptyList()
)

/**
 * Mantida por compatibilidade. Recomenda-se usar [extractPolygonsFromGeoJsonReader]
 * com um Reader em streaming (menos memória) sempre que possível.
 */
fun extractPolygonsFromGeoJson(
    geoJson: String,
    limitFeatures: Int = Int.MAX_VALUE
): List<PolygonRings> {
    return extractPolygonsFromGeoJsonReader(StringReader(geoJson), limitFeatures)
}

/**
 * Faz parse do GeoJSON em streaming (sem carregar a árvore inteira em memória)
 * usando [JsonReader] do Android. Aplica simplificação adaptativa para reduzir
 * o número de pontos dos polígonos, evitando OOM em concelhos muito grandes.
 */
fun extractPolygonsFromGeoJsonReader(
    reader: Reader,
    limitFeatures: Int = Int.MAX_VALUE
): List<PolygonRings> {
    val output = mutableListOf<PolygonRings>()

    JsonReader(reader).use { jsonReader ->
        jsonReader.beginObject()

        while (jsonReader.hasNext()) {
            when (jsonReader.nextName()) {
                "features" -> readFeatures(jsonReader, output, limitFeatures)
                else -> jsonReader.skipValue()
            }
        }

        jsonReader.endObject()
    }

    return output
}

private fun readFeatures(
    reader: JsonReader,
    output: MutableList<PolygonRings>,
    limitFeatures: Int
) {
    reader.beginArray()

    while (reader.hasNext()) {
        if (output.size >= limitFeatures) {
            reader.skipValue()
            continue
        }

        readFeature(reader, output, limitFeatures)
    }

    reader.endArray()
}

private fun readFeature(
    reader: JsonReader,
    output: MutableList<PolygonRings>,
    limitFeatures: Int
) {
    var municipalityName: String? = null
    var municipalityCode: String? = null
    var districtName: String? = null
    var subRegionName: String? = null
    var regionName: String? = null
    var pendingGeometry: PendingGeometry? = null

    reader.beginObject()

    while (reader.hasNext()) {
        when (reader.nextName()) {
            "properties" -> {
                if (reader.peek() == JsonToken.NULL) {
                    reader.nextNull()
                } else {
                    reader.beginObject()
                    while (reader.hasNext()) {
                        when (reader.nextName()) {
                            "Concelho" -> municipalityName = readNullableString(reader)
                            "DICO" -> municipalityCode = readNullableString(reader)
                            "Distrito" -> districtName = readNullableString(reader)
                            "NUTIII_DSG" -> subRegionName = readNullableString(reader)
                            "NUTII_DSG" -> regionName = readNullableString(reader)
                            else -> reader.skipValue()
                        }
                    }
                    reader.endObject()
                }
            }

            "geometry" -> {
                pendingGeometry = if (reader.peek() == JsonToken.NULL) {
                    reader.nextNull()
                    null
                } else {
                    readGeometry(reader)
                }
            }

            else -> reader.skipValue()
        }
    }

    reader.endObject()

    val geometry = pendingGeometry ?: return

    geometry.polygons.forEach { rings ->
        if (output.size >= limitFeatures) return@forEach
        if (rings.isEmpty()) return@forEach

        val outer = simplifyRing(rings[0])
        if (outer.size < 3) return@forEach

        val holes = rings
            .drop(1)
            .map(::simplifyRing)
            .filter { it.size >= 3 }

        output.add(
            PolygonRings(
                municipalityName = municipalityName,
                municipalityCode = municipalityCode,
                districtName = districtName,
                subRegionName = subRegionName,
                regionName = regionName,
                outer = outer,
                holes = holes
            )
        )
    }
}

/** Resultado intermédio: lista de polígonos, cada polígono é uma lista de anéis. */
private data class PendingGeometry(
    val polygons: List<List<List<LatLng>>>
)

private fun readGeometry(reader: JsonReader): PendingGeometry? {
    var type: String? = null
    var polygons: List<List<List<LatLng>>>? = null
    var nestedGeometries: List<PendingGeometry>? = null

    reader.beginObject()

    while (reader.hasNext()) {
        when (reader.nextName()) {
            "type" -> type = readNullableString(reader)

            "coordinates" -> {
                // Lemos as coordenadas numa estrutura genérica e depois interpretamos.
                polygons = readCoordinates(reader, type)
            }

            "geometries" -> {
                if (reader.peek() == JsonToken.NULL) {
                    reader.nextNull()
                } else {
                    val collected = mutableListOf<PendingGeometry>()
                    reader.beginArray()
                    while (reader.hasNext()) {
                        readGeometry(reader)?.let(collected::add)
                    }
                    reader.endArray()
                    nestedGeometries = collected
                }
            }

            else -> reader.skipValue()
        }
    }

    reader.endObject()

    return when (type) {
        "Polygon" -> polygons?.let { PendingGeometry(it) }
        "MultiPolygon" -> polygons?.let { PendingGeometry(it) }
        "GeometryCollection" -> {
            val merged = nestedGeometries
                ?.flatMap { it.polygons }
                .orEmpty()
            PendingGeometry(merged)
        }
        else -> null
    }
}

/**
 * Lê o array `coordinates` adaptando-se ao tipo:
 * - Polygon:      [ [ [lon,lat], ... ], ...rings ]
 * - MultiPolygon: [ Polygon, Polygon, ... ]
 *
 * Em ambos os casos devolvemos uma lista de polígonos (cada polígono é uma
 * lista de anéis). Para Polygon, devolvemos uma lista com um único polígono.
 */
private fun readCoordinates(
    reader: JsonReader,
    type: String?
): List<List<List<LatLng>>> {
    if (reader.peek() == JsonToken.NULL) {
        reader.nextNull()
        return emptyList()
    }

    return when (type) {
        "MultiPolygon" -> readPolygonArray(reader)
        else -> listOf(readRingArray(reader)) // Default: Polygon
    }
}

/** Lê um array de polígonos. Cada polígono é um array de anéis. */
private fun readPolygonArray(reader: JsonReader): List<List<List<LatLng>>> {
    val polygons = mutableListOf<List<List<LatLng>>>()
    reader.beginArray()
    while (reader.hasNext()) {
        polygons.add(readRingArray(reader))
    }
    reader.endArray()
    return polygons
}

/** Lê um array de anéis. Cada anel é um array de pontos [lon, lat]. */
private fun readRingArray(reader: JsonReader): List<List<LatLng>> {
    val rings = mutableListOf<List<LatLng>>()
    reader.beginArray()
    while (reader.hasNext()) {
        rings.add(readRing(reader))
    }
    reader.endArray()
    return rings
}

/** Lê um anel: array de pontos [lon, lat]. */
private fun readRing(reader: JsonReader): List<LatLng> {
    val points = mutableListOf<LatLng>()
    reader.beginArray()
    while (reader.hasNext()) {
        reader.beginArray()

        val lon = if (reader.hasNext()) reader.nextDouble() else Double.NaN
        val lat = if (reader.hasNext()) reader.nextDouble() else Double.NaN

        // Ignora valores extra (altitude, etc.)
        while (reader.hasNext()) reader.skipValue()

        reader.endArray()

        if (!lon.isNaN() && !lat.isNaN()) {
            points.add(LatLng(lat, lon))
        }
    }
    reader.endArray()
    return points
}

private fun readNullableString(reader: JsonReader): String? {
    return if (reader.peek() == JsonToken.NULL) {
        reader.nextNull()
        null
    } else {
        reader.nextString()?.takeIf { it.isNotBlank() }
    }
}

/**
 * Reduz a quantidade de pontos do anel mantendo a forma geral. Evita OOM
 * em concelhos com geometrias muito densas e melhora a performance do
 * GoogleMap. O fator é adaptativo: rings curtos passam intactos, rings
 * gigantes são amostrados de forma agressiva.
 */
private fun simplifyRing(points: List<LatLng>): List<LatLng> {
    if (points.size <= MAX_POINTS_PER_RING) return points

    val step = (points.size + MAX_POINTS_PER_RING - 1) / MAX_POINTS_PER_RING
    if (step <= 1) return points

    val simplified = ArrayList<LatLng>(points.size / step + 2)
    var i = 0
    while (i < points.size) {
        simplified.add(points[i])
        i += step
    }

    // Garante que o anel fica fechado preservando o ponto final original.
    val last = points.last()
    if (simplified.last() != last) simplified.add(last)

    return simplified
}

/**
 * Limite de pontos por anel. 600 mantém a forma reconhecível mesmo dos
 * concelhos costeiros mais recortados (Lisboa, Porto, etc.) e acelera
 * bastante o parse + o render dos polígonos no GoogleMap, sem perda visual
 * notória ao zoom típico de país.
 */
private const val MAX_POINTS_PER_RING = 600
