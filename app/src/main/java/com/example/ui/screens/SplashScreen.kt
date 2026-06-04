package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.ui.navigation.Screen
import kotlinx.coroutines.delay
import com.example.ui.theme.*

@Composable
fun SplashScreen(navController: NavController) {
    val infiniteTransition = rememberInfiniteTransition(label = "biometric_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    var initStatus by remember { mutableStateOf("Initializing local AI networks...") }

    LaunchedEffect(Unit) {
        delay(800)
        initStatus = "Loading local Face Mesh Detection model..."
        delay(800)
        initStatus = "Warming TensorFlow Lite on-device GPU delegates..."
        delay(700)
        initStatus = "Local biometric database ready."
        delay(500)
        navController.navigate(Screen.Login.route) {
            popUpTo(Screen.Splash.route) { inclusive = true }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SlateBackground),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp)
        ) {
            Spacer(modifier = Modifier.weight(1f))

            // Animated Visual Biometric Emblem matching Clean Minimalism
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(140.dp)
            ) {
                // Spinning outer radar ring
                Canvas(modifier = Modifier.size(130.dp)) {
                    drawArc(
                        color = BluePrimary.copy(alpha = 0.15f),
                        startAngle = rotationAngle,
                        sweepAngle = 280f,
                        useCenter = false,
                        style = Stroke(width = 3.dp.toPx())
                    )
                }

                // Pulsing inner shield/fingerprint icon in primary brand color
                Icon(
                    imageVector = Icons.Default.Fingerprint,
                    contentDescription = "EdgeAttend Loader",
                    tint = BluePrimary,
                    modifier = Modifier
                        .size(64.dp * pulseScale)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "EdgeAttend AI",
                color = BlueNavy,
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = (-1).sp
            )

            Text(
                text = "SECURE OFFLINE BIOMETRICS ENGINE",
                color = SlateTextSecondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.weight(1f))

            // Dynamic Initialization status log footer
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = "Encrypted Local DB",
                    tint = EmeraldLive,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = initStatus,
                    color = SlateTextPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
