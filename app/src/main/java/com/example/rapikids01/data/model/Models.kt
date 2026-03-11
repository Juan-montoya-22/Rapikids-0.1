package com.example.rapikids01.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Guarderia(
    val uid: String                      = "",
    val email: String                    = "",
    @SerialName("nombre_guarderia")
    val nombreGuarderia: String          = "",
    val direccion: String                = "",
    val nit: String                      = "",
    val telefono: String                 = "",
    @SerialName("foto_url")
    val fotoUrl: String                  = "",
    @SerialName("documento_url")
    val documentoUrl: String             = "",
    val verificada: Boolean              = false,
    val estado: String                   = "pendiente",
    @SerialName("mensaje_rechazo")
    val mensajeRechazo: String           = "",
    @SerialName("calificacion_promedio")
    val calificacionPromedio: Double     = 0.0,
    @SerialName("total_resenas")
    val totalResenas: Int                = 0,
    @SerialName("created_at")
    val createdAt: String                = ""
)

@Serializable
data class Usuario(
    val uid: String          = "",
    val email: String        = "",
    val nombre: String       = "",
    val telefono: String     = "",
    val role: String         = "",
    @SerialName("created_at")
    val createdAt: String    = ""
)

@Serializable
data class Admin(
    val uid: String          = "",
    val email: String        = "",
    val nombre: String       = "",
    val cargo: String        = "",
    @SerialName("created_at")
    val createdAt: String    = ""
)

@Serializable
data class Resena(
    val id: String                   = "",
    @SerialName("guarderia_uid")
    val guarderiaUid: String         = "",
    @SerialName("padre_uid")
    val padreUid: String             = "",
    val calificacion: Int            = 0,
    val comentario: String           = "",
    @SerialName("created_at")
    val createdAt: String            = ""
)
