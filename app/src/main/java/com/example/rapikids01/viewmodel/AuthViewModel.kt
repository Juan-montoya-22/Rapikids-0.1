package com.example.rapikids01.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rapikids01.UserRole
import com.example.rapikids01.data.auth.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


data class LoginUiState(
    val email: String           = "",
    val password: String        = "",
    val isLoading: Boolean      = false,
    val error: String?          = null,
    val loginSuccess: UserRole? = null
)

data class RegisterPadreUiState(
    val nombre: String          = "",
    val telefono: String        = "",
    val email: String           = "",
    val password: String        = "",
    val confirmPassword: String = "",
    val isLoading: Boolean      = false,
    val error: String?          = null,
    val success: Boolean        = false
)

data class RegisterGuarderiaUiState(
    val nombreGuarderia: String = "",
    val direccion: String       = "",
    val nit: String             = "",
    val telefono: String        = "",
    val email: String           = "",
    val password: String        = "",
    val confirmPassword: String = "",
    val documentoUri: String    = "",
    val latitud: Double?        = null,
    val longitud: Double?       = null,
    val isLoading: Boolean      = false,
    val error: String?          = null,
    val success: Boolean        = false
)

data class RegisterAdminUiState(
    val nombre: String          = "",
    val cargo: String           = "",
    val email: String           = "",
    val password: String        = "",
    val confirmPassword: String = "",
    val codigoSecreto: String   = "",
    val isLoading: Boolean      = false,
    val error: String?          = null,
    val success: Boolean        = false
)

