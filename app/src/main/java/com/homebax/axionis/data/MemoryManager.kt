package com.homebax.axionis.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

val Context.dataStore by preferencesDataStore(name = "axionis_memory")

enum class MemoryTier {
    TEMPORARY,  // Current session only
    PERSISTENT, // Saved to disk
    IMPORTANT   // Long-term/Core information
}

data class MemoryEntry(
    val content: String,
    val tier: MemoryTier,
    val timestamp: Long = System.currentTimeMillis()
)

class MemoryManager(private val context: Context) {
    private val persistentKey = stringPreferencesKey("persistent_memory")
    private val importantKey = stringPreferencesKey("important_memory")
    
    // Temporary memory is in-memory only
    private val temporaryMemory = mutableListOf<MemoryEntry>()

    fun addTemporary(content: String) {
        temporaryMemory.add(MemoryEntry(content, MemoryTier.TEMPORARY))
    }

    suspend fun addPersistent(content: String) {
        val currentRaw = context.dataStore.data.first()[persistentKey] ?: ""
        val newList = if (currentRaw.isEmpty()) content else "$currentRaw|||$content"
        context.dataStore.edit { prefs ->
            prefs[persistentKey] = newList
        }
    }

    suspend fun addImportant(content: String) {
        val currentRaw = context.dataStore.data.first()[importantKey] ?: ""
        val newList = if (currentRaw.isEmpty()) content else "$currentRaw|||$content"
        context.dataStore.edit { prefs ->
            prefs[importantKey] = newList
        }
    }

    fun getTemporary(): List<MemoryEntry> = temporaryMemory.toList()

    fun getPersistent(): Flow<List<MemoryEntry>> = context.dataStore.data.map { prefs ->
        val raw = prefs[persistentKey] ?: ""
        if (raw.isEmpty()) emptyList()
        else raw.split("|||").map { MemoryEntry(it, MemoryTier.PERSISTENT) }
    }

    fun getImportant(): Flow<List<MemoryEntry>> = context.dataStore.data.map { prefs ->
        val raw = prefs[importantKey] ?: ""
        if (raw.isEmpty()) emptyList()
        else raw.split("|||").map { MemoryEntry(it, MemoryTier.IMPORTANT) }
    }

    fun clearTemporary() {
        temporaryMemory.clear()
    }
}
