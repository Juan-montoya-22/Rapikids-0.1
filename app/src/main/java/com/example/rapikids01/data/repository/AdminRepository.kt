package com.example.rapikids01.data.repository

import com.example.rapikids01.data.model.Guarderia
import com.example.rapikids01.data.supabase.SupabaseClient.client
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class AdminRepository {


    suspend fun obtenerTodasGuarderias(): Result<List<Guarderia>> {
        return try {
            val lista = client.postgrest["guarderias"]
                .select { order("created_at", Order.DESCENDING) }
                .decodeList<Guarderia>()
            Result.success(lista)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun obtenerPendientes(): Result<List<Guarderia>> {
        return try {
            val lista = client.postgrest["guarderias"]
                .select {
                    filter { eq("estado", "pendiente") }
                    order("created_at", Order.ASCENDING)
                }
                .decodeList<Guarderia>()
            Result.success(lista)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun aprobarGuarderia(uid: String): Result<Unit> {
        return try {
            client.postgrest["guarderias"].update(
                buildJsonObject {
                    put("verificada", true)
                    put("estado", "verificada")
                    put("mensaje_rechazo", "")
                }
            ) {
                filter { eq("uid", uid) }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun rechazarGuarderia(uid: String, mensaje: String): Result<Unit> {
        return try {
            client.postgrest["guarderias"].update(
                buildJsonObject {
                    put("verificada", false)
                    put("estado", "rechazada")
                    put("mensaje_rechazo", mensaje)
                }
            ) {
                filter { eq("uid", uid) }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