class AuthViewModel(
    private val repository: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _loginState = MutableStateFlow(LoginUiState())
    val loginState: StateFlow<LoginUiState> = _loginState.asStateFlow()

    fun onLoginEmailChange(v: String)    = _loginState.update { it.copy(email = v, error = null) }
    fun onLoginPasswordChange(v: String) = _loginState.update { it.copy(password = v, error = null) }

    fun login(expectedRole: UserRole) {
        val s = _loginState.value
        if (s.email.isBlank() || s.password.isBlank()) {
            _loginState.update { it.copy(error = "Completa todos los campos") }
            return
        }
        viewModelScope.launch {
            _loginState.update { it.copy(isLoading = true, error = null) }
            repository.login(s.email.trim(), s.password).fold(
                onSuccess = { role ->
                    if (role != expectedRole) {
                        _loginState.update { it.copy(isLoading = false, error = "Este usuario no tiene el rol correcto") }
                    } else {
                        _loginState.update { it.copy(isLoading = false, loginSuccess = role) }
                    }
                },
                onFailure = { e ->
                    _loginState.update { it.copy(isLoading = false, error = parseError(e.message)) }
                }
            )
        }
    }

    fun resetLoginState() = _loginState.update { LoginUiState() }
    fun resetPadreState() = _registerPadreState.update { RegisterPadreUiState() }
    private val _registerPadreState = MutableStateFlow(RegisterPadreUiState())
    val registerPadreState: StateFlow<RegisterPadreUiState> = _registerPadreState.asStateFlow()

    fun onPadreNombreChange(v: String)          = _registerPadreState.update { it.copy(nombre = v, error = null) }
    fun onPadreTelefonoChange(v: String)        = _registerPadreState.update { it.copy(telefono = v, error = null) }
    fun onPadreEmailChange(v: String)           = _registerPadreState.update { it.copy(email = v, error = null) }
    fun onPadrePasswordChange(v: String)        = _registerPadreState.update { it.copy(password = v, error = null) }
    fun onPadreConfirmPasswordChange(v: String) = _registerPadreState.update { it.copy(confirmPassword = v, error = null) }

    fun registerPadre() {
        val s = _registerPadreState.value
        validatePadre(s)?.let { _registerPadreState.update { st -> st.copy(error = it) }; return }
        viewModelScope.launch {
            _registerPadreState.update { it.copy(isLoading = true, error = null) }
            repository.registerPadre(s.nombre.trim(), s.telefono.trim(), s.email.trim(), s.password).fold(
                onSuccess = { _registerPadreState.update { it.copy(isLoading = false, success = true) } },
                onFailure = { e -> _registerPadreState.update { it.copy(isLoading = false, error = parseError(e.message)) } }
            )
        }
    }

    private val _registerGuarderiaState = MutableStateFlow(RegisterGuarderiaUiState())
    val registerGuarderiaState: StateFlow<RegisterGuarderiaUiState> = _registerGuarderiaState.asStateFlow()

    fun resetGuarderiaState() = _registerGuarderiaState.update { RegisterGuarderiaUiState() }
    fun onGuarderiaNombreChange(v: String)          = _registerGuarderiaState.update { it.copy(nombreGuarderia = v, error = null) }
    fun onGuarderiaDireccionChange(v: String)       = _registerGuarderiaState.update { it.copy(direccion = v, error = null) }
    fun onGuarderiaNitChange(v: String)             = _registerGuarderiaState.update { it.copy(nit = v, error = null) }
    fun onGuarderiaTelefonoChange(v: String)        = _registerGuarderiaState.update { it.copy(telefono = v, error = null) }
    fun onGuarderiaEmailChange(v: String)           = _registerGuarderiaState.update { it.copy(email = v, error = null) }
    fun onGuarderiaPasswordChange(v: String)        = _registerGuarderiaState.update { it.copy(password = v, error = null) }
    fun onGuarderiaConfirmPasswordChange(v: String) = _registerGuarderiaState.update { it.copy(confirmPassword = v, error = null) }
    fun onGuarderiaDocumentoUriChange(v: String)    = _registerGuarderiaState.update { it.copy(documentoUri = v, error = null) }
    fun onGuarderiaCoordenadaChange(lat: Double, lng: Double) =
        _registerGuarderiaState.update { it.copy(latitud = lat, longitud = lng, error = null) }

    fun registerGuarderia(context: Context) {
        val s = _registerGuarderiaState.value
        validateGuarderia(s)?.let { _registerGuarderiaState.update { st -> st.copy(error = it) }; return }
        viewModelScope.launch {
            _registerGuarderiaState.update { it.copy(isLoading = true, error = null) }
            repository.registerGuarderia(
                context         = context,
                nombreGuarderia = s.nombreGuarderia.trim(),
                direccion       = s.direccion.trim(),
                nit             = s.nit.trim(),
                telefono        = s.telefono.trim(),
                email           = s.email.trim(),
                password        = s.password,
                documentoUri    = s.documentoUri,
                latitud         = s.latitud,
                longitud        = s.longitud
            ).fold(
                onSuccess = { _registerGuarderiaState.update { it.copy(isLoading = false, success = true) } },
                onFailure = { e -> _registerGuarderiaState.update { it.copy(isLoading = false, error = parseError(e.message)) } }
            )
        }
    }

    private val _registerAdminState = MutableStateFlow(RegisterAdminUiState())
    val registerAdminState: StateFlow<RegisterAdminUiState> = _registerAdminState.asStateFlow()

    fun onAdminNombreChange(v: String)          = _registerAdminState.update { it.copy(nombre = v, error = null) }
    fun onAdminCargoChange(v: String)           = _registerAdminState.update { it.copy(cargo = v, error = null) }
    fun onAdminEmailChange(v: String)           = _registerAdminState.update { it.copy(email = v, error = null) }
    fun onAdminPasswordChange(v: String)        = _registerAdminState.update { it.copy(password = v, error = null) }
    fun onAdminConfirmPasswordChange(v: String) = _registerAdminState.update { it.copy(confirmPassword = v, error = null) }
    fun onAdminCodigoChange(v: String)          = _registerAdminState.update { it.copy(codigoSecreto = v, error = null) }

    fun registerAdmin() {
        val s = _registerAdminState.value
        when {
            s.nombre.isBlank()              -> { _registerAdminState.update { it.copy(error = "El nombre es obligatorio") }; return }
            s.cargo.isBlank()               -> { _registerAdminState.update { it.copy(error = "El cargo es obligatorio") }; return }
            s.email.isBlank()               -> { _registerAdminState.update { it.copy(error = "El correo es obligatorio") }; return }
            !s.email.contains("@")          -> { _registerAdminState.update { it.copy(error = "Correo inválido") }; return }
            s.password.length < 6           -> { _registerAdminState.update { it.copy(error = "Mínimo 6 caracteres") }; return }
            s.password != s.confirmPassword -> { _registerAdminState.update { it.copy(error = "Las contraseñas no coinciden") }; return }
            s.codigoSecreto.isBlank()       -> { _registerAdminState.update { it.copy(error = "El código es obligatorio") }; return }
        }
        viewModelScope.launch {
            _registerAdminState.update { it.copy(isLoading = true, error = null) }
            repository.registerAdmin(
                s.nombre.trim(), s.cargo.trim(), s.email.trim(), s.password, s.codigoSecreto.trim()
            ).fold(
                onSuccess = { _registerAdminState.update { it.copy(isLoading = false, success = true) } },
                onFailure = { e -> _registerAdminState.update { it.copy(isLoading = false, error = parseError(e.message)) } }
            )
        }
    }

    suspend fun logout() {
        repository.logout()
        resetLoginState()
    }

    private fun validatePadre(s: RegisterPadreUiState): String? {
        if (s.nombre.isBlank())              return "El nombre es obligatorio"
        if (s.telefono.isBlank())            return "El teléfono es obligatorio"
        if (s.email.isBlank())               return "El correo es obligatorio"
        if (!s.email.contains("@"))          return "Correo inválido"
        if (s.password.length < 6)           return "Mínimo 6 caracteres en la contraseña"
        if (s.password != s.confirmPassword) return "Las contraseñas no coinciden"
        return null
    }

    private fun validateGuarderia(s: RegisterGuarderiaUiState): String? {
        if (s.nombreGuarderia.isBlank())     return "El nombre es obligatorio"
        if (s.direccion.isBlank())           return "La dirección es obligatoria"
        if (s.latitud == null)               return "Debes marcar la ubicación en el mapa"
        if (s.nit.isBlank())                 return "El NIT es obligatorio"
        if (s.telefono.isBlank())            return "El teléfono es obligatorio"
        if (s.email.isBlank())               return "El correo es obligatorio"
        if (!s.email.contains("@"))          return "Correo inválido"
        if (s.password.length < 6)           return "Mínimo 6 caracteres en la contraseña"
        if (s.password != s.confirmPassword) return "Las contraseñas no coinciden"
        return null
    }

    private fun parseError(msg: String?): String = when {
        msg == null                         -> "Error desconocido"
        "already registered" in msg        -> "Este correo ya está registrado"
        "Invalid login credentials" in msg -> "Correo o contraseña incorrectos"
        "network" in msg.lowercase()       -> "Sin conexión a internet"
        else                               -> msg
    }
}