package com.example

import com.example.data.repository.MedRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class ExampleRobolectricTest {

    @Test
    fun `verify pharmacy catalog and ambulances loaded`() {
        val repo = MedRepository()
        assertNotNull(repo.pharmacies.value)
        assertEquals(6, repo.pharmacies.value.size)
        assertEquals(4, repo.ambulances.value.size)
    }
}
