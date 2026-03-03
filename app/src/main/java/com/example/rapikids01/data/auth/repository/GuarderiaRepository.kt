package com.example.rapikids01.data.repository

import com.example.rapikids01.data.model.Guarderia
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class GuarderiaRepository {

    private val db = FirebaseFirestore.getInstance()

    /**
     * Trae todas las guarderías verificadas de Firestore,
     * ordenadas alfabéticamente por nombre.
     */
    suspend fun obtenerGuarderias(): Result<List<Guarderia>> {
        return try {
            val snapshot = db.collection("guarderias")
                .whereEqualTo("verificada", true)
                .get()
                .await()

            val lista = snapshot.documents.mapNotNull { doc ->
                doc.toObject(Guarderia::class.java)?.copy(uid = doc.id)
            }.sortedBy { it.nombreGuarderia.lowercase() }

            Result.success(lista)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Búsqueda local por nombre o dirección (ya teniendo la lista cargada).
     * Se hace en memoria para no generar lecturas extra en Firestore.
     */
    fun filtrarGuarderias(lista: List<Guarderia>, query: String): List<Guarderia> {
        if (query.isBlank()) return lista
        val q = query.trim().lowercase()
        return lista.filter {
            it.nombreGuarderia.lowercase().contains(q) ||
            it.direccion.lowercase().contains(q)
        }
    }
}
