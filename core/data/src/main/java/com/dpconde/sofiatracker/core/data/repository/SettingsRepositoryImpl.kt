package com.dpconde.sofiatracker.core.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : SettingsRepository {
    
    companion object {
        private val KEY_BABY_NAME = stringPreferencesKey("baby_name")
        private const val DEFAULT_BABY_NAME = "Sofía"
    }
    
    override fun getBabyName(): Flow<String> {
        return dataStore.data
            .map { preferences ->
                preferences[KEY_BABY_NAME] ?: DEFAULT_BABY_NAME
            }
    }
    
    override suspend fun setBabyName(name: String) {
        dataStore.edit { preferences ->
            preferences[KEY_BABY_NAME] = name
        }
    }
}