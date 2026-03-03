package com.example.rapikids01.screen.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.rapikids01.data.model.Guarderia
import com.example.rapikids01.viewmodel.HomePadreViewModel

// Paleta de colores
private val Purple     = Color(0xFF6A4DBA)
private val PurpleLight = Color(0xFF9C7EE8)
private val Orange     = Color(0xFFFF6B35)
private val Yellow     = Color(0xFFFFD23F)
private val GreenSoft  = Color(0xFF4CAF50)
private val BgSoft     = Color(0xFFF5F0FF)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomePadreScreen(
    onLogout: () -> Unit = {},
    onVerPerfil: (Guarderia) -> Unit = {},
    viewModel: HomePadreViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            // ── Header con gradiente ──────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(listOf(Purple, PurpleLight))
                    )
                    .statusBarsPadding()
            ) {
                Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "¡Hola! 👋",
                                fontSize = 14.sp,
                                color = Color.White.copy(alpha = 0.85f)
                            )
                            Text(
                                text = "Encuentra una guardería",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        IconButton(
                            onClick = onLogout,
                            modifier = Modifier
                                .size(40.dp)
                                .background(Color.White.copy(alpha = 0.2f), CircleShape)
                        ) {
                            Icon(
                                Icons.Default.ExitToApp,
                                contentDescription = "Cerrar sesión",
                                tint = Color.White
                            )
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    // ── Barra de búsqueda ─────────────────────────────
                    OutlinedTextField(
                        value = state.searchQuery,
                        onValueChange = viewModel::onSearchQueryChange,
                        placeholder = { Text("Buscar por nombre o dirección…", fontSize = 14.sp) },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = null, tint = Purple)
                        },
                        trailingIcon = {
                            AnimatedVisibility(
                                visible = state.searchQuery.isNotEmpty(),
                                enter = fadeIn(), exit = fadeOut()
                            ) {
                                IconButton(onClick = viewModel::limpiarBusqueda) {
                                    Icon(Icons.Default.Close, contentDescription = "Limpiar", tint = Purple)
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                    )

                    Spacer(Modifier.height(12.dp))
                }
            }
        },
        containerColor = BgSoft
    ) { padding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {

            when {
                // ── Cargando ──────────────────────────────────────────
                state.isLoading -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(color = Purple)
                        Spacer(Modifier.height(12.dp))
                        Text("Buscando guarderías…", color = Purple)
                    }
                }

                // ── Error ─────────────────────────────────────────────
                state.error != null -> {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("😕", fontSize = 48.sp)
                        Spacer(Modifier.height(12.dp))
                        Text(
                            text = state.error ?: "",
                            color = Color.Gray,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(Modifier.height(16.dp))
                        Button(
                            onClick = viewModel::cargarGuarderias,
                            colors = ButtonDefaults.buttonColors(containerColor = Purple),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, tint = Color.White)
                            Spacer(Modifier.width(8.dp))
                            Text("Reintentar", color = Color.White)
                        }
                    }
                }

                // ── Sin resultados ────────────────────────────────────
                state.guarderiasFiltradas.isEmpty() -> {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("🔍", fontSize = 48.sp)
                        Spacer(Modifier.height(12.dp))
                        Text(
                            text = "No encontramos guarderías\ncon \"${state.searchQuery}\"",
                            color = Color.Gray,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(Modifier.height(12.dp))
                        TextButton(onClick = viewModel::limpiarBusqueda) {
                            Text("Ver todas", color = Purple)
                        }
                    }
                }

                // ── Lista ─────────────────────────────────────────────
                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        item {
                            // Contador de resultados
                            Text(
                                text = "${state.guarderiasFiltradas.size} guardería(s) encontrada(s)",
                                fontSize = 13.sp,
                                color = Color.Gray,
                                modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
                            )
                        }

                        items(
                            items = state.guarderiasFiltradas,
                            key = { it.uid }
                        ) { guarderia ->
                            GuarderiaCard(
                                guarderia = guarderia,
                                onVerPerfil = { onVerPerfil(guarderia) }
                            )
                        }

                        item { Spacer(Modifier.height(8.dp)) }
                    }
                }
            }
        }
    }
}

// ── Card individual de guardería ──────────────────────────────────────────────

@Composable
fun GuarderiaCard(
    guarderia: Guarderia,
    onVerPerfil: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {

            // ── Foto / Banner superior ─────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
            ) {
                if (guarderia.fotoUrl.isNotBlank()) {
                    AsyncImage(
                        model = guarderia.fotoUrl,
                        contentDescription = "Foto de ${guarderia.nombreGuarderia}",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    // Placeholder colorido con inicial
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.linearGradient(listOf(PurpleLight, Orange))
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = guarderia.nombreGuarderia
                                .firstOrNull()?.uppercaseChar()?.toString() ?: "G",
                            fontSize = 52.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                // Badge verificada
                if (guarderia.verificada) {
                    Surface(
                        color = GreenSoft,
                        shape = RoundedCornerShape(bottomEnd = 0.dp, topStart = 0.dp,
                            bottomStart = 12.dp, topEnd = 0.dp),
                        modifier = Modifier.align(Alignment.TopEnd)
                    ) {
                        Text(
                            text = "✓ Verificada",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            // ── Contenido ─────────────────────────────────────────────
            Column(modifier = Modifier.padding(16.dp)) {

                // Nombre + Calificación
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = guarderia.nombreGuarderia,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A1A2E),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    Spacer(Modifier.width(8.dp))

                    // Estrellas
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .background(Yellow.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            Icons.Default.Star,
                            contentDescription = null,
                            tint = Yellow,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(3.dp))
                        Text(
                            text = if (guarderia.calificacionPromedio > 0)
                                String.format("%.1f", guarderia.calificacionPromedio)
                            else "Nuevo",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF7A6000)
                        )
                    }
                }

                Spacer(Modifier.height(10.dp))

                // Dirección
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Outlined.LocationOn,
                        contentDescription = null,
                        tint = Orange,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = guarderia.direccion,
                        fontSize = 13.sp,
                        color = Color.Gray,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(Modifier.height(6.dp))

                // Teléfono
                if (guarderia.telefono.isNotBlank()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Outlined.Phone,
                            contentDescription = null,
                            tint = Purple,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = guarderia.telefono,
                            fontSize = 13.sp,
                            color = Color.Gray
                        )
                    }
                }

                // Total reseñas
                if (guarderia.totalResenas > 0) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "${guarderia.totalResenas} reseña(s)",
                        fontSize = 12.sp,
                        color = Color.LightGray
                    )
                }

                Spacer(Modifier.height(14.dp))

                // ── Botón Ver perfil ───────────────────────────────────
                Button(
                    onClick = onVerPerfil,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Purple
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                ) {
                    Text(
                        text = "Ver perfil completo",
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }
            }
        }
    }
}
