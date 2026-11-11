package com.example.data.repository

import com.example.data.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

class MedRepository {

    // Initial Pre-loaded Pharmacies in the Healthcare District
    private val initialPharmacies = listOf(
        Pharmacy(
            id = "pharm_1",
            name = "Apollo Pharmacy 24x7",
            address = "Plot 14, Main Road, Healthcare Square",
            latitude = 17.4452,
            longitude = 78.3860,
            phone = "+91 98490 12345",
            rating = 4.8,
            reviewCount = 520,
            openHours = "Open 24 Hours",
            is24x7 = true,
            licenseNumber = "DL-APO-2024-9912",
            deliveryAvailable = true,
            estimatedDeliveryMins = 25,
            isNetworkMember = true
        ),
        Pharmacy(
            id = "pharm_2",
            name = "MedPlus Wellness Chemist",
            address = "Shop 3, Green Valley Commercial Complex",
            latitude = 17.4410,
            longitude = 78.3815,
            phone = "+91 98490 23456",
            rating = 4.6,
            reviewCount = 340,
            openHours = "7:00 AM - 11:30 PM",
            is24x7 = false,
            licenseNumber = "DL-MED-2023-4410",
            deliveryAvailable = true,
            estimatedDeliveryMins = 30,
            isNetworkMember = true
        ),
        Pharmacy(
            id = "pharm_3",
            name = "LifeLine Super Specialty Drugs",
            address = "Opp. City Hospital Gate 2, Metro Pillar 84",
            latitude = 17.4485,
            longitude = 78.3892,
            phone = "+91 98490 34567",
            rating = 4.9,
            reviewCount = 890,
            openHours = "Open 24 Hours",
            is24x7 = true,
            licenseNumber = "DL-LIF-2022-1082",
            deliveryAvailable = true,
            estimatedDeliveryMins = 20,
            isNetworkMember = true
        ),
        Pharmacy(
            id = "pharm_4",
            name = "City Care Chemists",
            address = "45 Lakeview Avenue, Near Central Bus Stand",
            latitude = 17.4390,
            longitude = 78.3790,
            phone = "+91 98490 45678",
            rating = 4.3,
            reviewCount = 180,
            openHours = "8:00 AM - 10:00 PM",
            is24x7 = false,
            licenseNumber = "DL-CC-2021-0841",
            deliveryAvailable = true,
            estimatedDeliveryMins = 35,
            isNetworkMember = true
        ),
        Pharmacy(
            id = "pharm_5",
            name = "Pulse Pharmacy & Surgical",
            address = "Ground Floor, Sunrise Medical Center",
            latitude = 17.4520,
            longitude = 78.3910,
            phone = "+91 98490 56789",
            rating = 4.7,
            reviewCount = 410,
            openHours = "Open 24 Hours",
            is24x7 = true,
            licenseNumber = "DL-PUL-2024-5511",
            deliveryAvailable = true,
            estimatedDeliveryMins = 28,
            isNetworkMember = true
        ),
        Pharmacy(
            id = "pharm_6",
            name = "GreenCross Organic & Generic Meds",
            address = "12 Heritage Mall, North Extension",
            latitude = 17.4360,
            longitude = 78.3745,
            phone = "+91 98490 67890",
            rating = 4.4,
            reviewCount = 215,
            openHours = "9:00 AM - 10:30 PM",
            is24x7 = false,
            licenseNumber = "DL-GC-2023-7720",
            deliveryAvailable = false,
            estimatedDeliveryMins = 45,
            isNetworkMember = true
        )
    )

