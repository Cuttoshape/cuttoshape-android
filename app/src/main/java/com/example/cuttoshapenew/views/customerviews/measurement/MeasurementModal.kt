package com.example.cuttoshapenew.views.customerviews.measurement

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cuttoshapenew.apiclients.Data
import com.example.cuttoshapenew.apiclients.RetrofitClient
import com.example.cuttoshapenew.apiclients.Measurement
import com.example.cuttoshapenew.apiclients.MeasurementRequest
import com.example.cuttoshapenew.apiclients.MeasurementResponse
import kotlinx.coroutines.launch
import java.util.UUID

@Composable
fun MeasurementModal(
    userId: String,
    onDismiss: () -> Unit,
) {
    val coroutineScope = rememberCoroutineScope()
    var measurements by remember { mutableStateOf<List<MeasurementResponse>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var expandedId by remember { mutableStateOf<String?>(null) }
    var showNewMeasurementModal by remember { mutableStateOf(false) }
    var showUpdateForm by remember { mutableStateOf<MeasurementResponse?>(null) }


    val context = LocalContext.current
    val buttonColor = Color(0xFF4A90E2)

    // Fetch measurements when the modal is opened
    LaunchedEffect(Unit) {
        try {
            measurements = RetrofitClient.getClient(context).getMeasurements(userId)
        } catch (e: Exception) {
            errorMessage = "Failed to load measurements: ${e.message}"
        } finally {
            isLoading = false
        }
    }

    // Modal content
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .background(color = Color.White),
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .background(color = Color.White)
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            } else if (errorMessage != null) {
                Text(text = errorMessage!!, color = Color.Red)
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .background(color = Color.White)
                ) {
                    items(measurements) { measurement ->
                        MeasurementItem(
                            measurement = measurement,
                            isExpanded = expandedId == measurement.id.toString(),
                            onExpandClick = {
                                expandedId =
                                    if (expandedId == measurement.id.toString()) null else measurement.id.toString()
                            },
                            onUpdateClick = { showUpdateForm = measurement },
                            onDeleteClick = {
                                coroutineScope.launch {
                                    try {
                                        RetrofitClient.getClient(context)
                                            .deleteMeasurement(measurement.id.toString())
                                        measurements =
                                            measurements.filter { it.id != measurement.id }
                                    } catch (e: Exception) {
                                        errorMessage = "Failed to delete: ${e.message}"
                                    }
                                }
                            }
                        )
                    }
                }

                // Add New Button
                Button(
                    onClick = { showNewMeasurementModal = true; onDismiss },
                    modifier = Modifier
                        .height(50.dp)
                        .align(Alignment.CenterHorizontally),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = buttonColor)
                ) {
                    Text("Add New", color = Color.White)
                }
            }

            // Close Button
            Button(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.End)
                    .padding(top = 8.dp)
            ) {
                Text("Close")
            }

            // Update Form (kept inline for updates)
            if (showUpdateForm != null) {
                MeasurementForm(
                    measurement = showUpdateForm,
                    onSave = { updatedMeasurement ->
                        coroutineScope.launch {
                            try {
                                RetrofitClient.getClient(context)
                                    .updateMeasurement(updatedMeasurement)
                                measurements = measurements.map {
                                    if (it.id == updatedMeasurement.id) updatedMeasurement else it
                                }
                                showUpdateForm = null
                            } catch (e: Exception) {
                                errorMessage = "Failed to update: ${e.message}"
                            }
                        }
                    },
                    onCancel = { showUpdateForm = null }
                )
            }
        }
            // New Measurement Modal
        if (showNewMeasurementModal) {
            NewMeasurementModal(
                onSave = { newMeasurement ->
                    coroutineScope.launch {
                        try {
                            val measurementWithUserId = newMeasurement.copy(userId = userId)
                            val addedMeasurement = RetrofitClient.getClient(context).addMeasurement(measurementWithUserId)
                            measurements = measurements + addedMeasurement
                            showNewMeasurementModal = false
                        } catch (e: Exception) {
                            errorMessage = "Failed to save: ${e.message}"
                        }
                    }
                },
                onCancel = { showNewMeasurementModal = false }
            )
        }

    }
}

