package com.example.rentscope.data.geo

import android.content.Context
import java.io.BufferedReader
import java.io.InputStreamReader

/**
 * Mantida por compatibilidade. Carrega o conteúdo todo do asset em memória.
 *
 * **Atenção**: para ficheiros GeoJSON grandes prefere [openAssetReader] em
 * conjunto com [extractPolygonsFromGeoJsonReader], que faz parse em streaming.
 */
fun loadAssetText(context: Context, fileName: String): String {
    return context.assets.open(fileName).bufferedReader().use { it.readText() }
}

/**
 * Devolve uma função "loader" que abre o asset como um Reader. Cada chamada
 * abre um novo InputStream — assim evitamos manter o ficheiro inteiro em
 * memória e podemos passar diretamente para [extractPolygonsFromGeoJsonReader].
 *
 * O caller é responsável por fechar o Reader retornado.
 */
fun openAssetReader(context: Context, fileName: String): BufferedReader {
    val input = context.assets.open(fileName)
    return BufferedReader(InputStreamReader(input, Charsets.UTF_8))
}