    private val initialMedicines = listOf(
        Medicine(
            id = "med_1",
            name = "Dolo 650",
            genericFormula = "Paracetamol (Acetaminophen) 650mg",
            category = "Analgesic & Antipyretic",
            dosage = "1 tablet every 6 hours after food (max 4 per day)",
            form = "Tablet",
            manufacturer = "Micro Labs Ltd",
            basePrice = 32.0,
            isPrescriptionRequired = false,
            indications = listOf("High Fever", "Headache", "Body Ache", "Post-vaccine discomfort"),
            sideEffects = "Rare mild nausea, excessive doses can cause liver toxicity.",
            precautions = "Do not exceed 4000mg/day. Avoid alcohol consumption.",
            stockByPharmacy = mapOf(
                "pharm_1" to StockInfo("pharm_1", 140, StockStatus.IN_STOCK, 30.0, 5),
                "pharm_2" to StockInfo("pharm_2", 85, StockStatus.IN_STOCK, 32.0, 0),
                "pharm_3" to StockInfo("pharm_3", 220, StockStatus.IN_STOCK, 28.0, 10),
                "pharm_4" to StockInfo("pharm_4", 0, StockStatus.OUT_OF_STOCK, 32.0, 0),
                "pharm_5" to StockInfo("pharm_5", 60, StockStatus.IN_STOCK, 31.0, 3)
            )
        ),
        Medicine(
            id = "med_2",
            name = "Paracetamol",
            genericFormula = "Paracetamol 500mg IP",
            category = "Analgesic & Antipyretic",
            dosage = "1-2 tablets 3 times daily after meals",
            form = "Tablet",
            manufacturer = "Cipla Ltd",
            basePrice = 18.0,
            isPrescriptionRequired = false,
            indications = listOf("Fever", "Mild Pain", "Toothache", "Period Cramps"),
            sideEffects = "Generally safe; rash in allergic individuals.",
            precautions = "Consult doctor if fever continues more than 3 days.",
            stockByPharmacy = mapOf(
                "pharm_1" to StockInfo("pharm_1", 90, StockStatus.IN_STOCK, 18.0, 0),
                "pharm_2" to StockInfo("pharm_2", 110, StockStatus.IN_STOCK, 17.0, 5),
                "pharm_3" to StockInfo("pharm_3", 75, StockStatus.IN_STOCK, 18.0, 0),
                "pharm_4" to StockInfo("pharm_4", 15, StockStatus.LOW_STOCK, 19.0, 0),
                "pharm_6" to StockInfo("pharm_6", 130, StockStatus.IN_STOCK, 16.0, 10)
            )
        ),
        Medicine(
            id = "med_3",
            name = "Amoxicillin & Clavulanic Acid",
            genericFormula = "Amoxicillin 500mg + Clavulanic Acid 125mg (Augmentin 625)",
            category = "Broad-Spectrum Antibiotic",
            dosage = "1 tablet twice daily for 5 to 7 days",
            form = "Tablet",
            manufacturer = "GSK Pharmaceuticals",
            basePrice = 210.0,
            isPrescriptionRequired = true,
            indications = listOf("Respiratory Tract Infection", "Ear/Nose/Throat Infection", "Skin Infection", "Dental Abscess"),
            sideEffects = "Diarrhea, mild stomach upset, nausea.",
            precautions = "Complete the full antibiotic course. Take with meals.",
            stockByPharmacy = mapOf(
                "pharm_1" to StockInfo("pharm_1", 45, StockStatus.IN_STOCK, 200.0, 5),
                "pharm_2" to StockInfo("pharm_2", 8, StockStatus.LOW_STOCK, 210.0, 0),
                "pharm_3" to StockInfo("pharm_3", 60, StockStatus.IN_STOCK, 195.0, 7),
                "pharm_4" to StockInfo("pharm_4", 0, StockStatus.OUT_OF_STOCK, 210.0, 0),
                "pharm_5" to StockInfo("pharm_5", 35, StockStatus.IN_STOCK, 205.0, 2)
            )
        ),
        Medicine(
            id = "med_4",
            name = "Azithromycin",
            genericFormula = "Azithromycin 500mg (Azithral 500)",
            category = "Macrolide Antibiotic",
            dosage = "1 tablet daily 1 hour before or 2 hours after meals for 3-5 days",
            form = "Tablet",
            manufacturer = "Alembic Pharma",
            basePrice = 120.0,
            isPrescriptionRequired = true,
            indications = listOf("Sore Throat", "Tonsillitis", "Pneumonia", "Sinusitis"),
            sideEffects = "Abdominal cramps, loose stools, temporary taste alteration.",
            precautions = "Do not take with antacids containing aluminum or magnesium.",
            stockByPharmacy = mapOf(
                "pharm_1" to StockInfo("pharm_1", 30, StockStatus.IN_STOCK, 115.0, 5),
                "pharm_2" to StockInfo("pharm_2", 0, StockStatus.OUT_OF_STOCK, 120.0, 0),
                "pharm_3" to StockInfo("pharm_3", 80, StockStatus.IN_STOCK, 110.0, 8),
                "pharm_4" to StockInfo("pharm_4", 6, StockStatus.LOW_STOCK, 122.0, 0),
                "pharm_5" to StockInfo("pharm_5", 50, StockStatus.IN_STOCK, 118.0, 2)
            )
        ),
        Medicine(
            id = "med_5",
            name = "Cetirizine",
            genericFormula = "Cetirizine Dihydrochloride 10mg",
            category = "Antihistamine / Anti-Allergic",
            dosage = "1 tablet once daily at bedtime",
            form = "Tablet",
            manufacturer = "Dr. Reddy's Laboratories",
            basePrice = 24.0,
            isPrescriptionRequired = false,
            indications = listOf("Runny Nose", "Sneezing", "Allergic Rhinitis", "Hives & Skin Rash", "Watery Eyes"),
            sideEffects = "Mild drowsiness, dry mouth, headache.",
            precautions = "Caution while driving or operating heavy machinery.",
            stockByPharmacy = mapOf(
                "pharm_1" to StockInfo("pharm_1", 200, StockStatus.IN_STOCK, 22.0, 8),
                "pharm_2" to StockInfo("pharm_2", 150, StockStatus.IN_STOCK, 24.0, 0),
                "pharm_3" to StockInfo("pharm_3", 180, StockStatus.IN_STOCK, 21.0, 10),
                "pharm_4" to StockInfo("pharm_4", 40, StockStatus.IN_STOCK, 24.0, 0),
                "pharm_6" to StockInfo("pharm_6", 90, StockStatus.IN_STOCK, 20.0, 15)
            )
        ),
        Medicine(
            id = "med_6",
            name = "Pantoprazole",
            genericFormula = "Pantoprazole Gastro-Resistant 40mg (Pan 40)",
            category = "Proton Pump Inhibitor (Acidity)",
            dosage = "1 tablet in morning 30 minutes before breakfast",
            form = "Tablet",
            manufacturer = "Alkem Laboratories",
            basePrice = 145.0,
            isPrescriptionRequired = false,
            indications = listOf("Acid Reflux (GERD)", "Heartburn", "Stomach Ulcer", "Gastritis"),
            sideEffects = "Headache, bloating, joint pain in prolonged usage.",
            precautions = "Swallow whole, do not chew or crush tablet.",
            stockByPharmacy = mapOf(
                "pharm_1" to StockInfo("pharm_1", 95, StockStatus.IN_STOCK, 138.0, 5),
                "pharm_2" to StockInfo("pharm_2", 80, StockStatus.IN_STOCK, 142.0, 2),
                "pharm_3" to StockInfo("pharm_3", 120, StockStatus.IN_STOCK, 135.0, 7),
                "pharm_4" to StockInfo("pharm_4", 0, StockStatus.OUT_OF_STOCK, 145.0, 0),
                "pharm_5" to StockInfo("pharm_5", 65, StockStatus.IN_STOCK, 140.0, 3)
            )
        ),
        Medicine(
            id = "med_7",
            name = "Ascoril D Plus Syrup",
            genericFormula = "Dextromethorphan + Phenylephrine + Chlorpheniramine",
            category = "Cough Syrup (Dry Cough)",
            dosage = "10ml three times daily after food",
            form = "Syrup",
            manufacturer = "Glenmark Pharmaceuticals",
            basePrice = 135.0,
            isPrescriptionRequired = false,
            indications = listOf("Dry Cough", "Nasal Congestion", "Throat Irritation", "Cold Symptoms"),
            sideEffects = "Drowsiness, mild dizziness, dry throat.",
            precautions = "Not suitable for children under 4 years without medical supervision.",
            stockByPharmacy = mapOf(
                "pharm_1" to StockInfo("pharm_1", 40, StockStatus.IN_STOCK, 130.0, 4),
                "pharm_2" to StockInfo("pharm_2", 25, StockStatus.IN_STOCK, 135.0, 0),
                "pharm_3" to StockInfo("pharm_3", 55, StockStatus.IN_STOCK, 125.0, 7),
                "pharm_4" to StockInfo("pharm_4", 4, StockStatus.LOW_STOCK, 138.0, 0),
                "pharm_5" to StockInfo("pharm_5", 30, StockStatus.IN_STOCK, 132.0, 2)
            )
        ),
        Medicine(
            id = "med_8",
            name = "Metformin 500mg",
            genericFormula = "Metformin Hydrochloride Sustained Release 500mg (Glycomet SR)",
            category = "Antidiabetic",
            dosage = "1 tablet with evening meal",
            form = "Tablet",
            manufacturer = "USV Private Ltd",
            basePrice = 42.0,
            isPrescriptionRequired = true,
            indications = listOf("Type 2 Diabetes Mellitus", "Insulin Resistance", "PCOS"),
            sideEffects = "Stomach discomfort, metallic taste, nausea during initial days.",
            precautions = "Monitor kidney functions regularly. Avoid skipping meals.",
            stockByPharmacy = mapOf(
                "pharm_1" to StockInfo("pharm_1", 110, StockStatus.IN_STOCK, 40.0, 5),
                "pharm_2" to StockInfo("pharm_2", 90, StockStatus.IN_STOCK, 42.0, 0),
                "pharm_3" to StockInfo("pharm_3", 160, StockStatus.IN_STOCK, 38.0, 8),
                "pharm_4" to StockInfo("pharm_4", 12, StockStatus.LOW_STOCK, 42.0, 0),
                "pharm_6" to StockInfo("pharm_6", 80, StockStatus.IN_STOCK, 39.0, 5)
            )
        ),
        Medicine(
            id = "med_9",
            name = "Telmisartan 40mg",
            genericFormula = "Telmisartan 40mg IP (Telma 40)",
            category = "Antihypertensive (Cardiovascular)",
            dosage = "1 tablet once daily morning with or without food",
            form = "Tablet",
            manufacturer = "Glenmark Pharmaceuticals",
            basePrice = 115.0,
            isPrescriptionRequired = true,
            indications = listOf("High Blood Pressure (Hypertension)", "Cardiovascular Protection"),
            sideEffects = "Dizziness, low blood pressure on standing, sinus pain.",
            precautions = "Do not stop suddenly without cardiologist consultation.",
            stockByPharmacy = mapOf(
                "pharm_1" to StockInfo("pharm_1", 60, StockStatus.IN_STOCK, 110.0, 4),
                "pharm_2" to StockInfo("pharm_2", 40, StockStatus.IN_STOCK, 115.0, 0),
                "pharm_3" to StockInfo("pharm_3", 95, StockStatus.IN_STOCK, 105.0, 9),
                "pharm_4" to StockInfo("pharm_4", 0, StockStatus.OUT_OF_STOCK, 115.0, 0),
                "pharm_5" to StockInfo("pharm_5", 50, StockStatus.IN_STOCK, 112.0, 3)
            )
        ),
        Medicine(
            id = "med_10",
            name = "Salbutamol Inhaler",
            genericFormula = "Salbutamol 100mcg CFC-Free (Asthalin Inhaler)",
            category = "Bronchodilator (Asthma)",
            dosage = "1-2 puffs as required for acute bronchospasm / wheezing",
            form = "Inhaler",
            manufacturer = "Cipla Ltd",
            basePrice = 168.0,
            isPrescriptionRequired = true,
            indications = listOf("Asthma Attack", "Wheezing", "COPD", "Exercise-induced bronchospasm"),
            sideEffects = "Fine muscle tremor, elevated pulse, throat irritation.",
            precautions = "Rinse mouth with water after inhalation. Always keep with you.",
            stockByPharmacy = mapOf(
                "pharm_1" to StockInfo("pharm_1", 25, StockStatus.IN_STOCK, 160.0, 5),
                "pharm_2" to StockInfo("pharm_2", 5, StockStatus.LOW_STOCK, 168.0, 0),
                "pharm_3" to StockInfo("pharm_3", 45, StockStatus.IN_STOCK, 155.0, 8),
                "pharm_4" to StockInfo("pharm_4", 0, StockStatus.OUT_OF_STOCK, 168.0, 0),
                "pharm_5" to StockInfo("pharm_5", 20, StockStatus.IN_STOCK, 162.0, 3)
            )
        ),
        Medicine(
            id = "med_11",
            name = "ORS Probiotic Solution",
            genericFormula = "Oral Rehydration Salts IP (WHO Formula) + Bacillus clausii",
            category = "Electrolytes & Rehydration",
            dosage = "Dissolve entire sachet in 1 liter clean drinking water, sip throughout day",
            form = "Powder / Sachet",
            manufacturer = "FDC Ltd (Electral)",
            basePrice = 22.0,
            isPrescriptionRequired = false,
            indications = listOf("Dehydration", "Diarrhea", "Vomiting", "Heat Exhaustion"),
            sideEffects = "None when prepared with right proportion of water.",
            precautions = "Do not boil solution after preparation. Consume within 24 hours.",
            stockByPharmacy = mapOf(
                "pharm_1" to StockInfo("pharm_1", 300, StockStatus.IN_STOCK, 20.0, 10),
                "pharm_2" to StockInfo("pharm_2", 240, StockStatus.IN_STOCK, 22.0, 0),
                "pharm_3" to StockInfo("pharm_3", 350, StockStatus.IN_STOCK, 20.0, 10),
                "pharm_4" to StockInfo("pharm_4", 80, StockStatus.IN_STOCK, 22.0, 0),
                "pharm_6" to StockInfo("pharm_6", 180, StockStatus.IN_STOCK, 19.0, 12)
            )
        ),
        Medicine(
            id = "med_12",
            name = "Human Insulin Glargine",
            genericFormula = "Insulin Glargine 100 IU/ml (Lantus Cartridge 3ml)",
            category = "Insulin / Critical Hormone",
            dosage = "Administer subcutaneously once daily at same time as advised",
            form = "Injection / Penfill",
            manufacturer = "Sanofi India",
            basePrice = 640.0,
            isPrescriptionRequired = true,
            indications = listOf("Type 1 Diabetes", "Severe Type 2 Diabetes", "Glycemic Control"),
            sideEffects = "Hypoglycemia (sweating, palpitations), injection site reaction.",
            precautions = "Store refrigerated at 2°C - 8°C. Do not freeze.",
            stockByPharmacy = mapOf(
                "pharm_1" to StockInfo("pharm_1", 18, StockStatus.IN_STOCK, 610.0, 5),
                "pharm_3" to StockInfo("pharm_3", 30, StockStatus.IN_STOCK, 590.0, 8),
                "pharm_5" to StockInfo("pharm_5", 12, StockStatus.IN_STOCK, 620.0, 3)
            )
        )
    )

