package com.example.rapikids01.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.rapikids01.UserRole
import com.example.rapikids01.navigation.Routes
import com.example.rapikids01.screen.home.Home
import com.example.rapikids01.screen.home.HomePadreScreen
import com.example.rapikids01.screen.home.HomeGuarderiaScreen
import com.example.rapikids01.screen.home.HomeAdminScreen
import com.example.rapikids01.screen.login.LoginScreen
import com.example.rapikids01.screen.register.RegisterAdminScreen
import com.example.rapikids01.screen.register.RegisterGuarderiaScreen
import com.example.rapikids01.screen.register.RegisterPadreScreen
import com.example.rapikids01.viewmodel.AdminViewModel
import com.example.rapikids01.viewmodel.AuthViewModel
import com.example.rapikids01.viewmodel.HomeGuarderiaViewModel
import kotlinx.coroutines.launch

@Composable
fun AppNavigation() {

    val navController          = rememberNavController()
    val authViewModel          : AuthViewModel          = viewModel()
    val homeGuarderiaViewModel : HomeGuarderiaViewModel = viewModel()
    val adminViewModel         : AdminViewModel         = viewModel()
    val scope                  = rememberCoroutineScope()

    NavHost(
        navController    = navController,
        startDestination = Routes.HOME
    ) {

        composable(Routes.HOME) {
            Home(
                onPadreClick     = { navController.navigate(Routes.LOGIN_PADRE) },
                onGuarderiaClick = { navController.navigate(Routes.LOGIN_GUARDERIA) },
                onAdminClick     = { navController.navigate(Routes.LOGIN_ADMIN) }  // 5 toques en el título
            )
        }

        composable(Routes.LOGIN_PADRE) {
            LoginScreen(
                role          = UserRole.PADRE,
                navController = navController,
                authViewModel = authViewModel
            )
        }

        composable(Routes.LOGIN_GUARDERIA) {
            LoginScreen(
                role          = UserRole.GUARDERIA,
                navController = navController,
                authViewModel = authViewModel
            )
        }

        composable(Routes.LOGIN_ADMIN) {
            LoginScreen(
                role          = UserRole.ADMIN,
                navController = navController,
                authViewModel = authViewModel
            )
        }

        composable(Routes.REGISTER_PADRE) {
            RegisterPadreScreen(
                navController = navController,
                authViewModel = authViewModel
            )
        }

        composable(Routes.REGISTER_GUARDERIA) {
            androidx.compose.runtime.LaunchedEffect(Unit) {
                authViewModel.resetGuarderiaState()
            }
            RegisterGuarderiaScreen(
                navController = navController,
                authViewModel = authViewModel
            )
        }

        composable(Routes.REGISTER_ADMIN) {
            RegisterAdminScreen(
                navController = navController,
                authViewModel = authViewModel
            )
        }

        composable(Routes.HOME_PADRE) {
            HomePadreScreen(
                onLogout = {
                    scope.launch {
                        authViewModel.logout()
                        navController.navigate(Routes.HOME) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                },
                onVerPerfil = { _: com.example.rapikids01.data.model.Guarderia -> /* TODO */ }
            )
        }

        composable(Routes.HOME_GUARDERIA) {
            HomeGuarderiaScreen(
                onLogout = {
                    scope.launch {
                        authViewModel.logout()
                        navController.navigate(Routes.HOME) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                },
                viewModel = homeGuarderiaViewModel
            )
        }

        composable(Routes.HOME_ADMIN) {
            HomeAdminScreen(
                onLogout = {
                    scope.launch {
                        authViewModel.logout()
                        navController.navigate(Routes.HOME) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                },
                viewModel = adminViewModel
            )
        }
    }
}

