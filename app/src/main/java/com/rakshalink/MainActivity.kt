package com.rakshalink

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rakshalink.services.InactivityState
import com.rakshalink.services.InactivityTracker
import com.rakshalink.ui.components.GlassButton
import com.rakshalink.ui.components.GlassOutlinedButton
import com.rakshalink.ui.navigation.RakshaNavGraph
import com.rakshalink.ui.theme.BackgroundDark
import com.rakshalink.ui.theme.PrimaryRed
import com.rakshalink.ui.theme.RakshaLinkTheme
import com.rakshalink.ui.theme.TextPrimary
import com.rakshalink.ui.theme.TextSecondary
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var inactivityTracker: InactivityTracker

    override fun onCreate(savedInstanceState: android.os.Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            RakshaLinkTheme {
                val inactivityState by inactivityTracker.state.collectAsState()

                LaunchedEffect(Unit) {
                    inactivityTracker.startTracking {
                        // Action on timeout
                    }
                }

                Box(
                    modifier = Modifier.fillMaxSize()
                ) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = BackgroundDark
                    ) {
                        RakshaNavGraph()
                    }

                    // Inactivity Warning Modal (Point 11 requirement)
                    if (inactivityState is InactivityState.Warning) {
                        val seconds = (inactivityState as InactivityState.Warning).secondsRemaining
                        AlertDialog(
                            onDismissRequest = { },
                            title = {
                                Text(
                                    text = "Are you still there?",
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                            },
                            text = {
                                Column {
                                    Text(
                                        text = "You have been idle for 30 minutes. For your security, you will be automatically signed out in:",
                                        color = TextSecondary,
                                        fontSize = 14.sp
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = "$seconds seconds",
                                        color = PrimaryRed,
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            },
                            confirmButton = {
                                TextButton(
                                    onClick = { inactivityTracker.staySignedIn { } }
                                ) {
                                    Text("Stay Signed In")
                                }
                            },
                            dismissButton = {
                                TextButton(
                                    onClick = { /* Sign out action */ }
                                ) {
                                    Text("Sign Out Now")
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}
