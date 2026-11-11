package com.example

import com.example.data.repository.MedRepository
import com.example.nlp.MedNlpEngine
import com.example.util.LocationHelper
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun `nlp engine detects emergency symptoms`() {
        val repo = MedRepository()
        val result = MedNlpEngine.processQuery("Patient has severe chest pain and breathlessness", repo.medicines.value)
        assertTrue(result.isEmergency)
        assertEquals("AMBULANCE", result.actionType)
    }

    @Test
    fun `nlp engine matches medicine queries`() {
        val repo = MedRepository()
        val result = MedNlpEngine.processQuery("Dolo 650", repo.medicines.value)
        assertFalse(result.isEmergency)
        assertTrue(result.matchedMedicines.isNotEmpty())
        assertEquals("Dolo 650", result.matchedMedicines.first().name)
    }

    @Test
    fun `haversine distance calculates reasonable distance`() {
        val dist = LocationHelper.calculateDistanceKm(17.4452, 78.3860, 17.4410, 78.3815)
        assertTrue(dist > 0.1 && dist < 5.0)
    }

    @Test
    fun `repository manages cart correctly`() {
        val repo = MedRepository()
        val med = repo.medicines.value.first()
        val pharm = repo.pharmacies.value.first()

        repo.addToCart(med, pharm, 2)
        assertEquals(1, repo.cart.value.size)
        assertEquals(2, repo.cart.value.first().quantity)

        repo.removeFromCart(med.id, pharm.id)
        assertEquals(0, repo.cart.value.size)
    }
}