    private val initialAmbulances = listOf(
        AmbulanceService(
            id = "amb_1",
            hospitalName = "Apollo Emergency Trauma Center",
            type = AmbulanceType.ALS,
            vehicleNumber = "TS-09-EM-1081",
            driverName = "Rajesh Sharma (Critical Care Tech)",
            driverPhone = "+91 99001 10801",
            latitude = 17.4460,
            longitude = 78.3870,
            baseFare = 1500.0,
            etaMinutes = 6,
            isAvailable = true,
            equipment = listOf("Transport Ventilator", "Defibrillator", "Oxygen Cylinder", "Multipara Monitor", "ICU Paramedic")
        ),
        AmbulanceService(
            id = "amb_2",
            hospitalName = "City Care General Hospital",
            type = AmbulanceType.BLS,
            vehicleNumber = "TS-09-EM-2022",
            driverName = "Mohammed Imran",
            driverPhone = "+91 99001 10802",
            latitude = 17.4415,
            longitude = 78.3800,
            baseFare = 750.0,
            etaMinutes = 9,
            isAvailable = true,
            equipment = listOf("Oxygen Delivery", "Foldable Stretcher", "First Aid Kit", "Suction Unit")
        ),
        AmbulanceService(
            id = "amb_3",
            hospitalName = "LifeLine Heart & Brain Institute",
            type = AmbulanceType.ALS,
            vehicleNumber = "TS-09-EM-3033",
            driverName = "Suresh Reddy (EMS Specialist)",
            driverPhone = "+91 99001 10803",
            latitude = 17.4500,
            longitude = 78.3905,
            baseFare = 1800.0,
            etaMinutes = 8,
            isAvailable = true,
            equipment = listOf("ECG Monitor", "Defibrillator", "Advance Airway", "Syringe Infusion Pump", "ER Doctor on Call")
        ),
        AmbulanceService(
            id = "amb_4",
            hospitalName = "Red Cross Patient Transit Services",
            type = AmbulanceType.PATIENT_TRANSPORT,
            vehicleNumber = "TS-09-PT-4044",
            driverName = "Vikram Patel",
            driverPhone = "+91 99001 10804",
            latitude = 17.4375,
            longitude = 78.3760,
            baseFare = 500.0,
            etaMinutes = 14,
            isAvailable = true,
            equipment = listOf("Wheelchair Ramp", "Comfort Stretcher", "Basic First Aid")
        )
    )

