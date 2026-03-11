package com.example.rapikids01.data.repository

import android.content.Context
import android.net.Uri
import com.example.rapikids01.data.model.Guarderia
import com.example.rapikids01.data.supabase.SupabaseClient.client
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.storage.storage
import io.github.jan.supabase.storage.upload
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class GuarderiaProfileRepository {

    private val uid get() = client.auth.currentUserOrNull()?.id

    suspend fun cargarPerfil(): Result<Guarderia> {
        return try {
            val id = uid ?: return Result.failure(Exception("Usuario no autenticado"))

            // Traer todos y filtrar en memoria para evitar problemas de tipo UUID
            val lista = client.postgrest["guarderias"]
                .select()
                .decodeList<Guarderia>()

            val guarderia = lista.firstOrNull { it.uid == id.toString() }
                ?: return Result.failure(Exception("Perfil no encontrado"))

            Result.success(guarderia)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun actualizarPerfil(
        nombreGuarderia: String,
        direccion: String,
        telefono: String
    ): Result<Unit> {
        return try {
            val id = uid ?: return Result.failure(Exception("Usuario no autenticado"))

            client.postgrest["guarderias"].update(
                buildJsonObject {
                    put("nombre_guarderia", nombreGuarderia)
                    put("direccion", direccion)
                    put("telefono", telefono)
                }
            ) {
                filter { eq("uid", id.toString()) }
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun subirFoto(context: Context, uri: Uri): Result<String> {
        return try {
            val id = uid ?: return Result.failure(Exception("Usuario no autenticado"))

            val bytes = context.contentResolver
                .openInputStream(uri)?.readBytes()
                ?: return Result.failure(Exception("No se pudo leer el archivo"))

            val filePath = "$id/foto_perfil.jpg"
            val bucket  = client.storage["guarderias"]

            bucket.upload(filePath, bytes) { upsert = true }

            val publicUrl = bucket.publicUrl(filePath) + "?t=${System.currentTimeMillis()}"

            client.postgrest["guarderias"].update(
                buildJsonObject { put("foto_url", publicUrl) }
            ) {
                filter { eq("uid", id.toString()) }
            }

            Result.success(publicUrl)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
