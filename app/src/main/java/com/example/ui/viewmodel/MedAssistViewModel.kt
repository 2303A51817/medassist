package com.example.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.*
import com.example.data.repository.MedRepository
import com.example.nlp.MedNlpEngine
import com.example.util.LocationHelper
import com.example.util.SpeechHelper
import com.example.util.UserLocation
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class UiState(
    val userLocation: UserLocation = LocationHelper.DEFAULT_LOCATION,
    val isLocating: Boolean = false,
    val searchQuery: String = "",
    val selectedCategory: String = "All",
    val inStockOnly: Boolean = false,
    val otcOnly: Boolean = false,
    val isVoiceSearchActive: Boolean = false,
    val voiceTranscript: String = "",
    val voiceError: String? = null,
    val isListeningToVoice: Boolean = false,
    val activeTab: AppNavTab = AppNavTab.HOME,
    val selectedPharmacyId: String? = null,
    val selectedMedicineId: String? = null,
    val isSosCountdownActive: Boolean = false,
    val sosCountdownSeconds: Int = 3,
    val showOrderSuccessDialog: Boolean = false,
    val lastPlacedOrder: MedicineOrder? = null,
    val checkoutDeliveryType: OrderType = OrderType.EXPRESS_DELIVERY,
    val checkoutPaymentMethod: PaymentMethod = PaymentMethod.UPI,
    val checkoutAddress: String = "Flat 402, Green Heights, Central District",
    val checkoutHasPrescription: Boolean = false,
    val isProcessingPayment: Boolean = false,
    val showOtpDialog: Boolean = false,
    val cardOtpInput: String = "",
    val userRoleMode: UserRoleMode = UserRoleMode.USER // USER or PHARMACIST
)

enum class AppNavTab(val title: String) {
    HOME("Find Medicines"),
    PHARMACIES("Pharmacies"),
    CHATBOT("AI MedBot"),
    AMBULANCE("Ambulance SOS"),
    ORDERS("My Orders")
}

enum class UserRoleMode {
    USER,
    PHARMACIST
}