    private val initialInterPharmacyRequests = listOf(
        InterPharmacyRequest(
            requestId = "REQ-58491",
            requestingPharmacyId = "pharm_4",
            requestingPharmacyName = "City Care Chemists",
            requestingPharmacyPhone = "+91 98490 45678",
            medicineName = "Human Insulin Glargine (Lantus)",
            requestedQuantity = 2,
            patientNotes = "Patient waiting at counter with emergency high glucose reading.",
            isUrgent = true,
            timestamp = System.currentTimeMillis() - 1000 * 60 * 15,
            status = "ACCEPTED",
            acceptedByPharmacyName = "Apollo Pharmacy 24x7"
        ),
        InterPharmacyRequest(
            requestId = "REQ-89214",
            requestingPharmacyId = "pharm_2",
            requestingPharmacyName = "MedPlus Wellness Chemist",
            requestingPharmacyPhone = "+91 98490 23456",
            medicineName = "Azithromycin 500mg",
            requestedQuantity = 5,
            patientNotes = "Regular customer post-surgery prescription.",
            isUrgent = false,
            timestamp = System.currentTimeMillis() - 1000 * 60 * 40,
            status = "SEARCHING_NETWORK",
            acceptedByPharmacyName = null
        )
    )

    private val initialOrders = listOf(
        MedicineOrder(
            id = "ORD-MED9821",
            items = listOf(
                CartItem(
                    medicine = initialMedicines[0], // Dolo 650
                    pharmacy = initialPharmacies[0],
                    quantity = 2
                ),
                CartItem(
                    medicine = initialMedicines[4], // Cetirizine
                    pharmacy = initialPharmacies[0],
                    quantity = 1
                )
            ),
            pharmacyId = "pharm_1",
            pharmacyName = "Apollo Pharmacy 24x7",
            pharmacyAddress = "Plot 14, Main Road, Healthcare Square",
            pharmacyPhone = "+91 98490 12345",
            totalAmount = 82.0,
            deliveryFee = 25.0,
            discount = 4.0,
            paymentMethod = PaymentMethod.UPI,
            transactionId = "TXN_UPI_8829104",
            orderType = OrderType.EXPRESS_DELIVERY,
            deliveryAddress = "Flat 402, Green Heights, Central District",
            prescriptionAttached = false,
            timestamp = System.currentTimeMillis() - 1000 * 60 * 90,
            status = OrderStatus.DELIVERED
        )
    )

