package com.example.data.model

import java.util.UUID

enum class StockStatus(val label: String) {
    IN_STOCK("In Stock"),
    LOW_STOCK("Low Stock"),
    OUT_OF_STOCK("Out of Stock")
}

data class StockInfo(
    val pharmacyId: String,
    val quantity: Int,
    val status: StockStatus,
    val price: Double,
    val discountPercent: Int = 0
)

data class Medicine(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val genericFormula: String,
    val category: String, // Analgesic, Antibiotic, Antidiabetic, Antihistamine, Cardiac, etc.
    val dosage: String,
    val form: String, // Tablet, Capsule, Syrup, Inhaler, Drops, Injection
    val manufacturer: String,
    val basePrice: Double,
    val isPrescriptionRequired: Boolean,
    val indications: List<String>,
    val sideEffects: String,
    val precautions: String,
    val stockByPharmacy: Map<String, StockInfo> = emptyMap()
)

data class Pharmacy(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val phone: String,
    val rating: Double,
    val reviewCount: Int,
    val openHours: String,
    val is24x7: Boolean,
    val licenseNumber: String,
    val deliveryAvailable: Boolean,
    val estimatedDeliveryMins: Int = 30,
    val isNetworkMember: Boolean = true
)

enum class AmbulanceType(val title: String, val subtitle: String, val badge: String) {
    BLS("Basic Life Support (BLS)", "Oxygen, Stretcher, First Aid Paramedic", "Standard"),
    ALS("Advanced Life Support (ALS)", "ICU on Wheels, Defibrillator, Ventilator, Critical Care Doctor", "Critical Care"),
    PATIENT_TRANSPORT("Patient Transport", "Non-emergency hospital/clinic transfer", "Transport")
}

data class AmbulanceService(
    val id: String = UUID.randomUUID().toString(),
    val hospitalName: String,
    val type: AmbulanceType,
    val vehicleNumber: String,
    val driverName: String,
    val driverPhone: String,
    val latitude: Double,
    val longitude: Double,
    val baseFare: Double,
    val etaMinutes: Int,
    val isAvailable: Boolean = true,
    val equipment: List<String>
)

data class CartItem(
    val medicine: Medicine,
    val pharmacy: Pharmacy,
    val quantity: Int
)

enum class OrderType {
    EXPRESS_DELIVERY,
    STORE_PICKUP
}

enum class PaymentMethod(val label: String) {
    UPI("UPI (GPay / PhonePe / Paytm)"),
    CARD("Credit / Debit Card"),
    NET_BANKING("Net Banking"),
    COD("Cash on Delivery / Pickup")
}

enum class OrderStatus(val title: String) {
    PLACED("Order Placed"),
    PACKED("Packed by Pharmacist"),
    OUT_FOR_DELIVERY("Out for Delivery"),
    READY_FOR_PICKUP("Ready for Store Pickup"),
    DELIVERED("Completed")
}

data class MedicineOrder(
    val id: String = "ORD-${UUID.randomUUID().toString().take(8).uppercase()}",
    val items: List<CartItem>,
    val pharmacyId: String,
    val pharmacyName: String,
    val pharmacyAddress: String,
    val pharmacyPhone: String,
    val totalAmount: Double,
    val deliveryFee: Double,
    val discount: Double,
    val paymentMethod: PaymentMethod,
    val transactionId: String,
    val orderType: OrderType,
    val deliveryAddress: String,
    val prescriptionAttached: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
    val status: OrderStatus = OrderStatus.PLACED
)

enum class AmbulanceStatus(val title: String) {
    DISPATCHED("Ambulance Dispatched"),
    EN_ROUTE("En Route to Location"),
    NEARBY("Arriving (Under 2 Mins)"),
    ARRIVED("Arrived at Patient Location")
}

data class AmbulanceBooking(
    val bookingId: String = "EMG-${UUID.randomUUID().toString().take(6).uppercase()}",
    val service: AmbulanceService,
    val patientName: String,
    val emergencyType: String,
    val pickupAddress: String,
    val pickupLatitude: Double,
    val pickupLongitude: Double,
    val status: AmbulanceStatus = AmbulanceStatus.DISPATCHED,
    val timestamp: Long = System.currentTimeMillis()
)

data class InterPharmacyRequest(
    val requestId: String = "REQ-${UUID.randomUUID().toString().take(6).uppercase()}",
    val requestingPharmacyId: String,
    val requestingPharmacyName: String,
    val requestingPharmacyPhone: String,
    val medicineName: String,
    val requestedQuantity: Int,
    val patientNotes: String,
    val isUrgent: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
    val status: String = "SEARCHING_NETWORK", // SEARCHING_NETWORK, ACCEPTED, FULFILLED
    val acceptedByPharmacyName: String? = null
)

data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
    val suggestedMedicines: List<Medicine> = emptyList(),
    val isEmergencyAlert: Boolean = false,
    val actionType: String? = null // "AMBULANCE", "VIEW_MEDICINES", null
)
