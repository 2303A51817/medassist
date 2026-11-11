package com.example.util

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.Locale
import kotlin.coroutines.resume
import kotlin.math.*

data class UserLocation(
    val latitude: Double,
    val longitude: Double,
    val addressName: String = "Medical District, City Center",
    val isRealGps: Boolean = false
)

object LocationHelper {
    // Default reference location (City Center Healthcare Zone)
    val DEFAULT_LOCATION = UserLocation(
        latitude = 17.4435,
        longitude = 78.3842,
        addressName = "Healthcare Hub, Central District",
        isRealGps = false
    )

    fun hasLocationPermission(context: Context): Boolean {
        val fine = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarse = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        return fine || coarse
    }

    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(context: Context): UserLocation {
        if (!hasLocationPermission(context)) {
            return DEFAULT_LOCATION
        }

        return try {
            val fusedClient = LocationServices.getFusedLocationProviderClient(context)
            val cts = CancellationTokenSource()

            val location: Location? = suspendCancellableCoroutine { cont ->
                fusedClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cts.token)
                    .addOnSuccessListener { loc ->
                        if (loc != null) {
                            cont.resume(loc)
                        } else {
                            fusedClient.lastLocation.addOnSuccessListener { lastLoc ->
                                cont.resume(lastLoc)
                            }.addOnFailureListener {
                                cont.resume(null)
                            }
                        }
                    }
                    .addOnFailureListener {
                        cont.resume(null)
                    }
                cont.invokeOnCancellation { cts.cancel() }
            }

            if (location != null) {
                val addressName = getAddressName(context, location.latitude, location.longitude)
                UserLocation(
                    latitude = location.latitude,
                    longitude = location.longitude,
                    addressName = addressName,
                    isRealGps = true
                )
            } else {
                DEFAULT_LOCATION
            }
        } catch (e: Exception) {
            DEFAULT_LOCATION
        }
    }

    private fun getAddressName(context: Context, lat: Double, lng: Double): String {
        return try {
            val geocoder = Geocoder(context, Locale.getDefault())
            @Suppress("DEPRECATION")
            val addresses = geocoder.getFromLocation(lat, lng, 1)
            if (!addresses.isNullOrEmpty()) {
                val addr = addresses[0]
                val subLocality = addr.subLocality ?: addr.locality ?: addr.subAdminArea ?: "Nearby"
                val thoroughfare = addr.thoroughfare ?: ""
                if (thoroughfare.isNotEmpty()) "$thoroughfare, $subLocality" else subLocality
            } else {
                String.format(Locale.US, "GPS: %.4f° N, %.4f° E", lat, lng)
            }
        } catch (e: Exception) {
            String.format(Locale.US, "GPS: %.4f° N, %.4f° E", lat, lng)
        }
    }

    /**
     * Calculates distance in kilometers between two coordinates using Haversine formula
     */
    fun calculateDistanceKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val earthRadius = 6371.0 // kilometers
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2).pow(2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2).pow(2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return earthRadius * c
    }

    fun formatDistance(km: Double): String {
        return if (km < 1.0) {
            "${(km * 1000).roundToInt()} m"
        } else {
            String.format(Locale.US, "%.1f km", km)
        }
    }

    fun estimateDriveMinutes(distanceKm: Double): Int {
        // Assume city emergency/delivery speed ~ 30 km/h with 4 min buffer
        val minutes = (distanceKm / 30.0 * 60).roundToInt() + 3
        return max(3, minutes)
    }
}
