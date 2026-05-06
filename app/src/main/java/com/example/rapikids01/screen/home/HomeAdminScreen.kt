package com.example.rapikids01.screen.home

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.rapikids01.data.model.Guarderia
import com.example.rapikids01.viewmodel.AdminTab
import com.example.rapikids01.viewmodel.AdminViewModel

private val DarkBg    = Color(0xFF1A1A2E)
private val DarkCard  = Color(0xFF16213E)
private val RedAccent = Color(0xFFE53935)
private val RedLight  = Color(0xFFEF9A9A)
private val GreenOk   = Color(0xFF43A047)
private val AmberWarn = Color(0xFFFFA000)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeAdminScreen(
    onLogout: () -> Unit = {},
    viewModel: AdminViewModel = viewModel()
) {
    val state             = viewModel.uiState.collectAsState().value
    val context           = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    var guarderiaDetalle   by remember { mutableStateOf<Guarderia?>(null) }
    var showRechazoDialog  by remember { mutableStateOf(false) }
    var guarderiaArechazar by remember { mutableStateOf<Guarderia?>(null) }
    var motivoRechazo      by remember { mutableStateOf("") }

    LaunchedEffect(Unit) { viewModel.cargarDatos() }
    LaunchedEffect(state.successMsg) {
        state.successMsg?.let { snackbarHostState.showSnackbar(it); viewModel.clearMessages() }
    }
    LaunchedEffect(state.error) {
        state.error?.let { snackbarHostState.showSnackbar("⚠️ $it"); viewModel.clearMessages() }
    }
    if (guarderiaDetalle != null) {
        GuarderiaDetalleAdmin(
            guarderia      = guarderiaDetalle!!,
            onBack         = { guarderiaDetalle = null },
            onAprobar      = {
                viewModel.aprobarGuarderia(guarderiaDetalle!!.uid)
                guarderiaDetalle = null
            },
            onRechazar     = {
                guarderiaArechazar = guarderiaDetalle
                showRechazoDialog  = true
            },
            onVerDocumento = { url ->
                if (url.isNotBlank()) {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                    context.startActivity(intent)
                }
            }
        )

        if (showRechazoDialog) {
            Dialog(onDismissRequest = { showRechazoDialog = false; motivoRechazo = "" }) {
                Card(
                    shape  = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkCard)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text("Motivo de rechazo", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 17.sp)
                        Spacer(Modifier.height(12.dp))
                        Text(guarderiaArechazar?.nombreGuarderia ?: "", color = RedLight, fontSize = 13.sp)
                        Spacer(Modifier.height(12.dp))
                        OutlinedTextField(
                            value         = motivoRechazo,
                            onValueChange = { motivoRechazo = it },
                            label         = { Text("Escribe el motivo", color = Color.Gray) },
                            minLines      = 3,
                            maxLines      = 5,
                            shape         = RoundedCornerShape(12.dp),
                            colors        = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor      = RedAccent,
                                unfocusedBorderColor    = Color.White.copy(alpha = 0.2f),
                                focusedTextColor        = Color.White,
                                unfocusedTextColor      = Color.White,
                                focusedContainerColor   = DarkBg,
                                unfocusedContainerColor = DarkBg,
                                cursorColor             = RedAccent
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(16.dp))
                        Row(
                            horizontalArrangement = Arrangement.End,
                            modifier              = Modifier.fillMaxWidth()
                        ) {
                            TextButton(onClick = { showRechazoDialog = false; motivoRechazo = "" }) {
                                Text("Cancelar", color = Color.Gray)
                            }
                            Spacer(Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    guarderiaArechazar?.let {
                                        viewModel.rechazarGuarderia(it.uid, motivoRechazo)
                                    }
                                    showRechazoDialog = false
                                    guarderiaDetalle  = null
                                    motivoRechazo     = ""
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = RedAccent),
                                shape  = RoundedCornerShape(10.dp)
                            ) {
                                Text("Rechazar", color = Color.White)
                            }
                        }
                    }
                }
            }
        }
        return
    }

    Scaffold(
        snackbarHost   = { SnackbarHost(snackbarHostState) },
        containerColor = DarkBg,
        topBar         = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.horizontalGradient(listOf(DarkBg, Color(0xFF0F3460))))
                    .statusBarsPadding()
            ) {
                Row(
                    modifier              = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Panel de Control", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Text("Administrador Rapikids", fontSize = 12.sp, color = RedLight)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        IconButton(
                            onClick  = { viewModel.cargarDatos() },
                            modifier = Modifier.background(Color.White.copy(alpha = 0.1f), CircleShape)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = "Actualizar", tint = Color.White)
                        }
                        IconButton(
                            onClick  = onLogout,
                            modifier = Modifier.background(Color.White.copy(alpha = 0.1f), CircleShape)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Salir", tint = RedAccent)
                        }
                    }
                }
            }
        }
    ) { padding ->

        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            Row(
                modifier              = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(modifier = Modifier.weight(1f), label = "Pendientes",  count = state.pendientes.size,              color = AmberWarn)
                StatCard(modifier = Modifier.weight(1f), label = "Total",       count = state.todas.size,                   color = RedAccent)
                StatCard(modifier = Modifier.weight(1f), label = "Verificadas", count = state.todas.count { it.verificada }, color = GreenOk)
            }
            TabRow(
                selectedTabIndex = if (state.tabSeleccionada == AdminTab.PENDIENTES) 0 else 1,
                containerColor   = DarkCard,
                contentColor     = RedAccent,
                indicator        = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(
                            tabPositions[if (state.tabSeleccionada == AdminTab.PENDIENTES) 0 else 1]
                        ),
                        color = RedAccent
                    )
                }
            ) {
                Tab(
                    selected = state.tabSeleccionada == AdminTab.PENDIENTES,
                    onClick  = { viewModel.cambiarTab(AdminTab.PENDIENTES) },
                    text     = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Pendientes", color = Color.White)
                            if (state.pendientes.isNotEmpty()) {
                                Spacer(Modifier.width(6.dp))
                                Badge(containerColor = RedAccent) {
                                    Text("${state.pendientes.size}", color = Color.White, fontSize = 10.sp)
                                }
                            }
                        }
                    }
                )
                Tab(
                    selected = state.tabSeleccionada == AdminTab.TODAS,
                    onClick  = { viewModel.cambiarTab(AdminTab.TODAS) },
                    text     = { Text("Todas", color = Color.White) }
                )
            }
            if (state.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = RedAccent)
                }
            } else {
                val lista = if (state.tabSeleccionada == AdminTab.PENDIENTES)
                    state.pendientes else state.todas

                if (lista.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("✅", fontSize = 48.sp)
                            Spacer(Modifier.height(12.dp))
                            Text(
                                text      = if (state.tabSeleccionada == AdminTab.PENDIENTES)
                                    "No hay guarderías pendientes" else "No hay guarderías registradas",
                                color     = Color.Gray,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        contentPadding      = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(lista, key = { it.uid }) { guarderia ->
                            GuarderiaAdminCard(
                                guarderia    = guarderia,
                                onVerDetalle = { guarderiaDetalle = guarderia }
                            )
                        }
                    }
                }
            }
        }
    }
}
@Composable
private fun GuarderiaAdminCard(
    guarderia: Guarderia,
    onVerDetalle: () -> Unit
) {
    val (estadoColor, estadoTexto, estadoIcon) = when (guarderia.estado) {
        "verificada" -> Triple(Color(0xFF43A047), "Verificada", Icons.Default.CheckCircle)
        "rechazada"  -> Triple(Color(0xFFE53935), "Rechazada",  Icons.Default.Cancel)
        else         -> Triple(Color(0xFFFFA000), "Pendiente",  Icons.Default.HourglassEmpty)
    }

    Card(
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = Color(0xFF16213E)),
        elevation = CardDefaults.cardElevation(4.dp),
        modifier  = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(guarderia.nombreGuarderia, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 16.sp)
                    Spacer(Modifier.height(4.dp))
                    Text(guarderia.direccion, color = Color.Gray, fontSize = 12.sp, maxLines = 1)
                    Spacer(Modifier.height(4.dp))
                    Text("NIT: ${guarderia.nit}", color = Color.Gray, fontSize = 12.sp)
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier          = Modifier
                        .background(estadoColor.copy(alpha = 0.15f), RoundedCornerShape(20.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Icon(estadoIcon, null, tint = estadoColor, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(estadoTexto, color = estadoColor, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            }
            Spacer(Modifier.height(12.dp))
            Button(
                onClick  = onVerDetalle,
                shape    = RoundedCornerShape(10.dp),
                colors   = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F3460)),
                modifier = Modifier.fillMaxWidth().height(40.dp)
            ) {
                Icon(Icons.Default.Visibility, null, tint = Color.White, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("Ver detalle", color = Color.White, fontSize = 13.sp)
            }
        }
    }
}
@Composable
private fun GuarderiaDetalleAdmin(
    guarderia: Guarderia,
    onBack: () -> Unit,
    onAprobar: () -> Unit,
    onRechazar: () -> Unit,
    onVerDocumento: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.horizontalGradient(listOf(DarkBg, Color(0xFF0F3460))))
                .statusBarsPadding()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier          = Modifier.padding(8.dp)
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, null, tint = Color.White)
                }
                Text("Detalle de Guardería", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 17.sp)
            }
        }

        LazyColumn(
            contentPadding      = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Card(
                    shape  = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkCard)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(guarderia.nombreGuarderia, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 20.sp)
                        Spacer(Modifier.height(16.dp))
                        DetalleRow("📍 Dirección", guarderia.direccion)
                        DetalleRow("📞 Teléfono",  guarderia.telefono)
                        DetalleRow("✉️ Correo",    guarderia.email)
                        DetalleRow("🏢 NIT",       guarderia.nit)
                        DetalleRow("📅 Registro",  guarderia.createdAt.take(10))
                    }
                }
            }

            item {
                val (estadoColor, estadoTexto) = when (guarderia.estado) {
                    "verificada" -> Pair(GreenOk,   "✅ Verificada")
                    "rechazada"  -> Pair(RedAccent, "❌ Rechazada")
                    else         -> Pair(AmberWarn, "⏳ Pendiente de revisión")
                }
                Card(
                    shape  = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = estadoColor.copy(alpha = 0.1f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Estado actual", color = Color.Gray, fontSize = 12.sp)
                        Spacer(Modifier.height(4.dp))
                        Text(estadoTexto, color = estadoColor, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        if (guarderia.estado == "rechazada" && guarderia.mensajeRechazo.isNotBlank()) {
                            Spacer(Modifier.height(8.dp))
                            Text("Motivo: ${guarderia.mensajeRechazo}", color = RedLight, fontSize = 13.sp)
                        }
                    }
                }
            }

            item {
                Card(
                    shape  = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkCard)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Documento de verificación", color = Color.Gray, fontSize = 12.sp)
                        Spacer(Modifier.height(10.dp))
                        if (guarderia.documentoUrl.isNotBlank()) {
                            Button(
                                onClick  = { onVerDocumento(guarderia.documentoUrl) },
                                shape    = RoundedCornerShape(10.dp),
                                colors   = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F3460)),
                                modifier = Modifier.fillMaxWidth().height(44.dp)
                            ) {
                                Icon(Icons.Default.OpenInNew, null, tint = Color.White, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Ver documento", color = Color.White)
                            }
                        } else {
                            Text("No se subió ningún documento", color = Color.Gray, fontSize = 13.sp)
                        }
                    }
                }
            }

            if (guarderia.estado == "pendiente") {
                item {
                    Row(
                        modifier              = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick  = onRechazar,
                            shape    = RoundedCornerShape(14.dp),
                            colors   = ButtonDefaults.buttonColors(containerColor = RedAccent),
                            modifier = Modifier.weight(1f).height(52.dp)
                        ) {
                            Icon(Icons.Default.Close, null, tint = Color.White, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Rechazar", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                        Button(
                            onClick  = onAprobar,
                            shape    = RoundedCornerShape(14.dp),
                            colors   = ButtonDefaults.buttonColors(containerColor = GreenOk),
                            modifier = Modifier.weight(1f).height(52.dp)
                        ) {
                            Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Aprobar", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DetalleRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Text(label, color = Color.Gray, fontSize = 13.sp, modifier = Modifier.width(120.dp))
        Text(value.ifBlank { "-" }, color = Color.White, fontSize = 13.sp, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun StatCard(modifier: Modifier, label: String, count: Int, color: Color) {
    Card(
        shape    = RoundedCornerShape(12.dp),
        colors   = CardDefaults.cardColors(containerColor = DarkCard),
        modifier = modifier
    ) {
        Column(
            modifier            = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(count.toString(), fontSize = 28.sp, fontWeight = FontWeight.Bold, color = color)
            Text(label, fontSize = 11.sp, color = Color.Gray)
        }
    }
}

@Composable
private fun tabIndicatorOffset(tabPosition: TabPosition): Modifier =
    Modifier.wrapContentSize(Alignment.BottomStart)
        .offset(x = tabPosition.left)
        .width(tabPosition.width)
        .height(3.dp)