    private val initialChatMessages = listOf(
        ChatMessage(
            id = "msg_welcome",
            text = "👋 Hello! Welcome to MedAssist Smart Healthcare.\n\n" +
                    "I am MedBot, your clinical NLP assistant. You can:\n" +
                    "• Speak or type symptoms to locate matching medicines\n" +
                    "• Check live stock in medical shops near you\n" +
                    "• Inquire about proper dosage and precautions\n" +
                    "• Request immediate emergency ambulance dispatch\n\n" +
                    "How are you or your family member feeling today?",
            isUser = false,
            suggestedMedicines = emptyList()
        )
    )

    // Reactive State Holders
    private val _pharmacies = MutableStateFlow(initialPharmacies)
    val pharmacies: StateFlow<List<Pharmacy>> = _pharmacies.asStateFlow()

    private val _medicines = MutableStateFlow(initialMedicines)
    val medicines: StateFlow<List<Medicine>> = _medicines.asStateFlow()

    private val _ambulances = MutableStateFlow(initialAmbulances)
    val ambulances: StateFlow<List<AmbulanceService>> = _ambulances.asStateFlow()

    private val _cart = MutableStateFlow<List<CartItem>>(emptyList())
    val cart: StateFlow<List<CartItem>> = _cart.asStateFlow()

