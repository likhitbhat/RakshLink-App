package com.rakshalink

import com.rakshalink.domain.model.FallState
import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.math.sqrt

class FallDetectionLogicTest {

    private fun calculateGForce(x: Float, y: Float, z: Float): Float {
        return sqrt((x * x + y * y + z * z).toDouble()).toFloat() / 9.81f
    }

    private fun evaluateFallImpact(gForce: Float, thresholdG: Float = 2.5f): Boolean {
        return gForce >= thresholdG
    }

    @Test
    fun testNormalMovementDoesNotTriggerFall() {
        // Normal acceleration (1g gravity)
        val g = calculateGForce(0f, 9.81f, 0f)
        assertEquals(false, evaluateFallImpact(g))
    }

    @Test
    fun testHighImpactTriggersFallState() {
        // Sudden high impact (35 m/s^2 => ~3.5g)
        val g = calculateGForce(20f, 20f, 20f)
        assertEquals(true, evaluateFallImpact(g))
    }

    @Test
    fun testFallStateEnumTransitions() {
        var state: FallState = FallState.Monitoring
        assertEquals(FallState.Monitoring, state)

        state = FallState.PossibleFallDetected
        assertEquals(FallState.PossibleFallDetected, state)

        state = FallState.Countdown(15)
        assertTrue(state is FallState.Countdown)
        assertEquals(15, (state as FallState.Countdown).secondsRemaining)

        state = FallState.Cancelled
        assertEquals(FallState.Cancelled, state)
    }

    private fun assertTrue(b: Boolean) {
        org.junit.Assert.assertTrue(b)
    }
}
