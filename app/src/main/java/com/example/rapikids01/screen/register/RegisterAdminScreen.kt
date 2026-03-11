package com.example.rapikids01.screen.register

import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.rapikids01.navigation.Routes
import com.example.rapikids01.viewmodel.AuthViewModel

// ── Colores panel de control ──────────────────────────────────────────────────
private val DarkBg     = Color(0xFF1A1A2E)
private val DarkCard   = Color(0xFF16213E)
private val RedAccent  = Color(0xFFE53935)
private val RedLight   = Color(0xFFEF9A9A)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterAdminScreen(
    navController: NavController,
    authViewModel: AuthViewModel
) {
    val state by authViewModel.registerAdminState.collectAsState()
    var showPassword        by remember { mutableStateOf(false) }
    var showConfirmPassword by remember { mutableStateOf(false) }
    var showCodigo          by remember { mutableStateOf(false) }

    // Navegar al éxito
    LaunchedEffect(state.success) {
        if (state.success) {
            navController.navigate(Routes.LOGIN_ADMIN) {
                popUpTo(Routes.REGISTER_ADMIN) { inclusive = true }
            }
        }
    }

    Scaffold(
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.horizontalGradient(listOf(DarkBg, Color(0xFF0F3460))))
                    .statusBarsPadding()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 12.dp)
                ) {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = Color.White)
                    }
                    Text(
                        text       = "Registro Administrador",
                        fontSize   = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color      = Color.White
                    )
                }
            }
        },
        containerColor = DarkBg
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // Ícono admin
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(RedAccent.copy(alpha = 0.15f), RoundedCornerShape(20.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.AdminPanelSettings,
                    contentDescription = null,
                    tint     = RedAccent,
                    modifier = Modifier.size(48.dp)
                )
            }

            Spacer(Modifier.height(8.dp))
            Text("Panel de Administración", fontSize = 14.sp, color = RedLight)
            Spacer(Modifier.height(24.dp))

            // Campos
            AdminField(
                value         = state.nombre,
                onValueChange = authViewModel::onAdminNombreChange,
                label         = "Nombre completo",
                icon          = Icons.Default.Person
            )
            Spacer(Modifier.height(12.dp))

            AdminField(
                value         = state.cargo,
                onValueChange = authViewModel::onAdminCargoChange,
                label         = "Cargo (ej: Director)",
                icon          = Icons.Default.Work
            )
            Spacer(Modifier.height(12.dp))

            AdminField(
                value         = state.email,
                onValueChange = authViewModel::onAdminEmailChange,
                label         = "Correo electrónico",
                icon          = Icons.Default.Email,
                keyboardType  = KeyboardType.Email
            )
            Spacer(Modifier.height(12.dp))

            // Contraseña
            OutlinedTextField(
                value         = state.password,
                onValueChange = authViewModel::onAdminPasswordChange,
                label         = { Text("Contraseña", color = Color.Gray) },
                leadingIcon   = { Icon(Icons.Default.Lock, null, tint = RedAccent) },
                trailingIcon  = {
                    IconButton(onClick = { showPassword = !showPassword }) {
                        Icon(
                            if (showPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            null, tint = Color.Gray
                        )
                    }
                },
                visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                singleLine    = true,
                shape         = RoundedCornerShape(12.dp),
                colors        = adminFieldColors(),
                modifier      = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))

            // Confirmar contraseña
            OutlinedTextField(
                value         = state.confirmPassword,
                onValueChange = authViewModel::onAdminConfirmPasswordChange,
                label         = { Text("Confirmar contraseña", color = Color.Gray) },
                leadingIcon   = { Icon(Icons.Default.Lock, null, tint = RedAccent) },
                trailingIcon  = {
                    IconButton(onClick = { showConfirmPassword = !showConfirmPassword }) {
                        Icon(
                            if (showConfirmPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            null, tint = Color.Gray
                        )
                    }
                },
                visualTransformation = if (showConfirmPassword) VisualTransformation.None else PasswordVisualTransformation(),
                singleLine    = true,
                shape         = RoundedCornerShape(12.dp),
                colors        = adminFieldColors(),
                modifier      = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(16.dp))

            // Código secreto
            HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
            Spacer(Modifier.height(16.dp))
            Text(
                text     = "🔐 Código de administrador",
                color    = RedLight,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value         = state.codigoSecreto,
                onValueChange = authViewModel::onAdminCodigoChange,
                label         = { Text("Código secreto", color = Color.Gray) },
                leadingIcon   = { Icon(Icons.Default.Key, null, tint = RedAccent) },
                trailingIcon  = {
                    IconButton(onClick = { showCodigo = !showCodigo }) {
                        Icon(
                            if (showCodigo) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            null, tint = Color.Gray
                        )
                    }
                },
                visualTransformation = if (showCodigo) VisualTransformation.None else PasswordVisualTransformation(),
                singleLine    = true,
                shape         = RoundedCornerShape(12.dp),
                colors        = adminFieldColors(),
                modifier      = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(20.dp))

            // Error
            if (state.error != null) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = RedAccent.copy(alpha = 0.15f)),
                    shape  = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text     = "⚠️ ${state.error}",
                        color    = RedLight,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(12.dp)
                    )
                }
                Spacer(Modifier.height(12.dp))
            }

            // Botón registrar
            Button(
                onClick  = authViewModel::registerAdmin,
                enabled  = !state.isLoading,
                shape    = RoundedCornerShape(14.dp),
                colors   = ButtonDefaults.buttonColors(containerColor = RedAccent),
                modifier = Modifier.fillMaxWidth().height(52.dp)
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp, modifier = Modifier.size(24.dp))
                } else {
                    Icon(Icons.Default.AdminPanelSettings, null, tint = Color.White)
                    Spacer(Modifier.width(8.dp))
                    Text("Crear cuenta de administrador", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }

            Spacer(Modifier.height(16.dp))
            TextButton(onClick = { navController.navigate(Routes.LOGIN_ADMIN) }) {
                Text("¿Ya tienes cuenta? Inicia sesión", color = RedLight, fontSize = 13.sp)
            }
        }
    }
}

@Composable
private fun AdminField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    OutlinedTextField(
        value         = value,
        onValueChange = onValueChange,
        label         = { Text(label, color = Color.Gray) },
        leadingIcon   = { Icon(icon, null, tint = Color(0xFFE53935)) },
        singleLine    = true,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        shape         = RoundedCornerShape(12.dp),
        colors        = adminFieldColors(),
        modifier      = Modifier.fillMaxWidth()
    )
}

@Composable
private fun adminFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor      = Color(0xFFE53935),
    unfocusedBorderColor    = Color.White.copy(alpha = 0.2f),
    focusedLabelColor       = Color(0xFFE53935),
    focusedContainerColor   = Color(0xFF16213E),
    unfocusedContainerColor = Color(0xFF16213E),
    focusedTextColor        = Color.White,
    unfocusedTextColor      = Color.White,
    cursorColor             = Color(0xFFE53935)
)
