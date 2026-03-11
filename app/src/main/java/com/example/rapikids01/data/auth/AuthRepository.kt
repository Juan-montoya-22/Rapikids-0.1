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

private const val ADMIN_SECRET_CODE = "RAPIKIDS-ADMIN-2026"

class AuthRepository {

    val currentUser get() = client.auth.currentUserOrNull()
    val isLoggedIn  get() = client.auth.currentUserOrNull() != null

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
    // Ahora recibe Context y Uri para subir el documento a Storage
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

            // ── Subir documento a Supabase Storage ────────────────────
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
                } catch (e: Exception) {
                    // Si falla la subida del doc, continuamos sin él
                    documentoUrl = ""
                }
            }

            // ── Insertar en users ─────────────────────────────────────
            client.postgrest["users"].insert(
                Usuario(
                    uid      = uid,
                    email    = email,
                    nombre   = nombreGuarderia,
                    telefono = telefono,
                    role     = "GUARDERIA"
                )
            )

            // ── Insertar en guarderias ────────────────────────────────
            client.postgrest["guarderias"].insert(
                Guarderia(
                    uid             = uid,
                    email           = email,
                    nombreGuarderia = nombreGuarderia,
                    direccion       = direccion,
                    nit             = nit,
                    telefono        = telefono,
                    documentoUrl    = documentoUrl,
                    verificada      = false,
                    estado          = "pendiente"
                )
            )

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
