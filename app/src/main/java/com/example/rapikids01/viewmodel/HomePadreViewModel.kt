package com.example.rapikids01.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rapikids01.data.model.Guarderia
import com.example.rapikids01.data.repository.GuarderiaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomePadreUiState(
    val todasGuarderias: List<Guarderia> = emptyList(),   // lista completa sin filtrar
    val guarderiasFiltradas: List<Guarderia> = emptyList(), // lista que se muestra
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)

class HomePadreViewModel(
    private val repository: GuarderiaRepository = GuarderiaRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomePadreUiState())
    val uiState: StateFlow<HomePadreUiState> = _uiState.asStateFlow()

    init {
        cargarGuarderias()
    }

    fun cargarGuarderias() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val result = repository.obtenerGuarderias()

            result.fold(
                onSuccess = { lista ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            todasGuarderias = lista,
                            guarderiasFiltradas = lista
                        )
                    }
                },
                onFailure = { e ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "No se pudo cargar las guarderías: ${e.message}"
                        )
                    }
                }
            )
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { state ->
            val filtradas = repository.filtrarGuarderias(state.todasGuarderias, query)
            state.copy(searchQuery = query, guarderiasFiltradas = filtradas)
        }
    }

    fun limpiarBusqueda() {
        _uiState.update { state ->
            state.copy(
                searchQuery = "",
                guarderiasFiltradas = state.todasGuarderias
            )
        }
    }
}
