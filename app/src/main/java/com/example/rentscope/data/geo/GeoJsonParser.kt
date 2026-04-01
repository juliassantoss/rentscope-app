package com.example.rentscope.data.geo

import com.google.android.gms.maps.model.LatLng
import org.json.JSONArray
import org.json.JSONObject

data class PolygonRings(
    val municipalityName: String?,
    val municipalityCode: String?,
    val outer: List<LatLng>,
    val holes: List<List<LatLng>> = emptyList()
)

fun extractPolygonsFromGeoJson(
    geoJson: String,
    limitFeatures: Int = Int.MAX_VALUE
): List<PolygonRings> {
    val root = JSONObject(geoJson)
    val features = root.optJSONArray("features") ?: return emptyList()

    val result = mutableListOf<PolygonRings>()

    for (i in 0 until features.length()) {
        if (result.size >= limitFeatures) break

        val feature = features.optJSONObject(i) ?: continue
        val geometry = feature.optJSONObject("geometry") ?: continue
        val properties = feature.optJSONObject("properties")

        val municipalityName = properties?.optString("Concelho")?.takeIf { it.isNotBlank() }
        val municipalityCode = properties?.optString("DICO")?.takeIf { it.isNotBlank() }

        parseGeometry(
            geometry = geometry,
            municipalityName = municipalityName,
            municipalityCode = municipalityCode,
            output = result,
            limitFeatures = limitFeatures
        )

        if (result.size >= limitFeatures) break
    }

    return result
}

private fun parseGeometry(
    geometry: JSONObject,
    municipalityName: String?,
    municipalityCode: String?,
    output: MutableList<PolygonRings>,
    limitFeatures: Int
) {
    when (geometry.optString("type")) {
        "Polygon" -> {
            val rings = geometry.optJSONArray("coordinates") ?: return
            parsePolygon(
                rings = rings,
                municipalityName = municipalityName,
                municipalityCode = municipalityCode,
                output = output
            )
        }

        "MultiPolygon" -> {
            val polygons = geometry.optJSONArray("coordinates") ?: return

            for (i in 0 until polygons.length()) {
                if (output.size >= limitFeatures) break

                val rings = polygons.optJSONArray(i) ?: continue
                parsePolygon(
                    rings = rings,
                    municipalityName = municipalityName,
                    municipalityCode = municipalityCode,
                    output = output
                )
            }
        }

        "GeometryCollection" -> {
            val geometries = geometry.optJSONArray("geometries") ?: return

            for (i in 0 until geometries.length()) {
                if (output.size >= limitFeatures) break

                val g = geometries.optJSONObject(i) ?: continue
                parseGeometry(
                    geometry = g,
                    municipalityName = municipalityName,
                    municipalityCode = municipalityCode,
                    output = output,
                    limitFeatures = limitFeatures
                )
            }
        }
    }
}

private fun parsePolygon(
    rings: JSONArray,
    municipalityName: String?,
    municipalityCode: String?,
    output: MutableList<PolygonRings>
) {
    if (rings.length() == 0) return

    val outer = parseRing(rings.optJSONArray(0) ?: return)
    if (outer.size < 3) return

    val holes = mutableListOf<List<LatLng>>()

    for (i in 1 until rings.length()) {
        val hole = parseRing(rings.optJSONArray(i) ?: continue)
        if (hole.size >= 3) {
            holes.add(hole)
        }
    }

    output.add(
        PolygonRings(
            municipalityName = municipalityName,
            municipalityCode = municipalityCode,
            outer = outer,
            holes = holes
        )
    )
}

private fun parseRing(ring: JSONArray): List<LatLng> {
    val points = mutableListOf<LatLng>()

    for (i in 0 until ring.length()) {
        val pt = ring.optJSONArray(i) ?: continue

        val lon = pt.optDouble(0, Double.NaN)
        val lat = pt.optDouble(1, Double.NaN)

        if (!lon.isNaN() && !lat.isNaN()) {
            points.add(LatLng(lat, lon))
        }
    }

    return points
}