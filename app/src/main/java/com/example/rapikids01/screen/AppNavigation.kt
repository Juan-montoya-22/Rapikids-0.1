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

        // ── Selector de rol ────────────────────────────────────────────
        composable(Routes.HOME) {
            Home(
                onPadreClick     = { navController.navigate(Routes.LOGIN_PADRE) },
                onGuarderiaClick = { navController.navigate(Routes.LOGIN_GUARDERIA) },
                onAdminClick     = { navController.navigate(Routes.LOGIN_ADMIN) }  // 5 toques en el título
            )
        }

        // ── Login Padre ────────────────────────────────────────────────
        composable(Routes.LOGIN_PADRE) {
            LoginScreen(
                role          = UserRole.PADRE,
                navController = navController,
                authViewModel = authViewModel
            )
        }

        // ── Login Guardería ────────────────────────────────────────────
        composable(Routes.LOGIN_GUARDERIA) {
            LoginScreen(
                role          = UserRole.GUARDERIA,
                navController = navController,
                authViewModel = authViewModel
            )
        }

        // ── Login Admin ────────────────────────────────────────────────
        composable(Routes.LOGIN_ADMIN) {
            LoginScreen(
                role          = UserRole.ADMIN,
                navController = navController,
                authViewModel = authViewModel
            )
        }

        // ── Registro Padre ─────────────────────────────────────────────
        composable(Routes.REGISTER_PADRE) {
            RegisterPadreScreen(
                navController = navController,
                authViewModel = authViewModel
            )
        }

        // ── Registro Guardería ─────────────────────────────────────────
        composable(Routes.REGISTER_GUARDERIA) {
            RegisterGuarderiaScreen(
                navController = navController,
                authViewModel = authViewModel
            )
        }

        // ── Registro Admin ─────────────────────────────────────────────
        composable(Routes.REGISTER_ADMIN) {
            RegisterAdminScreen(
                navController = navController,
                authViewModel = authViewModel
            )
        }

        // ── Home Padre ─────────────────────────────────────────────────
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

        // ── Home Guardería ─────────────────────────────────────────────
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

        // ── Home Admin ─────────────────────────────────────────────────
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
