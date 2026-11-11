package com.example.nlp

import com.example.data.model.Medicine

data class NlpAnalysisResult(
    val replyText: String,
    val matchedMedicines: List<Medicine>,
    val isEmergency: Boolean = false,
    val actionType: String? = null,
    val extractedSymptoms: List<String> = emptyList(),
    val extractedIntent: String
)

object MedNlpEngine {

    private val EMERGENCY_KEYWORDS = listOf(
        "chest pain", "heart attack", "can't breathe", "cannot breathe", "severe breathlessness",
        "stroke", "unconscious", "heavy bleeding", "cardiac arrest", "choking", "collapsed",
        "paralyzed", "seizure", "ambulance", "emergency", "dying"
    )

    private val SYMPTOM_MAP = mapOf(
        "fever" to listOf("Paracetamol", "Dolo 650", "Ibuprofen"),
        "temperature" to listOf("Paracetamol", "Dolo 650"),
        "headache" to listOf("Paracetamol", "Dolo 650", "Ibuprofen"),
        "migraine" to listOf("Ibuprofen", "Paracetamol"),
        "cough" to listOf("Ascoril D Plus Syrup", "Montelukast"),
        "cold" to listOf("Cetirizine", "Ascoril D Plus Syrup"),
        "sore throat" to listOf("Azithromycin", "Ascoril D Plus Syrup"),
        "infection" to listOf("Azithromycin", "Amoxicillin & Clavulanic Acid"),
        "bacterial" to listOf("Azithromycin", "Amoxicillin & Clavulanic Acid"),
        "acidity" to listOf("Pantoprazole", "Omeprazole"),
        "gas" to listOf("Pantoprazole", "Omeprazole"),
        "heartburn" to listOf("Pantoprazole", "Omeprazole"),
        "stomach" to listOf("Pantoprazole", "Omeprazole", "ORS Probiotic Solution"),
        "diarrhea" to listOf("ORS Probiotic Solution"),
        "dehydration" to listOf("ORS Probiotic Solution"),
        "vomiting" to listOf("ORS Probiotic Solution"),
        "diabetes" to listOf("Metformin 500mg", "Human Insulin Glargine"),
        "blood sugar" to listOf("Metformin 500mg", "Human Insulin Glargine"),
        "hypertension" to listOf("Telmisartan 40mg"),
        "blood pressure" to listOf("Telmisartan 40mg"),
        "bp" to listOf("Telmisartan 40mg"),
        "asthma" to listOf("Salbutamol Inhaler", "Montelukast"),
        "allergy" to listOf("Cetirizine", "Montelukast"),
        "sneezing" to listOf("Cetirizine"),
        "body ache" to listOf("Ibuprofen", "Dolo 650"),
        "pain" to listOf("Ibuprofen", "Paracetamol", "Dolo 650")
    )

