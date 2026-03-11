package com.example.rapikids01.screen.register

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.rapikids01.navigation.Routes
import com.example.rapikids01.viewmodel.AuthViewModel

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

    val primaryColor = Color(0xFF6A4DBA)
    val greenColor   = Color(0xFF4CAF50)

    // Selector de documento (PDF o imagen)
    val docLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { authViewModel.onGuarderiaDocumentoUriChange(it.toString()) }
    }

    // Navegar al éxito
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
                    containerColor         = primaryColor,
                    titleContentColor      = Color.White,
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
                        Icon(
                            if (showPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = null
                        )
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
                        Icon(
                            if (showConfirmPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = null
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape    = RoundedCornerShape(12.dp)
            )
            Spacer(Modifier.height(16.dp))

            // ── Selector de documento ──────────────────────────────────
            Text(
                text       = "Documento de verificación",
                fontWeight = FontWeight.SemiBold,
                fontSize   = 14.sp
            )
            Text(
                text     = "Sube el registro mercantil o licencia de funcionamiento (PDF o imagen)",
                fontSize = 12.sp,
                color    = Color.Gray
            )
            Spacer(Modifier.height(8.dp))

            val docSeleccionado = state.documentoUri.isNotBlank()
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { docLauncher.launch("*/*") }
                    .background(
                        if (docSeleccionado) greenColor.copy(alpha = 0.08f) else Color.Transparent,
                        RoundedCornerShape(12.dp)
                    )
                    .then(
                        Modifier.padding(0.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                OutlinedCard(
                    shape  = RoundedCornerShape(12.dp),
                    border = BorderStroke(
                        1.dp,
                        if (docSeleccionado) greenColor else Color.Gray.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier            = Modifier.padding(16.dp),
                        verticalAlignment   = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = if (docSeleccionado) Icons.Default.CheckCircle else Icons.Default.AttachFile,
                            contentDescription = null,
                            tint = if (docSeleccionado) greenColor else Color.Gray
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text  = if (docSeleccionado)
                                state.documentoUri.substringAfterLast("/").take(30)
                            else "Toca para seleccionar archivo",
                            color = if (docSeleccionado) greenColor else Color.Gray,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // ── Error ──────────────────────────────────────────────────
            if (state.error != null) {
                Card(
                    colors   = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                    shape    = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text     = "⚠️ ${state.error}",
                        color    = Color(0xFFB71C1C),
                        modifier = Modifier.padding(12.dp),
                        fontSize = 13.sp
                    )
                }
                Spacer(Modifier.height(12.dp))
            }

            // ── Botón registrar ────────────────────────────────────────
            Button(
                onClick  = { authViewModel.registerGuarderia(context) }, // ← pasa context
                enabled  = !state.isLoading,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape    = RoundedCornerShape(12.dp),
                colors   = ButtonDefaults.buttonColors(containerColor = primaryColor)
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(
                        color       = Color.White,
                        strokeWidth = 2.dp,
                        modifier    = Modifier.size(22.dp)
                    )
                } else {
                    Text("Enviar solicitud", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}