@Composable
fun MeasurementItem(
    measurement: MeasurementResponse,
    isExpanded: Boolean,
    onExpandClick: () -> Unit,
    onUpdateClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val buttonColor = Color(0xFF4A90E2)
    Column(
        modifier = Modifier
            .padding(8.dp)
            .background(color = Color.White)
    ) {
        Row(
            modifier = Modifier
                .clickable { onExpandClick() }
                .padding(8.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = measurement.name,
                fontWeight = FontWeight.Medium,
                fontSize = 22.sp,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = if (isExpanded) Icons.Default.ArrowDropDown else Icons.Default.PlayArrow,
                contentDescription = "Expand/Collapse",
                tint = buttonColor,
                modifier = Modifier.align(Alignment.CenterVertically)
            )
        }
        HorizontalDivider(
            modifier = Modifier.padding(horizontal = 8.dp),
            thickness = 1.dp,
            color = Color.LightGray
        )

        if (isExpanded) {
            Column(
                modifier = Modifier
                    .padding(8.dp)
                    .background(color = Color.White)
            ) {
                Text("Head: ${measurement.data.Head}", fontWeight = FontWeight.Normal, fontSize = 20.sp, modifier = Modifier.padding(top = 8.dp, start = 32.dp), color = Color.Gray)
                Text("Chest: ${measurement.data.Chest}", fontWeight = FontWeight.Normal, fontSize = 20.sp, modifier = Modifier.padding(top = 8.dp, start = 32.dp), color = Color.Gray)
                Text("Waist: ${measurement.data.Waist}", fontWeight = FontWeight.Normal, fontSize = 20.sp, modifier = Modifier.padding(top = 8.dp, start = 32.dp), color = Color.Gray)
                Text("Height: ${measurement.data.Height}", fontWeight = FontWeight.Normal, fontSize = 20.sp, modifier = Modifier.padding(top = 8.dp, start = 32.dp), color = Color.Gray)
                Text("Shoulder: ${measurement.data.Sholder}", fontWeight = FontWeight.Normal, fontSize = 20.sp, modifier = Modifier.padding(top = 8.dp, start = 32.dp), color = Color.Gray)
                Text("Arm Length: ${measurement.data.armLength}", fontWeight = FontWeight.Normal, fontSize = 20.sp, modifier = Modifier.padding(top = 8.dp, start = 32.dp), color = Color.Gray)
                Text("Leg Length: ${measurement.data.legLength}", fontWeight = FontWeight.Normal, fontSize = 20.sp, modifier = Modifier.padding(top = 8.dp, start = 32.dp), color = Color.Gray)
                Text("Gender: ${measurement.genderType}", fontWeight = FontWeight.Normal, fontSize = 20.sp, modifier = Modifier.padding(top = 8.dp, start = 32.dp), color = Color.Gray)
                Text("Age: ${measurement.age}", fontWeight = FontWeight.Normal, fontSize = 20.sp, modifier = Modifier.padding(top = 8.dp, start = 32.dp), color = Color.Gray)
                Text("Weight: ${measurement.weight}", fontWeight = FontWeight.Normal, fontSize = 20.sp, modifier = Modifier.padding(top = 8.dp, start = 32.dp), color = Color.Gray)

                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
                    thickness = 1.dp,
                    color = Color.LightGray

                )
                Row(
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Button(
                        modifier = Modifier.height(50.dp),
                        onClick = onUpdateClick,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = buttonColor)
                    ) {
                        Text("Update", color = Color.White)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        modifier = Modifier.height(50.dp),
                        onClick = onDeleteClick,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0XFFF9293E))
                    ) {
                        Text("Delete", color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun MeasurementForm(
    measurement: MeasurementResponse?,
    onSave: (MeasurementResponse) -> Unit,
    onCancel: () -> Unit
) {
    // Inline form for updates (unchanged from original)
    var name by remember { mutableStateOf(measurement?.name ?: "") }
    var head by remember { mutableStateOf(measurement?.data?.Head ?: "") }

    Column(modifier = Modifier.padding(top = 8.dp)) {
        TextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Name") },
            modifier = Modifier.fillMaxWidth()
        )
        TextField(
            value = head,
            onValueChange = { head = it },
            label = { Text("Head") },
            modifier = Modifier.fillMaxWidth()
        )
        Row(
            modifier = Modifier.padding(top = 8.dp)
        ) {
            Button(
                onClick = {
                    if (name.isNotBlank() && head.isNotBlank()) {
                        onSave(
                            MeasurementResponse(
                                id = measurement?.id ?: generateId(),
                                userId = measurement?.userId ?: "",
                                name = name,
                                age = measurement?.age ?: 0,
                                weight = measurement?.weight ?: 0,
                                height = measurement?.height ?: "",
                                data = Data(
                                    Head = head,
                                    Chest = measurement?.data?.Chest ?: "",
                                    Waist = measurement?.data?.Waist ?: "",
                                    Height = measurement?.data?.Height ?: "",
                                    Sholder = measurement?.data?.Sholder ?: "",
                                    armLength = measurement?.data?.armLength ?: "",
                                    legLength = measurement?.data?.legLength ?: ""
                                ),
                                genderType = measurement?.genderType ?: "",
                                createdAt = "",
                                createdBy = "",
                                updatedAt = "",
                                updatedBy = "",
                                deletedAt = null,
                                deletedBy = null
                            )
                        )
                    }
                }
            ) {
                Text("Save")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = onCancel) {
                Text("Cancel")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewMeasurementModal(
    onSave: (MeasurementRequest) -> Unit,
    onCancel: () -> Unit
) {
    val buttonColor = Color(0xFF4A90E2)

    // State for form fields
    var name by remember { mutableStateOf("") }
    var height by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var selectedGender by remember { mutableStateOf<String?>("") }
    var selectedMode by remember { mutableStateOf("Automatic") } // Default to Automatic
    var head by remember { mutableStateOf("") }
    var chest by remember { mutableStateOf("") }
    var waist by remember { mutableStateOf("") }
    var shoulder by remember { mutableStateOf("") }
    var armLength by remember { mutableStateOf("") }
    var legLength by remember { mutableStateOf("") }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        shadowElevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .background(buttonColor)
        ) {
            // Header with title and close button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(buttonColor)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Enter Your Measurements",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
                IconButton(onClick = onCancel) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color.White
                    )
                }
            }

            // Scrollable form fields
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                TextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black,
                        focusedLabelColor = Color.White,
                        unfocusedLabelColor = Color.Black.copy(alpha = 0.5f),
                        cursorColor = Color.White,
                        focusedIndicatorColor = Color.White,
                        unfocusedIndicatorColor = Color.White.copy(alpha = 0.5f)
                    )
                )
                TextField(
                    value = height,
                    onValueChange = { height = it },
                    label = { Text("Height (e.g., 170 cm)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black,
                        focusedLabelColor = Color.White,
                        unfocusedLabelColor = Color.Black.copy(alpha = 0.5f),
                        cursorColor = Color.White,
                        focusedIndicatorColor = Color.White,
                        unfocusedIndicatorColor = Color.White.copy(alpha = 0.5f)
                    )
                )
                TextField(
                    value = weight,
                    onValueChange = { weight = it },
                    label = { Text("Weight (e.g., 70 kg)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black,
                        focusedLabelColor = Color.White,
                        unfocusedLabelColor = Color.Black.copy(alpha = 0.5f),
                        cursorColor = Color.White,
                        focusedIndicatorColor = Color.White,
                        unfocusedIndicatorColor = Color.White.copy(alpha = 0.5f)
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                TextField(
                    value = age,
                    onValueChange = { age = it },
                    label = { Text("Age") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black,
                        focusedLabelColor = Color.White,
                        unfocusedLabelColor = Color.Black.copy(alpha = 0.5f),
                        cursorColor = Color.White,
                        focusedIndicatorColor = Color.White,
                        unfocusedIndicatorColor = Color.White.copy(alpha = 0.5f)
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                // Gender Selection
                val genders = listOf("MALE", "FEMALE")
                var genderExpanded by remember { mutableStateOf(false) }
                if (genders.isNotEmpty()) {
                    ExposedDropdownMenuBox(
                        expanded = genderExpanded,
                        onExpandedChange = { genderExpanded = !genderExpanded }
                    ) {
                        TextField(
                            value = selectedGender ?: "Gender",
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = genderExpanded) },
                            modifier = Modifier
                                .menuAnchor()
                                .padding(bottom = 8.dp)
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp)),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color(0xFFF5F4F9),
                                unfocusedContainerColor = Color(0xFFF5F4F9),
                                focusedTextColor = buttonColor,
                                unfocusedTextColor = buttonColor,
                                focusedLabelColor = Color.White,
                                unfocusedLabelColor = Color.Black.copy(alpha = 0.5f),
                                cursorColor = Color.White,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            )
                        )
                        ExposedDropdownMenu(
                            expanded = genderExpanded,
                            onDismissRequest = { genderExpanded = false },
                            modifier = Modifier
                                .background(Color.White)
                                .clip(RoundedCornerShape(45.dp))
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                        ) {
                            genders.forEachIndexed { index, gender ->
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp)
                                ) {
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                text = gender,
                                                color = Color.Black,
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .wrapContentWidth(Alignment.CenterHorizontally)
                                            )
                                        },
                                        onClick = {
                                            selectedGender = gender
                                            genderExpanded = false
                                        }
                                    )
                                    if (index < genders.size - 1) {
                                        Divider(
                                            color = Color.LightGray,
                                            thickness = 1.dp,
                                            modifier = Modifier.padding(horizontal = 16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Radio Button Group for Measurement Mode
                Column(modifier = Modifier.padding(bottom = 8.dp)) {
                    //Text("Measurement Mode", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = (selectedMode == "Manual"),
                            onClick = { selectedMode = "Manual" },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = Color.White,
                                unselectedColor = Color.Black
                            )
                        )
                        Text("Manual", modifier = Modifier.clickable { selectedMode = "Manual" })
                        Spacer(modifier = Modifier.width(16.dp))
                        RadioButton(
                            selected = (selectedMode == "Automatic"),
                            onClick = { selectedMode = "Automatic" },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = Color.White,
                                unselectedColor = Color.Black
                            )
                        )
                        Text("Automatic", modifier = Modifier.clickable { selectedMode = "Automatic" })
                    }
                }

                // Show manual measurement fields only if "Manual" is selected
                if (selectedMode == "Manual") {
                    TextField(
                        value = head,
                        onValueChange = { head = it },
                        label = { Text("Head") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black,
                            focusedLabelColor = Color.White,
                            unfocusedLabelColor = Color.Black.copy(alpha = 0.5f),
                            cursorColor = Color.White,
                            focusedIndicatorColor = Color.White,
                            unfocusedIndicatorColor = Color.White.copy(alpha = 0.5f)
                        ),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    TextField(
                        value = chest,
                        onValueChange = { chest = it },
                        label = { Text("Chest") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black,
                            focusedLabelColor = Color.White,
                            unfocusedLabelColor = Color.Black.copy(alpha = 0.5f),
                            cursorColor = Color.White,
                            focusedIndicatorColor = Color.White,
                            unfocusedIndicatorColor = Color.White.copy(alpha = 0.5f)
                        ),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    TextField(
                        value = waist,
                        onValueChange = { waist = it },
                        label = { Text("Waist") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black,
                            focusedLabelColor = Color.White,
                            unfocusedLabelColor = Color.Black.copy(alpha = 0.5f),
                            cursorColor = Color.White,
                            focusedIndicatorColor = Color.White,
                            unfocusedIndicatorColor = Color.White.copy(alpha = 0.5f)
                        ),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    TextField(
                        value = shoulder,
                        onValueChange = { shoulder = it },
                        label = { Text("Shoulder") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black,
                            focusedLabelColor = Color.White,
                            unfocusedLabelColor = Color.Black.copy(alpha = 0.5f),
                            cursorColor = Color.White,
                            focusedIndicatorColor = Color.White,
                            unfocusedIndicatorColor = Color.White.copy(alpha = 0.5f)
                        ),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    TextField(
                        value = armLength,
                        onValueChange = { armLength = it },
                        label = { Text("Arm Length") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black,
                            focusedLabelColor = Color.White,
                            unfocusedLabelColor = Color.Black.copy(alpha = 0.5f),
                            cursorColor = Color.White,
                            focusedIndicatorColor = Color.White,
                            unfocusedIndicatorColor = Color.White.copy(alpha = 0.5f)
                        ),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    TextField(
                        value = legLength,
                        onValueChange = { legLength = it },
                        label = { Text("Leg Length") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black,
                            focusedLabelColor = Color.White,
                            unfocusedLabelColor = Color.Black.copy(alpha = 0.5f),
                            cursorColor = Color.White,
                            focusedIndicatorColor = Color.White,
                            unfocusedIndicatorColor = Color.White.copy(alpha = 0.5f)
                        ),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }
            }

            // Save and Cancel Buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(
                    onClick = onCancel,
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .padding(end = 8.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
                ) {
                    Text("Cancel", color = Color.White)
                }
                Button(
                    onClick = {
                        if (name.isNotBlank() && height.isNotBlank() && weight.isNotBlank() &&
                            age.isNotBlank() && selectedGender?.isNotBlank() == true
                        ) {
                            val newMeasurement = MeasurementRequest(
                                userId = "", // Set in onSave callback
                                name = name,
                                age = age.toIntOrNull() ?: 0,
                                weight = weight.toIntOrNull() ?: 0,
                                height = height,
                                data = Data(
                                    Head = if (selectedMode == "Manual") head else "",
                                    Chest = if (selectedMode == "Manual") chest else "",
                                    Waist = if (selectedMode == "Manual") waist else "",
                                    Height = height,
                                    Sholder = if (selectedMode == "Manual") shoulder else "",
                                    armLength = if (selectedMode == "Manual") armLength else "",
                                    legLength = if (selectedMode == "Manual") legLength else ""
                                ),
                                genderType = selectedGender.toString(),
                            )
                            onSave(newMeasurement)
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .padding(start = 8.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = buttonColor),
                    enabled = name.isNotBlank() && height.isNotBlank() && weight.isNotBlank() &&
                            age.isNotBlank() && selectedGender?.isNotBlank() == true &&
                            (selectedMode == "Automatic" || (
                                    selectedMode == "Manual" && head.isNotBlank() && chest.isNotBlank() &&
                                            waist.isNotBlank() && shoulder.isNotBlank() && armLength.isNotBlank() && legLength.isNotBlank()
                                    ))
                ) {
                    Text("Submit", color = Color.White)
                }
            }
        }
    }
}

// Placeholder for generating a unique ID
fun generateId(): String = UUID.randomUUID().toString()