package com.example.rentscope.data.remote

import com.example.rentscope.data.remote.dto.PaisDto
import com.example.rentscope.data.remote.dto.auth.LoginRequestDto
import com.example.rentscope.data.remote.dto.auth.RegisterRequestDto
import com.example.rentscope.data.remote.dto.auth.TokenResponseDto
import com.example.rentscope.data.remote.dto.auth.UserDto
import com.example.rentscope.data.remote.dto.history.FavoritoCreateDto
import com.example.rentscope.data.remote.dto.history.FiltroSalvoCreateDto
import com.example.rentscope.data.remote.dto.history.FiltroSalvoDto
import com.example.rentscope.data.remote.dto.score.ScoreFiltroRequestDto
import com.example.rentscope.data.remote.dto.score.ScoreMunicipioDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface RentScopeApi {

    @GET("paises")
    suspend fun listarPaises(): List<PaisDto>

    @POST("auth/register")
    suspend fun register(
        @Body body: RegisterRequestDto
    ): Response<UserDto>

    @POST("auth/login")
    suspend fun login(
        @Body body: LoginRequestDto
    ): Response<TokenResponseDto>

    @GET("auth/me")
    suspend fun me(): Response<UserDto>

    @POST("filtros/aplicar")
    suspend fun aplicarFiltros(
        @Body body: ScoreFiltroRequestDto
    ): Response<List<ScoreMunicipioDto>>

    @POST("historico/filtros")
    suspend fun salvarFiltro(
        @Body body: FiltroSalvoCreateDto
    ): Response<FiltroSalvoDto>

    @GET("historico/filtros")
    suspend fun listarFiltrosSalvos(): Response<List<FiltroSalvoDto>>

    @DELETE("historico/filtros/{filtroId}")
    suspend fun removerFiltroSalvo(
        @Path("filtroId") filtroId: String
    ): Response<Unit>

    @POST("historico/favoritos")
    suspend fun adicionarFavorito(
        @Body body: FavoritoCreateDto
    ): Response<Unit>

    @GET("historico/favoritos")
    suspend fun listarFavoritos(): Response<List<FiltroSalvoDto>>

    @DELETE("historico/favoritos/{filtroId}")
    suspend fun removerFavorito(
        @Path("filtroId") filtroId: String
    ): Response<Unit>
}