class MedAssistViewModel(
    private val repository: MedRepository = MedRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    val pharmacies: StateFlow<List<Pharmacy>> = repository.pharmacies
    val medicines: StateFlow<List<Medicine>> = repository.medicines
    val ambulances: StateFlow<List<AmbulanceService>> = repository.ambulances
    val cart: StateFlow<List<CartItem>> = repository.cart
    val orders: StateFlow<List<MedicineOrder>> = repository.orders
    val interPharmacyRequests: StateFlow<List<InterPharmacyRequest>> = repository.interPharmacyRequests
    val chatMessages: StateFlow<List<ChatMessage>> = repository.chatMessages
    val activeAmbulanceBooking: StateFlow<AmbulanceBooking?> = repository.activeAmbulanceBooking
    val activePortalPharmacyId: StateFlow<String> = repository.activePortalPharmacyId

    private var speechHelper: SpeechHelper? = null
    private var sosJob: Job? = null
    private var trackingJob: Job? = null

    // Combined filtered medicines
    val filteredMedicines: StateFlow<List<Medicine>> = combine(
        medicines,
        _uiState
    ) { meds, state ->
        meds.filter { med ->
            val matchesCategory = state.selectedCategory == "All" ||
                    med.category.contains(state.selectedCategory, ignoreCase = true) ||
                    med.indications.any { it.contains(state.selectedCategory, ignoreCase = true) }

            val query = state.searchQuery.trim().lowercase()
            val matchesQuery = query.isEmpty() ||
                    med.name.lowercase().contains(query) ||
                    med.genericFormula.lowercase().contains(query) ||
                    med.category.lowercase().contains(query) ||
                    med.indications.any { it.lowercase().contains(query) }

            val matchesOtc = !state.otcOnly || !med.isPrescriptionRequired

            val matchesInStock = !state.inStockOnly || med.stockByPharmacy.values.any { it.status == StockStatus.IN_STOCK }

            matchesCategory && matchesQuery && matchesOtc && matchesInStock
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Pharmacies sorted by real-time distance
    val sortedPharmacies: StateFlow<List<Pair<Pharmacy, Double>>> = combine(
        pharmacies,
        _uiState
    ) { pharms, state ->
        pharms.map { pharm ->
            val dist = LocationHelper.calculateDistanceKm(
                state.userLocation.latitude,
                state.userLocation.longitude,
                pharm.latitude,
                pharm.longitude
            )
            pharm to dist
        }.sortedBy { it.second }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Ambulances sorted by distance
    val sortedAmbulances: StateFlow<List<Pair<AmbulanceService, Double>>> = combine(
        ambulances,
        _uiState
    ) { ambs, state ->
        ambs.map { amb ->
            val dist = LocationHelper.calculateDistanceKm(
                state.userLocation.latitude,
                state.userLocation.longitude,
                amb.latitude,
                amb.longitude
            )
            amb to dist
        }.sortedBy { it.second }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setNavTab(tab: AppNavTab) {
        _uiState.update { it.copy(activeTab = tab) }
    }

    fun setUserRoleMode(mode: UserRoleMode) {
        _uiState.update { it.copy(userRoleMode = mode) }
    }

    fun setSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun setSelectedCategory(cat: String) {
        _uiState.update { it.copy(selectedCategory = cat) }
    }

    fun toggleInStockFilter() {
        _uiState.update { it.copy(inStockOnly = !it.inStockOnly) }
    }

    fun toggleOtcFilter() {
        _uiState.update { it.copy(otcOnly = !it.otcOnly) }
    }

    fun selectPharmacy(id: String?) {
        _uiState.update { it.copy(selectedPharmacyId = id) }
    }

    fun selectMedicine(id: String?) {
        _uiState.update { it.copy(selectedMedicineId = id) }
    }

    // Location
    fun refreshLocation(context: Context) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLocating = true) }
            val loc = LocationHelper.getCurrentLocation(context)
            _uiState.update { it.copy(userLocation = loc, isLocating = false) }
        }
    }

    // Voice Search
    fun openVoiceSearch(context: Context) {
        _uiState.update {
            it.copy(
                isVoiceSearchActive = true,
                voiceTranscript = "Listening for medicine or symptom...",
                voiceError = null,
                isListeningToVoice = true
            )
        }

        speechHelper = SpeechHelper(context)
        if (speechHelper?.isAvailable() == true) {
            speechHelper?.startListening(
                onResult = { text ->
                    applyVoiceSearch(text)
                },
                onError = { err ->
                    _uiState.update {
                        it.copy(
                            voiceError = err,
                            isListeningToVoice = false,
                            voiceTranscript = "Tap a suggested query below or speak again"
                        )
                    }
                }
            )
        } else {
            // Emulators or devices without Google Speech Services
            _uiState.update {
                it.copy(
                    isListeningToVoice = false,
                    voiceError = "Speech recognition service unavailable on device. You can select popular voice inputs below:",
                    voiceTranscript = "Select Voice Query:"
                )
            }
        }
    }

    fun applyVoiceSearch(query: String) {
        speechHelper?.stopListening()
        _uiState.update {
            it.copy(
                isVoiceSearchActive = false,
                isListeningToVoice = false,
                searchQuery = query
            )
        }
    }

    fun closeVoiceSearch() {
        speechHelper?.stopListening()
        _uiState.update {
            it.copy(isVoiceSearchActive = false, isListeningToVoice = false, voiceError = null)
        }
    }

    // Cart
    fun addToCart(medicine: Medicine, pharmacy: Pharmacy, qty: Int = 1) {
        repository.addToCart(medicine, pharmacy, qty)
    }

    fun updateCartQty(medicineId: String, pharmacyId: String, qty: Int) {
        repository.updateCartQuantity(medicineId, pharmacyId, qty)
    }

    fun removeFromCart(medicineId: String, pharmacyId: String) {
        repository.removeFromCart(medicineId, pharmacyId)
    }

    fun clearCart() {
        repository.clearCart()
    }

    // Checkout Flow
    fun setCheckoutDeliveryType(type: OrderType) {
        _uiState.update { it.copy(checkoutDeliveryType = type) }
    }

    fun setCheckoutPaymentMethod(method: PaymentMethod) {
        _uiState.update { it.copy(checkoutPaymentMethod = method) }
    }

    fun setCheckoutAddress(address: String) {
        _uiState.update { it.copy(checkoutAddress = address) }
    }

    fun togglePrescriptionAttached() {
        _uiState.update { it.copy(checkoutHasPrescription = !it.checkoutHasPrescription) }
    }

    fun processCheckout(items: List<CartItem>, pharmacy: Pharmacy) {
        viewModelScope.launch {
            _uiState.update { it.copy(isProcessingPayment = true) }
            delay(1200) // Realistic banking payment gateway handshake

            val order = repository.placeOrder(
                pharmacy = pharmacy,
                items = items,
                deliveryType = _uiState.value.checkoutDeliveryType,
                deliveryAddress = _uiState.value.checkoutAddress,
                paymentMethod = _uiState.value.checkoutPaymentMethod,
                hasPrescription = _uiState.value.checkoutHasPrescription
            )

            _uiState.update {
                it.copy(
                    isProcessingPayment = false,
                    showOrderSuccessDialog = true,
                    lastPlacedOrder = order
                )
            }
        }
    }

    fun dismissOrderSuccessDialog() {
        _uiState.update { it.copy(showOrderSuccessDialog = false) }
    }

    // Chatbot NLP Logic
    fun sendChatMessage(text: String) {
        if (text.isBlank()) return

        // Add user message
        val userMsg = ChatMessage(text = text, isUser = true)
        repository.addChatMessage(userMsg)

        viewModelScope.launch {
            delay(400) // Brief NLP parsing thinking delay
            val nlpResult = MedNlpEngine.processQuery(text, medicines.value)

            val botMsg = ChatMessage(
                text = nlpResult.replyText,
                isUser = false,
                suggestedMedicines = nlpResult.matchedMedicines,
                isEmergencyAlert = nlpResult.isEmergency,
                actionType = nlpResult.actionType
            )
            repository.addChatMessage(botMsg)
        }
    }

    fun clearChat() {
        repository.clearChat()
    }

    // Ambulance & SOS
    fun startSosCountdown(service: AmbulanceService) {
        sosJob?.cancel()
        _uiState.update { it.copy(isSosCountdownActive = true, sosCountdownSeconds = 3) }
        sosJob = viewModelScope.launch {
            for (i in 3 downTo 1) {
                _uiState.update { it.copy(sosCountdownSeconds = i) }
                delay(1000)
            }
            _uiState.update { it.copy(isSosCountdownActive = false) }
            confirmAmbulanceDispatch(service)
        }
    }

    fun cancelSosCountdown() {
        sosJob?.cancel()
        _uiState.update { it.copy(isSosCountdownActive = false) }
    }

    fun confirmAmbulanceDispatch(service: AmbulanceService) {
        val booking = repository.dispatchAmbulance(
            service = service,
            patientName = "Self / Emergency Patient",
            emergencyType = "Acute Medical Distress",
            pickupAddress = _uiState.value.userLocation.addressName,
            lat = _uiState.value.userLocation.latitude,
            lng = _uiState.value.userLocation.longitude
        )

        // Start progressive live ETA update simulation
        startLiveAmbulanceTracking()
    }

    private fun startLiveAmbulanceTracking() {
        trackingJob?.cancel()
        trackingJob = viewModelScope.launch {
            delay(5000)
            repository.updateAmbulanceStatus(AmbulanceStatus.EN_ROUTE)
            delay(6000)
            repository.updateAmbulanceStatus(AmbulanceStatus.NEARBY)
            delay(8000)
            repository.updateAmbulanceStatus(AmbulanceStatus.ARRIVED)
        }
    }

    fun cancelAmbulanceBooking() {
        trackingJob?.cancel()
        repository.cancelAmbulanceBooking()
    }

    // Pharmacist Operations
    fun setPortalPharmacy(pharmacyId: String) {
        repository.setPortalPharmacy(pharmacyId)
    }

    fun updateStock(medicineId: String, qty: Int, price: Double) {
        val pharmacyId = activePortalPharmacyId.value
        repository.updateMedicineStock(medicineId, pharmacyId, qty, price)
    }

    fun addNewMedicine(
        name: String,
        generic: String,
        category: String,
        dosage: String,
        form: String,
        price: Double,
        quantity: Int,
        isRx: Boolean
    ) {
        val pharmacyId = activePortalPharmacyId.value
        repository.addNewMedicineToInventory(
            pharmacyId = pharmacyId,
            name = name,
            generic = generic,
            category = category,
            dosage = dosage,
            form = form,
            price = price,
            quantity = quantity,
            isRx = isRx
        )
    }

    fun broadcastInterPharmacy(
        medicineName: String,
        quantity: Int,
        notes: String,
        isUrgent: Boolean
    ) {
        val currentPharmacy = pharmacies.value.find { it.id == activePortalPharmacyId.value }
            ?: pharmacies.value.first()
        repository.broadcastInterPharmacyRequest(
            requestingPharmacyId = currentPharmacy.id,
            requestingPharmacyName = currentPharmacy.name,
            requestingPharmacyPhone = currentPharmacy.phone,
            medicineName = medicineName,
            quantity = quantity,
            patientNotes = notes,
            isUrgent = isUrgent
        )
    }

    fun acceptInterPharmacyRequest(requestId: String) {
        val currentPharmacy = pharmacies.value.find { it.id == activePortalPharmacyId.value }
            ?: pharmacies.value.first()
        repository.acceptInterPharmacyRequest(requestId, currentPharmacy.name)
    }
}