    private val _orders = MutableStateFlow(initialOrders)
    val orders: StateFlow<List<MedicineOrder>> = _orders.asStateFlow()

    private val _interPharmacyRequests = MutableStateFlow(initialInterPharmacyRequests)
    val interPharmacyRequests: StateFlow<List<InterPharmacyRequest>> = _interPharmacyRequests.asStateFlow()

    private val _chatMessages = MutableStateFlow(initialChatMessages)
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    private val _activeAmbulanceBooking = MutableStateFlow<AmbulanceBooking?>(null)
    val activeAmbulanceBooking: StateFlow<AmbulanceBooking?> = _activeAmbulanceBooking.asStateFlow()

    // Active Pharmacy in Portal Mode (Default to City Care Chemists for easy demo)
    private val _activePortalPharmacyId = MutableStateFlow("pharm_4")
    val activePortalPharmacyId: StateFlow<String> = _activePortalPharmacyId.asStateFlow()

    fun setPortalPharmacy(pharmacyId: String) {
        _activePortalPharmacyId.value = pharmacyId
    }

    // Cart Operations
    fun addToCart(medicine: Medicine, pharmacy: Pharmacy, quantity: Int = 1) {
        val currentList = _cart.value.toMutableList()
        val existingIndex = currentList.indexOfFirst {
            it.medicine.id == medicine.id && it.pharmacy.id == pharmacy.id
        }
        if (existingIndex >= 0) {
            val existing = currentList[existingIndex]
            currentList[existingIndex] = existing.copy(quantity = existing.quantity + quantity)
        } else {
            currentList.add(CartItem(medicine, pharmacy, quantity))
        }
        _cart.value = currentList
    }

