package com.example

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.fragment.app.FragmentActivity
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ui.screens.AppListScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SplashSetupScreen
import com.example.ui.screens.UpdateDialog
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.AppLockViewModel
import com.example.viewmodel.UpdateViewModel

class MainActivity : FragmentActivity() {
    private val viewModel: AppLockViewModel by viewModels()
    private val updateViewModel: UpdateViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Check for auto-updates when application starts
        updateViewModel.checkForUpdates()

        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Render global update dialog if an update is found
                    UpdateDialog(viewModel = updateViewModel)

                    val navController = rememberNavController()
                    NavHost(
                        navController = navController,
                        startDestination = "splash"
                    ) {
                        composable("splash") {
                            SplashSetupScreen(
                                viewModel = viewModel,
                                onNavigateToDashboard = {
                                    navController.navigate("dashboard") {
                                        popUpTo("splash") { inclusive = true }
                                    }
                                }
                            )
                        }
                        composable("dashboard") {
                            AppListScreen(
                                viewModel = viewModel,
                                onNavigateToSettings = {
                                    navController.navigate("settings")
                                }
                            )
                        }
                        composable("settings") {
                            SettingsScreen(
                                viewModel = viewModel,
                                onNavigateBack = {
                                    navController.popBackStack()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

