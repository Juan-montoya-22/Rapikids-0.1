package com.example.rapikids01.screen.register

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
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
import androidx.navigation.NavController
import com.example.rapikids01.navigation.Routes
import com.example.rapikids01.viewmodel.AuthViewModel

private const val TERMS_URL = "https://juan-montoya-22.github.io/Rapikids-0.1/"
private val Purple = Color(0xFF6A4DBA)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterPadreScreen(
    navController: NavController,
    authViewModel: AuthViewModel
) {
    val state   by authViewModel.registerPadreState.collectAsState()
    val context = LocalContext.current

    var showPassword        by remember { mutableStateOf(false) }
    var showConfirmPassword by remember { mutableStateOf(false) }
    var aceptaTerminos      by remember { mutableStateOf(false) }

    if (state.success) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Registro de Padre / Acudiente", fontWeight = FontWeight.Bold) },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor    = Purple,
                        titleContentColor = Color.White
                    )
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("📧", fontSize = 64.sp)
                Spacer(Modifier.height(24.dp))
                Text(
                    "¡Revisa tu correo!",
                    fontSize   = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color      = Purple
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    text      = "Te enviamos un enlace de verificación a ${state.email}. " +
                            "Una vez que confirmes tu correo podrás iniciar sesión como padre/acudiente.",
                    fontSize  = 15.sp,
                    color     = Color.Gray,
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp
                )
                Spacer(Modifier.height(32.dp))
                Button(
                    onClick = {
                        authViewModel.resetPadreState()
                        navController.navigate(Routes.LOGIN_PADRE) {
                            popUpTo(Routes.REGISTER_PADRE) { inclusive = true }
                        }
                    },
                    shape    = RoundedCornerShape(12.dp),
                    colors   = ButtonDefaults.buttonColors(containerColor = Purple),
                    modifier = Modifier.fillMaxWidth().height(50.dp)
                ) {
                    Text("Ir al inicio de sesión", fontWeight = FontWeight.SemiBold, color = Color.White)
                }
            }
        }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Registro de Padre / Acudiente", fontWeight = FontWeight.Bold) },
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
                value         = state.nombre,
                onValueChange = authViewModel::onPadreNombreChange,
                label         = { Text("Nombre completo") },
                singleLine    = true,
                modifier      = Modifier.fillMaxWidth(),
                shape         = RoundedCornerShape(12.dp)
            )
            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value           = state.telefono,
                onValueChange   = authViewModel::onPadreTelefonoChange,
                label           = { Text("Teléfono") },
                singleLine      = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier        = Modifier.fillMaxWidth(),
                shape           = RoundedCornerShape(12.dp)
            )
            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value           = state.email,
                onValueChange   = authViewModel::onPadreEmailChange,
                label           = { Text("Correo electrónico") },
                singleLine      = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier        = Modifier.fillMaxWidth(),
                shape           = RoundedCornerShape(12.dp)
            )
            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value                = state.password,
                onValueChange        = authViewModel::onPadrePasswordChange,
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
                onValueChange        = authViewModel::onPadreConfirmPasswordChange,
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

            Spacer(Modifier.height(20.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier          = Modifier.fillMaxWidth()
            ) {
                Checkbox(
                    checked         = aceptaTerminos,
                    onCheckedChange = { aceptaTerminos = it },
                    colors          = CheckboxDefaults.colors(checkedColor = Purple)
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

            Spacer(Modifier.height(16.dp))

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

            Button(
                onClick = {
                    if (!aceptaTerminos) return@Button
                    authViewModel.registerPadre()
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
                    Text("Crear cuenta", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            if (!aceptaTerminos) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text      = "Debes aceptar los términos y condiciones para registrarte",
                    color     = Color.Gray,
                    fontSize  = 12.sp,
                    modifier  = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }

            Spacer(Modifier.height(16.dp))

            TextButton(onClick = { navController.navigate(Routes.LOGIN_PADRE) }) {
                Text("¿Ya tienes cuenta? Inicia sesión", color = Purple, fontSize = 14.sp)
            }
        }
    }
}
