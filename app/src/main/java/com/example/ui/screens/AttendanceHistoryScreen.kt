package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.clickable
import androidx.compose.ui.layout.ContentScale
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.data.model.AttendanceLog
import com.example.ui.viewmodel.AttendanceViewModel
import androidx.compose.material3.ExperimentalMaterial3Api
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttendanceHistoryScreen(navController: NavController, viewModel: AttendanceViewModel) {
    val logs by viewModel.allLogs.collectAsState()
    var searchInput by remember { mutableStateOf("") }
    var selectedTabFilter by remember { mutableStateOf("ALL") }

    val filteredLogs = logs.filter {
        val matchesSearch = it.employeeName.contains(searchInput, ignoreCase = true) || it.employeeId.contains(searchInput, ignoreCase = true)
        val matchesTab = when (selectedTabFilter) {
            "ALL" -> true
            "CHECK_IN" -> it.type == "CHECK_IN"
            "CHECK_OUT" -> it.type == "CHECK_OUT"
            else -> true
        }
        matchesSearch && matchesTab
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Log Logs Terminal", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = BlueNavy, letterSpacing = (-0.5).sp) },
                navigationIcon = {
                    IconButton(
                        onClick = { navController.popBackStack() },
                        modifier = Modifier
                            .padding(horizontal = 8.dp)
                            .size(36.dp)
                            .background(SlateBackground, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = SlateTextPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PureWhite
                )
            )
        },
        contentWindowInsets = WindowInsets.safeDrawing
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(SlateBackground)
                .padding(innerPadding)
        ) {
            // Elegant 1px minimalist header boundary
            HorizontalDivider(color = SlateBorder, thickness = 1.dp)

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Search field styled matching Clean Minimalism
                OutlinedTextField(
                    value = searchInput,
                    onValueChange = { searchInput = it },
                    placeholder = { Text("Search logs by name or ID", color = SlateTextMuted) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    shape = RoundedCornerShape(14.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = SlateTextPrimary,
                        unfocusedTextColor = SlateTextPrimary,
                        focusedBorderColor = BluePrimary,
                        unfocusedBorderColor = SlateBorder,
                        focusedContainerColor = PureWhite,
                        unfocusedContainerColor = PureWhite,
                        focusedLabelColor = BluePrimary,
                        unfocusedLabelColor = SlateTextSecondary
                    ),
                    leadingIcon = { 
                        Icon(
                            imageVector = Icons.Default.Search, 
                            contentDescription = "Search", 
                            tint = SlateTextSecondary,
                            modifier = Modifier.size(18.dp)
                        ) 
                    }
                )

                // Filtering Tab Row custom Minimalism switcher
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                        .background(SlateBorder, RoundedCornerShape(12.dp))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    listOf("ALL" to "All Logs", "CHECK_IN" to "In", "CHECK_OUT" to "Out").forEach { (filterVal, label) ->
                        val isSelected = selectedTabFilter == filterVal
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .weight(1f)
                                .height(34.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (isSelected) PureWhite
                                    else Color.Transparent
                                )
                                .clickable { selectedTabFilter = filterVal }
                        ) {
                            Text(
                                text = label,
                                color = if (isSelected) BluePrimary else SlateTextSecondary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                if (filteredLogs.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.HistoryToggleOff,
                                contentDescription = "No logs",
                                tint = SlateTextMuted.copy(alpha = 0.5f),
                                modifier = Modifier.size(54.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "No Verification Logs Found",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = SlateTextPrimary,
                                letterSpacing = (-0.3).sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Scan faces locally via biometric scanning terminal to record verified check-ins offline.",
                                fontSize = 11.sp,
                                color = SlateTextSecondary,
                                textAlign = TextAlign.Center,
                                lineHeight = 16.sp
                            )
                        }
                    }
                } else {
                    Text(
                        text = "LOG ENTRIES (${filteredLogs.size})",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = SlateTextSecondary,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(bottom = 10.dp)
                    )

                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        items(filteredLogs) { log ->
                            AttendanceLogCard(log = log, viewModel = viewModel)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AttendanceLogCard(log: AttendanceLog, viewModel: AttendanceViewModel) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val dateString = remember(log.timestamp) {
        val sdf = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
        sdf.format(Date(log.timestamp))
    }

    val isCheckIn = log.type == "CHECK_IN"

    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(containerColor = PureWhite),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, SlateBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Captured Selfie or Initial Placeholder thumbnail layout
                if (log.photoPath != null) {
                    AsyncImage(
                        model = File(log.photoPath),
                        contentDescription = "Captured Selfie thumbnail",
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .border(1.dp, SlateBorder, RoundedCornerShape(8.dp))
                            .background(SlateBackground),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(SlateBackground),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Face,
                            contentDescription = "Default silhouette",
                            tint = SlateTextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = log.employeeName,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = SlateTextPrimary,
                            letterSpacing = (-0.2).sp
                        )
                        // Check in / Check out Badge indicator
                        Box(
                            modifier = Modifier
                                .background(
                                    if (isCheckIn) Color(0xFFECFDF5)
                                    else Color(0xFFEFF6FF),
                                    RoundedCornerShape(6.dp)
                                )
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = if (isCheckIn) "IN" else "OUT",
                                color = if (isCheckIn) EmeraldLiveDark else BluePrimary,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                    Text(
                        text = "ID: ${log.employeeId}",
                        fontSize = 11.sp,
                        color = SlateTextSecondary
                    )
                }

                // Confidence Match Score Circular Badge
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${String.format("%.1f", log.confidenceScore)}% match",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = BluePrimary
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.LockClock,
                            contentDescription = "Locally Locked",
                            tint = EmeraldLive,
                            modifier = Modifier.size(10.dp)
                        )
                        Text(
                            text = "Offline Secure",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = EmeraldLiveDark
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = SlateBorder, thickness = 1.dp)
            Spacer(modifier = Modifier.height(10.dp))

            // Sub details (Location Coordinates & Timestamps & deviceID)
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable {
                        val mapUri = android.net.Uri.parse("geo:0,0?q=${android.net.Uri.encode(log.gpsLocation)}")
                        val mapIntent = android.content.Intent(android.content.Intent.ACTION_VIEW, mapUri).apply {
                            setPackage("com.google.android.apps.maps")
                        }
                        try {
                            context.startActivity(mapIntent)
                        } catch (e: Exception) {
                            val webMapUri = android.net.Uri.parse("https://www.google.com/maps/search/?api=1&query=${android.net.Uri.encode(log.gpsLocation)}")
                            val webIntent = android.content.Intent(android.content.Intent.ACTION_VIEW, webMapUri)
                            try {
                                context.startActivity(webIntent)
                            } catch (ex: Exception) {
                                android.widget.Toast.makeText(context, "No map application found.", android.widget.Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "GPS coords",
                        tint = BluePrimary,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = log.gpsLocation,
                        fontSize = 11.sp,
                        color = BluePrimary,
                        textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = "Date",
                        tint = SlateTextMuted,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = dateString,
                        fontSize = 11.sp,
                        color = SlateTextSecondary
                    )
                }
            }
        }
    }
}
