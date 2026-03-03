package com.example.rapikids01.data.model

data class Guarderia(
    val uid: String = "",
    val nombreGuarderia: String = "",
    val direccion: String = "",
    val telefono: String = "",
    val email: String = "",
    val nit: String = "",
    val fotoUrl: String = "",
    val verificada: Boolean = false,
    val calificacionPromedio: Double = 0.0,
    val totalResenas: Int = 0,
    val createdAt: Long = 0L
)
