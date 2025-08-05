package com.dpconde.sofiatracker.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.dpconde.sofiatracker.domain.repository.SettingsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "sofia_tracker_settings")

@Singleton
class SettingsRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : SettingsRepository {
    
    companion object {
        private val KEY_BABY_NAME = stringPreferencesKey("baby_name")
        private const val DEFAULT_BABY_NAME = "Sofía"
    }
    
    override fun getBabyName(): Flow<String> {
        return context.dataStore.data
            .map { preferences ->
                preferences[KEY_BABY_NAME] ?: DEFAULT_BABY_NAME
            }
    }
    
    override suspend fun setBabyName(name: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_BABY_NAME] = name
        }
    }
}