package com.example.rapikids01.data.location

import android.annotation.SuppressLint
import android.content.Context
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.android.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.coroutines.resume
import kotlin.math.*

data class LatLng(val lat: Double, val lng: Double)

@Serializable
data class NominatimResult(
    val lat: String = "",
    val lon: String = ""
)

class LocationService(private val context: Context) {

    private val fusedClient = LocationServices.getFusedLocationProviderClient(context)

    private val httpClient = HttpClient(Android) {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }

    @SuppressLint("MissingPermission")
    suspend fun obtenerUbicacionActual(): Result<LatLng> {
        return try {
            val cts = CancellationTokenSource()
            val location = suspendCancellableCoroutine { cont ->
                fusedClient.getCurrentLocation(
                    Priority.PRIORITY_HIGH_ACCURACY,
                    cts.token
                ).addOnSuccessListener { loc ->
                    if (loc != null) cont.resume(loc) else cont.resume(null)
                }.addOnFailureListener { cont.resume(null) }
                cont.invokeOnCancellation { cts.cancel() }
            }
            if (location != null) Result.success(LatLng(location.latitude, location.longitude))
            else Result.failure(Exception("No se pudo obtener la ubicación"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun normalizarDireccion(direccion: String): String {
        var dir = direccion.trim()
        dir = dir.replace(Regex("(?i)\\bCl\\.?\\s"), "Calle ")
        dir = dir.replace(Regex("(?i)\\bCr\\.?\\s|\\bCra\\.?\\s"), "Carrera ")
        dir = dir.replace(Regex("(?i)\\bAv\\.?\\s|\\bAvda\\.?\\s"), "Avenida ")
        dir = dir.replace(Regex("(?i)\\bDg\\.?\\s|\\bDiag\\.?\\s"), "Diagonal ")
        dir = dir.replace(Regex("(?i)\\bTv\\.?\\s|\\bTransv\\.?\\s"), "Transversal ")
        dir = dir.replace(Regex("#[^,]+"), "").trim()
        dir = dir.replace(Regex("(\\d+)[a-zA-Z]\\b"), "$1")
        dir = dir.replace(",", "").replace(Regex("\\s+"), " ").trim()

        return "$dir, Bogotá, Colombia"
    }

    suspend fun geocodificarDireccion(direccion: String): Result<LatLng> {
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

            if (results.isEmpty()) return geocodificarFallback(direccion)

            val first = results.first()
            Result.success(LatLng(first.lat.toDouble(), first.lon.toDouble()))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    private suspend fun geocodificarFallback(direccion: String): Result<LatLng> {
        return try {
            val soloTexto = direccion
                .replace(Regex("#[^,]+"), "")
                .replace(Regex("\\d+"), "")
                .replace(Regex("[^a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]"), " ")
                .replace(Regex("\\s+"), " ").trim()

            val query = if (soloTexto.length > 3) "$soloTexto, Bogotá, Colombia"
                        else "Bogotá, Colombia"

            val response = httpClient.get("https://nominatim.openstreetmap.org/search") {
                parameter("q", query)
                parameter("format", "json")
                parameter("limit", "1")
                parameter("countrycodes", "co")
                header("User-Agent", "Rapikids/1.0 soporte@rapikids.com")
                header("Accept-Language", "es")
            }

            val results = response.body<List<NominatimResult>>()
            val first = results.firstOrNull()
                ?: return Result.failure(Exception("Dirección no encontrada"))

            Result.success(LatLng(first.lat.toDouble(), first.lon.toDouble()))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    fun calcularDistanciaKm(origen: LatLng, destino: LatLng): Double {
        val radioTierra = 6371.0
        val dLat = Math.toRadians(destino.lat - origen.lat)
        val dLng = Math.toRadians(destino.lng - origen.lng)
        val a = sin(dLat / 2).pow(2) +
                cos(Math.toRadians(origen.lat)) *
                cos(Math.toRadians(destino.lat)) *
                sin(dLng / 2).pow(2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return radioTierra * c
    }
}
