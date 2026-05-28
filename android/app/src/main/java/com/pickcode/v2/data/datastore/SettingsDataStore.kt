package com.pickcode.v2.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsDataStore(private val context: Context) {

    companion object {
        val AI_ENDPOINT = stringPreferencesKey("ai_endpoint")
        val AI_API_KEY = stringPreferencesKey("ai_api_key")
        val AI_MODEL = stringPreferencesKey("ai_model")
        val TAG_OPTIONS = stringPreferencesKey("tag_options")
    }

    val aiEndpoint: Flow<String> = context.dataStore.data.map { it[AI_ENDPOINT] ?: "" }
    val aiApiKey: Flow<String> = context.dataStore.data.map { it[AI_API_KEY] ?: "" }
    val aiModel: Flow<String> = context.dataStore.data.map { it[AI_MODEL] ?: "" }

    val tagOptions: Flow<List<String>> = context.dataStore.data.map { prefs ->
        val json = prefs[TAG_OPTIONS]
        if (json.isNullOrBlank()) {
            listOf("家", "公司", "老家", "生鲜", "私密")
        } else {
            Json.decodeFromString(json)
        }
    }

    suspend fun saveAiConfig(endpoint: String, apiKey: String, model: String) {
        context.dataStore.edit { prefs ->
            prefs[AI_ENDPOINT] = endpoint
            prefs[AI_API_KEY] = apiKey
            prefs[AI_MODEL] = model
        }
    }

    suspend fun saveTagOptions(tags: List<String>) {
        context.dataStore.edit { prefs ->
            prefs[TAG_OPTIONS] = Json.encodeToString(tags)
        }
    }
}
