package com.rakshalink

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class SafeZoneGeofenceTest {

    private fun calculateDistanceMeters(
        lat1: Double, lon1: Double,
        lat2: Double, lon2: Double
    ): Double {
        val r = 6371000.0 // Earth radius in meters
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return r * c
    }

    @Test
    fun testPointInsideSafeZone() {
        val centerLat = 12.9716
        val centerLng = 77.5946
        val radiusMeters = 200.0

        // Point ~50m away
        val currentLat = 12.9720
        val currentLng = 77.5946

        val distance = calculateDistanceMeters(centerLat, centerLng, currentLat, currentLng)
        assertTrue(distance <= radiusMeters)
    }

    @Test
    fun testPointOutsideSafeZoneTriggersExit() {
        val centerLat = 12.9716
        val centerLng = 77.5946
        val radiusMeters = 100.0

        // Point ~500m away
        val currentLat = 12.9760
        val currentLng = 77.5946

        val distance = calculateDistanceMeters(centerLat, centerLng, currentLat, currentLng)
        assertFalse(distance <= radiusMeters)
    }
}
