package com.example.rapikids01.data.auth

import android.content.Context
import android.net.Uri
import com.example.rapikids01.UserRole
import com.example.rapikids01.data.model.Admin
import com.example.rapikids01.data.model.Usuario
import com.example.rapikids01.data.supabase.SupabaseClient.client
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.storage.storage
import io.github.jan.supabase.storage.upload
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.android.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

private const val ADMIN_SECRET_CODE = "RAPIKIDS-ADMIN-2026"

@Serializable
private data class NominatimResult(
    val lat: String = "",
    val lon: String = ""
)

class AuthRepository {

    val currentUser get() = client.auth.currentUserOrNull()
    val isLoggedIn  get() = client.auth.currentUserOrNull() != null

    private val httpClient = HttpClient(Android) {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }

    // ── Login ─────────────────────────────────────────────────────────
    suspend fun login(email: String, password: String): Result<UserRole> {
        return try {
            client.auth.signInWith(Email) {
                this.email    = email
                this.password = password
            }

            val uid = client.auth.currentUserOrNull()?.id
                ?: return Result.failure(Exception("No se pudo obtener el usuario"))

            val usuarios = client.postgrest["users"]
                .select(Columns.list("role")) {
                    filter { eq("uid", uid) }
                }
                .decodeList<Map<String, String>>()

            val roleStr = usuarios.firstOrNull()?.get("role")
                ?: return Result.failure(Exception("Usuario sin rol asignado"))

            val role = when (roleStr) {
                "PADRE"     -> UserRole.PADRE
                "GUARDERIA" -> UserRole.GUARDERIA
                "ADMIN"     -> UserRole.ADMIN
                else        -> return Result.failure(Exception("Rol inválido"))
            }

            Result.success(role)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ── Registro Padre ────────────────────────────────────────────────
    suspend fun registerPadre(
        nombre: String,
        telefono: String,
        email: String,
        password: String
    ): Result<Unit> {
        return try {
            client.auth.signUpWith(Email) {
                this.email    = email
                this.password = password
                data = buildJsonObject {
                    put("nombre",   nombre)
                    put("telefono", telefono)
                    put("role",     "PADRE")
                }
            }
            // El trigger de Supabase crea el registro en public.users
            // cuando el usuario confirma su correo
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ── Registro Guardería ────────────────────────────────────────────
    suspend fun registerGuarderia(
        context: Context,
        nombreGuarderia: String,
        direccion: String,
        nit: String,
        telefono: String,
        email: String,
        password: String,
        documentoUri: String = "",
        latitud: Double? = null,
        longitud: Double? = null
    ): Result<Unit> {
        return try {
            client.auth.signUpWith(Email) {
                this.email    = email
                this.password = password
                data = buildJsonObject {
                    put("nombre",    nombreGuarderia)
                    put("telefono",  telefono)
                    put("role",      "GUARDERIA")
                    put("nit",       nit)
                    put("direccion", direccion)
                    if (latitud != null)  put("latitud",  latitud)
                    if (longitud != null) put("longitud", longitud)
                }
            }
            val uid = client.auth.currentUserOrNull()?.id
            if (uid != null && documentoUri.isNotBlank()) {
                try {
                    val uri   = Uri.parse(documentoUri)
                    val bytes = context.contentResolver.openInputStream(uri)?.readBytes()
                    if (bytes != null) {
                        val ext      = context.contentResolver.getType(uri)?.substringAfter("/") ?: "pdf"
                        val filePath = "$uid/documento.$ext"
                        val bucket   = client.storage["guarderias"]
                        bucket.upload(filePath, bytes) { upsert = true }
                        val documentoUrl = bucket.publicUrl(filePath)
                        // Guardar URL del documento en metadata
                        client.auth.updateUser {
                            data = buildJsonObject {
                                put("documento_url", documentoUrl)
                            }
                        }
                    }
                } catch (_: Exception) {}
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ── Registro Admin ────────────────────────────────────────────────
    suspend fun registerAdmin(
        nombre: String,
        cargo: String,
        email: String,
        password: String,
        codigoSecreto: String
    ): Result<Unit> {
        if (codigoSecreto != ADMIN_SECRET_CODE) {
            return Result.failure(Exception("Código de administrador incorrecto"))
        }
        return try {
            client.auth.signUpWith(Email) {
                this.email    = email
                this.password = password
                data = buildJsonObject {
                    put("nombre",   nombre)
                    put("telefono", "")
                    put("role",     "ADMIN")
                    put("cargo",    cargo)
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ── Normalizar dirección colombiana ───────────────────────────────
    private fun normalizarDireccion(direccion: String): String {
        var dir = direccion.trim()
        dir = dir.replace(Regex("(?i)(calle|carrera|avenida|diagonal|transversal|cra|cll|cl|cr|av|dg|tv)(\\d)"), "$1 $2")
        dir = dir.replace(Regex("(?i)\\bcll?\\.?\\s+"), "Calle ")
        dir = dir.replace(Regex("(?i)\\bcra?\\.?\\s+"), "Carrera ")
        dir = dir.replace(Regex("(?i)\\bav\\.?\\s+|\\bavda\\.?\\s+"), "Avenida ")
        dir = dir.replace(Regex("(?i)\\bdg\\.?\\s+|\\bdiag\\.?\\s+"), "Diagonal ")
        dir = dir.replace(Regex("(?i)\\btv\\.?\\s+|\\btransv\\.?\\s+"), "Transversal ")
        dir = dir.replace(Regex("#\\s*(\\d+)[a-zA-Z]?-?\\d*"), " $1")
        dir = dir.replace(Regex("(\\d+)[a-zA-Z](?=\\s|$)"), "$1")
        dir = dir.replace(Regex("(?i)\\bcalle\\b"), "Calle")
        dir = dir.replace(Regex("(?i)\\bcarrera\\b"), "Carrera")
        dir = dir.replace(Regex("(?i)\\bavenida\\b"), "Avenida")
        dir = dir.replace(Regex("(?i)\\bdiagonal\\b"), "Diagonal")
        dir = dir.replace(Regex("(?i)\\btransversal\\b"), "Transversal")
        dir = dir.replace(",", "").replace(Regex("\\s+"), " ").trim()
        return "$dir, Bogotá, Colombia"
    }

    // ── Geocodificar dirección usando Nominatim ───────────────────────
    private suspend fun geocodificarDireccionRegistro(direccion: String): Pair<Double?, Double?> {
        return try {
            val query = normalizarDireccion(direccion)
            val response = httpClient.get("https://nominatim.openstreetmap.org/search") {
                parameter("q", query)
                parameter("format", "json")
                parameter("limit", "1")
                parameter("countrycodes", "co")
                header("User-Agent", "Rapikids/1.0 soporte@rapikids.com")
                header("Accept-Language", "es")
            }
            val results = response.body<List<NominatimResult>>()
            val first = results.firstOrNull() ?: return Pair(null, null)
            Pair(first.lat.toDouble(), first.lon.toDouble())
        } catch (_: Exception) {
            Pair(null, null)
        }
    }

    // ── Logout ────────────────────────────────────────────────────────
    suspend fun logout() {
        try { client.auth.signOut() } catch (_: Exception) {}
    }
}