package com.rakshalink.ui.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")

    // Auth Graph
    object Welcome : Screen("welcome")
    object Login : Screen("login")
    object Signup : Screen("signup")
    object VerifyEmail : Screen("verify_email")
    object ForgotPassword : Screen("forgot_password")
    object ResetPassword : Screen("reset_password")
    object RoleSelection : Screen("role_selection")
    object PhoneAuth : Screen("phone_auth")
    object OtpVerification : Screen("otp_verification")
    object Permissions : Screen("permissions")

    // Wearer Graph
    object WearerDashboard : Screen("wearer_dashboard")
    object EmergencyActive : Screen("emergency_active")
    object SosDetail : Screen("sos_detail")
    object LiveTracking : Screen("live_tracking")
    object SafeZones : Screen("safe_zones")
    object FallDetection : Screen("fall_detection")
    object PendantSettings : Screen("pendant_settings")
    object EmergencyContacts : Screen("emergency_contacts")
    object History : Screen("history")
    object WearerSettings : Screen("wearer_settings")

    // Guardian Graph
    object GuardianDashboard : Screen("guardian_dashboard")
    object GuardianLiveMap : Screen("guardian_live_map")
    object WearerDetail : Screen("wearer_detail/{wearerId}") {
        fun createRoute(wearerId: String) = "wearer_detail/$wearerId"
    }
    object AlertInbox : Screen("alert_inbox")
    object GuardianSettings : Screen("guardian_settings")
}
