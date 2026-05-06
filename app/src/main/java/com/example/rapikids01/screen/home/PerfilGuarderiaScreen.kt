package com.example.rapikids01.screen.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.rapikids01.data.model.Guarderia
import com.example.rapikids01.data.model.Resena
import com.example.rapikids01.data.supabase.SupabaseClient.client
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import java.text.NumberFormat
import java.util.Locale

@Serializable
data class ResenaInsert(
    @SerialName("guarderia_uid") val guarderiaUid: String,
    @SerialName("padre_uid")     val padreUid: String,
    val calificacion: Int,
    val comentario: String
)

data class PerfilGuarderiaUiState(
    val guarderia: Guarderia?  = null,
    val resenas: List<Resena>  = emptyList(),
    val isLoading: Boolean     = false,
    val isSending: Boolean     = false,
    val error: String?         = null,
    val successMsg: String?    = null,
    val yaCalificó: Boolean    = false
)

class PerfilGuarderiaViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(PerfilGuarderiaUiState())
    val uiState: StateFlow<PerfilGuarderiaUiState> = _uiState.asStateFlow()

    fun cargar(guarderiaUid: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val guarderia = client.postgrest["guarderias"]
                    .select()
                    .decodeList<Guarderia>()
                    .firstOrNull { it.uid == guarderiaUid }
                    ?: throw Exception("Guardería no encontrada")

                val resenas = try {
                    client.postgrest["resenas"]
                        .select {
                            filter { eq("guarderia_uid", guarderiaUid) }
                            order("created_at", Order.DESCENDING)
                        }
                        .decodeList<Resena>()
                } catch (_: Exception) { emptyList() }

                val padreUid   = client.auth.currentUserOrNull()?.id?.toString() ?: ""
                val yaCalificó = resenas.any { it.padreUid == padreUid }

                _uiState.update {
                    it.copy(isLoading = false, guarderia = guarderia, resenas = resenas, yaCalificó = yaCalificó)
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun enviarResena(guarderiaUid: String, calificacion: Int, comentario: String) {
        if (comentario.isBlank()) { _uiState.update { it.copy(error = "Escribe un comentario") }; return }
        viewModelScope.launch {
            _uiState.update { it.copy(isSending = true, error = null) }
            try {
                val padreUid = client.auth.currentUserOrNull()?.id?.toString()
                    ?: throw Exception("No autenticado")

                val nombrePadre = try {
                    client.postgrest["users"]
                        .select(io.github.jan.supabase.postgrest.query.Columns.list("nombre")) {
                            filter { eq("uid", padreUid) }
                        }
                        .decodeList<Map<String, String>>()
                        .firstOrNull()?.get("nombre") ?: "Padre"
                } catch (_: Exception) { "Padre" }

                client.postgrest["resenas"].insert(
                    buildJsonObject {
                        put("guarderia_uid", JsonPrimitive(guarderiaUid))
                        put("padre_uid",     JsonPrimitive(padreUid))
                        put("calificacion",  JsonPrimitive(calificacion))
                        put("comentario",    JsonPrimitive(comentario))
                        put("nombre_padre",  JsonPrimitive(nombrePadre))
                    }
                )

                val todasResenas = client.postgrest["resenas"]
                    .select { filter { eq("guarderia_uid", guarderiaUid) } }
                    .decodeList<Resena>()

                val promedio = todasResenas.map { it.calificacion }.average()
                val total    = todasResenas.size

                client.postgrest["guarderias"].update(
                    buildJsonObject {
                        put("calificacion_promedio", JsonPrimitive(promedio))
                        put("total_resenas", JsonPrimitive(total))
                    }
                ) { filter { eq("uid", guarderiaUid) } }

                _uiState.update { it.copy(isSending = false, successMsg = "¡Reseña publicada!", yaCalificó = true) }
                cargar(guarderiaUid)
            } catch (e: Exception) {
                _uiState.update { it.copy(isSending = false, error = "Error: ${e.message}") }
            }
        }
    }

    fun clearMessages() = _uiState.update { it.copy(error = null, successMsg = null) }
}

private val Purple    = Color(0xFF6A4DBA)
private val StarColor = Color(0xFFFFC107)
private val TextDark  = Color(0xFF1A1A2E)
private val TextGray  = Color(0xFF65676B)
private val Teal      = Color(0xFF00897B)

@Composable
fun PerfilGuarderiaScreen(
    guarderia: Guarderia,
    onBack: () -> Unit,
    vm: PerfilGuarderiaViewModel = viewModel()
) {
    val state             = vm.uiState.collectAsState().value
    val snackbarHostState = remember { SnackbarHostState() }
    var showDialog        by remember { mutableStateOf(false) }

    LaunchedEffect(guarderia.uid) { vm.cargar(guarderia.uid) }
    LaunchedEffect(state.successMsg) { state.successMsg?.let { snackbarHostState.showSnackbar(it); vm.clearMessages() } }
    LaunchedEffect(state.error)      { state.error?.let      { snackbarHostState.showSnackbar("⚠️ $it"); vm.clearMessages() } }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }, containerColor = Color(0xFFF5F5F5)) { padding ->

        if (state.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = Purple) }
            return@Scaffold
        }

        val g = state.guarderia ?: guarderia

        LazyColumn(modifier = Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(bottom = 40.dp)) {
            item {
                Box(modifier = Modifier.fillMaxWidth().height(220.dp)) {
                    Box(modifier = Modifier.fillMaxWidth().height(160.dp).background(Brush.linearGradient(listOf(Purple, Color(0xFF9C27B0)))))
                    IconButton(onClick = onBack, modifier = Modifier.padding(8.dp).background(Color.Black.copy(alpha = 0.3f), CircleShape)) {
                        Icon(Icons.Default.ArrowBack, null, tint = Color.White)
                    }
                    Box(modifier = Modifier.size(90.dp).align(Alignment.BottomStart).padding(start = 20.dp).clip(CircleShape).background(Color.White)) {
                        if (g.fotoUrl.isNotBlank()) {
                            AsyncImage(model = g.fotoUrl, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize().clip(CircleShape))
                        } else {
                            Box(modifier = Modifier.fillMaxSize().background(Purple, CircleShape), contentAlignment = Alignment.Center) {
                                Text(g.nombreGuarderia.take(1).uppercase(), color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            item {
                Column(modifier = Modifier.background(Color.White).padding(horizontal = 20.dp, vertical = 14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(g.nombreGuarderia, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = TextDark, modifier = Modifier.weight(1f))
                        if (g.verificada) {
                            Row(modifier = Modifier.background(Color(0xFFE8F5E9), RoundedCornerShape(20.dp)).padding(horizontal = 10.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Verified, null, tint = Color(0xFF43A047), modifier = Modifier.size(14.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Verificada", color = Color(0xFF43A047), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        repeat(5) { i ->
                            Icon(
                                Icons.Default.Star, null,
                                tint = if (i.toDouble() < g.calificacionPromedio) StarColor else Color.LightGray,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(Modifier.width(8.dp))
                        Text(String.format("%.1f", g.calificacionPromedio), fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextDark)
                        Text("  (${g.totalResenas} reseñas)", color = TextGray, fontSize = 13.sp)
                    }
                }
                Spacer(Modifier.height(8.dp))
            }
            if (g.fotos.isNotEmpty()) {
                item {
                    Card(shape = RoundedCornerShape(0.dp), colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Fotos", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextDark)
                            Spacer(Modifier.height(12.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                g.fotos.take(3).forEach { fotoUrl ->
                                    AsyncImage(
                                        model = fotoUrl, contentDescription = null,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.weight(1f).height(100.dp).clip(RoundedCornerShape(10.dp))
                                    )
                                }
                            }
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                }
            }
            if (g.descripcion.isNotBlank()) {
                item {
                    Card(shape = RoundedCornerShape(0.dp), colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Description, null, tint = Purple, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Sobre nosotros", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextDark)
                            }
                            Spacer(Modifier.height(10.dp))
                            Text(g.descripcion, fontSize = 14.sp, color = TextDark, lineHeight = 20.sp)
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                }
            }
            item {
                Card(shape = RoundedCornerShape(0.dp), colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Información", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextDark)
                        Spacer(Modifier.height(14.dp))
                        InfoFilaPerfil(Icons.Default.LocationOn, "Dirección", g.direccion)
                        Spacer(Modifier.height(10.dp))
                        InfoFilaPerfil(Icons.Default.Phone,      "Teléfono",  g.telefono)
                        Spacer(Modifier.height(10.dp))
                        InfoFilaPerfil(Icons.Default.Email,      "Correo",    g.email)
                    }
                }
                Spacer(Modifier.height(8.dp))
            }
            if (g.precioMensual > 0) {
                item {
                    Card(shape = RoundedCornerShape(0.dp), colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.fillMaxWidth()) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AttachMoney, null, tint = Teal, modifier = Modifier.size(22.dp))
                            Spacer(Modifier.width(10.dp))
                            Column {
                                Text("Precio mensual", fontSize = 12.sp, color = TextGray)
                                val formato = NumberFormat.getNumberInstance(Locale("es", "CO"))
                                Text("$ ${formato.format(g.precioMensual)} COP / mes", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Teal)
                            }
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                }
            }
            if (g.horaApertura.isNotBlank() || g.horaCierre.isNotBlank() || g.diasAtencion.isNotBlank() || g.jornada.isNotBlank()) {
                item {
                    Card(shape = RoundedCornerShape(0.dp), colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Schedule, null, tint = Purple, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Horarios", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextDark)
                            }
                            Spacer(Modifier.height(14.dp))
                            if (g.horaApertura.isNotBlank()) { InfoFilaPerfil(Icons.Default.AccessTime,    "Apertura", g.horaApertura); Spacer(Modifier.height(8.dp)) }
                            if (g.horaCierre.isNotBlank())   { InfoFilaPerfil(Icons.Default.AccessTime,    "Cierre",   g.horaCierre);   Spacer(Modifier.height(8.dp)) }
                            if (g.diasAtencion.isNotBlank()) { InfoFilaPerfil(Icons.Default.CalendarMonth, "Días",     g.diasAtencion); Spacer(Modifier.height(8.dp)) }
                            if (g.jornada.isNotBlank())      { InfoFilaPerfil(Icons.Default.WbSunny,       "Jornada",  g.jornada) }
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                }
            }
            item {
                Card(shape = RoundedCornerShape(0.dp), colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.fillMaxWidth()) {
                    Box(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
                        if (state.yaCalificó) {
                            Row(modifier = Modifier.fillMaxWidth().background(Color(0xFFE8F5E9), RoundedCornerShape(12.dp)).padding(14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                                Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF43A047))
                                Spacer(Modifier.width(8.dp))
                                Text("Ya enviaste tu reseña", color = Color(0xFF43A047), fontWeight = FontWeight.SemiBold)
                            }
                        } else {
                            Button(onClick = { showDialog = true }, shape = RoundedCornerShape(14.dp), colors = ButtonDefaults.buttonColors(containerColor = Purple), modifier = Modifier.fillMaxWidth().height(52.dp)) {
                                Icon(Icons.Default.Star, null, tint = Color.White)
                                Spacer(Modifier.width(8.dp))
                                Text("Calificar y comentar", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 15.sp)
                            }
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
            }
            item {
                Box(modifier = Modifier.fillMaxWidth().background(Color.White).padding(horizontal = 20.dp, vertical = 14.dp)) {
                    Text("Reseñas", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextDark)
                }
            }

            if (state.resenas.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().background(Color.White).padding(32.dp), contentAlignment = Alignment.Center) {
                        Text("Aún no hay reseñas. ¡Sé el primero!", color = TextGray, textAlign = TextAlign.Center)
                    }
                }
            } else {
                items(state.resenas) { resena -> ResenaCardPerfil(resena) }
            }
        }
    }

    if (showDialog) {
        CalificarDialogPerfil(
            onDismiss = { showDialog = false },
            onEnviar  = { cal, com -> vm.enviarResena(guarderia.uid, cal, com); showDialog = false },
            isSending = state.isSending
        )
    }
}

@Composable
private fun InfoFilaPerfil(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.Top) {
        Icon(icon, null, tint = Purple, modifier = Modifier.size(20.dp).padding(top = 2.dp))
        Spacer(Modifier.width(12.dp))
        Column {
            Text(label, color = TextGray, fontSize = 12.sp)
            Text(value.ifBlank { "-" }, color = TextDark, fontSize = 14.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun ResenaCardPerfil(resena: Resena) {
    Card(shape = RoundedCornerShape(0.dp), colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.fillMaxWidth().padding(top = 1.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(40.dp).background(Purple.copy(alpha = 0.1f), CircleShape), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Person, null, tint = Purple, modifier = Modifier.size(22.dp))
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = resena.nombrePadre.ifBlank { "Padre / Acudiente" },
                        fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = TextDark
                    )
                    Text(resena.createdAt.take(10), color = TextGray, fontSize = 11.sp)
                }
                Row {
                    repeat(5) { i ->
                        Icon(Icons.Default.Star, null, tint = if (i < resena.calificacion) StarColor else Color.LightGray, modifier = Modifier.size(16.dp))
                    }
                }
            }
            Spacer(Modifier.height(10.dp))
            Text(resena.comentario, fontSize = 14.sp, color = TextDark, lineHeight = 20.sp)
        }
        HorizontalDivider(color = Color(0xFFF0F0F0))
    }
}

@Composable
private fun CalificarDialogPerfil(onDismiss: () -> Unit, onEnviar: (Int, String) -> Unit, isSending: Boolean) {
    var calificacion by remember { mutableStateOf(5) }
    var comentario   by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("Califica esta guardería", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = TextDark)
                Spacer(Modifier.height(4.dp))
                Text("Tu opinión ayuda a otros padres", color = TextGray, fontSize = 13.sp)
                Spacer(Modifier.height(16.dp))

                Text("Calificación", color = TextGray, fontSize = 12.sp)
                Spacer(Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
                    repeat(5) { i ->
                        IconButton(onClick = { calificacion = i + 1 }, modifier = Modifier.size(48.dp)) {
                            Icon(Icons.Default.Star, null, tint = if (i < calificacion) StarColor else Color.LightGray, modifier = Modifier.size(36.dp))
                        }
                    }
                }
                Text(
                    text = when (calificacion) { 1 -> "⭐ Muy malo"; 2 -> "⭐⭐ Malo"; 3 -> "⭐⭐⭐ Regular"; 4 -> "⭐⭐⭐⭐ Bueno"; else -> "⭐⭐⭐⭐⭐ Excelente" },
                    color = Purple, fontSize = 13.sp, fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(16.dp))

                OutlinedTextField(
                    value = comentario, onValueChange = { comentario = it },
                    label = { Text("Escribe tu comentario", color = TextGray) },
                    placeholder = { Text("Cuéntanos tu experiencia…", color = Color.LightGray) },
                    minLines = 3, maxLines = 5, shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Purple, focusedLabelColor = Purple, focusedTextColor = TextDark, unfocusedTextColor = TextDark),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(20.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Cancelar", color = TextGray) }
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = { onEnviar(calificacion, comentario) },
                        enabled = !isSending && comentario.isNotBlank(),
                        colors  = ButtonDefaults.buttonColors(containerColor = Purple),
                        shape   = RoundedCornerShape(10.dp)
                    ) {
                        if (isSending) CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp, modifier = Modifier.size(18.dp))
                        else Text("Publicar reseña", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

