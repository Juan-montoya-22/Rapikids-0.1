package com.example.rapikids01.screen.home

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.rapikids01.data.model.Guarderia
import com.example.rapikids01.viewmodel.GuarderiaConDistancia
import com.example.rapikids01.viewmodel.HomePadreViewModel
import java.util.Locale

private val Purple      = Color(0xFF6A4DBA)
private val PurpleLight = Color(0xFF9C7EE8)
private val Orange      = Color(0xFFFF6B35)
private val Yellow      = Color(0xFFFFD23F)
private val GreenSoft   = Color(0xFF4CAF50)
private val BgSoft      = Color(0xFFF5F0FF)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomePadreScreen(
    onLogout: () -> Unit = {},
    onVerPerfil: (Guarderia) -> Unit = {},
    viewModel: HomePadreViewModel = viewModel()
) {
    val state   by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    var guarderiaSeleccionada by remember { mutableStateOf<Guarderia?>(null) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[android.Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                      permissions[android.Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) viewModel.obtenerUbicacion(context)
        else viewModel.onPermisoDenegado()
    }

    LaunchedEffect(Unit) { viewModel.inicializarLocation(context) }

    if (guarderiaSeleccionada != null) {
        PerfilGuarderiaScreen(guarderia = guarderiaSeleccionada!!, onBack = { guarderiaSeleccionada = null })
        return
    }

    Scaffold(
        topBar = {
            Box(modifier = Modifier.fillMaxWidth().background(Brush.horizontalGradient(listOf(Purple, PurpleLight))).statusBarsPadding()) {
                Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column {
                            Text("¡Hola! 👋", fontSize = 14.sp, color = Color.White.copy(alpha = 0.85f))
                            Text("Encuentra una guardería", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                        IconButton(onClick = onLogout, modifier = Modifier.size(40.dp).background(Color.White.copy(alpha = 0.2f), CircleShape)) {
                            Icon(Icons.AutoMirrored.Filled.ExitToApp, null, tint = Color.White)
                        }
                    }
                    Spacer(Modifier.height(14.dp))
                    OutlinedTextField(
                        value = state.searchQuery, onValueChange = viewModel::onSearchQueryChange,
                        placeholder = { Text("Buscar por nombre o dirección…", fontSize = 14.sp) },
                        leadingIcon  = { Icon(Icons.Default.Search, null, tint = Purple) },
                        trailingIcon = {
                            AnimatedVisibility(visible = state.searchQuery.isNotEmpty(), enter = fadeIn(), exit = fadeOut()) {
                                IconButton(onClick = viewModel::limpiarBusqueda) { Icon(Icons.Default.Close, null, tint = Purple) }
                            }
                        },
                        singleLine = true, shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = Color.White, unfocusedContainerColor = Color.White, focusedBorderColor = Color.Transparent, unfocusedBorderColor = Color.Transparent),
                        modifier = Modifier.fillMaxWidth().height(54.dp)
                    )
                    Spacer(Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        if (state.ubicacionActual == null) {
                            Button(
                                onClick = {
                                    permissionLauncher.launch(arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION))
                                },
                                enabled = !state.isLoadingUbicacion,
                                shape = RoundedCornerShape(20.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                modifier = Modifier.height(34.dp)
                            ) {
                                if (state.isLoadingUbicacion) CircularProgressIndicator(color = Purple, strokeWidth = 2.dp, modifier = Modifier.size(16.dp))
                                else {
                                    Icon(Icons.Default.LocationOn, null, tint = Purple, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Usar mi ubicación", color = Purple, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        } else {
                            FilterChip(
                                selected = state.filtroCercanas, onClick = { viewModel.toggleFiltroCercanas() },
                                label = { Text("< 3 km", fontSize = 12.sp) },
                                leadingIcon = { Icon(Icons.Default.LocationOn, null, modifier = Modifier.size(14.dp)) },
                                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Color.White, selectedLabelColor = Purple, selectedLeadingIconColor = Purple, containerColor = Color.White.copy(alpha = 0.3f), labelColor = Color.White, iconColor = Color.White)
                            )
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.background(GreenSoft.copy(alpha = 0.2f), RoundedCornerShape(20.dp)).padding(horizontal = 10.dp, vertical = 6.dp)) {
                                Icon(Icons.Default.MyLocation, null, tint = GreenSoft, modifier = Modifier.size(14.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Ubicación activa", color = GreenSoft, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                        if (state.permisoDenegado) Text("Permiso denegado", color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
                    }
                    Spacer(Modifier.height(8.dp))
                }
            }
        },
        containerColor = BgSoft
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when {
                state.isLoading -> Column(modifier = Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = Purple); Spacer(Modifier.height(12.dp)); Text("Buscando guarderías…", color = Purple)
                }
                state.error != null -> Column(modifier = Modifier.align(Alignment.Center).padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("😕", fontSize = 48.sp); Spacer(Modifier.height(12.dp))
                    Text(state.error ?: "", color = Color.Gray, textAlign = TextAlign.Center); Spacer(Modifier.height(16.dp))
                    Button(onClick = viewModel::cargarGuarderias, colors = ButtonDefaults.buttonColors(containerColor = Purple), shape = RoundedCornerShape(12.dp)) {
                        Icon(Icons.Default.Refresh, null, tint = Color.White); Spacer(Modifier.width(8.dp)); Text("Reintentar", color = Color.White)
                    }
                }
                state.guarderiasFiltradas.isEmpty() -> Column(modifier = Modifier.align(Alignment.Center).padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(if (state.filtroCercanas) "📍" else "🔍", fontSize = 48.sp); Spacer(Modifier.height(12.dp))
                    Text(
                        text = when {
                            state.filtroCercanas -> "No hay guarderías verificadas\na menos de 3 km de ti"
                            state.searchQuery.isNotBlank() -> "No encontramos guarderías\ncon \"${state.searchQuery}\""
                            else -> "No hay guarderías disponibles"
                        }, color = Color.Gray, textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(12.dp))
                    if (state.filtroCercanas) TextButton(onClick = { viewModel.toggleFiltroCercanas() }) { Text("Ver todas las guarderías", color = Purple) }
                    else if (state.searchQuery.isNotBlank()) TextButton(onClick = viewModel::limpiarBusqueda) { Text("Ver todas", color = Purple) }
                }
                else -> LazyColumn(contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    item {
                        Text("${state.guarderiasFiltradas.size} guardería(s) encontrada(s)", fontSize = 13.sp, color = Color.Gray, modifier = Modifier.padding(start = 4.dp, bottom = 4.dp))
                    }
                    items(items = state.guarderiasFiltradas, key = { it.guarderia.uid.ifBlank { it.guarderia.nombreGuarderia } }) { item ->
                        GuarderiaCard(item = item, onVerPerfil = { guarderiaSeleccionada = item.guarderia })
                    }
                    item { Spacer(Modifier.height(8.dp)) }
                }
            }
        }
    }
}

// ── Card ──────────────────────────────────────────────────────────────────────
@Composable
fun GuarderiaCard(item: GuarderiaConDistancia, onVerPerfil: () -> Unit) {
    val guarderia = item.guarderia
    val context   = LocalContext.current

    // Dialog de confirmacion de contacto
    var accionPendiente by remember { mutableStateOf<(() -> Unit)?>(null) }

    if (accionPendiente != null) {
        AlertDialog(
            onDismissRequest = { accionPendiente = null },
            icon = { Icon(Icons.Default.Info, null, tint = Purple, modifier = Modifier.size(32.dp)) },
            title = {
                Text("Contacto directo", fontWeight = FontWeight.Bold, fontSize = 17.sp, color = Color(0xFF1A1A2E))
            },
            text = {
                Text(
                    "Al oprimir Aceptar, usted establecerá contacto directo con la guardería. La interacción no será responsabilidad de la aplicación Rapikids.",
                    fontSize = 14.sp, color = Color(0xFF555555), lineHeight = 20.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = { accionPendiente?.invoke(); accionPendiente = null },
                    colors  = ButtonDefaults.buttonColors(containerColor = Purple),
                    shape   = RoundedCornerShape(10.dp)
                ) { Text("Aceptar", color = Color.White, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { accionPendiente = null }) { Text("Cancelar", color = Color.Gray) }
            },
            shape = RoundedCornerShape(20.dp)
        )
    }

    Card(
        shape     = RoundedCornerShape(20.dp),
        colors    = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier  = Modifier.fillMaxWidth()
    ) {
        Column {
            // ── Imagen ─────────────────────────────────────────────────
            Box(modifier = Modifier.fillMaxWidth().height(140.dp)) {
                if (guarderia.fotoUrl.isNotBlank()) {
                    AsyncImage(model = guarderia.fotoUrl, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                } else {
                    Box(modifier = Modifier.fillMaxSize().background(Brush.linearGradient(listOf(PurpleLight, Orange))), contentAlignment = Alignment.Center) {
                        Text(guarderia.nombreGuarderia.firstOrNull()?.uppercaseChar()?.toString() ?: "G", fontSize = 52.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
                if (guarderia.verificada) {
                    Surface(color = GreenSoft, shape = RoundedCornerShape(bottomStart = 12.dp), modifier = Modifier.align(Alignment.TopEnd)) {
                        Text("✓ Verificada", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp))
                    }
                }
                // Badge distancia
                item.distanciaKm?.let { dist ->
                    Surface(color = Color.Black.copy(alpha = 0.6f), shape = RoundedCornerShape(topEnd = 12.dp), modifier = Modifier.align(Alignment.BottomStart)) {
                        Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocationOn, null, tint = Color.White, modifier = Modifier.size(12.dp))
                            Spacer(Modifier.width(3.dp))
                            Text(
                                text = if (dist < 1.0) "${(dist * 1000).toInt()} m" else String.format(Locale.getDefault(), "%.1f km", dist),
                                color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // ── Botones WhatsApp y Llamar en el banner ─────────────
                if (guarderia.telefono.isNotBlank()) {
                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Botón Llamar
                        Surface(
                            color    = Color.Black.copy(alpha = 0.6f),
                            shape    = CircleShape,
                            modifier = Modifier
                                .size(40.dp)
                                .clickable {
                                    accionPendiente = {
                                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${guarderia.telefono}"))
                                        context.startActivity(intent)
                                    }
                                }
                        ) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                Icon(Icons.Default.Phone, null, tint = Color.White, modifier = Modifier.size(20.dp))
                            }
                        }

                        // Botón WhatsApp
                        Surface(
                            color    = Color(0xFF25D366),
                            shape    = CircleShape,
                            modifier = Modifier
                                .size(40.dp)
                                .clickable {
                                    accionPendiente = {
                                        val numero = guarderia.telefono.replace(Regex("[^0-9]"), "")
                                        val waUrl  = "https://wa.me/57$numero"
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(waUrl))
                                        context.startActivity(intent)
                                    }
                                }
                        ) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                Icon(Icons.Default.Chat, null, tint = Color.White, modifier = Modifier.size(20.dp))
                            }
                        }
                    }
                }
            }

            Column(modifier = Modifier.padding(16.dp)) {
                // ── Nombre + calificación ──────────────────────────────
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                    Text(guarderia.nombreGuarderia, fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1A2E), maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                    Spacer(Modifier.width(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.background(Yellow.copy(alpha = 0.2f), RoundedCornerShape(8.dp)).padding(horizontal = 8.dp, vertical = 4.dp)) {
                        Icon(Icons.Default.Star, null, tint = Yellow, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(3.dp))
                        Text(
                            text = if (guarderia.calificacionPromedio > 0) String.format(Locale.getDefault(), "%.1f", guarderia.calificacionPromedio) else "Nuevo",
                            fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF7A6000)
                        )
                    }
                }

                Spacer(Modifier.height(10.dp))

                // ── Descripción ────────────────────────────────────────
                if (guarderia.descripcion.isNotBlank()) {
                    Text(guarderia.descripcion, fontSize = 13.sp, color = Color(0xFF555555), maxLines = 2, overflow = TextOverflow.Ellipsis, lineHeight = 18.sp)
                    Spacer(Modifier.height(10.dp))
                }



                // ── Correo clicable ────────────────────────────────────
                if (guarderia.email.isNotBlank()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clickable {
                                accionPendiente = {
                                    val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:${guarderia.email}"))
                                    intent.putExtra(Intent.EXTRA_SUBJECT, "Consulta sobre ${guarderia.nombreGuarderia}")
                                    context.startActivity(intent)
                                }
                            }
                            .background(Color(0xFFF0F0FF), RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.Email, null, tint = Purple, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(guarderia.email, fontSize = 13.sp, color = Purple, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                    Spacer(Modifier.height(10.dp))
                }

                // ── Precio si existe ───────────────────────────────────
                if (guarderia.precioMensual > 0) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AttachMoney, null, tint = Color(0xFF00897B), modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        val formato = java.text.NumberFormat.getNumberInstance(Locale("es", "CO"))
                        Text("$ ${formato.format(guarderia.precioMensual)} COP/mes", fontSize = 13.sp, color = Color(0xFF00897B), fontWeight = FontWeight.SemiBold)
                    }
                    Spacer(Modifier.height(10.dp))
                }

                if (guarderia.totalResenas > 0) {
                    Text("${guarderia.totalResenas} reseña(s)", fontSize = 12.sp, color = Color.LightGray)
                    Spacer(Modifier.height(8.dp))
                }

                // ── Botón ver perfil ───────────────────────────────────
                Button(
                    onClick  = onVerPerfil,
                    shape    = RoundedCornerShape(12.dp),
                    colors   = ButtonDefaults.buttonColors(containerColor = Purple),
                    modifier = Modifier.fillMaxWidth().height(44.dp)
                ) {
                    Text("Ver perfil completo", fontWeight = FontWeight.SemiBold, color = Color.White)
                }
            }
        }
    }
}