    fun updateCartQuantity(medicineId: String, pharmacyId: String, newQuantity: Int) {
        if (newQuantity <= 0) {
            removeFromCart(medicineId, pharmacyId)
            return
        }
        _cart.value = _cart.value.map { item ->
            if (item.medicine.id == medicineId && item.pharmacy.id == pharmacyId) {
                item.copy(quantity = newQuantity)
            } else {
                item
            }
        }
    }

    fun removeFromCart(medicineId: String, pharmacyId: String) {
        _cart.value = _cart.value.filterNot {
            it.medicine.id == medicineId && it.pharmacy.id == pharmacyId
        }
    }

    fun clearCart() {
        _cart.value = emptyList()
    }

    // Place Order
    fun placeOrder(
        pharmacy: Pharmacy,
        items: List<CartItem>,
        deliveryType: OrderType,
        deliveryAddress: String,
        paymentMethod: PaymentMethod,
        hasPrescription: Boolean
    ): MedicineOrder {
        val subtotal = items.sumOf { item ->
            val stock = item.medicine.stockByPharmacy[pharmacy.id]
            (stock?.price ?: item.medicine.basePrice) * item.quantity
        }
        val discount = if (subtotal > 200) 25.0 else 0.0
        val deliveryFee = if (deliveryType == OrderType.EXPRESS_DELIVERY) 30.0 else 0.0
        val total = (subtotal - discount + deliveryFee).coerceAtLeast(0.0)

        val txnId = "TXN_${paymentMethod.name}_${UUID.randomUUID().toString().take(8).uppercase()}"

        val order = MedicineOrder(
            items = items,
            pharmacyId = pharmacy.id,
            pharmacyName = pharmacy.name,
            pharmacyAddress = pharmacy.address,
            pharmacyPhone = pharmacy.phone,
            totalAmount = total,
            deliveryFee = deliveryFee,
            discount = discount,
            paymentMethod = paymentMethod,
            transactionId = txnId,
            orderType = deliveryType,
            deliveryAddress = deliveryAddress,
            prescriptionAttached = hasPrescription,
            timestamp = System.currentTimeMillis(),
            status = OrderStatus.PLACED
        )

        _orders.value = listOf(order) + _orders.value
        clearCart()
        return order
    }

