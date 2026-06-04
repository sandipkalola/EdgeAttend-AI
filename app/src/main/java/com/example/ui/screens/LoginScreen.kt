package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.ui.navigation.Screen
import com.example.ui.viewmodel.AttendanceViewModel
import com.example.ui.theme.*

@Composable
fun LoginScreen(navController: NavController, viewModel: AttendanceViewModel) {
    val pinState by viewModel.pinState.collectAsState()
    val isLoggedIn by viewModel.isLoggedIn.collectAsState()
    val loginError by viewModel.loginError.collectAsState()

    // Redirect to Dashboard when successfully logged in
    LaunchedEffect(isLoggedIn) {
        if (isLoggedIn) {
            navController.navigate(Screen.Dashboard.route) {
                popUpTo(Screen.Login.route) { inclusive = true }
            }
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing
    ) { innerPadding ->
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .background(SlateBackground)
                .padding(innerPadding)
                .padding(24.dp)
        ) {
            // Enterprise header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.WifiOff,
                    contentDescription = "No Network Connection",
                    tint = BlueNavy,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "SECURE LOCAL TERMINAL ACTIVE",
                    color = BlueNavy,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Security Lock Branding
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFEFF6FF))
                    .border(1.dp, SlateBorder, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "PIN Secured",
                    tint = BluePrimary,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Enter Terminal PIN",
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = SlateTextPrimary,
                letterSpacing = (-0.5).sp
            )

            Text(
                text = "Secure PIN check required to access matching database.",
                fontSize = 13.sp,
                color = SlateTextSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
            )

            // Pin Visual Dots Indicator
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 12.dp)
            ) {
                for (i in 1..4) {
                    val isActive = pinState.length >= i
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(
                                if (isActive) BlueNavy
                                else SlateBorder
                            )
                    )
                }
            }

            // Interactive Error Notification area
            if (loginError != null) {
                Text(
                    text = loginError ?: "",
                    color = AlertRed,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 10.dp)
                )
            } else {
                Text(
                    text = "Default Admin PIN: 1234",
                    color = SlateTextMuted,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 10.dp)
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // Beautiful Numeric Keyboard Layout (Slate border keycaps)
            val keys = listOf(
                listOf('1', '2', '3'),
                listOf('4', '5', '6'),
                listOf('7', '8', '9'),
                listOf('C', '0', '⌫')
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.widthIn(max = 320.dp)
            ) {
                for (row in keys) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        for (key in row) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1.3f)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(PureWhite)
                                    .border(1.dp, SlateBorder, RoundedCornerShape(16.dp))
                                    .clickable {
                                        when (key) {
                                            'C' -> viewModel.onPinClear()
                                            '⌫' -> viewModel.onPinDelete()
                                            else -> viewModel.onPinKeyPressed(key)
                                        }
                                    }
                            ) {
                                when (key) {
                                    '⌫' -> Icon(
                                        imageVector = Icons.Default.Backspace,
                                        contentDescription = "Backspace",
                                        tint = SlateTextPrimary
                                    )
                                    'C' -> Text(
                                        text = "C",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = AlertRed
                                    )
                                    else -> Text(
                                        text = key.toString(),
                                        fontSize = 22.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = SlateTextPrimary
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
