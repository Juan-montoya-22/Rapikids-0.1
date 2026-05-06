package com.example.rapikids01.data.repository

import com.example.rapikids01.data.model.Guarderia
import com.example.rapikids01.data.supabase.SupabaseClient.client
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order

class GuarderiaRepository {
    suspend fun obtenerGuarderias(): Result<List<Guarderia>> {
        return try {
            val lista = client.postgrest["guarderias"]
                .select {
                    filter { eq("estado", "verificada") }
                    order("nombre_guarderia", Order.ASCENDING)
                }
                .decodeList<Guarderia>()
            Result.success(lista)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    fun filtrarGuarderias(lista: List<Guarderia>, query: String): List<Guarderia> {
        if (query.isBlank()) return lista
        val q = query.trim().lowercase()
        return lista.filter {
            it.nombreGuarderia.lowercase().contains(q) ||
            it.direccion.lowercase().contains(q)
        }
    }
}
