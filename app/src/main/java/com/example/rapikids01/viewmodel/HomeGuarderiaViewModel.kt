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
    val descripcionEdit: String      = "",
    val precioMensualEdit: String    = "",
    val horaAperturaEdit: String     = "",
    val horaCierreEdit: String       = "",
    val diasAtencionEdit: String     = "",
    val jornadaEdit: String          = "",
    val isLoading: Boolean           = false,
    val isSaving: Boolean            = false,
    val isUploadingPhoto: Boolean    = false,
    val isUploadingDoc: Boolean      = false,
    val isUploadingCarrusel: Boolean = false,
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
                            isLoading         = false,
                            guarderia         = g,
                            nombreEdit        = g.nombreGuarderia,
                            direccionEdit     = g.direccion,
                            telefonoEdit      = g.telefono,
                            descripcionEdit   = g.descripcion,
                            precioMensualEdit = if (g.precioMensual > 0) g.precioMensual.toString() else "",
                            horaAperturaEdit  = g.horaApertura,
                            horaCierreEdit    = g.horaCierre,
                            diasAtencionEdit  = g.diasAtencion,
                            jornadaEdit       = g.jornada
                        )
                    }
                },
                onFailure = { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                }
            )
        }
    }

    fun toggleEditMode() = _uiState.update { state ->
        state.copy(
            isEditMode        = !state.isEditMode,
            nombreEdit        = state.guarderia?.nombreGuarderia ?: "",
            direccionEdit     = state.guarderia?.direccion ?: "",
            telefonoEdit      = state.guarderia?.telefono ?: "",
            descripcionEdit   = state.guarderia?.descripcion ?: "",
            precioMensualEdit = if ((state.guarderia?.precioMensual ?: 0) > 0) state.guarderia?.precioMensual.toString() else "",
            horaAperturaEdit  = state.guarderia?.horaApertura ?: "",
            horaCierreEdit    = state.guarderia?.horaCierre ?: "",
            diasAtencionEdit  = state.guarderia?.diasAtencion ?: "",
            jornadaEdit       = state.guarderia?.jornada ?: "",
            error             = null,
            successMsg        = null
        )
    }


    fun onNombreChange(v: String)        = _uiState.update { it.copy(nombreEdit = v) }
    fun onDireccionChange(v: String)     = _uiState.update { it.copy(direccionEdit = v) }
    fun onTelefonoChange(v: String)      = _uiState.update { it.copy(telefonoEdit = v) }
    fun onDescripcionChange(v: String)   = _uiState.update { it.copy(descripcionEdit = v) }
    fun onPrecioMensualChange(v: String) = _uiState.update { it.copy(precioMensualEdit = v) }
    fun onHoraAperturaChange(v: String)  = _uiState.update { it.copy(horaAperturaEdit = v) }
    fun onHoraCierreChange(v: String)    = _uiState.update { it.copy(horaCierreEdit = v) }
    fun onDiasAtencionChange(v: String)  = _uiState.update { it.copy(diasAtencionEdit = v) }
    fun onJornadaChange(v: String)       = _uiState.update { it.copy(jornadaEdit = v) }

    fun guardarCambios() {
        val s = _uiState.value
        if (s.nombreEdit.isBlank() || s.direccionEdit.isBlank() || s.telefonoEdit.isBlank()) {
            _uiState.update { it.copy(error = "Nombre, dirección y teléfono son obligatorios") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }
            repository.actualizarPerfil(
                nombreGuarderia = s.nombreEdit,
                direccion       = s.direccionEdit,
                telefono        = s.telefonoEdit,
                descripcion     = s.descripcionEdit,
                precioMensual   = s.precioMensualEdit.toIntOrNull() ?: 0,
                horaApertura    = s.horaAperturaEdit,
                horaCierre      = s.horaCierreEdit,
                diasAtencion    = s.diasAtencionEdit,
                jornada         = s.jornadaEdit
            ).fold(
                onSuccess = {
                    _uiState.update { state ->
                        state.copy(
                            isSaving   = false,
                            isEditMode = false,
                            successMsg = "Perfil actualizado correctamente",
                            guarderia  = state.guarderia?.copy(
                                nombreGuarderia = state.nombreEdit,
                                direccion       = state.direccionEdit,
                                telefono        = state.telefonoEdit,
                                descripcion     = state.descripcionEdit,
                                precioMensual   = state.precioMensualEdit.toIntOrNull() ?: 0,
                                horaApertura    = state.horaAperturaEdit,
                                horaCierre      = state.horaCierreEdit,
                                diasAtencion    = state.diasAtencionEdit,
                                jornada         = state.jornadaEdit
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

    fun subirFoto(context: Context, uri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(isUploadingPhoto = true, error = null) }
            repository.subirFoto(context, uri).fold(
                onSuccess = { url ->
                    _uiState.update { state ->
                        state.copy(isUploadingPhoto = false, successMsg = "Foto actualizada", guarderia = state.guarderia?.copy(fotoUrl = url))
                    }
                },
                onFailure = { e -> _uiState.update { it.copy(isUploadingPhoto = false, error = e.message) } }
            )
        }
    }

    fun subirFotoCarrusel(context: Context, uri: Uri, indice: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isUploadingCarrusel = true, error = null) }
            val fotosActuales = _uiState.value.guarderia?.fotos ?: emptyList()
            repository.subirFotoCarrusel(context, uri, fotosActuales, indice).fold(
                onSuccess = { nuevasFotos ->
                    _uiState.update { state ->
                        state.copy(
                            isUploadingCarrusel = false,
                            successMsg          = "Foto del carrusel actualizada",
                            guarderia           = state.guarderia?.copy(fotos = nuevasFotos.filter { it.isNotBlank() })
                        )
                    }
                },
                onFailure = { e -> _uiState.update { it.copy(isUploadingCarrusel = false, error = e.message) } }
            )
        }
    }

    fun eliminarFotoCarrusel(indice: Int) {
        viewModelScope.launch {
            val fotosActuales = _uiState.value.guarderia?.fotos ?: emptyList()
            repository.eliminarFotoCarrusel(fotosActuales, indice).fold(
                onSuccess = { nuevasFotos ->
                    _uiState.update { state ->
                        state.copy(
                            successMsg = "Foto eliminada",
                            guarderia  = state.guarderia?.copy(fotos = nuevasFotos.filter { it.isNotBlank() })
                        )
                    }
                },
                onFailure = { e -> _uiState.update { it.copy(error = e.message) } }
            )
        }
    }

    fun resubirDocumento(context: Context, uri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(isUploadingDoc = true, error = null) }
            repository.resubirDocumento(context, uri).fold(
                onSuccess = {
                    _uiState.update { state ->
                        state.copy(
                            isUploadingDoc = false,
                            successMsg     = "Documento enviado. Tu cuenta vuelve a revisión.",
                            guarderia      = state.guarderia?.copy(estado = "pendiente", mensajeRechazo = "")
                        )
                    }
                },
                onFailure = { e -> _uiState.update { it.copy(isUploadingDoc = false, error = e.message) } }
            )
        }
    }

    fun clearMessages() = _uiState.update { it.copy(error = null, successMsg = null) }
}
