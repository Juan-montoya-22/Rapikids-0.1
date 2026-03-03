package com.example.rapikids01.data.auth

import com.example.rapikids01.UserRole
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

/**
 * AuthRepository
 * Única fuente de verdad para todas las operaciones de autenticación.
 * Usa coroutines (suspend) en lugar de callbacks → más limpio con ViewModel.
 */
class AuthRepository {

    private val auth = FirebaseAuth.getInstance()
    private val db   = FirebaseFirestore.getInstance()

    // ── Estado de sesión ──────────────────────────────────────────────────

    val currentUser get() = auth.currentUser

    val isLoggedIn get() = auth.currentUser != null

    // ── Login ─────────────────────────────────────────────────────────────

    suspend fun login(email: String, password: String): Result<UserRole> {
        return try {
            auth.signInWithEmailAndPassword(email, password).await()

            val uid = auth.currentUser?.uid
                ?: return Result.failure(Exception("No se pudo obtener el usuario"))

            val doc = db.collection("users").document(uid).get().await()

            if (!doc.exists())
                return Result.failure(Exception("El usuario no tiene datos registrados"))

            val role = when (doc.getString("role")) {
                "PADRE"     -> UserRole.PADRE
                "GUARDERIA" -> UserRole.GUARDERIA
                else        -> return Result.failure(Exception("Rol inválido o no definido"))
            }

            Result.success(role)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ── Registro Padre ────────────────────────────────────────────────────

    suspend fun registerPadre(
        nombre: String,
        telefono: String,
        email: String,
        password: String
    ): Result<Unit> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val uid = result.user?.uid
                ?: return Result.failure(Exception("No se pudo obtener el UID"))

            val userData = hashMapOf<String, Any>(
                "uid"       to uid,
                "email"     to email,
                "nombre"    to nombre,
                "telefono"  to telefono,
                "role"      to "PADRE",
                "createdAt" to System.currentTimeMillis()
            )

            db.collection("users").document(uid).set(userData).await()

            Result.success(Unit)

        } catch (e: Exception) {
            // Si Firestore falla después de crear Auth, limpiamos el usuario
            auth.currentUser?.delete()?.await()
            Result.failure(e)
        }
    }

    // ── Registro Guardería ────────────────────────────────────────────────

    suspend fun registerGuarderia(
        nombreGuarderia: String,
        direccion: String,
        nit: String,
        telefono: String,
        email: String,
        password: String,
        documentoUrl: String = ""   // Se llenará después de subir archivo a Storage
    ): Result<Unit> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val uid = result.user?.uid
                ?: return Result.failure(Exception("No se pudo obtener el UID"))

            val userData = hashMapOf<String, Any>(
                "uid"            to uid,
                "email"          to email,
                "nombreGuarderia" to nombreGuarderia,
                "direccion"      to direccion,
                "nit"            to nit,
                "telefono"       to telefono,
                "role"           to "GUARDERIA",
                "documentoUrl"   to documentoUrl,
                "verificada"     to false,           // Admin revisa documentos
                "createdAt"      to System.currentTimeMillis()
            )

            // Guardamos en colección separada "guarderias" + referencia en "users"
            db.collection("users").document(uid).set(userData).await()
            db.collection("guarderias").document(uid).set(userData).await()

            Result.success(Unit)

        } catch (e: Exception) {
            auth.currentUser?.delete()?.await()
            Result.failure(e)
        }
    }

    // ── Logout ────────────────────────────────────────────────────────────

    fun logout() = auth.signOut()
}