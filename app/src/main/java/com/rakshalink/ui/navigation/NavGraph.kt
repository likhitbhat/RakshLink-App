package com.rakshalink.ui.navigation

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.rakshalink.domain.model.UserRole
import kotlinx.coroutines.launch
import com.rakshalink.ui.auth.AuthViewModel
import com.rakshalink.ui.auth.ForgotPasswordScreen
import com.rakshalink.ui.auth.LoginScreen
import com.rakshalink.ui.auth.OtpVerificationScreen
import com.rakshalink.ui.auth.PhoneAuthScreen
import com.rakshalink.ui.auth.RoleSelectionScreen
import com.rakshalink.ui.auth.SignupScreen
import com.rakshalink.ui.auth.TwilioAuthViewModel
import com.rakshalink.ui.auth.VerifyEmailScreen
import com.rakshalink.ui.auth.WelcomeScreen
import com.rakshalink.ui.guardian.AlertInboxScreen
import com.rakshalink.ui.guardian.GuardianDashboardScreen
import com.rakshalink.ui.guardian.GuardianLiveMapScreen
import com.rakshalink.ui.guardian.GuardianSettingsScreen
import com.rakshalink.ui.guardian.GuardianViewModel
import com.rakshalink.ui.guardian.WearerDetailScreen
import com.rakshalink.ui.onboarding.PermissionsScreen
import com.rakshalink.ui.splash.SplashScreen
import com.rakshalink.ui.wearer.EmergencyActiveScreen
import com.rakshalink.ui.wearer.EmergencyContactsScreen
import com.rakshalink.ui.wearer.FallDetectionScreen
import com.rakshalink.ui.wearer.HistoryScreen
import com.rakshalink.ui.wearer.LiveTrackingScreen
import com.rakshalink.ui.wearer.PendantSettingsScreen
import com.rakshalink.ui.wearer.SafeZonesScreen
import com.rakshalink.ui.wearer.WearerDashboardScreen
import com.rakshalink.ui.wearer.WearerSettingsScreen
import com.rakshalink.ui.wearer.WearerViewModel

private fun hasRequiredPermissions(context: Context): Boolean {
    return ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED
}

