package com.example.rapikids01.screen.login

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.example.rapikids01.UserRole
import com.example.rapikids01.navigation.Routes
import com.example.rapikids01.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    role: UserRole,
    navController: NavController,
    authViewModel: AuthViewModel = viewModel()
) {
    val state by authViewModel.loginState.collectAsState()
    var passwordVisible by remember { mutableStateOf(false) }

    val primaryColor = when (role) {
        UserRole.ADMIN     -> Color(0xFFE53935)  // rojo admin
        UserRole.GUARDERIA -> Color(0xFF3949AB)  // azul guardería
        else               -> Color(0xFF6A4DBA)  // morado padre
    }

    val titulo = when (role) {
        UserRole.ADMIN     -> "🔐 Panel Administrador"
        UserRole.GUARDERIA -> "🏫 Iniciar sesión"
        else               -> "👨‍👩‍👧 Iniciar sesión"
    }

    val topBarTitle = when (role) {
        UserRole.ADMIN     -> "Acceso Administrador"
        UserRole.GUARDERIA -> "Acceso Guarderías"
        else               -> "Acceso Padres"
    }

    LaunchedEffect(state.loginSuccess) {
        state.loginSuccess?.let { userRole ->
            val destination = when (userRole) {
                UserRole.PADRE     -> Routes.HOME_PADRE
                UserRole.GUARDERIA -> Routes.HOME_GUARDERIA
                UserRole.ADMIN     -> Routes.HOME_ADMIN
            }
            navController.navigate(destination) {
                popUpTo(Routes.HOME) { inclusive = false }
            }
            authViewModel.resetLoginState()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = topBarTitle, fontWeight = FontWeight.Bold)
                },
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
                .padding(horizontal = 24.dp),
            verticalArrangement   = Arrangement.Center,
            horizontalAlignment   = Alignment.CenterHorizontally
        ) {

            Text(
                text       = titulo,
                fontSize   = 22.sp,
                fontWeight = FontWeight.Bold,
                color      = primaryColor
            )

            Spacer(Modifier.height(32.dp))
            OutlinedTextField(
                value         = state.email,
                onValueChange = authViewModel::onLoginEmailChange,
                label         = { Text("Correo electrónico") },
                singleLine    = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier      = Modifier.fillMaxWidth(),
                shape         = RoundedCornerShape(12.dp)
            )

            Spacer(Modifier.height(14.dp))
            OutlinedTextField(
                value         = state.password,
                onValueChange = authViewModel::onLoginPasswordChange,
                label         = { Text("Contraseña") },
                singleLine    = true,
                visualTransformation = if (passwordVisible)
                    VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailingIcon  = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector        = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = null
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape    = RoundedCornerShape(12.dp)
            )

            Spacer(Modifier.height(8.dp))
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
                Spacer(Modifier.height(8.dp))
            }

            Spacer(Modifier.height(16.dp))
            Button(
                onClick  = { authViewModel.login(role) },
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
                    Text("Ingresar", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(Modifier.height(16.dp))
            TextButton(
                onClick = {
                    val registerRoute = when (role) {
                        UserRole.ADMIN     -> Routes.REGISTER_ADMIN
                        UserRole.GUARDERIA -> Routes.REGISTER_GUARDERIA
                        else               -> Routes.REGISTER_PADRE
                    }
                    navController.navigate(registerRoute)
                }
            ) {
                Text(
                    text     = "¿No tienes cuenta? Regístrate aquí",
                    color    = primaryColor,
                    fontSize = 14.sp
                )
            }
        }
    }
}

