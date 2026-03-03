package com.example.rapikids01.screen.register

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.rapikids01.navigation.Routes
import com.example.rapikids01.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterGuarderiaScreen(
    navController: NavController,
    authViewModel: AuthViewModel = viewModel()
) {
    val state by authViewModel.registerGuarderiaState.collectAsState()
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmVisible  by remember { mutableStateOf(false) }
    var selectedFileName by remember { mutableStateOf<String?>(null) }

    val primaryColor = Color(0xFF6A4DBA)

    // ── Selector de documento (PDF, imágenes) ──────────────────────────
    val documentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            authViewModel.onGuarderiaDocumentoUriChange(it.toString())
            selectedFileName = it.lastPathSegment ?: "Documento seleccionado"
        }
    }

    // Navegar al login cuando el registro es exitoso
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
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = primaryColor,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(Modifier.height(24.dp))

            Text(
                text = "🏫 Registra tu guardería",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = primaryColor
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = "Tu cuenta será revisada antes de ser activada",
                fontSize = 13.sp,
                color = Color.Gray
            )

            Spacer(Modifier.height(24.dp))

            // ── Nombre de la guardería ─────────────────────────────────
            OutlinedTextField(
                value = state.nombreGuarderia,
                onValueChange = authViewModel::onGuarderiaNombreChange,
                label = { Text("Nombre de la guardería") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(Modifier.height(12.dp))

            // ── Dirección ─────────────────────────────────────────────
            OutlinedTextField(
                value = state.direccion,
                onValueChange = authViewModel::onGuarderiaDireccionChange,
                label = { Text("Dirección") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(Modifier.height(12.dp))

            // ── NIT ───────────────────────────────────────────────────
            OutlinedTextField(
                value = state.nit,
                onValueChange = authViewModel::onGuarderiaNitChange,
                label = { Text("NIT") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(Modifier.height(12.dp))

            // ── Teléfono ───────────────────────────────────────────────
            OutlinedTextField(
                value = state.telefono,
                onValueChange = authViewModel::onGuarderiaTelefonoChange,
                label = { Text("Teléfono de contacto") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(Modifier.height(12.dp))

            // ── Email ──────────────────────────────────────────────────
            OutlinedTextField(
                value = state.email,
                onValueChange = authViewModel::onGuarderiaEmailChange,
                label = { Text("Correo electrónico") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(Modifier.height(12.dp))

            // ── Contraseña ─────────────────────────────────────────────
            OutlinedTextField(
                value = state.password,
                onValueChange = authViewModel::onGuarderiaPasswordChange,
                label = { Text("Contraseña") },
                singleLine = true,
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = null
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(Modifier.height(12.dp))

            // ── Confirmar contraseña ───────────────────────────────────
            OutlinedTextField(
                value = state.confirmPassword,
                onValueChange = authViewModel::onGuarderiaConfirmPasswordChange,
                label = { Text("Confirmar contraseña") },
                singleLine = true,
                visualTransformation = if (confirmVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailingIcon = {
                    IconButton(onClick = { confirmVisible = !confirmVisible }) {
                        Icon(
                            if (confirmVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = null
                        )
                    }
                },
                isError = state.confirmPassword.isNotEmpty() && state.password != state.confirmPassword,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(Modifier.height(20.dp))

            // ── Carga de documento ─────────────────────────────────────
            Text(
                text = "Documento de verificación",
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = Color.DarkGray,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(6.dp))

            Text(
                text = "Sube el registro mercantil o licencia de funcionamiento (PDF o imagen)",
                fontSize = 12.sp,
                color = Color.Gray,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(10.dp))

            // Botón selector de archivo
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(70.dp)
                    .border(
                        width = 1.5.dp,
                        color = if (selectedFileName != null) Color(0xFF4CAF50) else primaryColor,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .clickable { documentLauncher.launch("*/*") },
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    Icon(
                        imageVector = if (selectedFileName != null) Icons.Default.CheckCircle else Icons.Default.AttachFile,
                        contentDescription = null,
                        tint = if (selectedFileName != null) Color(0xFF4CAF50) else primaryColor,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = selectedFileName ?: "Seleccionar documento",
                        color = if (selectedFileName != null) Color(0xFF4CAF50) else primaryColor,
                        fontSize = 14.sp,
                        fontWeight = if (selectedFileName != null) FontWeight.SemiBold else FontWeight.Normal
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            // ── Error general ──────────────────────────────────────────
            if (state.error != null) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "⚠️ ${state.error}",
                        color = Color(0xFFB71C1C),
                        modifier = Modifier.padding(12.dp),
                        fontSize = 13.sp
                    )
                }
                Spacer(Modifier.height(12.dp))
            }

            // ── Botón Registrar ────────────────────────────────────────
            Button(
                onClick = { authViewModel.registerGuarderia() },
                enabled = !state.isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(22.dp)
                    )
                } else {
                    Text("Enviar solicitud", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(Modifier.height(16.dp))

            TextButton(onClick = { navController.popBackStack() }) {
                Text("¿Ya tienes cuenta? Inicia sesión", color = primaryColor, fontSize = 14.sp)
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}
