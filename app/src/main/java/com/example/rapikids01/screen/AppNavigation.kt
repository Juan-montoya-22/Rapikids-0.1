package com.example.rapikids01.screen

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.rapikids01.UserRole
import com.example.rapikids01.navigation.Routes
import com.example.rapikids01.screen.Home.Home
import com.example.rapikids01.screen.home.HomeGuarderiaScreen
import com.example.rapikids01.screen.home.HomePadreScreen
import com.example.rapikids01.screen.login.LoginScreen
import com.example.rapikids01.screen.register.RegisterGuarderiaScreen
import com.example.rapikids01.screen.register.RegisterPadreScreen
import com.example.rapikids01.viewmodel.AuthViewModel
import com.example.rapikids01.viewmodel.HomePadreViewModel
import com.example.rapikids01.data.model.Guarderia

@Composable
fun AppNavigation() {

    val navController  = rememberNavController()
    val authViewModel: AuthViewModel         = viewModel()
    val homePadreViewModel: HomePadreViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = Routes.HOME
    ) {

        composable(Routes.HOME) {
            Home(
                onPadreClick     = { navController.navigate(Routes.LOGIN_PADRE) },
                onGuarderiaClick = { navController.navigate(Routes.LOGIN_GUARDERIA) }
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

        composable(Routes.REGISTER_PADRE) {
            RegisterPadreScreen(
                navController = navController,
                authViewModel = authViewModel
            )
        }

        composable(Routes.REGISTER_GUARDERIA) {
            RegisterGuarderiaScreen(
                navController = navController,
                authViewModel = authViewModel
            )
        }

        composable(Routes.HOME_PADRE) {
            HomePadreScreen(
                onLogout = {
                    authViewModel.logout()
                    navController.navigate(Routes.HOME) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.HOME_GUARDERIA) {
            HomeGuarderiaScreen(
                onLogout = {
                    authViewModel.logout()
                    navController.navigate(Routes.HOME) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    }
}
