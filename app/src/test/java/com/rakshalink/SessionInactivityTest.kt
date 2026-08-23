package com.rakshalink

import com.rakshalink.services.InactivityState
import com.rakshalink.services.InactivityTracker
import org.junit.Assert.assertEquals
import org.junit.Test

class SessionInactivityTest {

    @Test
    fun testInitialInactivityStateIsActive() {
        val tracker = InactivityTracker()
        assertEquals(InactivityState.Active, tracker.state.value)
    }

    @Test
    fun testUserInteractionResetsStateToActive() {
        val tracker = InactivityTracker()
        var signedOut = false
        tracker.resetUserInteraction { signedOut = true }
        assertEquals(InactivityState.Active, tracker.state.value)
        assertEquals(false, signedOut)
    }
}
