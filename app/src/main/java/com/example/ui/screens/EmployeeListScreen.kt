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
import androidx.navigation.NavController
import com.example.data.model.Employee
import com.example.ui.viewmodel.AttendanceViewModel
import androidx.compose.material3.ExperimentalMaterial3Api
import com.example.ui.theme.*
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmployeeListScreen(navController: NavController, viewModel: AttendanceViewModel) {
    val employees by viewModel.allEmployees.collectAsState()
    var searchInput by remember { mutableStateOf("") }
    var employeeToDelete by remember { mutableStateOf<Employee?>(null) }

    val filteredEmployees = employees.filter {
        it.name.contains(searchInput, ignoreCase = true) || it.id.contains(searchInput, ignoreCase = true)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Staff Database", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = BlueNavy, letterSpacing = (-0.5).sp) },
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
            Divider(color = SlateBorder, thickness = 1.dp)

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // High contrast Search Bar fields matching Clean Minimalism
                OutlinedTextField(
                    value = searchInput,
                    onValueChange = { searchInput = it },
                    placeholder = { Text("Search by name or code ID", color = SlateTextMuted) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
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
                    },
                    trailingIcon = {
                        if (searchInput.isNotEmpty()) {
                            IconButton(onClick = { searchInput = "" }) {
                                Icon(
                                    imageVector = Icons.Default.Clear, 
                                    contentDescription = "Clear",
                                    tint = SlateTextSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                )

                if (filteredEmployees.isEmpty()) {
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
                                imageVector = Icons.Default.ContactSupport,
                                contentDescription = "Search empty",
                                tint = SlateTextMuted.copy(alpha = 0.5f),
                                modifier = Modifier.size(54.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = if (employees.isEmpty()) "No Employees Registered" else "No Profiles Match Search",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = SlateTextPrimary,
                                letterSpacing = (-0.3).sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = if (employees.isEmpty()) "Tap standard 'Enroll Employee' on Dashboard to onboard new profiles."
                                else "Check your query text spelling or reset the text search completely.",
                                fontSize = 11.sp,
                                color = SlateTextSecondary,
                                textAlign = TextAlign.Center,
                                lineHeight = 16.sp
                            )
                        }
                    }
                } else {
                    Text(
                        text = "REGISTERED STAFF (${filteredEmployees.size})",
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
                        items(filteredEmployees) { emp ->
                            EmployeeRowItem(
                                employee = emp,
                                onDelete = { employeeToDelete = emp }
                            )
                        }
                    }
                }
            }
        }

        // Delete Confirmation dialog with clean Material 3 typography
        if (employeeToDelete != null) {
            AlertDialog(
                containerColor = PureWhite,
                onDismissRequest = { employeeToDelete = null },
                confirmButton = {
                    Button(
                        onClick = {
                            val emp = employeeToDelete
                            if (emp != null) {
                                viewModel.deleteEmployee(emp)
                            }
                            employeeToDelete = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Delete Permanently", color = PureWhite, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    OutlinedButton(
                        onClick = { employeeToDelete = null },
                        border = androidx.compose.foundation.BorderStroke(1.dp, SlateBorder),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Cancel", color = SlateTextPrimary, fontWeight = FontWeight.Bold)
                    }
                },
                title = { Text("Purge Biometric Record?", fontWeight = FontWeight.ExtraBold, color = BlueNavy, fontSize = 18.sp) },
                text = {
                    Text(
                        "Are you absolutely sure you want to delete '${employeeToDelete?.name}' from this local device database?\n\nThis will permanently destroy all 3 captured face embeddings angles and history metrics locally. This cannot be undone.",
                        fontSize = 13.sp,
                        color = SlateTextSecondary,
                        lineHeight = 18.sp
                    )
                }
            )
        }
    }
}

@Composable
fun EmployeeRowItem(employee: Employee, onDelete: () -> Unit) {
    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(containerColor = PureWhite),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, SlateBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Visual circle profile icon or actual captured photo
            if (employee.photoPath != null && File(employee.photoPath).exists()) {
                AsyncImage(
                    model = File(employee.photoPath),
                    contentDescription = "Employee photo",
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .border(1.dp, SlateBorder, CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFEFF6FF))
                ) {
                    Text(
                        text = employee.name.take(1).uppercase(),
                        color = BluePrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = employee.name,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = SlateTextPrimary,
                    letterSpacing = (-0.2).sp
                )
                Text(
                    text = "ID: ${employee.id}",
                    fontSize = 12.sp,
                    color = SlateTextSecondary
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Display badge representing mathematical vector metrics registered
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BiometricBadge("FRONT")
                    BiometricBadge("LEFT")
                    BiometricBadge("RIGHT")
                }
            }

            IconButton(
                onClick = onDelete,
                modifier = Modifier
                    .size(34.dp)
                    .background(Color(0xFFFEE2E2), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.DeleteForever,
                    contentDescription = "Delete record",
                    tint = AlertRed,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
fun BiometricBadge(label: String) {
    Box(
        modifier = Modifier
            .background(Color(0xFFECFDF5), RoundedCornerShape(6.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = label,
            fontSize = 9.sp,
            fontWeight = FontWeight.ExtraBold,
            color = EmeraldLiveDark
        )
    }
}
