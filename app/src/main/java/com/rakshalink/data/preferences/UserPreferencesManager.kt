package com.rakshalink.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

@Singleton
class UserPreferencesManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        val KEY_USER_ROLE = stringPreferencesKey("user_role")
        val KEY_USER_PHONE = stringPreferencesKey("user_phone")
        val KEY_USER_ID = stringPreferencesKey("user_id")
        val KEY_IS_AUTHENTICATED = booleanPreferencesKey("is_authenticated")
        val KEY_PUSH_ENABLED = booleanPreferencesKey("push_enabled")
        val KEY_SHARE_LOCATION_ENABLED = booleanPreferencesKey("share_location_enabled")
        val KEY_THEME = stringPreferencesKey("theme")
        val KEY_LANGUAGE = stringPreferencesKey("language")
        val KEY_SOUND_ENABLED = booleanPreferencesKey("sound_enabled")
        val KEY_VIBRATION_ENABLED = booleanPreferencesKey("vibration_enabled")
        val KEY_ALERT_VOLUME = intPreferencesKey("alert_volume")
        val KEY_QUIET_HOURS_ENABLED = booleanPreferencesKey("quiet_hours_enabled")
        val KEY_QUIET_HOURS_START = stringPreferencesKey("quiet_hours_start")
        val KEY_QUIET_HOURS_END = stringPreferencesKey("quiet_hours_end")
        val KEY_ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
    }

    val userRoleFlow: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_USER_ROLE] ?: "wearer"
    }

    val userIdFlow: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_USER_ID] ?: ""
    }

    val userPhoneOrEmailFlow: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_USER_PHONE] ?: ""
    }

    val pushEnabledFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_PUSH_ENABLED] ?: true
    }

    val shareLocationEnabledFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_SHARE_LOCATION_ENABLED] ?: true
    }

    val themeFlow: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_THEME] ?: "dark"
    }

    val languageFlow: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_LANGUAGE] ?: "en"
    }

    val soundEnabledFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_SOUND_ENABLED] ?: true
    }

    val vibrationEnabledFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_VIBRATION_ENABLED] ?: true
    }

    val alertVolumeFlow: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[KEY_ALERT_VOLUME] ?: 80
    }

    val quietHoursEnabledFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_QUIET_HOURS_ENABLED] ?: false
    }

    val onboardingCompletedFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_ONBOARDING_COMPLETED] ?: false
    }

    suspend fun setUserRole(role: String) {
        context.dataStore.edit { prefs -> prefs[KEY_USER_ROLE] = role }
    }

    suspend fun saveAuthSession(userId: String, phone: String, role: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_USER_ID] = userId
            prefs[KEY_USER_PHONE] = phone
            prefs[KEY_USER_ROLE] = role
            prefs[KEY_IS_AUTHENTICATED] = true
        }
    }

    suspend fun clearAuthSession() {
        context.dataStore.edit { prefs ->
            prefs[KEY_USER_ID] = ""
            prefs[KEY_USER_PHONE] = ""
            prefs[KEY_IS_AUTHENTICATED] = false
        }
    }

    suspend fun setPushEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs -> prefs[KEY_PUSH_ENABLED] = enabled }
    }

    suspend fun setShareLocationEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs -> prefs[KEY_SHARE_LOCATION_ENABLED] = enabled }
    }

    suspend fun setTheme(theme: String) {
        context.dataStore.edit { prefs -> prefs[KEY_THEME] = theme }
    }

    suspend fun setSoundEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs -> prefs[KEY_SOUND_ENABLED] = enabled }
    }

    suspend fun setVibrationEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs -> prefs[KEY_VIBRATION_ENABLED] = enabled }
    }

    suspend fun setAlertVolume(volume: Int) {
        context.dataStore.edit { prefs -> prefs[KEY_ALERT_VOLUME] = volume }
    }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        context.dataStore.edit { prefs -> prefs[KEY_ONBOARDING_COMPLETED] = completed }
    }
}
