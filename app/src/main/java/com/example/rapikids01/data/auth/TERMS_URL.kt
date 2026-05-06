package com.example.rapikids01.data.auth

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import com.example.rapikids01.navigation.Routes
import com.example.rapikids01.viewmodel.AuthViewModel
import org.maplibre.android.MapLibre
import org.maplibre.android.annotations.MarkerOptions
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.geometry.LatLng as MLatLng
import org.maplibre.android.maps.MapView

private const val TERMS_URL = "https://juan-montoya-22.github.io/Rapikids-0.1/"
private val Purple     = Color(0xFF6A4DBA)
private val GreenColor = Color(0xFF4CAF50)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterGuarderiaScreen(
    navController: NavController,
    authViewModel: AuthViewModel
) {
    val state   by authViewModel.registerGuarderiaState.collectAsState()
    val context = LocalContext.current


    var showPassword        by remember { mutableStateOf(false) }
    var showConfirmPassword by remember { mutableStateOf(false) }
    var aceptaTerminos      by remember { mutableStateOf(false) }
    var terminosError       by remember { mutableStateOf(false) }

    val docLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { authViewModel.onGuarderiaDocumentoUriChange(it.toString()) }
    }

    LaunchedEffect(state.success) {
        if (state.success) {
            navController.navigate(Routes.LOGIN_GUARDERIA) {
                popUpTo(Routes.REGISTER_GUARDERIA) { inclusive = true }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Registro de Guardería", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor             = Purple,
                    titleContentColor          = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            OutlinedTextField(
                value         = state.nombreGuarderia,
                onValueChange = authViewModel::onGuarderiaNombreChange,
                label         = { Text("Nombre de la guardería") },
                singleLine    = true,
                modifier      = Modifier.fillMaxWidth(),
                shape         = RoundedCornerShape(12.dp)
            )
            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value         = state.direccion,
                onValueChange = authViewModel::onGuarderiaDireccionChange,
                label         = { Text("Dirección") },
                singleLine    = true,
                modifier      = Modifier.fillMaxWidth(),
                shape         = RoundedCornerShape(12.dp)
            )
            Spacer(Modifier.height(12.dp))

            Text("Ubicación en el mapa", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            Text(
                "Toca el punto exacto donde está tu guardería",
                fontSize = 12.sp, color = Color.Gray
            )
            Spacer(Modifier.height(8.dp))
            Card(
                shape     = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(2.dp),
                modifier  = Modifier
                    .fillMaxWidth()
                    .height(320.dp)
            ) {
                SelectorUbicacionMapa(
                    ubicacionMarcada = state.latitud != null,
                    onUbicacionSeleccionada = { lat, lng ->
                        authViewModel.onGuarderiaCoordenadaChange(lat, lng)
                    }
                )
            }
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value         = state.nit,
                onValueChange = authViewModel::onGuarderiaNitChange,
                label         = { Text("NIT") },
                singleLine    = true,
                modifier      = Modifier.fillMaxWidth(),
                shape         = RoundedCornerShape(12.dp)
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value           = state.telefono,
                onValueChange   = authViewModel::onGuarderiaTelefonoChange,
                label           = { Text("Teléfono de contacto") },
                singleLine      = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier        = Modifier.fillMaxWidth(),
                shape           = RoundedCornerShape(12.dp)
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value           = state.email,
                onValueChange   = authViewModel::onGuarderiaEmailChange,
                label           = { Text("Correo electrónico") },
                singleLine      = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier        = Modifier.fillMaxWidth(),
                shape           = RoundedCornerShape(12.dp)
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value                = state.password,
                onValueChange        = authViewModel::onGuarderiaPasswordChange,
                label                = { Text("Contraseña") },
                singleLine           = true,
                visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon         = {
                    IconButton(onClick = { showPassword = !showPassword }) {
                        Icon(if (showPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff, null)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape    = RoundedCornerShape(12.dp)
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value                = state.confirmPassword,
                onValueChange        = authViewModel::onGuarderiaConfirmPasswordChange,
                label                = { Text("Confirmar contraseña") },
                singleLine           = true,
                visualTransformation = if (showConfirmPassword) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon         = {
                    IconButton(onClick = { showConfirmPassword = !showConfirmPassword }) {
                        Icon(if (showConfirmPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff, null)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape    = RoundedCornerShape(12.dp)
            )
            Spacer(Modifier.height(16.dp))
            Text("Documento de verificación", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            Text(
                "Sube el registro mercantil o licencia de funcionamiento (PDF o imagen)",
                fontSize = 12.sp, color = Color.Gray
            )
            Spacer(Modifier.height(8.dp))

            val docSeleccionado = state.documentoUri.isNotBlank()
            OutlinedCard(
                shape    = RoundedCornerShape(12.dp),
                border   = BorderStroke(1.dp, if (docSeleccionado) GreenColor else Color.Gray.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth().clickable { docLauncher.launch("*/*") }
            ) {
                Row(
                    modifier              = Modifier.padding(16.dp),
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        if (docSeleccionado) Icons.Default.CheckCircle else Icons.Default.AttachFile,
                        null,
                        tint = if (docSeleccionado) GreenColor else Color.Gray
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text  = if (docSeleccionado) "Documento seleccionado ✓" else "Toca para seleccionar archivo",
                        color = if (docSeleccionado) GreenColor else Color.Gray,
                        fontSize = 13.sp
                    )
                }
            }
            Spacer(Modifier.height(20.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier          = Modifier.fillMaxWidth()
            ) {
                Checkbox(
                    checked         = aceptaTerminos,
                    onCheckedChange = { aceptaTerminos = it; if (it) terminosError = false },
                    colors          = CheckboxDefaults.colors(
                        checkedColor   = Purple,
                        checkmarkColor = Color.White,
                        uncheckedColor = if (terminosError) Color(0xFFB71C1C) else Color.Gray
                    )
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = buildAnnotatedString {
                        append("He leído y acepto los ")
                        withStyle(SpanStyle(
                            color          = Purple,
                            fontWeight     = FontWeight.SemiBold,
                            textDecoration = TextDecoration.Underline
                        )) { append("Términos y Condiciones") }
                        append(" de Rapikids")
                    },
                    fontSize = 13.sp,
                    modifier = Modifier.weight(1f).clickable {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(TERMS_URL))
                        context.startActivity(intent)
                    }
                )
            }

            if (terminosError) {
                Text(
                    "⚠️ Debes aceptar los términos y condiciones para continuar",
                    color    = Color(0xFFB71C1C),
                    fontSize = 12.sp,
                    modifier = Modifier.padding(start = 12.dp, top = 2.dp)
                )
            }
            Spacer(Modifier.height(16.dp))
            if (state.error != null) {
                Card(
                    colors   = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                    shape    = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "⚠️ ${state.error}",
                        color    = Color(0xFFB71C1C),
                        modifier = Modifier.padding(12.dp),
                        fontSize = 13.sp
                    )
                }
                Spacer(Modifier.height(12.dp))
            }
            Button(
                onClick = {
                    if (!aceptaTerminos) { terminosError = true; return@Button }
                    terminosError = false
                    authViewModel.registerGuarderia(context)
                },
                enabled  = !state.isLoading,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape    = RoundedCornerShape(12.dp),
                colors   = ButtonDefaults.buttonColors(
                    containerColor = if (aceptaTerminos) Purple else Color.Gray
                )
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp, modifier = Modifier.size(22.dp))
                } else {
                    Text("Enviar solicitud", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            if (!aceptaTerminos) {
                Spacer(Modifier.height(8.dp))
                Text(
                    "Debes aceptar los términos y condiciones para registrarte",
                    color     = Color.Gray,
                    fontSize  = 12.sp,
                    textAlign = TextAlign.Center,
                    modifier  = Modifier.fillMaxWidth()
                )
            }
            Spacer(Modifier.height(16.dp))

            TextButton(onClick = { navController.navigate(Routes.LOGIN_GUARDERIA) }) {
                Text("¿Ya tienes cuenta? Inicia sesión", color = Purple, fontSize = 14.sp)
            }
        }
    }
}

@Composable
fun SelectorUbicacionMapa(
    ubicacionMarcada: Boolean = false,
    onUbicacionSeleccionada: (Double, Double) -> Unit
) {
    val context = LocalContext.current
    var marcadorActual by remember { mutableStateOf<org.maplibre.android.annotations.Marker?>(null) }

    Column {
        Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
            AndroidView(
                factory = { ctx ->
                    MapLibre.getInstance(ctx)
                    MapView(ctx).apply {
                        getMapAsync { map ->
                            map.setStyle(
                                org.maplibre.android.maps.Style.Builder()
                                    .fromUri("https://tiles.openfreemap.org/styles/liberty")
                            ) {
                                // Centro inicial: Bogotá
                                map.cameraPosition = CameraPosition.Builder()
                                    .target(MLatLng(4.7110, -74.0721))
                                    .zoom(12.0)
                                    .build()

                                // Click mueve el pin
                                map.addOnMapClickListener { latLng ->
                                    marcadorActual?.let { map.removeMarker(it) }
                                    marcadorActual = map.addMarker(
                                        MarkerOptions()
                                            .position(latLng)
                                            .title("Ubicación de la guardería")
                                    )
                                    onUbicacionSeleccionada(latLng.latitude, latLng.longitude)
                                    true
                                }
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
            Surface(
                color    = Color.Black.copy(alpha = 0.6f),
                shape    = RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp),
                modifier = Modifier.align(Alignment.TopCenter).fillMaxWidth()
            ) {
                Text(
                    "Toca el mapa para marcar la ubicación exacta",
                    color     = Color.White,
                    fontSize  = 12.sp,
                    textAlign = TextAlign.Center,
                    modifier  = Modifier.padding(8.dp)
                )
            }
        }
        if (ubicacionMarcada) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFE8F5E9), RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp))
                    .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.LocationOn, null, tint = GreenColor, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text(
                    "Ubicación marcada ✓",
                    color      = Color(0xFF2E7D32),
                    fontSize   = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFFFF3E0), RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp))
                    .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Warning, null, tint = Color(0xFFE65100), modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text(
                    "Debes marcar la ubicación para continuar",
                    color    = Color(0xFFE65100),
                    fontSize = 12.sp
                )
            }
        }
    }
}