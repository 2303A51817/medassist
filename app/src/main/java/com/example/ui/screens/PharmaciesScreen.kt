package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.components.MedicineStockPill
import com.example.ui.theme.StarRatingColor
import com.example.ui.viewmodel.MedAssistViewModel
import com.example.util.LocationHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PharmaciesScreen(
    viewModel: MedAssistViewModel,
    pharmaciesWithDist: List<Pair<Pharmacy, Double>>,
    allMedicines: List<Medicine>,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var filter24x7Only by remember { mutableStateOf(false) }
    var filterDeliveryOnly by remember { mutableStateOf(false) }
    var expandedPharmacyId by remember { mutableStateOf<String?>(null) }

    val displayedPharmacies = pharmaciesWithDist.filter { (pharm, _) ->
        (!filter24x7Only || pharm.is24x7) && (!filterDeliveryOnly || pharm.deliveryAvailable)
    }

    Column(modifier = modifier.fillMaxSize()) {
        // Filter bar
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 1.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Filter:",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                FilterChip(
                    selected = filter24x7Only,
                    onClick = { filter24x7Only = !filter24x7Only },
                    label = { Text("24x7 Open", fontSize = 11.sp) },
                    leadingIcon = {
                        if (filter24x7Only) {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                        }
                    },
                    modifier = Modifier.testTag("filter_24x7")
                )

                FilterChip(
                    selected = filterDeliveryOnly,
                    onClick = { filterDeliveryOnly = !filterDeliveryOnly },
                    label = { Text("Home Delivery", fontSize = 11.sp) },
                    leadingIcon = {
                        if (filterDeliveryOnly) {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                        }
                    },
                    modifier = Modifier.testTag("filter_delivery")
                )
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(top = 12.dp, bottom = 80.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Medical Shops Near You (${displayedPharmacies.size})",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Sorted by GPS distance",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            items(displayedPharmacies, key = { it.first.id }) { (pharmacy, distanceKm) ->
                val isExpanded = expandedPharmacyId == pharmacy.id
                val pharmacyMedicines = allMedicines.filter { med ->
                    val stock = med.stockByPharmacy[pharmacy.id]
                    stock != null && stock.status != StockStatus.OUT_OF_STOCK
                }

                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("pharmacy_card_${pharmacy.name}")
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        // Title & Rating
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = pharmacy.name,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = pharmacy.address,
                                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            // Rating Badge
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.secondaryContainer,
                                modifier = Modifier.padding(start = 8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = "Rating",
                                        tint = StarRatingColor,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = "${pharmacy.rating}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Distance, Hours, Delivery Chips
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // GPS Distance
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.NearMe,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = LocationHelper.formatDistance(distanceKm),
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    )
                                }
                            }

                            // 24x7 or Hours
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (pharmacy.is24x7) Color(0xFFE8F5E9) else MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Text(
                                    text = if (pharmacy.is24x7) "24x7 Open" else pharmacy.openHours,
                                    color = if (pharmacy.is24x7) Color(0xFF1B5E20) else MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                )
                            }

                            // Delivery
                            if (pharmacy.deliveryAvailable) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant
                                ) {
                                    Text(
                                        text = "⚡ ~${pharmacy.estimatedDeliveryMins}m Delivery",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Action Buttons: Call, Directions, View Inventory
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    val dialIntent = Intent(Intent.ACTION_DIAL).apply {
                                        data = Uri.parse("tel:${pharmacy.phone}")
                                    }
                                    try {
                                        context.startActivity(dialIntent)
                                    } catch (ignored: Exception) {}
                                },
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("call_pharmacy_${pharmacy.name}")
                            ) {
                                Icon(Icons.Default.Phone, contentDescription = "Call", modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Call", fontSize = 11.sp)
                            }

                            OutlinedButton(
                                onClick = {
                                    val mapIntent = Intent(
                                        Intent.ACTION_VIEW,
                                        Uri.parse("geo:${pharmacy.latitude},${pharmacy.longitude}?q=${Uri.encode(pharmacy.name)}")
                                    )
                                    try {
                                        context.startActivity(mapIntent)
                                    } catch (e: Exception) {
                                        // Fallback browser intent
                                        val webIntent = Intent(
                                            Intent.ACTION_VIEW,
                                            Uri.parse("https://maps.google.com/?q=${pharmacy.latitude},${pharmacy.longitude}")
                                        )
                                        context.startActivity(webIntent)
                                    }
                                },
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("directions_pharmacy_${pharmacy.name}")
                            ) {
                                Icon(Icons.Default.Directions, contentDescription = "Directions", modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Map", fontSize = 11.sp)
                            }

                            Button(
                                onClick = {
                                    expandedPharmacyId = if (isExpanded) null else pharmacy.id
                                },
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                modifier = Modifier
                                    .weight(1.2f)
                                    .testTag("view_stock_${pharmacy.name}")
                            ) {
                                Text(if (isExpanded) "Hide Stock" else "Live Stock (${pharmacyMedicines.size})", fontSize = 11.sp)
                            }
                        }

                        // Expanded Live Medicines Stock List
                        AnimatedVisibility(visible = isExpanded) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 12.dp)
                            ) {
                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "In-Stock Medicines at ${pharmacy.name}:",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(6.dp))

                                pharmacyMedicines.forEach { med ->
                                    val stock = med.stockByPharmacy[pharmacy.id]
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = med.name,
                                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold)
                                            )
                                            Text(
                                                text = "${med.dosage} • ₹${(stock?.price ?: med.basePrice).toInt()}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }

                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            if (stock != null) {
                                                MedicineStockPill(status = stock.status, quantity = stock.quantity)
                                            }
                                            Spacer(modifier = Modifier.width(6.dp))
                                            FilledTonalIconButton(
                                                onClick = { viewModel.addToCart(med, pharmacy, 1) },
                                                modifier = Modifier.size(28.dp)
                                            ) {
                                                Icon(Icons.Default.Add, contentDescription = "Add", modifier = Modifier.size(16.dp))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
