package com.example.rapikids01.data.repository

import android.content.Context
import android.net.Uri
import com.example.rapikids01.data.model.Guarderia
import com.example.rapikids01.data.supabase.SupabaseClient.client
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.storage.storage
import io.github.jan.supabase.storage.upload
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class GuarderiaProfileRepository {

    private var cachedUid: String? = null

    private fun getUid(): String? {
        if (cachedUid != null) return cachedUid
        val uid = client.auth.currentUserOrNull()?.id?.toString()
        cachedUid = uid
        return uid
    }

    suspend fun cargarPerfil(): Result<Guarderia> {
        return try {
            val id = client.auth.currentUserOrNull()?.id?.toString()
                ?: return Result.failure(Exception("Usuario no autenticado"))
            cachedUid = id

            val lista = client.postgrest["guarderias"]
                .select {
                    filter { eq("uid", id) }
                }
                .decodeList<Guarderia>()

            val guarderia = lista.firstOrNull()
                ?: return Result.failure(Exception("Perfil no encontrado"))

            Result.success(guarderia)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    suspend fun actualizarPerfil(
        nombreGuarderia: String,
        direccion: String,
        telefono: String,
        descripcion: String,
        precioMensual: Int,
        horaApertura: String,
        horaCierre: String,
        diasAtencion: String,
        jornada: String
    ): Result<Unit> {
        return try {
            val id = getUid() ?: return Result.failure(Exception("Sesión expirada"))

            client.postgrest["guarderias"].update(
                buildJsonObject {
                    put("nombre_guarderia", nombreGuarderia)
                    put("direccion",        direccion)
                    put("telefono",         telefono)
                    put("descripcion",      descripcion)
                    put("precio_mensual",   precioMensual)
                    put("hora_apertura",    horaApertura)
                    put("hora_cierre",      horaCierre)
                    put("dias_atencion",    diasAtencion)
                    put("jornada",          jornada)
                }
            ) {
                filter { eq("uid", id) }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    suspend fun subirFoto(context: Context, uri: Uri): Result<String> {
        return try {
            val id = getUid() ?: return Result.failure(Exception("Sesión expirada"))

            val bytes = context.contentResolver.openInputStream(uri)?.readBytes()
                ?: return Result.failure(Exception("No se pudo leer el archivo"))

            val filePath  = "$id/foto_perfil.jpg"
            val bucket    = client.storage["guarderias"]
            bucket.upload(filePath, bytes) { upsert = true }
            val publicUrl = bucket.publicUrl(filePath) + "?t=${System.currentTimeMillis()}"

            client.postgrest["guarderias"].update(
                buildJsonObject { put("foto_url", publicUrl) }
            ) {
                filter { eq("uid", id) }
            }

            Result.success(publicUrl)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    suspend fun subirFotoCarrusel(
        context: Context,
        uri: Uri,
        fotosActuales: List<String>,
        indice: Int          // 0, 1 o 2
    ): Result<List<String>> {
        return try {
            val id = getUid() ?: return Result.failure(Exception("Sesión expirada"))

            val bytes = context.contentResolver.openInputStream(uri)?.readBytes()
                ?: return Result.failure(Exception("No se pudo leer el archivo"))

            val filePath  = "$id/carrusel_$indice.jpg"
            val bucket    = client.storage["guarderias"]
            bucket.upload(filePath, bytes) { upsert = true }
            val publicUrl = bucket.publicUrl(filePath) + "?t=${System.currentTimeMillis()}"
            val nuevasFotos = fotosActuales.toMutableList()
            while (nuevasFotos.size <= indice) nuevasFotos.add("")
            nuevasFotos[indice] = publicUrl

            val fotasNoVacias = nuevasFotos.filter { it.isNotBlank() }

            client.postgrest["guarderias"].update(
                buildJsonObject {
                    put("fotos", buildJsonArray {
                        fotasNoVacias.forEach { add(JsonPrimitive(it)) }
                    })
                }
            ) {
                filter { eq("uid", id) }
            }

            Result.success(nuevasFotos)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun eliminarFotoCarrusel(
        fotosActuales: List<String>,
        indice: Int
    ): Result<List<String>> {
        return try {
            val id = getUid() ?: return Result.failure(Exception("Sesión expirada"))

            val nuevasFotos = fotosActuales.toMutableList()
            if (indice < nuevasFotos.size) nuevasFotos[indice] = ""

            val fotasNoVacias = nuevasFotos.filter { it.isNotBlank() }

            client.postgrest["guarderias"].update(
                buildJsonObject {
                    put("fotos", buildJsonArray {
                        fotasNoVacias.forEach { add(JsonPrimitive(it)) }
                    })
                }
            ) {
                filter { eq("uid", id) }
            }

            Result.success(nuevasFotos)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    suspend fun resubirDocumento(context: Context, uri: Uri): Result<Unit> {
        return try {
            val id = getUid() ?: return Result.failure(Exception("Sesión expirada"))

            val bytes = context.contentResolver.openInputStream(uri)?.readBytes()
                ?: return Result.failure(Exception("No se pudo leer el archivo"))

            val ext      = context.contentResolver.getType(uri)?.substringAfter("/") ?: "pdf"
            val filePath = "$id/documento.$ext"
            val bucket   = client.storage["guarderias"]
            bucket.upload(filePath, bytes) { upsert = true }
            val documentoUrl = bucket.publicUrl(filePath)

            client.postgrest["guarderias"].update(
                buildJsonObject {
                    put("documento_url",   documentoUrl)
                    put("estado",          "pendiente")
                    put("verificada",      false)
                    put("mensaje_rechazo", "")
                }
            ) {
                filter { eq("uid", id) }
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