@Composable
fun RakshaNavGraph(
    navController: NavHostController = rememberNavController(),
    authViewModel: AuthViewModel = hiltViewModel(),
    twilioAuthViewModel: TwilioAuthViewModel = hiltViewModel(),
    wearerViewModel: WearerViewModel = hiltViewModel(),
    guardianViewModel: GuardianViewModel = hiltViewModel()
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val currentRole by authViewModel.selectedRole.collectAsState()
    val context = LocalContext.current

    val showBottomBar = currentRoute in listOf(
        Screen.WearerDashboard.route,
        Screen.LiveTracking.route,
        Screen.SafeZones.route,
        Screen.PendantSettings.route,
        Screen.WearerSettings.route,
        Screen.GuardianDashboard.route,
        Screen.GuardianLiveMap.route,
        Screen.AlertInbox.route,
        Screen.GuardianSettings.route
    )

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                RakshaBottomNavigationBar(
                    navController = navController,
                    userRole = currentRole
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Splash.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            // Splash Destination
            composable(Screen.Splash.route) {
                val scope = androidx.compose.runtime.rememberCoroutineScope()
                SplashScreen(
                    onSplashFinished = {
                        scope.launch {
                            try {
                                val isLoggedIn = authViewModel.isUserLoggedIn()
                                if (isLoggedIn) {
                                    val role = authViewModel.restoreUserRole()
                                    val hasPerms = hasRequiredPermissions(context)
                                    if (!hasPerms) {
                                        navController.navigate(Screen.Permissions.route) {
                                            popUpTo(Screen.Splash.route) { inclusive = true }
                                        }
                                    } else {
                                        if (role == UserRole.GUARDIAN) {
                                            navController.navigate(Screen.GuardianDashboard.route) {
                                                popUpTo(Screen.Splash.route) { inclusive = true }
                                            }
                                        } else {
                                            navController.navigate(Screen.WearerDashboard.route) {
                                                popUpTo(Screen.Splash.route) { inclusive = true }
                                            }
                                        }
                                    }
                                } else {
                                    navController.navigate(Screen.Welcome.route) {
                                        popUpTo(Screen.Splash.route) { inclusive = true }
                                    }
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                                navController.navigate(Screen.Welcome.route) {
                                    popUpTo(Screen.Splash.route) { inclusive = true }
                                }
                            }
                        }
                    }
                )
            }

            // Auth Destinations
            composable(Screen.Welcome.route) {
                WelcomeScreen(
                    onNavigateToRoleSelection = { navController.navigate(Screen.RoleSelection.route) },
                    onNavigateToLogin = { navController.navigate(Screen.Login.route) },
                    onNavigateToPhoneAuth = { navController.navigate(Screen.PhoneAuth.route) }
                )
            }

            composable(Screen.RoleSelection.route) {
                RoleSelectionScreen(
                    viewModel = authViewModel,
                    onNavigateToSignup = { navController.navigate(Screen.Signup.route) },
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable(Screen.Login.route) {
                LoginScreen(
                    viewModel = authViewModel,
                    onLoginSuccess = { role ->
                        authViewModel.selectRole(role)
                        val hasPerms = hasRequiredPermissions(context)
                        if (!hasPerms) {
                            navController.navigate(Screen.Permissions.route) {
                                popUpTo(Screen.Welcome.route) { inclusive = true }
                            }
                        } else {
                            if (role == UserRole.GUARDIAN) {
                                navController.navigate(Screen.GuardianDashboard.route) {
                                    popUpTo(Screen.Welcome.route) { inclusive = true }
                                }
                            } else {
                                navController.navigate(Screen.WearerDashboard.route) {
                                    popUpTo(Screen.Welcome.route) { inclusive = true }
                                }
                            }
                        }
                    },
                    onNavigateToSignup = { navController.navigate(Screen.Signup.route) },
                    onNavigateToForgotPassword = { navController.navigate(Screen.ForgotPassword.route) },
                    onNavigateToPhoneAuth = { navController.navigate(Screen.PhoneAuth.route) },
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable(Screen.PhoneAuth.route) {
                PhoneAuthScreen(
                    viewModel = twilioAuthViewModel,
                    onOtpSentSuccess = {
                        navController.navigate(Screen.OtpVerification.route)
                    },
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable(Screen.OtpVerification.route) {
                OtpVerificationScreen(
                    viewModel = twilioAuthViewModel,
                    userRole = currentRole,
                    onVerificationSuccess = { role ->
                        authViewModel.selectRole(role)
                        val hasPerms = hasRequiredPermissions(context)
                        if (!hasPerms) {
                            navController.navigate(Screen.Permissions.route) {
                                popUpTo(Screen.Welcome.route) { inclusive = true }
                            }
                        } else {
                            if (role == UserRole.GUARDIAN) {
                                navController.navigate(Screen.GuardianDashboard.route) {
                                    popUpTo(Screen.Welcome.route) { inclusive = true }
                                }
                            } else {
                                navController.navigate(Screen.WearerDashboard.route) {
                                    popUpTo(Screen.Welcome.route) { inclusive = true }
                                }
                            }
                        }
                    },
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable(Screen.Signup.route) {
                SignupScreen(
                    viewModel = authViewModel,
                    onSignupSuccess = { navController.navigate(Screen.VerifyEmail.route) },
                    onNavigateToLogin = { navController.navigate(Screen.Login.route) },
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable(Screen.VerifyEmail.route) {
                VerifyEmailScreen(
                    onNavigateToLogin = { navController.navigate(Screen.Login.route) },
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable(Screen.ForgotPassword.route) {
                ForgotPasswordScreen(
                    viewModel = authViewModel,
                    onNavigateToLogin = { navController.navigate(Screen.Login.route) },
                    onBackClick = { navController.popBackStack() }
                )
            }

            // Onboarding Permissions
            composable(Screen.Permissions.route) {
                PermissionsScreen(
                    onPermissionsGranted = {
                        val role = authViewModel.selectedRole.value
                        if (role == UserRole.GUARDIAN) {
                            navController.navigate(Screen.GuardianDashboard.route) {
                                popUpTo(Screen.Permissions.route) { inclusive = true }
                            }
                        } else {
                            navController.navigate(Screen.WearerDashboard.route) {
                                popUpTo(Screen.Permissions.route) { inclusive = true }
                            }
                        }
                    }
                )
            }

            // Wearer Destinations
            composable(Screen.WearerDashboard.route) {
                WearerDashboardScreen(
                    viewModel = wearerViewModel,
                    onNavigateToTracking = { navController.navigate(Screen.LiveTracking.route) },
                    onNavigateToSafeZones = { navController.navigate(Screen.SafeZones.route) },
                    onNavigateToContacts = { navController.navigate(Screen.EmergencyContacts.route) },
                    onNavigateToEmergencyActive = { navController.navigate(Screen.EmergencyActive.route) }
                )
            }

            composable(Screen.EmergencyActive.route) {
                EmergencyActiveScreen(
                    viewModel = wearerViewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Screen.LiveTracking.route) {
                LiveTrackingScreen(
                    viewModel = wearerViewModel,
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable(Screen.SafeZones.route) {
                SafeZonesScreen(
                    viewModel = wearerViewModel,
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable(Screen.FallDetection.route) {
                FallDetectionScreen(
                    viewModel = wearerViewModel,
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable(Screen.PendantSettings.route) {
                PendantSettingsScreen(
                    viewModel = wearerViewModel,
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable(Screen.EmergencyContacts.route) {
                EmergencyContactsScreen(
                    viewModel = wearerViewModel,
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable(Screen.History.route) {
                HistoryScreen(
                    viewModel = wearerViewModel,
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable(Screen.WearerSettings.route) {
                WearerSettingsScreen(
                    viewModel = wearerViewModel,
                    onNavigateToContacts = { navController.navigate(Screen.EmergencyContacts.route) },
                    onNavigateToSafeZones = { navController.navigate(Screen.SafeZones.route) },
                    onNavigateToHistory = { navController.navigate(Screen.History.route) },
                    onSignOutClick = {
                        authViewModel.resetState()
                        navController.navigate(Screen.Welcome.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    onBackClick = { navController.popBackStack() }
                )
            }

            // Guardian Destinations
            composable(Screen.GuardianDashboard.route) {
                GuardianDashboardScreen(
                    viewModel = guardianViewModel,
                    onNavigateToWearerDetail = { wearerId ->
                        navController.navigate(Screen.WearerDetail.createRoute(wearerId))
                    },
                    onNavigateToMap = { navController.navigate(Screen.GuardianLiveMap.route) }
                )
            }

            composable(Screen.GuardianLiveMap.route) {
                GuardianLiveMapScreen(
                    viewModel = guardianViewModel,
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable(
                route = Screen.WearerDetail.route,
                arguments = listOf(navArgument("wearerId") { type = NavType.StringType })
            ) { backStackEntry ->
                val id = backStackEntry.arguments?.getString("wearerId") ?: ""
                WearerDetailScreen(
                    wearerId = id,
                    viewModel = guardianViewModel,
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable(Screen.AlertInbox.route) {
                AlertInboxScreen(
                    viewModel = guardianViewModel,
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable(Screen.GuardianSettings.route) {
                GuardianSettingsScreen(
                    viewModel = guardianViewModel,
                    onSignOutClick = {
                        authViewModel.resetState()
                        navController.navigate(Screen.Welcome.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    onBackClick = { navController.popBackStack() }
                )
            }
        }
    }
}
