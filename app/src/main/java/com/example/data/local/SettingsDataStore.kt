package com.example.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.data.model.BusinessSettings
import com.example.data.model.ThemePreference
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.skywallDataStore: DataStore<Preferences> by
  preferencesDataStore(name = "skywall_settings")

class SettingsDataStore(private val context: Context) {
  private object Keys {
    val BIZ_NAME = stringPreferencesKey("biz_name")
    val BIZ_ADDRESS = stringPreferencesKey("biz_address")
    val BIZ_PHONE = stringPreferencesKey("biz_phone")
    val THEME = stringPreferencesKey("theme")
    val SEEDED_SAMPLE = booleanPreferencesKey("seeded_sample")
  }

  val settingsFlow: Flow<BusinessSettings> =
    context.skywallDataStore.data.map { prefs ->
      BusinessSettings(
        name = prefs[Keys.BIZ_NAME]?.takeIf { it.isNotBlank() } ?: BusinessSettings.DEFAULT_NAME,
        address =
          prefs[Keys.BIZ_ADDRESS]?.takeIf { it.isNotBlank() } ?: BusinessSettings.DEFAULT_ADDRESS,
        phone = prefs[Keys.BIZ_PHONE]?.takeIf { it.isNotBlank() } ?: BusinessSettings.DEFAULT_PHONE,
        theme = ThemePreference.fromKey(prefs[Keys.THEME]),
      )
    }

  suspend fun saveSettings(settings: BusinessSettings) {
    context.skywallDataStore.edit { prefs ->
      prefs[Keys.BIZ_NAME] = settings.name.trim().ifEmpty { BusinessSettings.DEFAULT_NAME }
      prefs[Keys.BIZ_ADDRESS] =
        settings.address.trim().ifEmpty { BusinessSettings.DEFAULT_ADDRESS }
      prefs[Keys.BIZ_PHONE] = settings.phone.trim().ifEmpty { BusinessSettings.DEFAULT_PHONE }
      prefs[Keys.THEME] = settings.theme.key
    }
  }

  suspend fun hasSeededSample(): Boolean {
    return context.skywallDataStore.data.first()[Keys.SEEDED_SAMPLE] ?: false
  }

  suspend fun markSampleSeeded() {
    context.skywallDataStore.edit { prefs -> prefs[Keys.SEEDED_SAMPLE] = true }
  }
}
