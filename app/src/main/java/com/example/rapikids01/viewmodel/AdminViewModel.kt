package com.example.rapikids01.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rapikids01.data.model.Guarderia
import com.example.rapikids01.data.repository.AdminRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class AdminTab { PENDIENTES, TODAS }

data class AdminUiState(
    val pendientes: List<Guarderia>  = emptyList(),
    val todas: List<Guarderia>       = emptyList(),
    val tabSeleccionada: AdminTab    = AdminTab.PENDIENTES,
    val isLoading: Boolean           = false,
    val successMsg: String?          = null,
    val error: String?               = null
)

class AdminViewModel(
    private val repository: AdminRepository = AdminRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminUiState())
    val uiState: StateFlow<AdminUiState> = _uiState.asStateFlow()

    init { cargarDatos() }

    fun cargarDatos() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val pendientesResult = repository.obtenerPendientes()
                val todasResult      = repository.obtenerTodasGuarderias()

                _uiState.update {
                    it.copy(
                        isLoading  = false,
                        pendientes = pendientesResult.getOrElse { emptyList() },
                        todas      = todasResult.getOrElse { emptyList() }
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun cambiarTab(tab: AdminTab) = _uiState.update { it.copy(tabSeleccionada = tab) }

    fun aprobarGuarderia(uid: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            repository.aprobarGuarderia(uid).fold(
                onSuccess = {
                    _uiState.update { it.copy(successMsg = "Guardería aprobada correctamente") }
                    cargarDatos()
                },
                onFailure = { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                }
            )
        }
    }

    fun rechazarGuarderia(uid: String, mensaje: String) {
        if (mensaje.isBlank()) {
            _uiState.update { it.copy(error = "Debes escribir un motivo de rechazo") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            repository.rechazarGuarderia(uid, mensaje).fold(
                onSuccess = {
                    _uiState.update { it.copy(successMsg = "Guardería rechazada") }
                    cargarDatos()
                },
                onFailure = { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                }
            )
        }
    }

    fun clearMessages() = _uiState.update { it.copy(error = null, successMsg = null) }
}

