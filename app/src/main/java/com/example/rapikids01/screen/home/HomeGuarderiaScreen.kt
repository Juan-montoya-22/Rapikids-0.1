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
import java.text.NumberFormat
import java.util.Locale

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

    var mostrarFaq by remember { mutableStateOf(false) } // 👈 NUEVO

    val photoLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { viewModel.subirFoto(context, it) }
    }
    val docLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { viewModel.resubirDocumento(context, it) }
    }

    var carruselSlotSeleccionado by remember { mutableStateOf(0) }
    val carruselLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { viewModel.subirFotoCarrusel(context, it, carruselSlotSeleccionado) }
    }

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(Unit) { viewModel.cargarPerfil() }
    LaunchedEffect(state.successMsg) { state.successMsg?.let { snackbarHostState.showSnackbar(it); viewModel.clearMessages() } }
    LaunchedEffect(state.error)      { state.error?.let      { snackbarHostState.showSnackbar("⚠️ $it"); viewModel.clearMessages() } }

    if (mostrarFaq) {
        FaqDialog(esPadre = false, onDismiss = { mostrarFaq = false })
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = BgGray,
        floatingActionButton = {
            FloatingActionButton(
                onClick        = { mostrarFaq = true },
                containerColor = BlueLight,
                contentColor   = Color.White,
                shape          = CircleShape
            ) {
                Icon(Icons.Default.Help, contentDescription = "Ayuda")
            }
        }
    ) { padding ->

        if (state.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = BlueLight) }
            return@Scaffold
        }

        val g = state.guarderia ?: run {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("No se pudo cargar el perfil", color = TextGray)
                    Spacer(Modifier.height(12.dp))
                    Button(onClick = { viewModel.cargarPerfil() }, colors = ButtonDefaults.buttonColors(containerColor = BlueLight)) { Text("Reintentar") }
                }
            }
            return@Scaffold
        }

        Column(modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState())) {
            Box(modifier = Modifier.fillMaxWidth().height(260.dp)) {
                Box(modifier = Modifier.fillMaxWidth().height(190.dp).background(Brush.linearGradient(listOf(NavyBlue, BlueLight)))) {
                    IconButton(onClick = onLogout, modifier = Modifier.align(Alignment.TopEnd).padding(12.dp).background(Color.White.copy(alpha = 0.2f), CircleShape)) {
                        Icon(Icons.AutoMirrored.Filled.ExitToApp, null, tint = Color.White)
                    }
                    Column(modifier = Modifier.align(Alignment.BottomStart).padding(start = 140.dp, bottom = 56.dp)) {
                        Text(g.nombreGuarderia, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White, maxLines = 2)
                        Spacer(Modifier.height(4.dp))
                        when (g.estado) {
                            "verificada" -> BadgeEstado(Icons.Default.Verified,       Verified,          "Verificada")
                            "rechazada"  -> BadgeEstado(Icons.Default.Cancel,         RedError,          "Rechazada")
                            else         -> BadgeEstado(Icons.Default.HourglassEmpty, Color(0xFFFFA000), "Pendiente de verificación")
                        }
                    }
                }
                Box(modifier = Modifier.align(Alignment.BottomStart).padding(start = 20.dp)) {
                    Box(
                        modifier = Modifier.size(110.dp).clip(CircleShape).border(4.dp, CardWhite, CircleShape)
                            .background(BlueLight).clickable { photoLauncher.launch("image/*") },
                        contentAlignment = Alignment.Center
                    ) {
                        when {
                            state.isUploadingPhoto -> CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp)
                            !g.fotoUrl.isNullOrBlank() -> AsyncImage(model = g.fotoUrl, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                            else -> Text(g.nombreGuarderia.firstOrNull()?.uppercaseChar()?.toString() ?: "G", fontSize = 40.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                    Box(
                        modifier = Modifier.align(Alignment.BottomEnd).size(30.dp)
                            .background(BlueLight, CircleShape).border(2.dp, CardWhite, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.CameraAlt, null, tint = Color.White, modifier = Modifier.size(16.dp))
                    }
                }
            }

            Spacer(Modifier.height(8.dp))
            Card(colors = CardDefaults.cardColors(containerColor = CardWhite), elevation = CardDefaults.cardElevation(2.dp), shape = RoundedCornerShape(0.dp), modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp), horizontalArrangement = Arrangement.End) {
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
                        TextButton(onClick = viewModel::toggleEditMode) { Text("Cancelar", color = TextGray) }
                        Spacer(Modifier.width(8.dp))
                        Button(
                            onClick  = viewModel::guardarCambios,
                            enabled  = !state.isSaving,
                            shape    = RoundedCornerShape(8.dp),
                            colors   = ButtonDefaults.buttonColors(containerColor = BlueLight)
                        ) {
                            if (state.isSaving) CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp, modifier = Modifier.size(18.dp))
                            else { Icon(Icons.Default.Save, null, modifier = Modifier.size(16.dp)); Spacer(Modifier.width(6.dp)); Text("Guardar") }
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))
            Card(colors = CardDefaults.cardColors(containerColor = CardWhite), elevation = CardDefaults.cardElevation(2.dp), shape = RoundedCornerShape(0.dp), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.PhotoLibrary, null, tint = BlueLight, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Fotos del espacio", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = TextDark)
                        Spacer(Modifier.width(4.dp))
                        Text("(máx. 3)", fontSize = 12.sp, color = TextGray)
                    }
                    Spacer(Modifier.height(14.dp))
                    if (state.isUploadingCarrusel) {
                        Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = BlueLight)
                        }
                    } else {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            val fotos = g.fotos.take(3).toMutableList()
                            while (fotos.size < 3) fotos.add("")
                            fotos.forEachIndexed { index, fotoUrl ->
                                Box(
                                    modifier = Modifier.weight(1f).height(100.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(BgGray)
                                        .border(1.dp, Color.LightGray, RoundedCornerShape(10.dp))
                                        .clickable {
                                            carruselSlotSeleccionado = index
                                            carruselLauncher.launch("image/*")
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (fotoUrl.isNotBlank()) {
                                        AsyncImage(model = fotoUrl, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(10.dp)))
                                        Box(
                                            modifier = Modifier.align(Alignment.TopEnd).padding(4.dp).size(22.dp)
                                                .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                                                .clickable { viewModel.eliminarFotoCarrusel(index) },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(Icons.Default.Close, null, tint = Color.White, modifier = Modifier.size(12.dp))
                                        }
                                    } else {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Icon(Icons.Default.AddPhotoAlternate, null, tint = Color.LightGray, modifier = Modifier.size(28.dp))
                                            Text("Agregar", fontSize = 10.sp, color = Color.LightGray)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))
            Card(colors = CardDefaults.cardColors(containerColor = CardWhite), elevation = CardDefaults.cardElevation(2.dp), shape = RoundedCornerShape(0.dp), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Información", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = TextDark)
                    Spacer(Modifier.height(16.dp))
                    if (!state.isEditMode) {
                        InfoRow(Icons.Default.Business,    "Guardería", g.nombreGuarderia)
                        HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = BgGray)
                        InfoRow(Icons.Outlined.LocationOn, "Dirección", g.direccion)
                        HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = BgGray)
                        InfoRow(Icons.Outlined.Phone,      "Teléfono",  g.telefono)
                        HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = BgGray)
                        InfoRow(Icons.Outlined.Email,      "Correo",    g.email)
                        HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = BgGray)
                        InfoRow(Icons.Default.Badge,       "NIT",       g.nit)
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
            Card(colors = CardDefaults.cardColors(containerColor = CardWhite), elevation = CardDefaults.cardElevation(2.dp), shape = RoundedCornerShape(0.dp), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Description, null, tint = BlueLight, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Descripción", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = TextDark)
                    }
                    Spacer(Modifier.height(12.dp))
                    if (!state.isEditMode) {
                        Text(
                            g.descripcion.ifBlank { "Sin descripción aún. Edita tu perfil para agregar una." },
                            fontSize   = 14.sp,
                            color      = if (g.descripcion.isBlank()) TextGray else TextDark,
                            lineHeight = 20.sp
                        )
                    } else {
                        OutlinedTextField(
                            value         = state.descripcionEdit,
                            onValueChange = viewModel::onDescripcionChange,
                            label         = { Text("Describe tu guardería") },
                            placeholder   = { Text("Ej: Ofrecemos un ambiente seguro y estimulante para niños de 0 a 5 años...") },
                            minLines      = 3,
                            maxLines      = 6,
                            shape         = RoundedCornerShape(10.dp),
                            colors        = OutlinedTextFieldDefaults.colors(focusedBorderColor = BlueLight, focusedLabelColor = BlueLight),
                            modifier      = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))
            Card(colors = CardDefaults.cardColors(containerColor = CardWhite), elevation = CardDefaults.cardElevation(2.dp), shape = RoundedCornerShape(0.dp), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AttachMoney, null, tint = BlueLight, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Precio mensual", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = TextDark)
                    }
                    Spacer(Modifier.height(12.dp))
                    if (!state.isEditMode) {
                        if (g.precioMensual > 0) {
                            val formato = NumberFormat.getNumberInstance(Locale("es", "CO"))
                            Text("$ ${formato.format(g.precioMensual)} COP / mes", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Teal)
                        } else {
                            Text("Precio no especificado", fontSize = 14.sp, color = TextGray)
                        }
                    } else {
                        EditField("Precio mensual (COP)", state.precioMensualEdit, viewModel::onPrecioMensualChange, KeyboardType.Number)
                    }
                }
            }

            Spacer(Modifier.height(8.dp))
            Card(colors = CardDefaults.cardColors(containerColor = CardWhite), elevation = CardDefaults.cardElevation(2.dp), shape = RoundedCornerShape(0.dp), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Schedule, null, tint = BlueLight, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Horarios", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = TextDark)
                    }
                    Spacer(Modifier.height(12.dp))
                    if (!state.isEditMode) {
                        if (g.horaApertura.isNotBlank() || g.horaCierre.isNotBlank()) {
                            InfoRow(Icons.Default.AccessTime,    "Apertura", g.horaApertura.ifBlank { "-" })
                            HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = BgGray)
                            InfoRow(Icons.Default.AccessTime,    "Cierre",   g.horaCierre.ifBlank { "-" })
                        }
                        if (g.diasAtencion.isNotBlank()) {
                            HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = BgGray)
                            InfoRow(Icons.Default.CalendarMonth, "Días",     g.diasAtencion)
                        }
                        if (g.jornada.isNotBlank()) {
                            HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = BgGray)
                            InfoRow(Icons.Default.WbSunny,       "Jornada",  g.jornada)
                        }
                        if (g.horaApertura.isBlank() && g.horaCierre.isBlank() && g.diasAtencion.isBlank() && g.jornada.isBlank()) {
                            Text("Horario no especificado. Edita tu perfil para agregar.", fontSize = 14.sp, color = TextGray)
                        }
                    } else {
                        EditField("Hora apertura (ej: 7:00 AM)",            state.horaAperturaEdit,  viewModel::onHoraAperturaChange)
                        Spacer(Modifier.height(10.dp))
                        EditField("Hora cierre (ej: 6:00 PM)",              state.horaCierreEdit,    viewModel::onHoraCierreChange)
                        Spacer(Modifier.height(10.dp))
                        EditField("Días de atención (ej: Lunes a Viernes)", state.diasAtencionEdit,  viewModel::onDiasAtencionChange)
                        Spacer(Modifier.height(10.dp))
                        Text("Jornada", fontSize = 13.sp, color = TextGray)
                        Spacer(Modifier.height(6.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("Mañana", "Tarde", "Completa").forEach { j ->
                                FilterChip(
                                    selected = state.jornadaEdit == j,
                                    onClick  = { viewModel.onJornadaChange(j) },
                                    label    = { Text(j, fontSize = 12.sp) },
                                    colors   = FilterChipDefaults.filterChipColors(selectedContainerColor = BlueLight, selectedLabelColor = Color.White)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))
            Card(colors = CardDefaults.cardColors(containerColor = CardWhite), elevation = CardDefaults.cardElevation(2.dp), shape = RoundedCornerShape(0.dp), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Estado de la cuenta", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = TextDark)
                    Spacer(Modifier.height(14.dp))
                    when (g.estado) {
                        "verificada" -> StatusBanner(Icons.Default.CheckCircle, Teal, Color(0xFFE8F5E9), "✅ Cuenta verificada", "Tu guardería es visible para todos los padres.")
                        "rechazada"  -> {
                            StatusBanner(Icons.Default.Cancel, RedError, Color(0xFFFFEBEE), "❌ Cuenta rechazada", "Tu solicitud fue rechazada por el administrador.")
                            if (g.mensajeRechazo.isNotBlank()) {
                                Spacer(Modifier.height(10.dp))
                                Row(modifier = Modifier.fillMaxWidth().background(Color(0xFFFFF3E0), RoundedCornerShape(10.dp)).padding(12.dp)) {
                                    Icon(Icons.Default.Info, null, tint = Color(0xFFFFA000), modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Column {
                                        Text("Motivo:", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFFFFA000))
                                        Text(g.mensajeRechazo, fontSize = 13.sp, color = TextDark)
                                    }
                                }
                            }
                            Spacer(Modifier.height(16.dp))
                            HorizontalDivider(color = BgGray)
                            Spacer(Modifier.height(16.dp))
                            Text("¿Quieres corregir tu solicitud?", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = TextDark)
                            Spacer(Modifier.height(4.dp))
                            Text("Sube un nuevo documento y tu cuenta volverá a revisión.", fontSize = 13.sp, color = TextGray, lineHeight = 18.sp)
                            Spacer(Modifier.height(12.dp))
                            Button(
                                onClick  = { docLauncher.launch("*/*") },
                                enabled  = !state.isUploadingDoc,
                                shape    = RoundedCornerShape(12.dp),
                                colors   = ButtonDefaults.buttonColors(containerColor = BlueLight),
                                modifier = Modifier.fillMaxWidth().height(50.dp)
                            ) {
                                if (state.isUploadingDoc) CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp, modifier = Modifier.size(22.dp))
                                else {
                                    Icon(Icons.Default.UploadFile, null, tint = Color.White)
                                    Spacer(Modifier.width(8.dp))
                                    Text("Subir nuevo documento", fontWeight = FontWeight.SemiBold, color = Color.White)
                                }
                            }
                        }
                        else -> StatusBanner(Icons.Default.HourglassEmpty, Color(0xFFFFA000), Color(0xFFFFF8E1), "⏳ Verificación pendiente", "Estamos revisando tus documentos.")
                    }
                }
            }
            if (g.totalResenas > 0) {
                Spacer(Modifier.height(8.dp))
                Card(colors = CardDefaults.cardColors(containerColor = CardWhite), elevation = CardDefaults.cardElevation(2.dp), shape = RoundedCornerShape(0.dp), modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Calificaciones", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = TextDark)
                        Spacer(Modifier.height(14.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(String.format("%.1f", g.calificacionPromedio), fontSize = 48.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFFC107))
                            Spacer(Modifier.width(16.dp))
                            Column {
                                Row { repeat(5) { i -> Icon(Icons.Default.Star, null, tint = if (i < g.calificacionPromedio.toInt()) Color(0xFFFFC107) else Color.LightGray, modifier = Modifier.size(22.dp)) } }
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
private fun BadgeEstado(icon: ImageVector, color: Color, texto: String) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.background(color, RoundedCornerShape(20.dp)).padding(horizontal = 10.dp, vertical = 3.dp)) {
        Icon(icon, null, tint = Color.White, modifier = Modifier.size(14.dp))
        Spacer(Modifier.width(4.dp))
        Text(texto, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
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
private fun EditField(label: String, value: String, onValueChange: (String) -> Unit, keyboardType: KeyboardType = KeyboardType.Text) {
    OutlinedTextField(
        value         = value,
        onValueChange = onValueChange,
        label         = { Text(label) },
        singleLine    = true,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        shape         = RoundedCornerShape(10.dp),
        colors        = OutlinedTextFieldDefaults.colors(focusedBorderColor = BlueLight, focusedLabelColor = BlueLight),
        modifier      = Modifier.fillMaxWidth()
    )
}

@Composable
private fun StatusBanner(icon: ImageVector, iconColor: Color, bgColor: Color, title: String, subtitle: String) {
    Row(modifier = Modifier.fillMaxWidth().background(bgColor, RoundedCornerShape(12.dp)).padding(14.dp), verticalAlignment = Alignment.Top) {
        Icon(icon, null, tint = iconColor, modifier = Modifier.size(28.dp))
        Spacer(Modifier.width(12.dp))
        Column {
            Text(title, fontWeight = FontWeight.Bold, color = iconColor, fontSize = 15.sp)
            Spacer(Modifier.height(4.dp))
            Text(subtitle, fontSize = 13.sp, color = TextGray, lineHeight = 18.sp)
        }
    }
}
