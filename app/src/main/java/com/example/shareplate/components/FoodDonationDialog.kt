package com.example.shareplate.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.shareplate.data.FoodDonation
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType

@Composable
fun FoodDonationDialog(
    onDismiss: () -> Unit,
    onSubmit: (FoodDonation, Uri?) -> Unit,
    latitude: Double,
    longitude: Double
) {
    var foodName by remember { mutableStateOf("") }
    var servingCount by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedImageUri = uri
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Food Donation") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = foodName,
                    onValueChange = { foodName = it },
                    label = { Text("Food Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                OutlinedTextField(
                    value = servingCount,
                    onValueChange = { servingCount = it },
                    label = { Text("Number of Servings") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Button(
                        onClick = { imagePicker.launch("image/*") },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Select Image")
                    }
                }
                
                selectedImageUri?.let { uri ->
                    AsyncImage(
                        model = uri,
                        contentDescription = "Selected food image",
                        modifier = Modifier
                            .size(200.dp)
                            .padding(8.dp)
                            .align(Alignment.CenterHorizontally)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (foodName.isNotBlank() && servingCount.toIntOrNull() != null) {
                        val donation = FoodDonation(
                            food_name = foodName,
                            serving_count = servingCount.toIntOrNull() ?: 0,
                            image_url = null,
                            latitude = latitude,
                            longitude = longitude
                        )
                        onSubmit(donation, selectedImageUri)
                        onDismiss()
                    }
                },
                enabled = foodName.isNotBlank() && servingCount.toIntOrNull() != null
            ) {
                Text("Submit")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
} 