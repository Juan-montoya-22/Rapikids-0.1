package com.example.rapikids01.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rapikids01.data.location.LatLng
import com.example.rapikids01.data.location.LocationService
import com.example.rapikids01.data.model.Guarderia
import com.example.rapikids01.data.repository.GuarderiaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.example.rapikids01.data.supabase.SupabaseClient.client
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns

data class GuarderiaConDistancia(
    val guarderia: Guarderia,
    val distanciaKm: Double? = null
)

data class HomePadreUiState(
    val todasGuarderias: List<GuarderiaConDistancia>     = emptyList(),
    val guarderiasFiltradas: List<GuarderiaConDistancia> = emptyList(),
    val searchQuery: String         = "",
    val isLoading: Boolean          = false,
    val isLoadingUbicacion: Boolean = false,
    val error: String?              = null,
    val ubicacionActual: LatLng?    = null,
    val filtroCercanas: Boolean     = false,
    val permisoDenegado: Boolean    = false,
    val nombrePadre: String         = "",
    val mostrarMapa: Boolean = false
)

class HomePadreViewModel(
    private val repository: GuarderiaRepository = GuarderiaRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomePadreUiState())
    val uiState: StateFlow<HomePadreUiState> = _uiState.asStateFlow()

    private var locationService: LocationService? = null

    init {
        cargarGuarderias()
        cargarNombrePadre()
    }

    private fun cargarNombrePadre() {
        viewModelScope.launch {
            try {
                val uid = client.auth.currentUserOrNull()?.id ?: return@launch
                val resultado = client.postgrest["users"]
                    .select(Columns.list("nombre")) {
                        filter { eq("uid", uid) }
                    }
                    .decodeList<Map<String, String>>()
                val nombre = resultado.firstOrNull()?.get("nombre") ?: ""
                _uiState.update { it.copy(nombrePadre = nombre) }
            } catch (_: Exception) {}
        }
    }

    fun toggleMapa() = _uiState.update { it.copy(mostrarMapa = !it.mostrarMapa) }
    fun inicializarLocation(context: Context) {
        if (locationService == null) locationService = LocationService(context)
    }

    fun cargarGuarderias() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            repository.obtenerGuarderias().fold(
                onSuccess = { lista ->
                    val conDistancia = lista.map { GuarderiaConDistancia(it) }
                    _uiState.update {
                        it.copy(
                            isLoading           = false,
                            todasGuarderias     = conDistancia,
                            guarderiasFiltradas = conDistancia
                        )
                    }
                    _uiState.value.ubicacionActual?.let { calcularDistanciasDesdeSupabase(it) }
                },
                onFailure = { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                }
            )
        }
    }

    fun obtenerUbicacion(context: Context) {
        inicializarLocation(context)
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingUbicacion = true, error = null) }
            locationService!!.obtenerUbicacionActual().fold(
                onSuccess = { latLng ->
                    _uiState.update { it.copy(ubicacionActual = latLng, isLoadingUbicacion = false) }
                    calcularDistanciasDesdeSupabase(latLng)
                },
                onFailure = { e ->
                    _uiState.update { it.copy(isLoadingUbicacion = false, error = "No se pudo obtener la ubicación: ${e.message}") }
                }
            )
        }
    }

    fun onPermisoDenegado() = _uiState.update { it.copy(permisoDenegado = true, isLoadingUbicacion = false) }

    private fun calcularDistanciasDesdeSupabase(ubicacionPadre: LatLng) {
        val service = locationService ?: return

        val conDistancia = _uiState.value.todasGuarderias.map { item ->
            val lat = item.guarderia.latitud
            val lng = item.guarderia.longitud
            val distancia = if (lat != null && lng != null) {
                service.calcularDistanciaKm(ubicacionPadre, LatLng(lat, lng))
            } else null
            item.copy(distanciaKm = distancia)
        }

        _uiState.update { state ->
            state.copy(
                todasGuarderias     = conDistancia,
                guarderiasFiltradas = aplicarFiltros(conDistancia, state.searchQuery, state.filtroCercanas)
            )
        }
    }

    fun toggleFiltroCercanas() {
        _uiState.update { state ->
            val nuevoFiltro = !state.filtroCercanas
            state.copy(
                filtroCercanas      = nuevoFiltro,
                guarderiasFiltradas = aplicarFiltros(state.todasGuarderias, state.searchQuery, nuevoFiltro)
            )
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { state ->
            state.copy(
                searchQuery         = query,
                guarderiasFiltradas = aplicarFiltros(state.todasGuarderias, query, state.filtroCercanas)
            )
        }
    }

    fun limpiarBusqueda() {
        _uiState.update { state ->
            state.copy(
                searchQuery         = "",
                guarderiasFiltradas = aplicarFiltros(state.todasGuarderias, "", state.filtroCercanas)
            )
        }
    }

    private fun aplicarFiltros(
        lista: List<GuarderiaConDistancia>,
        query: String,
        soloCercanas: Boolean
    ): List<GuarderiaConDistancia> {
        var resultado = lista
        if (query.isNotBlank()) {
            val q = query.trim().lowercase()
            resultado = resultado.filter {
                it.guarderia.nombreGuarderia.lowercase().contains(q) ||
                it.guarderia.direccion.lowercase().contains(q)
            }
        }
        if (soloCercanas) {
            resultado = resultado.filter { it.distanciaKm != null && it.distanciaKm <= 3.0 }
        }
        resultado = resultado.sortedWith(compareBy { it.distanciaKm ?: Double.MAX_VALUE })
        return resultado
    }
}

