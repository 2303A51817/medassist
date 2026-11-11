package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.*
import com.example.ui.components.MedicineStockPill
import com.example.ui.viewmodel.MedAssistViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PharmacyPortalScreen(
    viewModel: MedAssistViewModel,
    pharmacies: List<Pharmacy>,
    medicines: List<Medicine>,
    interPharmacyRequests: List<InterPharmacyRequest>,
    activePharmacyId: String,
    modifier: Modifier = Modifier
) {
    var selectedPortalTab by remember { mutableStateOf(0) } // 0: Inventory, 1: Inter-Pharmacy Network
    var showAddMedicineDialog by remember { mutableStateOf(false) }
    var showBroadcastDialog by remember { mutableStateOf(false) }
    var editingMedicineForStock by remember { mutableStateOf<Medicine?>(null) }

    val currentPharmacy = pharmacies.find { it.id == activePharmacyId } ?: pharmacies.first()

    // Pharmacy inventory stats
    val inStockCount = medicines.count {
        it.stockByPharmacy[currentPharmacy.id]?.status == StockStatus.IN_STOCK
    }
    val lowStockCount = medicines.count {
        it.stockByPharmacy[currentPharmacy.id]?.status == StockStatus.LOW_STOCK
    }
    val outOfStockCount = medicines.count {
        it.stockByPharmacy[currentPharmacy.id]?.status == StockStatus.OUT_OF_STOCK
    }

    Column(modifier = modifier.fillMaxSize()) {
        // Portal Header & Pharmacy Switcher
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Pharmacist Control Center",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Managing: ${currentPharmacy.name}",
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.primary),
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    // Select pharmacy to manage
                    var showDropdown by remember { mutableStateOf(false) }
                    Box {
                        OutlinedButton(
                            onClick = { showDropdown = true },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text("Switch Store", fontSize = 11.sp)
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null, modifier = Modifier.size(16.dp))
                        }

                        DropdownMenu(
                            expanded = showDropdown,
                            onDismissRequest = { showDropdown = false }
                        ) {
                            pharmacies.forEach { pharm ->
                                DropdownMenuItem(
                                    text = { Text(pharm.name, fontSize = 12.sp) },
                                    onClick = {
                                        viewModel.setPortalPharmacy(pharm.id)
                                        showDropdown = false
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Stats row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "$inStockCount", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                            Text(text = "In Stock", fontSize = 10.sp, color = MaterialTheme.colorScheme.primary)
                        }
                    }

                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "$lowStockCount", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                            Text(text = "Low Stock", fontSize = 10.sp, color = MaterialTheme.colorScheme.secondary)
                        }
                    }

                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "$outOfStockCount", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                            Text(text = "Out of Stock", fontSize = 10.sp, color = MaterialTheme.colorScheme.error)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Tab Switcher (Inventory vs Inter-Pharmacy Network)
                PrimaryTabRow(selectedTabIndex = selectedPortalTab) {
                    Tab(
                        selected = selectedPortalTab == 0,
                        onClick = { selectedPortalTab = 0 },
                        text = { Text("Inventory & Stock") },
                        icon = { Icon(Icons.Default.Inventory, contentDescription = null, modifier = Modifier.size(18.dp)) }
                    )
                    Tab(
                        selected = selectedPortalTab == 1,
                        onClick = { selectedPortalTab = 1 },
                        text = { Text("Inter-Pharmacy Network") },
                        icon = { Icon(Icons.Default.Hub, contentDescription = null, modifier = Modifier.size(18.dp)) }
                    )
                }
            }
        }

        // Tab Content
        if (selectedPortalTab == 0) {
            // Inventory Management
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(top = 12.dp, bottom = 80.dp)
            ) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Catalog Inventory (${medicines.size} items)",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )

                        FilledTonalButton(
                            onClick = { showAddMedicineDialog = true },
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.testTag("add_medicine_button")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Add Medicine", fontSize = 11.sp)
                        }
                    }
                }

                items(medicines, key = { it.id }) { med ->
                    val stock = med.stockByPharmacy[currentPharmacy.id]
                    val qty = stock?.quantity ?: 0
                    val price = stock?.price ?: med.basePrice
                    val status = stock?.status ?: StockStatus.OUT_OF_STOCK

                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
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
                                    text = med.name,
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = "${med.genericFormula} • ${med.dosage}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    MedicineStockPill(status = status, quantity = qty)
                                    Text(
                                        text = "Price: ₹${price.toInt()}",
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                                    )
                                }
                            }

                            Button(
                                onClick = { editingMedicineForStock = med },
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                modifier = Modifier.testTag("edit_stock_${med.name}")
                            ) {
                                Text("Update", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        } else {
            // Inter-Pharmacy Network Tab
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(top = 12.dp, bottom = 80.dp)
            ) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Hub, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "MedLink Inter-Pharmacy Network",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "When a patient asks for a medicine unavailable at your counter, broadcast an alert to nearby network pharmacies. Help customers without losing their care.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Button(
                                onClick = { showBroadcastDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                                modifier = Modifier.testTag("broadcast_request_button")
                            ) {
                                Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Broadcast New Unavailable Medicine Request", fontSize = 12.sp)
                            }
                        }
                    }
                }

                item {
                    Text(
                        text = "Active Network Inquiries (${interPharmacyRequests.size})",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                items(interPharmacyRequests, key = { it.requestId }) { req ->
                    val isMyRequest = req.requestingPharmacyId == currentPharmacy.id

                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = if (req.isUrgent) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer
                                    ) {
                                        Text(
                                            text = if (req.isUrgent) "CRITICAL" else "REGULAR",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = if (req.isUrgent) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onPrimaryContainer,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = req.requestId,
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (req.status == "ACCEPTED") Color(0xFFE8F5E9) else Color(0xFFFFF3E0)
                                ) {
                                    Text(
                                        text = if (req.status == "ACCEPTED") "Fulfilled by ${req.acceptedByPharmacyName}" else "Searching Network...",
                                        color = if (req.status == "ACCEPTED") Color(0xFF2E7D32) else Color(0xFFE65100),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "Medicine: ${req.medicineName} (${req.requestedQuantity} units)",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "Requested by: ${req.requestingPharmacyName} • ${req.requestingPharmacyPhone}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (req.patientNotes.isNotEmpty()) {
                                Text(
                                    text = "Notes: \"${req.patientNotes}\"",
                                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.primary)
                                )
                            }

                            if (!isMyRequest && req.status == "SEARCHING_NETWORK") {
                                Spacer(modifier = Modifier.height(10.dp))
                                Button(
                                    onClick = { viewModel.acceptInterPharmacyRequest(req.requestId) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("fulfill_request_${req.requestId}")
                                ) {
                                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("We Have Stock • Accept & Transfer")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Dialog: Update Stock
    editingMedicineForStock?.let { med ->
        val existingStock = med.stockByPharmacy[currentPharmacy.id]
        var qtyInput by remember { mutableStateOf("${existingStock?.quantity ?: 10}") }
        var priceInput by remember { mutableStateOf("${(existingStock?.price ?: med.basePrice).toInt()}") }

        Dialog(onDismissRequest = { editingMedicineForStock = null }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Update Stock for ${med.name}",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    OutlinedTextField(
                        value = qtyInput,
                        onValueChange = { qtyInput = it },
                        label = { Text("Available Units (Quantity)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = priceInput,
                        onValueChange = { priceInput = it },
                        label = { Text("Price per Pack (₹)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = { editingMedicineForStock = null }) {
                            Text("Cancel")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                val q = qtyInput.toIntOrNull() ?: 0
                                val p = priceInput.toDoubleOrNull() ?: med.basePrice
                                viewModel.updateStock(med.id, q, p)
                                editingMedicineForStock = null
                            }
                        ) {
                            Text("Save Changes")
                        }
                    }
                }
            }
        }
    }

    // Dialog: Broadcast Inter-Pharmacy Request
    if (showBroadcastDialog) {
        var medName by remember { mutableStateOf("") }
        var medQty by remember { mutableStateOf("2") }
        var notes by remember { mutableStateOf("Patient waiting at counter") }
        var isUrgent by remember { mutableStateOf(true) }

        Dialog(onDismissRequest = { showBroadcastDialog = false }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Broadcast Unavailable Medicine",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    OutlinedTextField(
                        value = medName,
                        onValueChange = { medName = it },
                        label = { Text("Medicine Name / Formulation") },
                        placeholder = { Text("e.g. Insulin Glargine or Augmentin") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = medQty,
                        onValueChange = { medQty = it },
                        label = { Text("Quantity Needed") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Patient / Prescription Note") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = isUrgent, onCheckedChange = { isUrgent = it })
                        Text("High Urgency (Critical patient request)", fontSize = 12.sp)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showBroadcastDialog = false }) { Text("Cancel") }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (medName.isNotBlank()) {
                                    viewModel.broadcastInterPharmacy(
                                        medicineName = medName,
                                        quantity = medQty.toIntOrNull() ?: 1,
                                        notes = notes,
                                        isUrgent = isUrgent
                                    )
                                    showBroadcastDialog = false
                                }
                            },
                            enabled = medName.isNotBlank()
                        ) {
                            Text("Broadcast Alert")
                        }
                    }
                }
            }
        }
    }

    // Dialog: Add New Medicine to Inventory
    if (showAddMedicineDialog) {
        var newName by remember { mutableStateOf("") }
        var newGeneric by remember { mutableStateOf("") }
        var newCategory by remember { mutableStateOf("Analgesic & Antipyretic") }
        var newDosage by remember { mutableStateOf("1 tablet daily after meal") }
        var newPrice by remember { mutableStateOf("45") }
        var newQty by remember { mutableStateOf("50") }
        var isRx by remember { mutableStateOf(false) }

        Dialog(onDismissRequest = { showAddMedicineDialog = false }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Add Medicine to Inventory",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    OutlinedTextField(
                        value = newName,
                        onValueChange = { newName = it },
                        label = { Text("Brand Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newGeneric,
                        onValueChange = { newGeneric = it },
                        label = { Text("Generic Formula") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = newPrice,
                            onValueChange = { newPrice = it },
                            label = { Text("Price (₹)") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = newQty,
                            onValueChange = { newQty = it },
                            label = { Text("Initial Stock") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = isRx, onCheckedChange = { isRx = it })
                        Text("Requires Doctor's Prescription (Rx)", fontSize = 12.sp)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showAddMedicineDialog = false }) { Text("Cancel") }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (newName.isNotBlank()) {
                                    viewModel.addNewMedicine(
                                        name = newName,
                                        generic = newGeneric.ifBlank { newName },
                                        category = newCategory,
                                        dosage = newDosage,
                                        form = "Tablet",
                                        price = newPrice.toDoubleOrNull() ?: 50.0,
                                        quantity = newQty.toIntOrNull() ?: 20,
                                        isRx = isRx
                                    )
                                    showAddMedicineDialog = false
                                }
                            },
                            enabled = newName.isNotBlank()
                        ) {
                            Text("Add to Stock")
                        }
                    }
                }
            }
        }
    }
}
