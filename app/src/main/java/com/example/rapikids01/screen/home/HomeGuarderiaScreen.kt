package com.example.rapikids01.screen.home

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.rapikids01.viewmodel.HomeGuarderiaViewModel

private val NavyBlue  = Color(0xFF1A237E)
private val BlueLight = Color(0xFF3949AB)
private val Teal      = Color(0xFF00897B)
private val BgGray    = Color(0xFFF0F2F5)
private val CardWhite = Color.White
private val TextDark  = Color(0xFF1C1E21)
private val TextGray  = Color(0xFF65676B)
private val Verified  = Color(0xFF1877F2)
private val RedError  = Color(0xFFE53935)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeGuarderiaScreen(
    onLogout: () -> Unit = {},
    viewModel: HomeGuarderiaViewModel = viewModel()
) {
    val state   by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    val photoLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? -> uri?.let { viewModel.subirFoto(context, it) } }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) { viewModel.cargarPerfil() }

    LaunchedEffect(state.successMsg) {
        state.successMsg?.let { snackbarHostState.showSnackbar(it); viewModel.clearMessages() }
    }
    LaunchedEffect(state.error) {
        state.error?.let { snackbarHostState.showSnackbar("⚠️ $it"); viewModel.clearMessages() }
    }

    Scaffold(
        snackbarHost   = { SnackbarHost(snackbarHostState) },
        containerColor = BgGray
    ) { padding ->

        if (state.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = BlueLight)
            }
            return@Scaffold
        }

        val g = state.guarderia

        if (g == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("No se pudo cargar el perfil", color = TextGray)
                    Spacer(Modifier.height(12.dp))
                    Button(
                        onClick = { viewModel.cargarPerfil() },
                        colors  = ButtonDefaults.buttonColors(containerColor = BlueLight)
                    ) { Text("Reintentar") }
                }
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {

            // ── Portada + Foto ────────────────────────────────────────
            Box(modifier = Modifier.fillMaxWidth().height(260.dp)) {

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(190.dp)
                        .background(Brush.linearGradient(listOf(NavyBlue, BlueLight)))
                ) {
                    // Botón logout
                    IconButton(
                        onClick  = onLogout,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(12.dp)
                            .background(Color.White.copy(alpha = 0.2f), CircleShape)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ExitToApp, null, tint = Color.White)
                    }

                    // Nombre + badge estado
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(start = 140.dp, bottom = 56.dp)
                    ) {
                        Text(
                            g.nombreGuarderia,
                            fontSize   = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color      = Color.White,
                            maxLines   = 2
                        )
                        Spacer(Modifier.height(4.dp))

                        // Badge según estado
                        when (g.estado) {
                            "verificada" -> Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .background(Verified, RoundedCornerShape(20.dp))
                                    .padding(horizontal = 10.dp, vertical = 3.dp)
                            ) {
                                Icon(Icons.Default.Verified, null, tint = Color.White, modifier = Modifier.size(14.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Verificada", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }
                            "rechazada" -> Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .background(RedError, RoundedCornerShape(20.dp))
                                    .padding(horizontal = 10.dp, vertical = 3.dp)
                            ) {
                                Icon(Icons.Default.Cancel, null, tint = Color.White, modifier = Modifier.size(14.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Rechazada", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }
                            else -> Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .background(Color(0xFFFFA000), RoundedCornerShape(20.dp))
                                    .padding(horizontal = 10.dp, vertical = 3.dp)
                            ) {
                                Icon(Icons.Default.HourglassEmpty, null, tint = Color.White, modifier = Modifier.size(14.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Pendiente de verificación", color = Color.White, fontSize = 12.sp)
                            }
                        }
                    }
                }

                // Foto circular clicable
                Box(modifier = Modifier.align(Alignment.BottomStart).padding(start = 20.dp)) {
                    Box(
                        modifier = Modifier
                            .size(110.dp)
                            .clip(CircleShape)
                            .border(4.dp, CardWhite, CircleShape)
                            .background(BlueLight)
                            .clickable { photoLauncher.launch("image/*") },
                        contentAlignment = Alignment.Center
                    ) {
                        when {
                            state.isUploadingPhoto -> CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp)
                            !g.fotoUrl.isNullOrBlank() -> AsyncImage(
                                model              = g.fotoUrl,
                                contentDescription = null,
                                contentScale       = ContentScale.Crop,
                                modifier           = Modifier.fillMaxSize()
                            )
                            else -> Text(
                                text       = g.nombreGuarderia.firstOrNull()?.uppercaseChar()?.toString() ?: "G",
                                fontSize   = 40.sp,
                                fontWeight = FontWeight.Bold,
                                color      = Color.White
                            )
                        }
                    }
                    // Ícono cámara
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .size(30.dp)
                            .background(BlueLight, CircleShape)
                            .border(2.dp, CardWhite, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.CameraAlt, null, tint = Color.White, modifier = Modifier.size(16.dp))
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // ── Botón editar ──────────────────────────────────────────
            Card(
                colors    = CardDefaults.cardColors(containerColor = CardWhite),
                elevation = CardDefaults.cardElevation(2.dp),
                shape     = RoundedCornerShape(0.dp),
                modifier  = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier              = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    if (!state.isEditMode) {
                        OutlinedButton(
                            onClick = viewModel::toggleEditMode,
                            shape   = RoundedCornerShape(8.dp),
                            colors  = ButtonDefaults.outlinedButtonColors(contentColor = BlueLight),
                            border  = androidx.compose.foundation.BorderStroke(1.dp, BlueLight)
                        ) {
                            Icon(Icons.Default.Edit, null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Editar perfil", fontWeight = FontWeight.SemiBold)
                        }
                    } else {
                        TextButton(onClick = viewModel::toggleEditMode) {
                            Text("Cancelar", color = TextGray)
                        }
                        Spacer(Modifier.width(8.dp))
                        Button(
                            onClick  = viewModel::guardarCambios,
                            enabled  = !state.isSaving,
                            shape    = RoundedCornerShape(8.dp),
                            colors   = ButtonDefaults.buttonColors(containerColor = BlueLight)
                        ) {
                            if (state.isSaving) {
                                CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp, modifier = Modifier.size(18.dp))
                            } else {
                                Icon(Icons.Default.Save, null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Guardar", fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // ── Información ───────────────────────────────────────────
            Card(
                colors    = CardDefaults.cardColors(containerColor = CardWhite),
                elevation = CardDefaults.cardElevation(2.dp),
                shape     = RoundedCornerShape(0.dp),
                modifier  = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Información", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = TextDark)
                    Spacer(Modifier.height(16.dp))

                    if (!state.isEditMode) {
                        InfoRow(Icons.Default.Business,      "Guardería",  g.nombreGuarderia)
                        HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = BgGray)
                        InfoRow(Icons.Outlined.LocationOn,   "Dirección",  g.direccion)
                        HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = BgGray)
                        InfoRow(Icons.Outlined.Phone,        "Teléfono",   g.telefono)
                        HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = BgGray)
                        InfoRow(Icons.Outlined.Email,        "Correo",     g.email)
                        HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = BgGray)
                        InfoRow(Icons.Default.Badge,         "NIT",        g.nit)
                    } else {
                        EditField("Nombre de la guardería", state.nombreEdit,    viewModel::onNombreChange)
                        Spacer(Modifier.height(12.dp))
                        EditField("Dirección",              state.direccionEdit, viewModel::onDireccionChange)
                        Spacer(Modifier.height(12.dp))
                        EditField("Teléfono",               state.telefonoEdit,  viewModel::onTelefonoChange, KeyboardType.Phone)
                        Spacer(Modifier.height(8.dp))
                        Text("El correo y NIT no se pueden editar", fontSize = 12.sp, color = TextGray)
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // ── Estado de la cuenta ───────────────────────────────────
            Card(
                colors    = CardDefaults.cardColors(containerColor = CardWhite),
                elevation = CardDefaults.cardElevation(2.dp),
                shape     = RoundedCornerShape(0.dp),
                modifier  = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Estado de la cuenta", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = TextDark)
                    Spacer(Modifier.height(14.dp))

                    when (g.estado) {
                        "verificada" -> StatusBanner(
                            icon     = Icons.Default.CheckCircle,
                            iconColor = Teal,
                            bgColor  = Color(0xFFE8F5E9),
                            title    = "✅ Cuenta verificada",
                            subtitle = "Tu guardería es visible para todos los padres en la aplicación."
                        )
                        "rechazada" -> {
                            StatusBanner(
                                icon      = Icons.Default.Cancel,
                                iconColor = RedError,
                                bgColor   = Color(0xFFFFEBEE),
                                title     = "❌ Cuenta rechazada",
                                subtitle  = "Tu solicitud fue rechazada por el administrador."
                            )
                            // Mostrar motivo si existe
                            if (g.mensajeRechazo.isNotBlank()) {
                                Spacer(Modifier.height(10.dp))
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0xFFFFF3E0), RoundedCornerShape(10.dp))
                                        .padding(12.dp)
                                ) {
                                    Icon(Icons.Default.Info, null, tint = Color(0xFFFFA000), modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Column {
                                        Text("Motivo:", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFFFFA000))
                                        Text(g.mensajeRechazo, fontSize = 13.sp, color = TextDark)
                                    }
                                }
                            }
                        }
                        else -> StatusBanner(
                            icon      = Icons.Default.HourglassEmpty,
                            iconColor = Color(0xFFFFA000),
                            bgColor   = Color(0xFFFFF8E1),
                            title     = "⏳ Verificación pendiente",
                            subtitle  = "Estamos revisando tus documentos. Te notificaremos cuando tu cuenta sea aprobada."
                        )
                    }
                }
            }

            // ── Calificaciones ────────────────────────────────────────
            if (g.totalResenas > 0) {
                Spacer(Modifier.height(8.dp))
                Card(
                    colors    = CardDefaults.cardColors(containerColor = CardWhite),
                    elevation = CardDefaults.cardElevation(2.dp),
                    shape     = RoundedCornerShape(0.dp),
                    modifier  = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Calificaciones", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = TextDark)
                        Spacer(Modifier.height(14.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                String.format("%.1f", g.calificacionPromedio),
                                fontSize   = 48.sp,
                                fontWeight = FontWeight.Bold,
                                color      = Color(0xFFFFC107)
                            )
                            Spacer(Modifier.width(16.dp))
                            Column {
                                Row {
                                    repeat(5) { i ->
                                        Icon(
                                            Icons.Default.Star,
                                            null,
                                            tint     = if (i < g.calificacionPromedio.toInt()) Color(0xFFFFC107) else Color.LightGray,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                }
                                Spacer(Modifier.height(4.dp))
                                Text("${g.totalResenas} reseñas", color = TextGray, fontSize = 13.sp)
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun InfoRow(icon: ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.Top) {
        Icon(icon, null, tint = BlueLight, modifier = Modifier.size(22.dp).padding(top = 2.dp))
        Spacer(Modifier.width(12.dp))
        Column {
            Text(label, fontSize = 12.sp, color = TextGray)
            Text(value.ifBlank { "-" }, fontSize = 15.sp, color = TextDark, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun EditField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    OutlinedTextField(
        value           = value,
        onValueChange   = onValueChange,
        label           = { Text(label) },
        singleLine      = true,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        shape           = RoundedCornerShape(10.dp),
        colors          = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = BlueLight,
            focusedLabelColor  = BlueLight
        ),
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun StatusBanner(
    icon: ImageVector,
    iconColor: Color,
    bgColor: Color,
    title: String,
    subtitle: String
) {
    Row(
        modifier          = Modifier.fillMaxWidth().background(bgColor, RoundedCornerShape(12.dp)).padding(14.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(icon, null, tint = iconColor, modifier = Modifier.size(28.dp))
        Spacer(Modifier.width(12.dp))
        Column {
            Text(title, fontWeight = FontWeight.Bold, color = iconColor, fontSize = 15.sp)
            Spacer(Modifier.height(4.dp))
            Text(subtitle, fontSize = 13.sp, color = TextGray, lineHeight = 18.sp)
        }
    }
}