    // Ambulance Operations
    fun dispatchAmbulance(
        service: AmbulanceService,
        patientName: String,
        emergencyType: String,
        pickupAddress: String,
        lat: Double,
        lng: Double
    ): AmbulanceBooking {
        val booking = AmbulanceBooking(
            service = service,
            patientName = patientName,
            emergencyType = emergencyType,
            pickupAddress = pickupAddress,
            pickupLatitude = lat,
            pickupLongitude = lng,
            status = AmbulanceStatus.DISPATCHED,
            timestamp = System.currentTimeMillis()
        )
        _activeAmbulanceBooking.value = booking
        return booking
    }

    fun updateAmbulanceStatus(status: AmbulanceStatus) {
        _activeAmbulanceBooking.value = _activeAmbulanceBooking.value?.copy(status = status)
    }

    fun cancelAmbulanceBooking() {
        _activeAmbulanceBooking.value = null
    }

    // Pharmacist Portal Operations
    fun updateMedicineStock(
        medicineId: String,
        pharmacyId: String,
        newQuantity: Int,
        newPrice: Double
    ) {
        _medicines.value = _medicines.value.map { med ->
            if (med.id == medicineId) {
                val currentStockMap = med.stockByPharmacy.toMutableMap()
                val newStatus = when {
                    newQuantity <= 0 -> StockStatus.OUT_OF_STOCK
                    newQuantity < 10 -> StockStatus.LOW_STOCK
                    else -> StockStatus.IN_STOCK
                }
                currentStockMap[pharmacyId] = StockInfo(
                    pharmacyId = pharmacyId,
                    quantity = newQuantity,
                    status = newStatus,
                    price = newPrice
                )
                med.copy(stockByPharmacy = currentStockMap)
            } else {
                med
            }
        }
    }

    fun addNewMedicineToInventory(
        pharmacyId: String,
        name: String,
        generic: String,
        category: String,
        dosage: String,
        form: String,
        price: Double,
        quantity: Int,
        isRx: Boolean
    ) {
        val newMed = Medicine(
            name = name,
            genericFormula = generic,
            category = category,
            dosage = dosage,
            form = form,
            manufacturer = "Standard Pharma Ltd",
            basePrice = price,
            isPrescriptionRequired = isRx,
            indications = listOf("Prescribed Treatment", category),
            sideEffects = "Follow pharmacist advice.",
            precautions = "Keep in cool and dry place.",
            stockByPharmacy = mapOf(
                pharmacyId to StockInfo(
                    pharmacyId = pharmacyId,
                    quantity = quantity,
                    status = if (quantity > 10) StockStatus.IN_STOCK else StockStatus.LOW_STOCK,
                    price = price
                )
            )
        )
        _medicines.value = listOf(newMed) + _medicines.value
    }

    // Inter-Pharmacy Network Broadcast
    fun broadcastInterPharmacyRequest(
        requestingPharmacyId: String,
        requestingPharmacyName: String,
        requestingPharmacyPhone: String,
        medicineName: String,
        quantity: Int,
        patientNotes: String,
        isUrgent: Boolean
    ): InterPharmacyRequest {
        val req = InterPharmacyRequest(
            requestingPharmacyId = requestingPharmacyId,
            requestingPharmacyName = requestingPharmacyName,
            requestingPharmacyPhone = requestingPharmacyPhone,
            medicineName = medicineName,
            requestedQuantity = quantity,
            patientNotes = patientNotes,
            isUrgent = isUrgent,
            status = "SEARCHING_NETWORK"
        )
        _interPharmacyRequests.value = listOf(req) + _interPharmacyRequests.value
        return req
    }

    fun acceptInterPharmacyRequest(requestId: String, fulfillingPharmacyName: String) {
        _interPharmacyRequests.value = _interPharmacyRequests.value.map { req ->
            if (req.requestId == requestId) {
                req.copy(status = "ACCEPTED", acceptedByPharmacyName = fulfillingPharmacyName)
            } else {
                req
            }
        }
    }

    // Chatbot Operations
    fun addChatMessage(message: ChatMessage) {
        _chatMessages.value = _chatMessages.value + message
    }

    fun clearChat() {
        _chatMessages.value = initialChatMessages
    }
}
