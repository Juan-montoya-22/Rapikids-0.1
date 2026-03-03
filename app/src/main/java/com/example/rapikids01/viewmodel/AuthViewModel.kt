package com.example.rapikids01.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rapikids01.UserRole
import com.example.rapikids01.data.auth.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// ── UI States ─────────────────────────────────────────────────────────────────

data class LoginUiState(
    val email: String        = "",
    val password: String     = "",
    val isLoading: Boolean   = false,
    val error: String?       = null,
    val loginSuccess: UserRole? = null   // null = no logueado todavía
)

data class RegisterPadreUiState(
    val nombre: String       = "",
    val telefono: String     = "",
    val email: String        = "",
    val password: String     = "",
    val confirmPassword: String = "",
    val isLoading: Boolean   = false,
    val error: String?       = null,
    val success: Boolean     = false
)

data class RegisterGuarderiaUiState(
    val nombreGuarderia: String = "",
    val direccion: String       = "",
    val nit: String             = "",
    val telefono: String        = "",
    val email: String           = "",
    val password: String        = "",
    val confirmPassword: String = "",
    val documentoUri: String    = "",   // URI local del archivo seleccionado
    val isLoading: Boolean      = false,
    val error: String?          = null,
    val success: Boolean        = false
)

// ── ViewModel ─────────────────────────────────────────────────────────────────

