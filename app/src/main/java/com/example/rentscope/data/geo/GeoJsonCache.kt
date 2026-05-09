package com.example.rentscope.data.geo

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

/**
 * Cache em memória do GeoJSON já parseado.
 *
 * O parse de [caop_portugal_continente_concelhos.json] é caro (centenas de
 * polígonos, milhares de pontos cada). Como o ficheiro é estático, fazê-lo
 * só uma vez por sessão da app evita esperas longas sempre que o utilizador
 * volta ao mapa.
 *
 * É thread-safe via [Mutex]: várias chamadas concorrentes durante o primeiro
 * load partilham o mesmo trabalho em vez de o duplicarem.
 */
object GeoJsonCache {

    private val mutex = Mutex()

    @Volatile
    private var cachedPolygons: List<PolygonRings>? = null

    /** Devolve a chave usada para o cache, útil em testes / debug. */
    fun isLoaded(): Boolean = cachedPolygons != null

    suspend fun loadPortugalConcelhos(context: Context): List<PolygonRings> {
        cachedPolygons?.let { return it }

        return mutex.withLock {
            cachedPolygons?.let { return@withLock it }

            val parsed = withContext(Dispatchers.IO) {
                openAssetReader(context, FILE_PORTUGAL_CONCELHOS).use { reader ->
                    extractPolygonsFromGeoJsonReader(reader)
                }
            }

            cachedPolygons = parsed
            parsed
        }
    }

    /**
     * Liberta o cache. Pode ser útil em respostas a low-memory callbacks. Em uso
     * normal não deves precisar de chamar isto — o cache vive enquanto a app vive.
     */
    fun clear() {
        cachedPolygons = null
    }

    private const val FILE_PORTUGAL_CONCELHOS = "caop_portugal_continente_concelhos.json"
}
