package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.*
import com.example.ui.viewmodel.AppNavTab
import com.example.ui.viewmodel.MedAssistViewModel
import com.example.ui.viewmodel.UiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartCheckoutScreen(
    viewModel: MedAssistViewModel,
    uiState: UiState,
    cartItems: List<CartItem>,
    modifier: Modifier = Modifier
) {
    var cardNumber by remember { mutableStateOf("4532 •••• •••• 8821") }
    var cardExpiry by remember { mutableStateOf("08/29") }
    var cardCvv by remember { mutableStateOf("•••") }
    var upiId by remember { mutableStateOf("akshay@oksbi") }

    val hasRxItem = cartItems.any { it.medicine.isPrescriptionRequired }

    // Group items by pharmacy (in typical cases from the same pharmacy)
    val primaryPharmacy = cartItems.firstOrNull()?.pharmacy

    val itemsSubtotal = cartItems.sumOf { item ->
        val stock = item.medicine.stockByPharmacy[item.pharmacy.id]
        (stock?.price ?: item.medicine.basePrice) * item.quantity
    }
    val discount = if (itemsSubtotal > 200) 25.0 else 0.0
    val deliveryFee = if (uiState.checkoutDeliveryType == OrderType.EXPRESS_DELIVERY) 30.0 else 0.0
    val grandTotal = (itemsSubtotal - discount + deliveryFee).coerceAtLeast(0.0)

    Column(modifier = modifier.fillMaxSize()) {
        if (cartItems.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Outlined.ShoppingCart,
                        contentDescription = "Empty Cart",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.size(72.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Your Medicine Cart is Empty",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Search or use voice search to find medicines from nearby medical shops.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Button(
                        onClick = { viewModel.setNavTab(AppNavTab.HOME) },
                        modifier = Modifier.testTag("explore_medicines_button")
                    ) {
                        Text("Find Medicines")
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(top = 16.dp, bottom = 90.dp)
            ) {
                // Pharmacy Header
                item {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocalPharmacy,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Fulfilling Pharmacy: ${primaryPharmacy?.name ?: "Local Pharmacy"}",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text = primaryPharmacy?.address ?: "Medical District",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }

                // Cart Items List
                items(cartItems, key = { "${it.medicine.id}_${it.pharmacy.id}" }) { item ->
                    val unitPrice = item.medicine.stockByPharmacy[item.pharmacy.id]?.price ?: item.medicine.basePrice

                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        shape = RoundedCornerShape(14.dp),
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
                                    text = item.medicine.name,
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = "${item.medicine.dosage} • ₹${unitPrice.toInt()} each",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                if (item.medicine.isPrescriptionRequired) {
                                    Text(
                                        text = "Prescription (Rx) Required",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = MaterialTheme.colorScheme.error,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }
                            }

                            // Stepper: - QTY +
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                FilledTonalIconButton(
                                    onClick = {
                                        viewModel.updateCartQty(item.medicine.id, item.pharmacy.id, item.quantity - 1)
                                    },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(Icons.Default.Remove, contentDescription = "Decrease", modifier = Modifier.size(16.dp))
                                }
                                Text(
                                    text = "${item.quantity}",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    modifier = Modifier.padding(horizontal = 10.dp)
                                )
                                FilledTonalIconButton(
                                    onClick = {
                                        viewModel.updateCartQty(item.medicine.id, item.pharmacy.id, item.quantity + 1)
                                    },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = "Increase", modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }

                // Prescription Upload Checklist if required
                if (hasRxItem) {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (uiState.checkoutHasPrescription) Color(0xFFE8F5E9) else MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
                            ),
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
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = if (uiState.checkoutHasPrescription) Icons.Default.CheckCircle else Icons.Default.Description,
                                        contentDescription = null,
                                        tint = if (uiState.checkoutHasPrescription) Color(0xFF2E7D32) else MaterialTheme.colorScheme.error
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = if (uiState.checkoutHasPrescription) "Doctor's Prescription Verified" else "Doctor's Prescription Required",
                                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                        )
                                        Text(
                                            text = if (uiState.checkoutHasPrescription) "Prescription attached for pharmacy validation" else "Tap attach to certify your medical prescription",
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                }

                                Button(
                                    onClick = { viewModel.togglePrescriptionAttached() },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (uiState.checkoutHasPrescription) Color(0xFF2E7D32) else MaterialTheme.colorScheme.error
                                    ),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(if (uiState.checkoutHasPrescription) "Attached" else "Attach Rx", fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }

                // Delivery Mode
                item {
                    Text(
                        text = "Fulfillment Option",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (uiState.checkoutDeliveryType == OrderType.EXPRESS_DELIVERY) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .weight(1f)
                                .clickable { viewModel.setCheckoutDeliveryType(OrderType.EXPRESS_DELIVERY) }
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "⚡ Express Delivery",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = if (uiState.checkoutDeliveryType == OrderType.EXPRESS_DELIVERY) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "Doorstep in 25-35 mins",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (uiState.checkoutDeliveryType == OrderType.STORE_PICKUP) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .weight(1f)
                                .clickable { viewModel.setCheckoutDeliveryType(OrderType.STORE_PICKUP) }
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "🏬 Store Pickup",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = if (uiState.checkoutDeliveryType == OrderType.STORE_PICKUP) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "Ready at counter in 10 mins",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }

                // Delivery Address Input
                if (uiState.checkoutDeliveryType == OrderType.EXPRESS_DELIVERY) {
                    item {
                        OutlinedTextField(
                            value = uiState.checkoutAddress,
                            onValueChange = { viewModel.setCheckoutAddress(it) },
                            label = { Text("Delivery Address (GPS Auto-detected)") },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                // Secure Payment Methods
                item {
                    Text(
                        text = "Secure Payment Methods",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        PaymentMethod.values().forEach { method ->
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (uiState.checkoutPaymentMethod == method) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surface,
                                border = if (uiState.checkoutPaymentMethod == method) null else androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { viewModel.setCheckoutPaymentMethod(method) }
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        RadioButton(
                                            selected = uiState.checkoutPaymentMethod == method,
                                            onClick = { viewModel.setCheckoutPaymentMethod(method) }
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Column {
                                            Text(
                                                text = method.label,
                                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold)
                                            )
                                            Text(
                                                text = when (method) {
                                                    PaymentMethod.UPI -> "Instant approval via Google Pay / UPI"
                                                    PaymentMethod.CARD -> "Visa, Mastercard, RuPay with 3D Secure OTP"
                                                    PaymentMethod.NET_BANKING -> "Direct net banking payment"
                                                    PaymentMethod.COD -> "Pay cash upon medicine inspection"
                                                },
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Bill Summary
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "Price Details",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Items Total", style = MaterialTheme.typography.bodySmall)
                                Text("₹${itemsSubtotal.toInt()}", style = MaterialTheme.typography.bodySmall)
                            }
                            if (discount > 0) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Pharmacy Discount", style = MaterialTheme.typography.bodySmall, color = Color(0xFF2E7D32))
                                    Text("-₹${discount.toInt()}", style = MaterialTheme.typography.bodySmall, color = Color(0xFF2E7D32))
                                }
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Delivery Fee", style = MaterialTheme.typography.bodySmall)
                                Text(if (deliveryFee == 0.0) "FREE" else "₹${deliveryFee.toInt()}", style = MaterialTheme.typography.bodySmall)
                            }
                            HorizontalDivider()
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Total Amount", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                                Text(
                                    "₹${grandTotal.toInt()}",
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                )
                            }
                        }
                    }
                }

                // Security Badge + Pay Button
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "SSL Encrypted",
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "256-Bit SSL Encrypted Healthcare Payment",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Button(
                            onClick = {
                                if (primaryPharmacy != null) {
                                    viewModel.processCheckout(cartItems, primaryPharmacy)
                                }
                            },
                            enabled = !uiState.isProcessingPayment && (!hasRxItem || uiState.checkoutHasPrescription),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("pay_securely_button")
                        ) {
                            if (uiState.isProcessingPayment) {
                                CircularProgressIndicator(
                                    color = Color.White,
                                    modifier = Modifier.size(22.dp),
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Securing Transaction...")
                            } else {
                                Icon(Icons.Default.VerifiedUser, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "Pay ₹${grandTotal.toInt()} Securely",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                            }
                        }

                        if (hasRxItem && !uiState.checkoutHasPrescription) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Please attach prescription above to proceed with Rx medicines.",
                                color = MaterialTheme.colorScheme.error,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }
    }

    // Order Success & Digital Invoice Modal
    if (uiState.showOrderSuccessDialog && uiState.lastPlacedOrder != null) {
        val order = uiState.lastPlacedOrder
        Dialog(onDismissRequest = { viewModel.dismissOrderSuccessDialog() }) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
                    .testTag("digital_invoice_dialog")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE8F5E9)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Success",
                            tint = Color(0xFF2E7D32),
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Medicine Order Placed!",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Order ID: ${order.id}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Digital Invoice Box
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "Official Pharmacy Receipt",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Pharmacy: ${order.pharmacyName}",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium)
                            )
                            Text(
                                text = "Address: ${order.deliveryAddress}",
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                text = "Payment: ${order.paymentMethod.name} • ${order.transactionId}",
                                style = MaterialTheme.typography.bodySmall
                            )
                            HorizontalDivider()
                            order.items.forEach { itm ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "${itm.medicine.name} (x${itm.quantity})",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                    Text(
                                        text = "₹${((itm.medicine.stockByPharmacy[itm.pharmacy.id]?.price ?: itm.medicine.basePrice) * itm.quantity).toInt()}",
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold)
                                    )
                                }
                            }
                            HorizontalDivider()
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Total Paid", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                                Text(
                                    "₹${order.totalAmount.toInt()}",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            viewModel.dismissOrderSuccessDialog()
                            viewModel.setNavTab(AppNavTab.ORDERS)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("View in Orders")
                    }
                }
            }
        }
    }
}
