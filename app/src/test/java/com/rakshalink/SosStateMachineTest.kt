package com.rakshalink

import com.rakshalink.data.preferences.UserPreferencesManager
import com.rakshalink.data.remote.supabase.SupabaseClientProvider
import com.rakshalink.data.repository.SosRepositoryImpl
import com.rakshalink.domain.model.SosState
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SosStateMachineTest {

    private lateinit var repository: SosRepositoryImpl
    private val supabaseProvider: SupabaseClientProvider = mockk(relaxed = true)
    private val userPreferencesManager: UserPreferencesManager = mockk(relaxed = true)

    @Before
    fun setup() {
        repository = SosRepositoryImpl(supabaseProvider, userPreferencesManager)
    }

    @Test
    fun testInitialStateIsIdle() {
        assertEquals(SosState.Idle, repository.sosState.value)
    }

    @Test
    fun testStartPressingTransitionsToPressing() {
        repository.startPressing()
        assertEquals(SosState.Pressing, repository.sosState.value)
    }

    @Test
    fun testCancelPressingReturnsToIdle() {
        repository.startPressing()
        repository.cancelPressing()
        assertEquals(SosState.Idle, repository.sosState.value)
    }

    @Test
    fun testArmSosAndConfirmationTransitions() {
        repository.armSos()
        assertEquals(SosState.Armed, repository.sosState.value)

        repository.showConfirmation()
        assertEquals(SosState.Confirmation, repository.sosState.value)
    }

    @Test
    fun testActiveSosTrigger() = runTest {
        val alertId = repository.triggerActiveSos(12.9716, 77.5946)
        assertTrue(alertId.isNotEmpty())
        assertTrue(repository.sosState.value is SosState.Active)
    }
}
