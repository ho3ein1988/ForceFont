package com.fontforce.xposed.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Extension to get DataStore from Context
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "fontforce_prefs")

data class HookMethod(
    val id: String,
    val name: String,
    val description: String,
    val badge: String? = null,   // "Core" | "Beta" | null
    val defaultEnabled: Boolean = true
)

// Canonical list of all hook methods shown in UI
val ALL_HOOK_METHODS = listOf(
    HookMethod(
        id = "typeface",
        name = "Typeface Hook",
        description = "Replaces custom typefaces system-wide",
        badge = "Core",
        defaultEnabled = true
    ),
    HookMethod(
        id = "textview",
        name = "TextView Hook",
        description = "Intercepts setTypeface calls",
        badge = "Core",
        defaultEnabled = true
    ),
    HookMethod(
        id = "paint",
        name = "Paint Hook",
        description = "Hooks Paint.setTypeface",
        badge = "Core",
        defaultEnabled = true
    ),
    HookMethod(
        id = "webview",
        name = "WebView Hook",
        description = "Forces system font in web pages",
        badge = null,
        defaultEnabled = true
    ),
    HookMethod(
        id = "flutter",
        name = "Flutter Hook",
        description = "Experimental Flutter app support",
        badge = "Beta",
        defaultEnabled = false
    )
)

class HookPreferences(private val context: Context) {

    // Returns a Flow<Boolean> for each method id
    fun methodEnabled(id: String): Flow<Boolean> {
        val key = booleanPreferencesKey("method_$id")
        val default = ALL_HOOK_METHODS.find { it.id == id }?.defaultEnabled ?: true
        return context.dataStore.data.map { prefs -> prefs[key] ?: default }
    }

    // Returns a Flow<Map<id, enabled>> for all methods at once
    fun allMethodStates(): Flow<Map<String, Boolean>> {
        return context.dataStore.data.map { prefs ->
            ALL_HOOK_METHODS.associate { method ->
                val key = booleanPreferencesKey("method_${method.id}")
                method.id to (prefs[key] ?: method.defaultEnabled)
            }
        }
    }

    suspend fun setMethodEnabled(id: String, enabled: Boolean) {
        val key = booleanPreferencesKey("method_$id")
        context.dataStore.edit { prefs -> prefs[key] = enabled }
    }
}
