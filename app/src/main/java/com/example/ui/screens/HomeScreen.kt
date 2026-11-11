package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.components.MedicineCard
import com.example.ui.components.MedicineStockPill
import com.example.ui.viewmodel.MedAssistViewModel
import com.example.ui.viewmodel.UiState
import com.example.util.LocationHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: MedAssistViewModel,
    uiState: UiState,
    medicines: List<Medicine>,
    pharmacies: List<Pharmacy>,
    onOpenVoiceSearch: () -> Unit,
    modifier: Modifier = Modifier
) {
    val categories = listOf("All", "Fever", "Antibiotic", "Allergic", "Acidity", "Diabetes", "Hypertension", "Asthma")
    var selectedMedicineForDetails by remember { mutableStateOf<Medicine?>(null) }

    Column(modifier = modifier.fillMaxSize()) {
        // Search & Voice Search Row
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 1.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                OutlinedTextField(
                    value = uiState.searchQuery,
                    onValueChange = { viewModel.setSearchQuery(it) },
                    placeholder = { Text("Search medicine, generic formula, symptom...") },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = "Search", tint = MaterialTheme.colorScheme.primary)
                    },
                    trailingIcon = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (uiState.searchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear")
                                }
                            }
                            // Voice Search Button with distinct colorful styling
                            FilledIconButton(
                                onClick = onOpenVoiceSearch,
                                colors = IconButtonDefaults.filledIconButtonColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                                ),
                                modifier = Modifier
                                    .size(40.dp)
                                    .padding(end = 4.dp)
                                    .testTag("voice_search_mic_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Mic,
                                    contentDescription = "Voice Search",
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    },
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("medicine_search_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Categories Row
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(categories) { cat ->
                        FilterChip(
                            selected = uiState.selectedCategory == cat,
                            onClick = { viewModel.setSelectedCategory(cat) },
                            label = { Text(cat, fontSize = 12.sp) },
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier.testTag("category_chip_$cat")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Fast Filters Row (In Stock Only, OTC Only)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = uiState.inStockOnly,
                        onClick = { viewModel.toggleInStockFilter() },
                        label = { Text("In Stock Only", fontSize = 11.sp) },
                        leadingIcon = {
                            if (uiState.inStockOnly) {
                                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                            }
                        },
                        modifier = Modifier.testTag("filter_in_stock")
                    )

                    FilterChip(
                        selected = uiState.otcOnly,
                        onClick = { viewModel.toggleOtcFilter() },
                        label = { Text("Over-the-Counter (OTC)", fontSize = 11.sp) },
                        leadingIcon = {
                            if (uiState.otcOnly) {
                                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                            }
                        },
                        modifier = Modifier.testTag("filter_otc")
                    )
                }
            }
        }

        // Results List
        if (medicines.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.SearchOff,
                        contentDescription = "No results",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No medicines found",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Try searching by generic name or ask MedBot AI to check other options.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(top = 12.dp, bottom = 80.dp)
            ) {
                item {
                    Text(
                        text = "Found ${medicines.size} medicines matching query near ${uiState.userLocation.addressName.take(30)}...",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                items(medicines, key = { it.id }) { med ->
                    MedicineCard(
                        medicine = med,
                        pharmacies = pharmacies,
                        onSelectMedicine = { selectedMedicineForDetails = it },
                        onAddToCart = { medicine, pharmacy ->
                            viewModel.addToCart(medicine, pharmacy, 1)
                        }
                    )
                }
            }
        }
    }

    // Medicine Detail Bottom Sheet
    selectedMedicineForDetails?.let { med ->
        ModalBottomSheet(
            onDismissRequest = { selectedMedicineForDetails = null },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 32.dp)
            ) {
                // Title & Rx
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = med.name,
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = med.genericFormula,
                            style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.primary),
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    if (med.isPrescriptionRequired) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.errorContainer
                        ) {
                            Text(
                                text = "Prescription (Rx)",
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Indications & Dosage Details
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "Dosage: ${med.dosage}",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium)
                        )
                        Text(
                            text = "Form & Pack: ${med.form} • ${med.manufacturer}",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = "Indications: ${med.indications.joinToString(", ")}",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = "Precautions: ${med.precautions}",
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Real-Time Stock at Nearby Medical Shops",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(8.dp))

                // List of Pharmacies with stock for this medicine
                pharmacies.forEach { pharm ->
                    val stock = med.stockByPharmacy[pharm.id]
                    val distKm = LocationHelper.calculateDistanceKm(
                        uiState.userLocation.latitude,
                        uiState.userLocation.longitude,
                        pharm.latitude,
                        pharm.longitude
                    )
                    val status = stock?.status ?: StockStatus.OUT_OF_STOCK
                    val price = stock?.price ?: med.basePrice

                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = pharm.name,
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold)
                                )
                                Text(
                                    text = "${LocationHelper.formatDistance(distKm)} away • ${pharm.address}",
                                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    MedicineStockPill(status = status, quantity = stock?.quantity)
                                    Text(
                                        text = "₹${price.toInt()}",
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    )
                                }
                            }

                            if (status != StockStatus.OUT_OF_STOCK) {
                                Button(
                                    onClick = {
                                        viewModel.addToCart(med, pharm, 1)
                                        selectedMedicineForDetails = null
                                    },
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                    modifier = Modifier.padding(start = 8.dp)
                                ) {
                                    Text("Order", fontSize = 12.sp)
                                }
                            } else {
                                OutlinedButton(
                                    onClick = {
                                        // Broadcast into network
                                        viewModel.broadcastInterPharmacy(
                                            medicineName = med.name,
                                            quantity = 1,
                                            notes = "Patient requested at ${pharm.name}",
                                            isUrgent = false
                                        )
                                    },
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                                ) {
                                    Text("Request", fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
