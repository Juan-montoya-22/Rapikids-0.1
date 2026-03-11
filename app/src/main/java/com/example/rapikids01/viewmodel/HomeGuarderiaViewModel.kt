package com.example.rapikids01.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rapikids01.data.model.Guarderia
import com.example.rapikids01.data.repository.GuarderiaProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomeGuarderiaUiState(
    val guarderia: Guarderia?        = null,
    val nombreEdit: String           = "",
    val direccionEdit: String        = "",
    val telefonoEdit: String         = "",
    val isLoading: Boolean           = false,
    val isSaving: Boolean            = false,
    val isUploadingPhoto: Boolean    = false,
    val isEditMode: Boolean          = false,
    val error: String?               = null,
    val successMsg: String?          = null
)

class HomeGuarderiaViewModel(
    private val repository: GuarderiaProfileRepository = GuarderiaProfileRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeGuarderiaUiState())
    val uiState: StateFlow<HomeGuarderiaUiState> = _uiState.asStateFlow()

    init { cargarPerfil() }

    fun cargarPerfil() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            repository.cargarPerfil().fold(
                onSuccess = { g ->
                    _uiState.update {
                        it.copy(
                            isLoading     = false,
                            guarderia     = g,
                            nombreEdit    = g.nombreGuarderia,
                            direccionEdit = g.direccion,
                            telefonoEdit  = g.telefono
                        )
                    }
                },
                onFailure = { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                }
            )
        }
    }

    fun toggleEditMode() = _uiState.update {
        it.copy(
            isEditMode    = !it.isEditMode,
            nombreEdit    = it.guarderia?.nombreGuarderia ?: "",
            direccionEdit = it.guarderia?.direccion ?: "",
            telefonoEdit  = it.guarderia?.telefono ?: "",
            error         = null,
            successMsg    = null
        )
    }

    fun onNombreChange(v: String)    = _uiState.update { it.copy(nombreEdit = v) }
    fun onDireccionChange(v: String) = _uiState.update { it.copy(direccionEdit = v) }
    fun onTelefonoChange(v: String)  = _uiState.update { it.copy(telefonoEdit = v) }

    fun guardarCambios() {
        val s = _uiState.value
        if (s.nombreEdit.isBlank() || s.direccionEdit.isBlank() || s.telefonoEdit.isBlank()) {
            _uiState.update { it.copy(error = "Todos los campos son obligatorios") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }
            repository.actualizarPerfil(s.nombreEdit, s.direccionEdit, s.telefonoEdit).fold(
                onSuccess = {
                    _uiState.update { state ->
                        state.copy(
                            isSaving   = false,
                            isEditMode = false,
                            successMsg = "Perfil actualizado correctamente",
                            guarderia  = state.guarderia?.copy(
                                nombreGuarderia = state.nombreEdit,
                                direccion       = state.direccionEdit,
                                telefono        = state.telefonoEdit
                            )
                        )
                    }
                },
                onFailure = { e ->
                    _uiState.update { it.copy(isSaving = false, error = e.message) }
                }
            )
        }
    }

    // Ahora recibe Context para leer el archivo con ContentResolver
    fun subirFoto(context: Context, uri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(isUploadingPhoto = true, error = null) }
            repository.subirFoto(context, uri).fold(
                onSuccess = { url ->
                    _uiState.update { state ->
                        state.copy(
                            isUploadingPhoto = false,
                            successMsg       = "Foto actualizada",
                            guarderia        = state.guarderia?.copy(fotoUrl = url)
                        )
                    }
                },
                onFailure = { e ->
                    _uiState.update { it.copy(isUploadingPhoto = false, error = e.message) }
                }
            )
        }
    }

    fun clearMessages() = _uiState.update { it.copy(error = null, successMsg = null) }
}