    fun processQuery(query: String, allMedicines: List<Medicine>): NlpAnalysisResult {
        val cleanQuery = query.lowercase().trim()

        // 1. Emergency Detection (Critical NLP Filter)
        val matchedEmergency = EMERGENCY_KEYWORDS.filter { cleanQuery.contains(it) }
        if (matchedEmergency.isNotEmpty()) {
            return NlpAnalysisResult(
                replyText = "⚠️ CRITICAL MEDICAL ALERT: Your symptoms (${matchedEmergency.joinToString(", ")}) indicate a potentially severe condition requiring immediate clinical intervention.\n\n" +
                        "• Tap the RED EMERGENCY DISPATCH button below to send an ALS (ICU) Ambulance immediately.\n" +
                        "• Keep the patient calm and seated. Do not attempt self-medication.",
                matchedMedicines = emptyList(),
                isEmergency = true,
                actionType = "AMBULANCE",
                extractedSymptoms = matchedEmergency,
                extractedIntent = "EMERGENCY_SOS"
            )
        }

        // 2. Specific Medicine Name Lookups
        val matchedByName = allMedicines.filter { med ->
            cleanQuery.contains(med.name.lowercase()) ||
            med.name.lowercase().contains(cleanQuery) ||
            cleanQuery.contains(med.genericFormula.lowercase())
        }
        if (matchedByName.isNotEmpty() && !cleanQuery.contains("dosage") && !cleanQuery.contains("how to take")) {
            val med = matchedByName.first()
            val rxNote = if (med.isPrescriptionRequired) "⚠️ Prescription (Rx) Required" else "✅ Over The Counter (OTC)"
            return NlpAnalysisResult(
                replyText = "Found \"${med.name}\" in nearby pharmacies:\n\n" +
                        "• Generic: ${med.genericFormula}\n" +
                        "• Indication: ${med.indications.joinToString(", ")}\n" +
                        "• Status: $rxNote\n" +
                        "• Standard Dosage: ${med.dosage}\n" +
                        "• Approx Price: ₹${med.basePrice.toInt()}\n\n" +
                        "You can check real-time stock across nearby medical shops or order directly below.",
                matchedMedicines = matchedByName,
                isEmergency = false,
                actionType = "VIEW_MEDICINES",
                extractedIntent = "FIND_SPECIFIC_MEDICINE"
            )
        }

        // 3. Dosage / How-to-take queries
        if (cleanQuery.contains("dosage") || cleanQuery.contains("how many") || cleanQuery.contains("how to take") || cleanQuery.contains("empty stomach")) {
            val targetMed = allMedicines.find { cleanQuery.contains(it.name.lowercase()) } ?: allMedicines.first()
            return NlpAnalysisResult(
                replyText = "📋 Dosage & Usage Guidelines for ${targetMed.name}:\n\n" +
                        "• Recommended Dose: ${targetMed.dosage}\n" +
                        "• Formulation: ${targetMed.form}\n" +
                        "• Key Precautions: ${targetMed.precautions}\n" +
                        "• Possible Side Effects: ${targetMed.sideEffects}\n\n" +
                        "⚠️ Note: Always follow your consulting doctor's prescription and instructions on packaging.",
                matchedMedicines = listOf(targetMed),
                isEmergency = false,
                extractedIntent = "DOSAGE_INQUIRY"
            )
        }

        // 4. Symptom NLP Extraction & Mapping
        val detectedSymptoms = mutableListOf<String>()
        val candidateNames = mutableSetOf<String>()

        for ((symptom, meds) in SYMPTOM_MAP) {
            if (cleanQuery.contains(symptom)) {
                detectedSymptoms.add(symptom)
                candidateNames.addAll(meds)
            }
        }

        if (detectedSymptoms.isNotEmpty()) {
            val matchingMedicines = allMedicines.filter { candidateNames.contains(it.name) }
            val formattedSymptoms = detectedSymptoms.distinct().joinToString(", ") { it.replaceFirstChar(Char::titlecase) }
            return NlpAnalysisResult(
                replyText = "🩺 Symptom Analysis based on NLP Clinical Triage:\n\n" +
                        "Identified: $formattedSymptoms\n\n" +
                        "Commonly prescribed medications for these indications are listed below. You can check which nearby pharmacy has them in stock and place an order.\n\n" +
                        "💡 Stay hydrated and consult a healthcare provider if symptoms persist over 48 hours.",
                matchedMedicines = matchingMedicines,
                isEmergency = false,
                actionType = "VIEW_MEDICINES",
                extractedSymptoms = detectedSymptoms,
                extractedIntent = "SYMPTOM_ANALYSIS"
            )
        }

        // 5. General Greeting or Help
        if (cleanQuery.contains("hello") || cleanQuery.contains("hi") || cleanQuery.contains("hey") || cleanQuery.contains("help")) {
            return NlpAnalysisResult(
                replyText = "👋 Hello! I am MedBot, your intelligent health and pharmacy assistant.\n\n" +
                        "How can I help you today?\n" +
                        "• Type or speak your symptoms (e.g. \"headache and mild fever\")\n" +
                        "• Check medicine availability (e.g. \"Do nearby shops have Dolo 650?\")\n" +
                        "• Ask dosage questions (e.g. \"dosage for Cetirizine\")\n" +
                        "• Request emergency ambulance support.",
                matchedMedicines = allMedicines.take(3),
                isEmergency = false,
                extractedIntent = "GREETING"
            )
        }

        // 6. Fallback General NLP Match
        val partialMatches = allMedicines.filter { med ->
            med.indications.any { cleanQuery.contains(it.lowercase()) } ||
            cleanQuery.contains(med.category.lowercase())
        }

        return NlpAnalysisResult(
            replyText = "I parsed your inquiry for \"$query\". Here are verified medicines in nearby pharmacies that match your medical category.\n\n" +
                    "Feel free to ask for specific indications, side effects, or nearby availability.",
            matchedMedicines = if (partialMatches.isNotEmpty()) partialMatches else allMedicines.take(4),
            isEmergency = false,
            actionType = "VIEW_MEDICINES",
            extractedIntent = "GENERAL_SEARCH"
        )
    }
}
