package com.example.rapikids01.data.auth

import android.content.Context
import android.net.Uri
import com.example.rapikids01.UserRole
import com.example.rapikids01.data.model.Admin
import com.example.rapikids01.data.model.Guarderia
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
            }

            val uid = client.auth.currentUserOrNull()?.id
                ?: return Result.failure(Exception("No se pudo obtener el UID"))

            client.postgrest["users"].insert(
                Usuario(uid = uid, email = email, nombre = nombre, telefono = telefono, role = "PADRE")
            )

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
        documentoUri: String = ""
    ): Result<Unit> {
        return try {
            client.auth.signUpWith(Email) {
                this.email    = email
                this.password = password
            }

            val uid = client.auth.currentUserOrNull()?.id
                ?: return Result.failure(Exception("No se pudo obtener el UID"))

            // ── Subir documento a Storage ─────────────────────────────
            var documentoUrl = ""
            if (documentoUri.isNotBlank()) {
                try {
                    val uri   = Uri.parse(documentoUri)
                    val bytes = context.contentResolver.openInputStream(uri)?.readBytes()
                    if (bytes != null) {
                        val ext      = context.contentResolver.getType(uri)?.substringAfter("/") ?: "pdf"
                        val filePath = "$uid/documento.$ext"
                        val bucket   = client.storage["guarderias"]
                        bucket.upload(filePath, bytes) { upsert = true }
                        documentoUrl = bucket.publicUrl(filePath)
                    }
                } catch (_: Exception) {}
            }

            // ── Geocodificar dirección al registrarse ─────────────────
            val (latitud, longitud) = geocodificarDireccionRegistro(direccion)

            // ── Insertar en users ─────────────────────────────────────
            client.postgrest["users"].insert(
                Usuario(uid = uid, email = email, nombre = nombreGuarderia, telefono = telefono, role = "GUARDERIA")
            )

            // ── Insertar en guarderias con coordenadas ────────────────
            val guarderiaJson = kotlinx.serialization.json.buildJsonObject {
                put("uid",              kotlinx.serialization.json.JsonPrimitive(uid.toString()))
                put("email",            kotlinx.serialization.json.JsonPrimitive(email))
                put("nombre_guarderia", kotlinx.serialization.json.JsonPrimitive(nombreGuarderia))
                put("direccion",        kotlinx.serialization.json.JsonPrimitive(direccion))
                put("nit",              kotlinx.serialization.json.JsonPrimitive(nit))
                put("telefono",         kotlinx.serialization.json.JsonPrimitive(telefono))
                put("documento_url",    kotlinx.serialization.json.JsonPrimitive(documentoUrl))
                put("verificada",       kotlinx.serialization.json.JsonPrimitive(false))
                put("estado",           kotlinx.serialization.json.JsonPrimitive("pendiente"))
                put("descripcion",      kotlinx.serialization.json.JsonPrimitive(""))
                put("precio_mensual",   kotlinx.serialization.json.JsonPrimitive(0))
                put("hora_apertura",    kotlinx.serialization.json.JsonPrimitive(""))
                put("hora_cierre",      kotlinx.serialization.json.JsonPrimitive(""))
                put("dias_atencion",    kotlinx.serialization.json.JsonPrimitive(""))
                put("jornada",          kotlinx.serialization.json.JsonPrimitive(""))
                if (latitud != null)  put("latitud",  kotlinx.serialization.json.JsonPrimitive(latitud))
                if (longitud != null) put("longitud", kotlinx.serialization.json.JsonPrimitive(longitud))
            }
            client.postgrest["guarderias"].insert(guarderiaJson)

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
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

    // ── Normalizar dirección colombiana ───────────────────────────────
    // Maneja casos como: "calle141#103f-37" → "Calle 141, Bogotá, Colombia"
    private fun normalizarDireccion(direccion: String): String {
        var dir = direccion.trim().lowercase()

        // 1. Expandir abreviaciones pegadas a número: "calle141" → "calle 141"
        dir = dir.replace(Regex("(?i)(calle|carrera|avenida|diagonal|transversal|cra|cll|cl|cr|av|dg|tv)(\\d)"), "$1 $2")

        // 2. Expandir abreviaciones colombianas
        dir = dir.replace(Regex("(?i)\\bcll?\\.?\\s+"), "Calle ")
        dir = dir.replace(Regex("(?i)\\bcra?\\.?\\s+"), "Carrera ")
        dir = dir.replace(Regex("(?i)\\bav\\.?\\s+|\\bavda\\.?\\s+"), "Avenida ")
        dir = dir.replace(Regex("(?i)\\bdg\\.?\\s+|\\bdiag\\.?\\s+"), "Diagonal ")
        dir = dir.replace(Regex("(?i)\\btv\\.?\\s+|\\btransv\\.?\\s+"), "Transversal ")

        // 3. Eliminar número de apartamento/local: "#103f-37" → ""
        dir = dir.replace(Regex("#[^,]+"), "").trim()

        // 4. Eliminar letra suelta al final de número: "141b" → "141"
        dir = dir.replace(Regex("(\\d+)[a-zA-Z]\\b"), "$1")

        // 5. Capitalizar primera letra de cada palabra clave
        dir = dir.replace(Regex("(?i)\\bcalle\\b"), "Calle")
        dir = dir.replace(Regex("(?i)\\bcarrera\\b"), "Carrera")
        dir = dir.replace(Regex("(?i)\\bavenida\\b"), "Avenida")
        dir = dir.replace(Regex("(?i)\\bdiagonal\\b"), "Diagonal")
        dir = dir.replace(Regex("(?i)\\btransversal\\b"), "Transversal")

        // 6. Limpiar caracteres extra
        dir = dir.replace(",", "").replace(Regex("\\s+"), " ").trim()

        return "$dir, Bogotá, Colombia"
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
            }

            val uid = client.auth.currentUserOrNull()?.id
                ?: return Result.failure(Exception("No se pudo obtener el UID"))

            client.postgrest["users"].insert(
                Usuario(uid = uid, email = email, nombre = nombre, telefono = "", role = "ADMIN")
            )

            client.postgrest["admins"].insert(
                Admin(uid = uid, email = email, nombre = nombre, cargo = cargo)
            )

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ── Logout ────────────────────────────────────────────────────────
    suspend fun logout() {
        try { client.auth.signOut() } catch (_: Exception) {}
    }
}