class AuthViewModel(
    private val repository: AuthRepository = AuthRepository()
) : ViewModel() {

    // ── Login State ───────────────────────────────────────────────────────

    private val _loginState = MutableStateFlow(LoginUiState())
    val loginState: StateFlow<LoginUiState> = _loginState.asStateFlow()

    fun onLoginEmailChange(value: String)    = _loginState.update { it.copy(email = value, error = null) }
    fun onLoginPasswordChange(value: String) = _loginState.update { it.copy(password = value, error = null) }

    fun login(expectedRole: UserRole) {
        val state = _loginState.value

        if (state.email.isBlank() || state.password.isBlank()) {
            _loginState.update { it.copy(error = "Por favor completa todos los campos") }
            return
        }

        viewModelScope.launch {
            _loginState.update { it.copy(isLoading = true, error = null) }

            val result = repository.login(state.email.trim(), state.password)

            result.fold(
                onSuccess = { role ->
                    if (role != expectedRole) {
                        _loginState.update {
                            it.copy(
                                isLoading = false,
                                error = "Este usuario no tiene el rol correcto para este acceso"
                            )
                        }
                    } else {
                        _loginState.update { it.copy(isLoading = false, loginSuccess = role) }
                    }
                },
                onFailure = { e ->
                    _loginState.update {
                        it.copy(isLoading = false, error = parseFirebaseError(e.message))
                    }
                }
            )
        }
    }

    fun resetLoginState() = _loginState.update { LoginUiState() }

    // ── Register Padre State ──────────────────────────────────────────────

    private val _registerPadreState = MutableStateFlow(RegisterPadreUiState())
    val registerPadreState: StateFlow<RegisterPadreUiState> = _registerPadreState.asStateFlow()

    fun onPadreNombreChange(v: String)           = _registerPadreState.update { it.copy(nombre = v, error = null) }
    fun onPadreTelefonoChange(v: String)         = _registerPadreState.update { it.copy(telefono = v, error = null) }
    fun onPadreEmailChange(v: String)            = _registerPadreState.update { it.copy(email = v, error = null) }
    fun onPadrePasswordChange(v: String)         = _registerPadreState.update { it.copy(password = v, error = null) }
    fun onPadreConfirmPasswordChange(v: String)  = _registerPadreState.update { it.copy(confirmPassword = v, error = null) }

    fun registerPadre() {
        val s = _registerPadreState.value

        val validationError = validatePadreFields(s)
        if (validationError != null) {
            _registerPadreState.update { it.copy(error = validationError) }
            return
        }

        viewModelScope.launch {
            _registerPadreState.update { it.copy(isLoading = true, error = null) }

            val result = repository.registerPadre(
                nombre   = s.nombre.trim(),
                telefono = s.telefono.trim(),
                email    = s.email.trim(),
                password = s.password
            )

            result.fold(
                onSuccess = { _registerPadreState.update { it.copy(isLoading = false, success = true) } },
                onFailure = { e ->
                    _registerPadreState.update {
                        it.copy(isLoading = false, error = parseFirebaseError(e.message))
                    }
                }
            )
        }
    }

    // ── Register Guardería State ──────────────────────────────────────────

    private val _registerGuarderiaState = MutableStateFlow(RegisterGuarderiaUiState())
    val registerGuarderiaState: StateFlow<RegisterGuarderiaUiState> = _registerGuarderiaState.asStateFlow()

    fun onGuarderiaNombreChange(v: String)          = _registerGuarderiaState.update { it.copy(nombreGuarderia = v, error = null) }
    fun onGuarderiaDireccionChange(v: String)        = _registerGuarderiaState.update { it.copy(direccion = v, error = null) }
    fun onGuarderiaNitChange(v: String)              = _registerGuarderiaState.update { it.copy(nit = v, error = null) }
    fun onGuarderiaTelefonoChange(v: String)         = _registerGuarderiaState.update { it.copy(telefono = v, error = null) }
    fun onGuarderiaEmailChange(v: String)            = _registerGuarderiaState.update { it.copy(email = v, error = null) }
    fun onGuarderiaPasswordChange(v: String)         = _registerGuarderiaState.update { it.copy(password = v, error = null) }
    fun onGuarderiaConfirmPasswordChange(v: String)  = _registerGuarderiaState.update { it.copy(confirmPassword = v, error = null) }
    fun onGuarderiaDocumentoUriChange(v: String)     = _registerGuarderiaState.update { it.copy(documentoUri = v, error = null) }

    fun registerGuarderia() {
        val s = _registerGuarderiaState.value

        val validationError = validateGuarderiaFields(s)
        if (validationError != null) {
            _registerGuarderiaState.update { it.copy(error = validationError) }
            return
        }

        viewModelScope.launch {
            _registerGuarderiaState.update { it.copy(isLoading = true, error = null) }

            val result = repository.registerGuarderia(
                nombreGuarderia = s.nombreGuarderia.trim(),
                direccion       = s.direccion.trim(),
                nit             = s.nit.trim(),
                telefono        = s.telefono.trim(),
                email           = s.email.trim(),
                password        = s.password,
                documentoUrl    = s.documentoUri
            )

            result.fold(
                onSuccess = { _registerGuarderiaState.update { it.copy(isLoading = false, success = true) } },
                onFailure = { e ->
                    _registerGuarderiaState.update {
                        it.copy(isLoading = false, error = parseFirebaseError(e.message))
                    }
                }
            )
        }
    }

    // ── Logout ────────────────────────────────────────────────────────────

    fun logout() {
        repository.logout()
        resetLoginState()
    }

    // ── Helpers ───────────────────────────────────────────────────────────

    private fun validatePadreFields(s: RegisterPadreUiState): String? {
        if (s.nombre.isBlank())    return "El nombre es obligatorio"
        if (s.telefono.isBlank())  return "El teléfono es obligatorio"
        if (s.email.isBlank())     return "El correo es obligatorio"
        if (!s.email.contains("@")) return "Ingresa un correo válido"
        if (s.password.length < 6) return "La contraseña debe tener al menos 6 caracteres"
        if (s.password != s.confirmPassword) return "Las contraseñas no coinciden"
        return null
    }

    private fun validateGuarderiaFields(s: RegisterGuarderiaUiState): String? {
        if (s.nombreGuarderia.isBlank()) return "El nombre de la guardería es obligatorio"
        if (s.direccion.isBlank())       return "La dirección es obligatoria"
        if (s.nit.isBlank())             return "El NIT es obligatorio"
        if (s.telefono.isBlank())        return "El teléfono es obligatorio"
        if (s.email.isBlank())           return "El correo es obligatorio"
        if (!s.email.contains("@"))      return "Ingresa un correo válido"
        if (s.password.length < 6)       return "La contraseña debe tener al menos 6 caracteres"
        if (s.password != s.confirmPassword) return "Las contraseñas no coinciden"
        return null
    }

    private fun parseFirebaseError(message: String?): String {
        return when {
            message == null                          -> "Error desconocido"
            "email address is already in use" in message -> "Este correo ya está registrado"
            "password is invalid" in message         -> "Contraseña incorrecta"
            "no user record" in message              -> "No existe una cuenta con este correo"
            "network error" in message               -> "Sin conexión a internet"
            "badly formatted" in message             -> "El correo no tiene un formato válido"
            else                                     -> message
        }
    }
}
